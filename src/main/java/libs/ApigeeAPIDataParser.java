package libs;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.lang.System.exit;

public class ApigeeAPIDataParser {

    static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    public final ApigeeAPIConfig apigeeAPIConfig;
    public final ApigeeAPIDataRecord apigeeAPIDataRecord;

    public ApigeeAPIDataParser(ApigeeAPIConfig apigeeAPIConfig, ApigeeAPIDataRecord apigeeAPIDataRecord) {
        this.apigeeAPIConfig = apigeeAPIConfig;
        this.apigeeAPIDataRecord = apigeeAPIDataRecord;
    }

    public List<EventServiceRecord> parse() {

        Hashtable<String, Float> lookup_4xx = new Hashtable<>();
        Hashtable<String, Float> lookup_5xx = new Hashtable<>();
        if (apigeeAPIConfig.enable_4xx_errors_request) {
            try {
                JsonNode root = apigeeAPIDataRecord.toJSON_4xx();
                JsonNode json_dimensions = root.get("environments").get(0).get("dimensions");
                if (json_dimensions == null) {
                    logger.log(Level.WARNING, "No data for 4xx query to Apigee API. Continuing...");
                } else {
                    logger.log(Level.FINE, "Processing 4xx Data: " + json_dimensions);
                    for (JsonNode json_metrics : json_dimensions) {
                        logger.log(Level.FINE, "Processing 4xx Metrics: " + json_metrics);
                        String dimensions_string = json_metrics.path("name").toString().replace("\"", "");
                        float count_4xx_errors = 0f;
                        for (JsonNode json_metric_array : json_metrics) {
                            logger.log(Level.FINE, "Processing Metric Array: " + json_metric_array);
                            for (JsonNode json_metric : json_metric_array) {
                                logger.log(Level.FINE, "Processing Metric: " + json_metric);
                                String metric_name = json_metric.path("name").toString().replace("\"", "");

                                float metric_value;
                                if (json_metric.findValue("value") != null) {
                                    metric_value = Float.parseFloat(json_metric.findValue("value").toString().replace("\"", ""));
                                } else {
                                    metric_value = Float.parseFloat(json_metric.path("values").get(0).toString().replace("\"", ""));
                                }
                                logger.log(Level.FINE, "Name = " + metric_name + ", Value = " + metric_value);
                                count_4xx_errors = metric_value;
                            }
                        }
                        lookup_4xx.put(dimensions_string, count_4xx_errors);
                    }
                    logger.log(Level.FINE, "Created 4xx Lookup: " + lookup_4xx);
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Could not create 4xx lookup... " + e.getMessage());
                e.printStackTrace();
                exit(1);
            }
        }
        if (apigeeAPIConfig.enable_5xx_errors_request) {
            try {
                JsonNode root = apigeeAPIDataRecord.toJSON_5xx();
                JsonNode json_dimensions = root.get("environments").get(0).get("dimensions");
                if (json_dimensions == null) {
                    logger.log(Level.WARNING, "No data for 5xx query to Apigee API. Continuing...");
                } else {
                    logger.log(Level.FINE, "Processing 5xx Data: " + json_dimensions);
                    for (JsonNode json_metrics : json_dimensions) {
                        logger.log(Level.FINE, "Processing 5xx Metrics: " + json_metrics);
                        String dimensions_string = json_metrics.path("name").toString().replace("\"", "");
                        float count_5xx_errors = 0f;
                        for (JsonNode json_metric_array : json_metrics) {
                            logger.log(Level.FINE, "Processing Metric Array: " + json_metric_array);
                            for (JsonNode json_metric : json_metric_array) {
                                logger.log(Level.FINE, "Processing Metric: " + json_metric);
                                String metric_name = json_metric.path("name").toString().replace("\"", "");

                                float metric_value;
                                if (json_metric.findValue("value") != null) {
                                    metric_value = Float.parseFloat(json_metric.findValue("value").toString().replace("\"", ""));
                                } else {
                                    metric_value = Float.parseFloat(json_metric.path("values").get(0).toString().replace("\"", ""));
                                }
                                logger.log(Level.FINE, "Name = " + metric_name + ", Value = " + metric_value);
                                count_5xx_errors = metric_value;
                            }
                        }
                        lookup_5xx.put(dimensions_string, count_5xx_errors);
                    }
                    logger.log(Level.FINE, "Created 5xx Lookup: " + lookup_5xx);
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Could not create 5xx lookup... " + e.getMessage());
                e.printStackTrace();
                exit(1);
            }
        }


        try {
            logger.log(Level.INFO, "Parsing data to EventServiceRecords...");
            JsonNode root = apigeeAPIDataRecord.toJSON();
            JsonNode json_dimensions = root.get("environments").get(0).get("dimensions");
            List<EventServiceRecord> eventServiceRecordList = new ArrayList<>();
            if (json_dimensions == null) {
                logger.log(Level.WARNING, "No data for query to Apigee API. Exiting");
                exit(0);
            }

            logger.log(Level.FINE, "Processing Data: " + json_dimensions);
            for (JsonNode json_metrics : json_dimensions) {

                logger.log(Level.FINE, "Processing Metrics: " + json_metrics);
                String dimensions_string = json_metrics.path("name").toString().replace("\"", "");
                String[] dimensions = dimensions_string.split(",");
                String api_proxy = dimensions[0];
                String api_product_dimension = "";
                String client_id_dimension = "";
                String developer_app_dimension = "";
                String proxy_basepath_dimension = "";
                int dimensionIndex = 1;
                if (apigeeAPIConfig.api_product_dimension) {
                    api_product_dimension = dimensions[dimensionIndex];
                    dimensionIndex++;
                }
                if (apigeeAPIConfig.client_id_dimension) {
                    client_id_dimension = dimensions[dimensionIndex];
                    dimensionIndex++;
                }
                if (apigeeAPIConfig.developer_app_dimension) {
                    developer_app_dimension = dimensions[dimensionIndex];
                    dimensionIndex++;
                }
                if (apigeeAPIConfig.proxy_basepath_dimension) {
                    proxy_basepath_dimension = dimensions[dimensionIndex];
                    dimensionIndex++;
                }

                long timestamp = 0;
                int iterator = 0;
                while (timestamp == 0) {
                    if (json_metrics.path("metrics").get(iterator).path("values").get(0).path("timestamp").toString().length() > 0) {
                        timestamp = Long.parseLong(json_metrics.path("metrics").get(iterator).path("values").get(0).path("timestamp").toString());
                    }
                    iterator++;
                    if (iterator > 10) {
                        throw new RuntimeException("Failed to find timestamp in Apigee Response");
                    }
                }
                Hashtable<String, Float> valuesForPush = new Hashtable<>();

                for (JsonNode json_metric_array : json_metrics) {
                    logger.log(Level.FINE, "Processing Metric Array: " + json_metric_array);
                    for (JsonNode json_metric : json_metric_array) {
                        logger.log(Level.FINE, "Processing Metric: " + json_metric);
                        String metric_name = json_metric.path("name").toString().replace("\"", "");

                        Float metric_value;
                        if (json_metric.findValue("value") != null) {
                            metric_value = Float.parseFloat(json_metric.findValue("value").toString().replace("\"", ""));
                        } else {
                            metric_value = Float.parseFloat(json_metric.path("values").get(0).toString().replace("\"", ""));
                        }
                        logger.log(Level.FINE, "Name = " + metric_name + ", Value = " + metric_value);

                        switch (metric_name) {
                            case "sum(message_count)" -> valuesForPush.put("sum(message_count)", metric_value);
                            case "global-avg-request_processing_latency" -> valuesForPush.put("global-avg-request_processing_latency", metric_value);
                            case "avg(total_response_time)" -> valuesForPush.put("avg(total_response_time)", metric_value);
                            case "sum(policy_error)" -> valuesForPush.put("sum(policy_error)", metric_value);
                            case "sum(is_error)" -> valuesForPush.put("sum(is_error)", metric_value);
                            case "avg(target_response_time)" -> valuesForPush.put("avg(target_response_time)", metric_value);
                            case "avg(request_processing_latency)" -> valuesForPush.put("avg(request_processing_latency)", metric_value);
                            case "sum(target_error)" -> valuesForPush.put("sum(target_error)", metric_value);
                            case "min(request_processing_latency)" -> valuesForPush.put("min(request_processing_latency)", metric_value);
                            case "max(request_processing_latency)" -> valuesForPush.put("max(request_processing_latency)", metric_value);
                            case "min(target_response_time)" -> valuesForPush.put("min(target_response_time)", metric_value);
                            case "max(target_response_time)" -> valuesForPush.put("max(target_response_time)", metric_value);
                            case "min(total_response_time)" -> valuesForPush.put("min(total_response_time)", metric_value);
                            case "max(total_response_time)" -> valuesForPush.put("max(total_response_time)", metric_value);
                            case "sum(request_size)" -> valuesForPush.put("sum(request_size)", metric_value);
                            case "avg(request_size)" -> valuesForPush.put("avg(request_size)", metric_value);
                            case "min(request_size)" -> valuesForPush.put("min(request_size)", metric_value);
                            case "max(request_size)" -> valuesForPush.put("max(request_size)", metric_value);
                            case "sum(response_size)" -> valuesForPush.put("sum(response_size)", metric_value);
                            case "avg(response_size)" -> valuesForPush.put("avg(response_size)", metric_value);
                            case "min(response_size)" -> valuesForPush.put("min(response_size)", metric_value);
                            case "max(response_size)" -> valuesForPush.put("max(response_size)", metric_value);
                        }
                    }
                }
                Float error_value_4xx = 0f;
                if (lookup_4xx.get(dimensions_string) != null) {
                    error_value_4xx = lookup_4xx.get(dimensions_string);
                }
                Float error_value_5xx = 0f;
                if (lookup_5xx.get(dimensions_string) != null) {
                    error_value_5xx = lookup_5xx.get(dimensions_string);
                }

                logger.log(Level.FINE, "Record Values: " + valuesForPush);
                eventServiceRecordList.add(new EventServiceRecord(timestamp, apigeeAPIConfig.organization, apigeeAPIConfig.environment, api_product_dimension, api_proxy, client_id_dimension, developer_app_dimension, proxy_basepath_dimension, error_value_4xx, error_value_5xx, valuesForPush.get("avg(request_processing_latency)"), valuesForPush.get("avg(target_response_time)"), valuesForPush.get("avg(total_response_time)"), valuesForPush.get("max(request_processing_latency)"), valuesForPush.get("max(target_response_time)"), valuesForPush.get("max(total_response_time)"), valuesForPush.get("min(request_processing_latency)"), valuesForPush.get("min(target_response_time)"), valuesForPush.get("min(total_response_time)"), valuesForPush.get("sum(request_size)"), valuesForPush.get("avg(request_size)"), valuesForPush.get("min(request_size)"), valuesForPush.get("max(request_size)"), valuesForPush.get("sum(response_size)"), valuesForPush.get("avg(response_size)"), valuesForPush.get("min(response_size)"), valuesForPush.get("max(response_size)"), valuesForPush.get("sum(is_error)"), valuesForPush.get("sum(message_count)"), valuesForPush.get("sum(policy_error)"), valuesForPush.get("sum(target_error)")));
            }
            logger.log(Level.FINE, "All Records for push: " + eventServiceRecordList);
            return eventServiceRecordList;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Could not parse data from Apigee. " + e.getMessage());
            e.printStackTrace();
            exit(1);
        }
        return null;
    }
}
