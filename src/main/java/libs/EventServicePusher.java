package libs;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.lang.System.exit;

public class EventServicePusher {

    static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private final EventServiceConfig eventServiceConfig;
    private final OptionsConfig optionsConfig;
    private final List<EventServiceRecord> eventServiceRecords;
    private int request_attempts = 0;

    public EventServicePusher(EventServiceConfig eventServiceConfig, OptionsConfig optionsConfig, List<EventServiceRecord> eventServiceRecords) {
        this.eventServiceRecords = eventServiceRecords;
        this.optionsConfig = optionsConfig;
        this.eventServiceConfig = eventServiceConfig;
    }

    public void push() {

        int numberOfRecordsRemaining = eventServiceRecords.size();
        int blocksPushed = 0;
        int blockLimit = optionsConfig.max_event_service_records_per_block;
        while (numberOfRecordsRemaining > 0) {
            int start = blocksPushed * blockLimit;
            if (numberOfRecordsRemaining > blockLimit) {
                int end = ((blocksPushed + 1) * blockLimit);
                logger.log(Level.INFO, "Pushing Block " + (blocksPushed + 1) + "...");
                logger.log(Level.FINE, "Block (" + start + ", " + end + "): " + eventServiceRecords.subList(start, end));
                pushBlock(eventServiceRecords.subList(start, end));
                numberOfRecordsRemaining = numberOfRecordsRemaining - blockLimit;
                blocksPushed = blocksPushed + 1;
            } else {
                int end = (start + numberOfRecordsRemaining);
                logger.log(Level.INFO, "Pushing Block " + (blocksPushed + 1) + "...");
                logger.log(Level.FINE, "Block (" + start + ", " + end + "): " + eventServiceRecords.subList(start, end));
                pushBlock(eventServiceRecords.subList(start, end));
                numberOfRecordsRemaining = 0;
            }
        }
    }

    private void pushBlock(List<EventServiceRecord> eventServiceRecordsBlock) {
        try {
            var client = HttpClient.newHttpClient();
            logger.log(Level.FINE, "Pushing to https://" + eventServiceConfig.host + eventServiceConfig.publish_api + eventServiceConfig.schema);
            logger.log(Level.FINE, "With record data: " + eventServiceRecords.toString());
            var request = HttpRequest.newBuilder(URI.create("https://" + eventServiceConfig.host + eventServiceConfig.publish_api + eventServiceConfig.schema))
                    .POST(HttpRequest.BodyPublishers.ofString(eventServiceRecordsBlock.toString()))
                    .header("X-Events-API-AccountName", eventServiceConfig.global_account_name)
                    .header("X-Events-API-Key", eventServiceConfig.api_key)
                    .header("Content-Type", "application/vnd.appd.events+json;v=2")
                    .build();

            var response = client.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() == 200) {
                logger.log(Level.INFO, "Pushed Records to Event Service...");
            } else {
                request_attempts = request_attempts + 1;
                if (request_attempts < optionsConfig.request_retries) {
                    logger.log(Level.WARNING, "Request (" + request_attempts + ") Failed, Response = " + response.statusCode() + ". Retrying...");
                    this.pushBlock(eventServiceRecordsBlock);
                }
                throw new Exception("Failed to push events, response code = " + response.statusCode() + ", body = " + response.body());
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to push records to Event Service " + e.getMessage());
            e.printStackTrace();
            exit(1);
        }
    }
}
