import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

public class JavaScriptExample {
    public static void main(String[] args) {
        // Create a Context instance
        try (Context context = Context.create("js")) {
            // Evaluate the JavaScript code
            context.eval("js", "/* JavaScript code goes here */");

            // Call the processEncryptResponse function
            callProcessEncryptResponse(context);
        }
    }

    private static void callProcessEncryptResponse(Context context) {
        // Define the response object
        Value response = context.eval("js", "({" +
                "randomString: 'ranS'," +
                "publickey: 'modulus|exponent'" +
                "})");

        // Define the strToEncrypt parameter
        Value strToEncrypt = context.asValue("abyte1");

        // Call the processEncryptResponse function
        Value processEncryptResponseFunction = context.eval("js", "processEncryptResponse");
        Value result = processEncryptResponseFunction.execute(strToEncrypt, response);

        // Handle the result
        System.out.println("Received response: " + result.toString());
    }
}
