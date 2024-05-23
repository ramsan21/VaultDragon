import org.graalvm.polyglot.*;

public class ReturnParameter {
    public static void main(String[] args) {
        try (Context context = Context.create()) {
            Value bindFunction = context.eval("js", "function returnParameter(param) { return param; }");
            Value result = bindFunction.execute(args[0]);
            System.out.println("Input parameter: " + args[0]);
            System.out.println("Result: " + result.asString());
        } catch (PolyglotException e) {
            e.printStackTrace();
        }
    }
}
