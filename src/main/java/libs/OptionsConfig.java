package libs;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.lang.System.exit;

public class OptionsConfig {
    static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    public final int max_event_service_records_per_block;
    public final int time_lag_minutes;
    public final int request_retries;

    public OptionsConfig(int max_event_service_records_per_block, int time_lag_minutes, int request_retries) {
        this.max_event_service_records_per_block = max_event_service_records_per_block;
        this.time_lag_minutes = time_lag_minutes;
        this.request_retries = request_retries;
    }

    @Override
    public String toString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(this);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to return String of OptionsConfig. " + e.getMessage());
            e.printStackTrace();
            exit(1);
        }
        return null;
    }
}
