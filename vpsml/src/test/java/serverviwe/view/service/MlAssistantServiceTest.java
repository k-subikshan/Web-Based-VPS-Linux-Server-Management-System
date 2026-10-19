package serverviwe.view.service;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class MlAssistantServiceTest {
    @Test void flagsCriticalResourceUsage() {
        MlAssistantService service = new MlAssistantService();
        Map<String,String> stats = Map.of("cpu", "96", "ram", "95", "disk", "94", "load", "9.2");
        Map<String,Object> result = service.analyze("test-host", stats);
        assertEquals("HIGH", result.get("risk"));
        assertTrue((Integer)result.get("riskScore") >= 60);
        assertFalse(((java.util.List<?>)result.get("findings")).isEmpty());
    }
    @Test void respondsToMetricsQuestion() {
        MlAssistantService service = new MlAssistantService();
        var result = service.chat("test-host", "show CPU and RAM usage", Map.of("cpu","25.0","ram","45.0","disk","50.0","load","0.5 0.4 0.3"));
        assertTrue(((String)result.get("answer")).contains("CPU: 25.0%"));
        assertNotNull(result.get("analysis"));
    }
}
