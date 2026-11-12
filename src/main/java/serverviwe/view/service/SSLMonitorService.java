package serverviwe.view.service;

import javax.net.ssl.HttpsURLConnection;
import java.net.URI;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class SSLMonitorService {
    public long getDaysRemaining(String website) throws Exception {
        URI uri=URI.create(website.trim());
        if(!"https".equalsIgnoreCase(uri.getScheme())) throw new IllegalArgumentException("HTTPS URL required");
        HttpsURLConnection c=(HttpsURLConnection)uri.toURL().openConnection(); c.setConnectTimeout(5000); c.setReadTimeout(5000); c.connect();
        try { X509Certificate cert=(X509Certificate)c.getServerCertificates()[0]; return Duration.between(Instant.now(),cert.getNotAfter().toInstant()).toDays(); }
        finally { c.disconnect(); }
    }
}
