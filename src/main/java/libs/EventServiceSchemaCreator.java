package libs;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.lang.System.exit;

public class EventServiceSchemaCreator {

    static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    EventServiceConfig eventServiceConfig;
    String schemaDefinition = "{\"schema\" : {\"organization\": \"string\", \"environment\": \"string\", \"api_product\": \"string\", \"apiproxy\": \"string\", \"client_id\": \"string\", \"developer_app\": \"string\", \"proxy_basepath\": \"string\", \"Count_4XXs\": \"float\", \"Count_5XXs\": \"float\", \"Average_Request_Processing_Latency\": \"float\", \"Average_Target_Response_Time\": \"float\", \"Average_Total_Response_Time\": \"float\", \"Maximum_Global_Request_Processing_Latency\": \"float\", \"Maximum_Target_Response_Time\": \"float\", \"Maximum_Total_Response_Time\": \"float\", \"Minimum_Global_Request_Processing_Latency\": \"float\", \"Minimum_Target_Response_Time\": \"float\", \"Minimum_Total_Response_Time\": \"float\", \"Sum_Request_Size\": \"float\", \"Average_Request_Size\": \"float\", \"Maximum_Request_Size\": \"float\", \"Minimum_Request_Size\": \"float\", \"Sum_Response_Size\": \"float\", \"Average_Response_Size\": \"float\", \"Maximum_Response_Size\": \"float\", \"Minimum_Response_Size\": \"float\", \"Total_Error_Count\": \"float\", \"Total_Message_Count\": \"float\", \"Total_Policy_Errors\": \"float\", \"Total_Target_Errors\": \"float\"}}";

    public EventServiceSchemaCreator(EventServiceConfig eventServiceConfig) {
        this.eventServiceConfig = eventServiceConfig;
    }

    public Boolean checkForSchema() {
        try {
            var client = HttpClient.newHttpClient();
            var request = HttpRequest.newBuilder(URI.create("https://" + eventServiceConfig.host + eventServiceConfig.create_api + eventServiceConfig.schema))
                    .GET()
                    .header("X-Events-API-AccountName", eventServiceConfig.global_account_name)
                    .header("X-Events-API-Key", eventServiceConfig.api_key)
                    .header("Content-Type", "application/vnd.appd.events+json;v=2")
                    .build();

            var response = client.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() == 200) {
                return true;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to check for Schema in Events Service. " + e.getMessage());
            e.printStackTrace();
            exit(1);
        }
        return false;
    }

    public void createSchema() {
        try {
            var client = HttpClient.newHttpClient();
            var request = HttpRequest.newBuilder(URI.create("https://" + eventServiceConfig.host + eventServiceConfig.create_api + eventServiceConfig.schema))
                    .POST(HttpRequest.BodyPublishers.ofString(schemaDefinition))
                    .header("X-Events-API-AccountName", eventServiceConfig.global_account_name)
                    .header("X-Events-API-Key", eventServiceConfig.api_key)
                    .header("Content-Type", "application/vnd.appd.events+json;v=2")
                    .build();

            var response = client.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() != 201) {
                throw new RuntimeException("Failed to create schema in Events Service. Response = " + response.statusCode());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to check for Create Schema in Events Service. " + e.getMessage());
            e.printStackTrace();
            exit(1);
        }
    }
}
