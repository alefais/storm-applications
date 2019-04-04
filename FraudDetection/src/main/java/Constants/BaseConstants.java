package Constants;

/**
 * Constants useful for all the applications.
 */
public interface BaseConstants {
    String LOCAL_MODE = "local";
    String REMOTE_MODE = "remote";
    int DEFAULT_RATE = -1;
    String HELP = "help";

    interface BaseComponent {
        String SPOUT = "spout";
        String SINK  = "sink";
    }

    interface BaseField {
        String TIMESTAMP = "timestamp";
    }
}
