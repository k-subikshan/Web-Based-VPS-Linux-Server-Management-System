package serverviwe.view.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import serverviwe.view.service.AuditService;
import serverviwe.view.service.SSHService;
import java.util.*;

@Controller @RequiredArgsConstructor
public class FileController {
    private final SSHService ssh; private final AuditService audit;

    @GetMapping("/files") public String files(@RequestParam(required=false) String path,HttpSession session,Model model){
        try{ String p=SSHService.normalizePath(path==null?(String)session.getAttribute("path"):path); load(p,session,model); return "pathfolder"; }
        catch(Exception e){model.addAttribute("error",e.getMessage()); return "pathfolder";}
    }
    @GetMapping("/internal/path") public String open(@RequestParam String path,HttpSession session){ session.setAttribute("path",SSHService.normalizePath(path)); return "redirect:/files?path="+java.net.URLEncoder.encode(SSHService.normalizePath(path),java.nio.charset.StandardCharsets.UTF_8); }

    private void load(String path,HttpSession s,Model m)throws Exception{
        String host=(String)s.getAttribute("host"); Integer port=(Integer)s.getAttribute("port"); String user=(String)s.getAttribute("username"); String pass=(String)s.getAttribute("password");
        if(host==null||pass==null) throw new IllegalStateException("No active VPS connection. Please connect again.");
        List<String> dirs=ssh.listDirectories(host,port,user,pass,path), files=ssh.listFiles(host,port,user,pass,path); s.setAttribute("path",path);
        m.addAttribute("folders",dirs); m.addAttribute("files",files); m.addAttribute("path",path); m.addAttribute("host",host); m.addAttribute("port",port); m.addAttribute("username",user); m.addAttribute("parentPath",path.equals("/")?"/":(path.substring(0,path.lastIndexOf('/')).isBlank()?"/":path.substring(0,path.lastIndexOf('/'))));
    }
    @PostMapping("/files/upload") public String upload(@RequestParam MultipartFile file,@RequestParam String path,HttpSession s,Model m){try{String p=SSHService.normalizePath(path); String name=file.getOriginalFilename(); if(name==null||name.isBlank()||!name.matches("[A-Za-z0-9._@+ -]{1,150}")) throw new IllegalArgumentException("Invalid file name"); ssh.upload((String)s.getAttribute("host"),(Integer)s.getAttribute("port"),(String)s.getAttribute("username"),(String)s.getAttribute("password"),p+"/"+name,file.getBytes()); audit.log("FILE_UPLOAD",(String)s.getAttribute("host"),(String)s.getAttribute("username"),p+"/"+name); return "redirect:/files?path="+java.net.URLEncoder.encode(p,java.nio.charset.StandardCharsets.UTF_8);}catch(Exception e){m.addAttribute("error",e.getMessage()); return "redirect:/files?path="+java.net.URLEncoder.encode(SSHService.normalizePath(path),java.nio.charset.StandardCharsets.UTF_8);}}
    @GetMapping("/files/download") public ResponseEntity<ByteArrayResource> download(@RequestParam String path,HttpSession s)throws Exception{String p=SSHService.normalizePath(path); byte[] data=ssh.download((String)s.getAttribute("host"),(Integer)s.getAttribute("port"),(String)s.getAttribute("username"),(String)s.getAttribute("password"),p); String name=p.substring(p.lastIndexOf('/')+1); audit.log("FILE_DOWNLOAD",(String)s.getAttribute("host"),(String)s.getAttribute("username"),p); return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\""+name.replace("\"","")+"\"").contentLength(data.length).body(new ByteArrayResource(data));}
    @PostMapping("/files/delete") public String delete(@RequestParam String path,HttpSession s){try{String p=SSHService.normalizePath(path); if("/".equals(p)) throw new IllegalArgumentException("Root directory cannot be deleted"); ssh.execute((String)s.getAttribute("host"),(Integer)s.getAttribute("port"),(String)s.getAttribute("username"),(String)s.getAttribute("password"),"rm -i -- " + SSHService.shellQuote(p)); audit.log("FILE_DELETE",(String)s.getAttribute("host"),(String)s.getAttribute("username"),p);}catch(Exception ignored){} return "redirect:/files?path="+java.net.URLEncoder.encode(SSHService.normalizePath((String)s.getAttribute("path")),java.nio.charset.StandardCharsets.UTF_8);}
}
