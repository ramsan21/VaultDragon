import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

public class JavaScriptRunner {
    public static void main(String[] args) {
        try (Context context = Context.create("js")) {
            // Load the JavaScript code
            String javaScriptCode = "/* Your JavaScript code goes here */";
            Value bindings = context.eval("js", javaScriptCode);

            // Call the EncryptionManager function
            Value encryptionManager = bindings.getMember("EncryptionManager");
            Value processEncryptResponse = encryptionManager.getMember("processEncryptResponse");

            // Pass arguments to the processEncryptResponse function
            String strToEncrypt = "Hello, World!";
            Value response = context.eval("js", "({ randomString: 'abc123', publickey: '...' })");
            Value encryptedData = processEncryptResponse.execute(strToEncrypt, response);

            System.out.println("Encrypted data: " + encryptedData.asString());
        }
    }
}
