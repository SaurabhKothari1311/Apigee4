package libs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.lang.System.exit;

public class ApigeeAPIDataRecord {

    static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    public final String content;
    public final String content_4xx;
    public final String content_5xx;

    public ApigeeAPIDataRecord(String content, String content_4xx, String content_5xx) {
        this.content = content;
        this.content_4xx = content_4xx;
        this.content_5xx = content_5xx;
    }

    public JsonNode toJSON() {
        try {
            return new ObjectMapper().readTree(content);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to return JsonNode of ApigeeAPIDataRecord content. " + e.getMessage());
            e.printStackTrace();
            exit(1);
        }
        return null;
    }

    public JsonNode toJSON_4xx() {
        try {
            return new ObjectMapper().readTree(content_4xx);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to return JsonNode of ApigeeAPIDataRecord content_4xx. " + e.getMessage());
            e.printStackTrace();
            exit(1);
        }
        return null;
    }

    public JsonNode toJSON_5xx() {
        try {
            return new ObjectMapper().readTree(content_5xx);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to return JsonNode of ApigeeAPIDataRecord content_5xx. " + e.getMessage());
            e.printStackTrace();
            exit(1);
        }
        return null;
    }
}
