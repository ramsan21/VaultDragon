import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class JavaScriptExecutor {
    public static void main(String[] args) {
        try (Context context = Context.create()) {
            // Load JavaScript file content
            String jsFilePath = "path/to/encryptionManager.js"; // Update the path to your JavaScript file
            String jsCode = new String(Files.readAllBytes(Paths.get(jsFilePath)));

            // Evaluate the JavaScript code
            context.eval("js", jsCode);

            // Call the JavaScript function
            Value encryptionManager = context.eval("js", "EncryptionManager()");
            Value processEncryptResponse = encryptionManager.getMember("processEncryptResponse");

            // Prepare the JavaScript parameters
            String randomString = "randomString";
            String modulus = "modulus";
            String exponent = "exponent";
            String publicKey = modulus + "|" + exponent;
            String strToEncrypt = "abyte1";

            Value response = context.eval("js", "({ randomString: '" + randomString + "', publickey: '" + publicKey + "' })");

            // Execute the function and get the response
            Value encryptedDataPromise = processEncryptResponse.execute(strToEncrypt, response);
            Value encryptedData = encryptedDataPromise.invokeMember("then", (Value res) -> {
                System.out.println("Received response: " + res);
                return res;
            });

            // Wait for the promise to resolve (if needed)
            context.getEngine().getLanguages().get("js").getPolyglotContext().eval("js", "async () => await " + encryptedDataPromise.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
