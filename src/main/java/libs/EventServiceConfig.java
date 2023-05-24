package libs;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.lang.System.exit;

public class EventServiceConfig {

    static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    public final String host;
    public final String publish_api;
    public final String create_api;
    public final String schema;
    public final String global_account_name;
    public final String api_key;

    public EventServiceConfig(String host, String publish_api, String create_api, String schema, String global_account_name, String api_key) {
        this.host = host;
        this.publish_api = publish_api;
        this.create_api = create_api;
        this.schema = schema;
        this.global_account_name = global_account_name;
        this.api_key = api_key;
    }

    @Override
    public String toString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(this);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to return string from EventServiceConfig. " + e.getMessage());
            e.printStackTrace();
            exit(1);
        }
        return null;
    }
}
