package serverviwe.view.service;

import com.jcraft.jsch.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Service
public class SSHService {
    @Value("${vps.ssh.connect-timeout-ms:10000}") private int connectTimeout;
    @Value("${vps.ssh.command-timeout-ms:30000}") private int commandTimeout;
    @Value("${vps.ssh.strict-host-key-checking:no}") private String strictHostKeyChecking;

    public Session connect(String host, int port, String username, String password) throws JSchException {
        String cleanHost = require(host, "Host");
        String cleanUser = require(username, "Username");
        if (port < 1 || port > 65535) throw new IllegalArgumentException("Invalid SSH port");
        if (password == null) throw new IllegalArgumentException("Password is required");
        JSch jsch = new JSch();
        Session session = jsch.getSession(cleanUser, cleanHost, port);
        session.setPassword(password);
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", strictHostKeyChecking);
        session.setConfig(config);
        session.connect(connectTimeout);
        return session;
    }

    public CommandResult execute(String host, int port, String username, String password, String command) throws Exception {
        Session session = connect(host, port, username, password);
        try {
            return execute(session, command);
        } finally {
            session.disconnect();
        }
    }

    public CommandResult execute(Session session, String command) throws Exception {
        if (session == null || !session.isConnected()) throw new IllegalStateException("SSH session is not connected");
        ChannelExec channel = null;
        try {
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setInputStream(null);
            InputStream stdout = channel.getInputStream();
            ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();
            channel.setErrStream(errBuffer);
            channel.connect(connectTimeout);
            String out = readUntilClosed(stdout, channel);
            String err = errBuffer.toString(StandardCharsets.UTF_8);
            long deadline = System.currentTimeMillis() + commandTimeout;
            while (!channel.isClosed() && System.currentTimeMillis() < deadline) Thread.sleep(50);
            int exit = channel.isClosed() ? channel.getExitStatus() : -1;
            return new CommandResult(out, err, exit);
        } finally { if (channel != null) channel.disconnect(); }
    }

    private String readUntilClosed(InputStream stream, ChannelExec channel) throws IOException {
        StringBuilder sb = new StringBuilder();
        byte[] buffer = new byte[4096];
        long lastData = System.currentTimeMillis();
        while (true) {
            while (stream.available() > 0) {
                int n = stream.read(buffer);
                if (n < 0) return sb.toString();
                sb.append(new String(buffer, 0, n, StandardCharsets.UTF_8));
                lastData = System.currentTimeMillis();
            }
            if (channel.isClosed()) {
                while (stream.available() > 0) {
                    int n = stream.read(buffer); if (n < 0) break;
                    sb.append(new String(buffer, 0, n, StandardCharsets.UTF_8));
                }
                return sb.toString();
            }
            if (System.currentTimeMillis() - lastData > commandTimeout) throw new IOException("SSH command timed out");
            try { Thread.sleep(30); } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IOException("Interrupted", e); }
        }
    }

    public List<String> listDirectories(String host, int port, String username, String password, String path) throws Exception {
        String p = normalizePath(path);
        CommandResult r = execute(host, port, username, password,
                "find -- " + shellQuote(p) + " -maxdepth 1 -mindepth 1 -type d -printf '%f\\n' | sort");
        ensureSuccess(r);
        if (r.stdout().isBlank()) return List.of();
        return Arrays.stream(r.stdout().split("\\R")).map(String::trim).filter(s -> !s.isBlank()).toList();
    }

    public List<String> listFiles(String host, int port, String username, String password, String path) throws Exception {
        String p = normalizePath(path);
        CommandResult r = execute(host, port, username, password,
                "find -- " + shellQuote(p) + " -maxdepth 1 -mindepth 1 -type f -printf '%f\\n' | sort");
        ensureSuccess(r);
        if (r.stdout().isBlank()) return List.of();
        return Arrays.stream(r.stdout().split("\\R")).map(String::trim).filter(s -> !s.isBlank()).toList();
    }

    public byte[] download(String host, int port, String username, String password, String remotePath) throws Exception {
        Session session = connect(host, port, username, password);
        try {
            ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
            sftp.connect(connectTimeout);
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) { sftp.get(normalizePath(remotePath), out); return out.toByteArray(); }
            finally { sftp.disconnect(); }
        } finally { session.disconnect(); }
    }

    public void upload(String host, int port, String username, String password, String remotePath, byte[] data) throws Exception {
        Session session = connect(host, port, username, password);
        try {
            ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
            sftp.connect(connectTimeout);
            try (ByteArrayInputStream in = new ByteArrayInputStream(data)) { sftp.put(in, normalizePath(remotePath)); }
            finally { sftp.disconnect(); }
        } finally { session.disconnect(); }
    }

    public static String normalizePath(String path) {
        if (path == null || path.isBlank()) return "/";
        String p = path.trim().replace('\\', '/');
        if (!p.startsWith("/")) p = "/" + p;
        p = p.replaceAll("/{2,}", "/");
        if (p.length() > 1 && p.endsWith("/")) p = p.substring(0, p.length()-1);
        if (p.contains("\0")) throw new IllegalArgumentException("Invalid path");
        return p;
    }
    public static String shellQuote(String s) { return "'" + s.replace("'", "'\\''") + "'"; }
    private static String require(String value, String name) { String v = value == null ? "" : value.trim(); if (v.isBlank()) throw new IllegalArgumentException(name + " is required"); return v; }
    private static void ensureSuccess(CommandResult r) throws IOException { if (r.exitCode() != 0) throw new IOException(r.stderr().isBlank() ? "Remote command failed" : r.stderr().trim()); }

    public record CommandResult(String stdout, String stderr, int exitCode) {
        public String combined() { return (stdout + (stderr.isBlank() ? "" : "\n" + stderr)).trim(); }
    }
}
