package serverviwe.view.service;

import org.springframework.stereotype.Service;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class WebsiteMonitorService {
    public Map<String,Object> check(String website) throws Exception {
        URI uri=URI.create(website.trim());
        if(!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) throw new IllegalArgumentException("Only HTTP/HTTPS URLs are supported");
        long start=System.currentTimeMillis(); HttpURLConnection c=(HttpURLConnection)uri.toURL().openConnection(); c.setRequestMethod("GET"); c.setConnectTimeout(5000); c.setReadTimeout(5000); c.setInstanceFollowRedirects(true); c.setRequestProperty("User-Agent","VPS-Management-Monitor/1.0");
        int status; try{status=c.getResponseCode();}finally{c.disconnect();}
        Map<String,Object> r=new LinkedHashMap<>(); r.put("status",status); r.put("responseTime",System.currentTimeMillis()-start); r.put("online",status>=200&&status<400); return r;
    }
}
