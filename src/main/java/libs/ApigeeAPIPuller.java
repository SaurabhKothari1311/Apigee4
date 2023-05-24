package libs;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.lang.System.exit;

public class ApigeeAPIPuller {

    static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private final ApigeeAPIConfig apigeeAPIConfig;
    private final OptionsConfig optionsConfig;
    private int request_attempts = 0;

    public ApigeeAPIPuller(ApigeeAPIConfig apigeeAPIConfig, OptionsConfig optionsConfig) {
        this.apigeeAPIConfig = apigeeAPIConfig;
        this.optionsConfig = optionsConfig;
    }

    public ApigeeAPIDataRecord pull() {

        logger.log(Level.INFO, "Pulling Data from Apigee Metrics API...");
        int timeLag = optionsConfig.time_lag_minutes;

        LocalDateTime localDateTimeEnd = LocalDateTime.now(ZoneOffset.UTC).minusMinutes(timeLag);
        DateTimeFormatter dateTimeFormatterEnd = DateTimeFormatter.ofPattern("M/dd/yyyy%20HH:mm");
        String formattedDateEnd = localDateTimeEnd.format(dateTimeFormatterEnd);
        LocalDateTime localDateTimeStart = localDateTimeEnd.minusMinutes(1);
        DateTimeFormatter dateTimeFormatterStart = DateTimeFormatter.ofPattern("M/dd/yyyy%20HH:mm");
        String formattedDateStart = localDateTimeStart.format(dateTimeFormatterStart);
        logger.log(Level.INFO, "Getting Metrics for period starting '" + formattedDateStart.replace("%20", " ") + "' and ending '" + formattedDateEnd.replace("%20", " ") + "'.");

        try {
            String filter = "";
            if (apigeeAPIConfig.api_proxies.length() > 1) {
                filter = "&filter=(apiproxy%20in%20" + apigeeAPIConfig.api_proxies + ")";
            }
            if (apigeeAPIConfig.api_proxies_inverse.length() > 1) {
                filter = filter + "&filter=(apiproxy%20notin%20" + apigeeAPIConfig.api_proxies_inverse + ")";
            }
            String dimensions = "apis";
            if (apigeeAPIConfig.api_product_dimension) {
                dimensions = dimensions + ",api_product";
            }
            if (apigeeAPIConfig.client_id_dimension) {
                dimensions = dimensions + ",client_id";
            }
            if (apigeeAPIConfig.developer_app_dimension) {
                dimensions = dimensions + ",developer_app";
            }
            if (apigeeAPIConfig.proxy_basepath_dimension) {
                dimensions = dimensions + ",proxy_basepath";
            }
            String metrics = "?select=sum(message_count),avg(total_response_time),avg(target_response_time),avg(request_processing_latency),sum(is_error),sum(target_error),sum(policy_error),max(total_response_time),min(total_response_time),max(target_response_time),min(target_response_time),min(request_processing_latency),max(request_processing_latency),sum(request_size),avg(request_size),min(request_size),max(request_size),sum(response_size),avg(response_size),min(response_size),max(response_size)";
            String endpoint = apigeeAPIConfig.api_host + "v1/o/" + apigeeAPIConfig.organization + "/e/" + apigeeAPIConfig.environment + "/stats/" + dimensions + metrics + "&timeRange=" + formattedDateStart + "~" + formattedDateEnd + "&timeUnit=minute" + filter;
            logger.log(Level.FINE, "Apigee Data Request URL: " + endpoint);
            String plainCreds = apigeeAPIConfig.username + ":" + apigeeAPIConfig.password;
            byte[] base64Bytes = Base64.getEncoder().encode(plainCreds.getBytes());
            var client = HttpClient.newHttpClient();
            var request = HttpRequest.newBuilder(URI.create(endpoint))
                .GET()
                .header("Authorization", "Basic " + new String(base64Bytes))
                .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                logger.log(Level.FINE, "Apigee Data: " + response.body());

                String response_4xx_body = "";
                if (apigeeAPIConfig.enable_4xx_errors_request) {
                    logger.log(Level.INFO, "4xx Errors Enabled, Requesting...");
                    logger.log(Level.FINE, "Apigee 4XX Request URL: " + endpoint);
                    metrics = "?select=sum(message_count)";
                    String filter_4xx = filter + "&filter=(response_status_code%20in%20400,401,403,404,405,408)";
                    endpoint = apigeeAPIConfig.api_host + "v1/o/" + apigeeAPIConfig.organization + "/e/" + apigeeAPIConfig.environment + "/stats/" + dimensions + metrics + "&timeRange=" + formattedDateStart + "~" + formattedDateEnd + "&timeUnit=minute" + filter_4xx;
                    logger.log(Level.FINE, "Apigee Data Request URL (4xx): " + endpoint);
                    client = HttpClient.newHttpClient();
                    request = HttpRequest.newBuilder(URI.create(endpoint))
                            .GET()
                            .header("Authorization", "Basic " + new String(base64Bytes))
                            .build();
                    HttpResponse<String> response_4xx = client.send(request, HttpResponse.BodyHandlers.ofString());
                    response_4xx_body = response_4xx.body();
                    logger.log(Level.FINE, "4XX Response: " + response_4xx_body);
                }

                String response_5xx_body = "";
                if (apigeeAPIConfig.enable_5xx_errors_request) {
                    logger.log(Level.INFO, "5xx Errors Enabled, Requesting...");
                    logger.log(Level.FINE, "Apigee 5XX Request URL: " + endpoint);
                    metrics = "?select=sum(message_count)";
                    String filter_5xx = filter + "&filter=(response_status_code%20in%20500,501,502,503,504)";
                    endpoint = apigeeAPIConfig.api_host + "v1/o/" + apigeeAPIConfig.organization + "/e/" + apigeeAPIConfig.environment + "/stats/" + dimensions + metrics + "&timeRange=" + formattedDateStart + "~" + formattedDateEnd + "&timeUnit=minute" + filter_5xx;
                    logger.log(Level.FINE, "Apigee Data Request URL (5xx): " + endpoint);
                    client = HttpClient.newHttpClient();
                    request = HttpRequest.newBuilder(URI.create(endpoint))
                            .GET()
                            .header("Authorization", "Basic " + new String(base64Bytes))
                            .build();
                    HttpResponse<String> response_5xx = client.send(request, HttpResponse.BodyHandlers.ofString());
                    response_5xx_body = response_5xx.body();
                    logger.log(Level.FINE, "4XX Response: " + response_5xx_body);
                }                
                
                return new ApigeeAPIDataRecord(response.body(), response_4xx_body, response_5xx_body);
            } else {
                request_attempts = request_attempts + 1;
                if (request_attempts < optionsConfig.request_retries) {
                    logger.log(Level.WARNING, "Request (" + request_attempts + ") Failed, Response = " + response.statusCode() + ". Retrying...");
                    this.pull();
                }
                throw new Exception("Failed to pull Apigee Data Record, Response = " + response.statusCode());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to pull from Apigee. " + e.getMessage());
            e.printStackTrace();
            exit(1);
        }
        return null;
    }
}
