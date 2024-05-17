import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class JavaScriptExecutor {
    public static void main(String[] args) {
        try (Context context = Context.newBuilder("js").allowAllAccess(true).build()) {
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

            // Execute the function and get the response as a promise
            Value encryptedDataPromise = processEncryptResponse.execute(strToEncrypt, response);

            // Create a proxy to handle the promise resolution
            encryptedDataPromise.invokeMember("then", ProxyExecutable.from((Value... args1) -> {
                Value res = args1[0];
                System.out.println("Received response: " + res);
                return res;
            })).invokeMember("catch", ProxyExecutable.from((Value... args2) -> {
                Value err = args2[0];
                System.err.println("Error: " + err);
                return err;
            }));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
