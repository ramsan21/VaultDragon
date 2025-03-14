import java.io.PrintWriter;
import java.io.StringWriter;

catch (Exception e) {
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    String stackTrace = sw.toString();
    
    log.debug("Exception occurred: {}", stackTrace);
}