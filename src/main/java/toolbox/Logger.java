package toolbox;

public class Logger {
    public static void out(final String message) {
        System.out.println(message);
    }
    
    public static void error(final String message) {
        System.err.println(message);
    }
}
