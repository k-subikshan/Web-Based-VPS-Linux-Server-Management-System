package serverviwe.view.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SSHServiceTest {
    @Test void normalizePathAddsRootAndRemovesDuplicateSlashes(){
        assertEquals("/var/www/app", SSHService.normalizePath("var//www/app/"));
        assertEquals("/", SSHService.normalizePath("/"));
    }
    @Test void shellQuoteEscapesSingleQuotes(){
        assertEquals("'hello'\\''world'", SSHService.shellQuote("hello'world"));
    }
    @Test void blankPathBecomesRoot(){ assertEquals("/", SSHService.normalizePath("  ")); }
}
