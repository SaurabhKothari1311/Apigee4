import libs.*;

import javax.naming.ConfigurationException;
import java.io.*;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.Level;
import static java.lang.System.exit;

public class Runner {

    static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    static {
        try {
            InputStream stream = Runner.class.getClassLoader().getResourceAsStream("logging.properties");
            LogManager.getLogManager().readConfiguration(stream);
            logger.log(Level.INFO, "Loaded java.util.logging configuration from logging.properties");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to read logging.properties. " + e.getMessage());
            e.printStackTrace();
            exit(1);
        }
    }
    static OptionsConfig optionsConfig = loadOptionsConfig();
    static EventServiceConfig eventServiceConfig = loadEventServiceConfig();
    static ApigeeAPIConfig apigeeAPIConfig = loadApigeeAPIConfig();
    static long startTimestamp;

    public static void main(String[] args) {
        startTimestamp = System.currentTimeMillis();
        logger.log(Level.INFO, "-----STARTING RUN - APIGEE METRICS REPORTER-----");
        logger.log(Level.FINE, "Loaded OptionsConfig: " + optionsConfig.toString());
        logger.log(Level.FINE, "Loaded EventServiceConfig: " + eventServiceConfig.toString());
        logger.log(Level.FINE, "Loaded ApigeeAPIConfig: " + apigeeAPIConfig.toString());
        createEventServiceSchemaIfMissing();
        run();
        long finishTimestamp = System.currentTimeMillis() - startTimestamp;
        logger.log(Level.INFO, "-----COMPLETED RUN - APIGEE METRICS REPORTER-----");
        logger.log(Level.INFO, "-----(RUN COMPLETED IN "+ finishTimestamp + " ms)-----");
    }

    private static void createEventServiceSchemaIfMissing() {
        logger.log(Level.INFO, "Checking for schema '" + eventServiceConfig.schema + "' in Events Service '" + eventServiceConfig.host + "'");
        EventServiceSchemaCreator eventServiceSchemaCreator = new EventServiceSchemaCreator(eventServiceConfig);
        if (!eventServiceSchemaCreator.checkForSchema()) {
            logger.log(Level.INFO, "Schema " + eventServiceConfig.schema + " Not Found, Creating");
            eventServiceSchemaCreator.createSchema();
        } else {
            logger.log(Level.INFO, "Schema " + eventServiceConfig.schema + " Found, Continuing");
        }
    }

    private static void run() {

        logger.log(Level.INFO, "Pulling Metrics From Apigee API...");

        ApigeeAPIPuller apigeeAPIPuller = new ApigeeAPIPuller(apigeeAPIConfig, optionsConfig);
        ApigeeAPIDataRecord apigeeAPIDataRecord = apigeeAPIPuller.pull();

        logger.log(Level.INFO, "Parsing Metrics...");
        ApigeeAPIDataParser apigeeAPIDataParser = new ApigeeAPIDataParser(apigeeAPIConfig, apigeeAPIDataRecord);
        List<EventServiceRecord> eventServiceRecords = apigeeAPIDataParser.parse();
        logger.log(Level.INFO, eventServiceRecords.size() + " Event Service Records To Send");

        logger.log(Level.INFO, "Pushing Records To Event Service (Max Block = " + optionsConfig.max_event_service_records_per_block + ")...");
        EventServicePusher eventServicePusher = new EventServicePusher(eventServiceConfig, optionsConfig, eventServiceRecords);
        eventServicePusher.push();
    }

    private static EventServiceConfig loadEventServiceConfig() {
        logger.log(Level.INFO, "Loading java.util.logging configuration from events_service.properties");

        try {
            Properties props = new Properties();
            props.load(Runner.class.getResourceAsStream("/events_service.properties"));
            String host;
            if ((props.getProperty("host").length() < 1) || (props.getProperty("host").matches(".*/+.*"))) {
                throw new ConfigurationException("host in events_service.properties MUST be set (Format \"domain.name.only.com\")");
            } else {
                host = props.getProperty("host");
            }
            String publish_api;
            if ((props.getProperty("publish_api").length() < 1) || (!props.getProperty("publish_api").matches("/.*/"))) {
                throw new ConfigurationException("publish_api in events_service.properties MUST be set (Format \"/include/slashes/\")");
            } else {
                publish_api = props.getProperty("publish_api");
            }
            String create_api;
            if ((props.getProperty("create_api").length() < 1) || (!props.getProperty("create_api").matches("/.*/"))) {
                throw new ConfigurationException("create_api in events_service.properties MUST be set (Format \"/include/slashes/\")");
            } else {
                create_api = props.getProperty("create_api");
            }
            String schema;
            if ((props.getProperty("schema").length() < 1) || (!props.getProperty("schema").matches("[a-zA-Z0-9_]*"))) {
                throw new ConfigurationException("schema in events_service.properties MUST be set (Format is numbers, letters and underscores only)");
            } else {
                schema = props.getProperty("schema");
            }
            String global_account_name;
            if ((props.getProperty("global_account_name").length() < 1) || (!props.getProperty("global_account_name").matches("[a-zA-Z0-9_-]*"))) {
                throw new ConfigurationException("global_account_name in events_service.properties MUST be set (Format is numbers, letters, underscores and dashes only)");
            } else {
                global_account_name = props.getProperty("global_account_name");
            }
            String api_key;
            if ((props.getProperty("api_key").length() < 1) || (!props.getProperty("api_key").matches("[a-zA-Z0-9-]*"))) {
                throw new ConfigurationException("api_key in events_service.properties MUST be set (Format is numbers, letters and dashes only)");
            } else {
                api_key = props.getProperty("api_key");
            }
            EventServiceConfig eventServiceConfig = new EventServiceConfig(host, publish_api, create_api, schema, global_account_name, api_key);
            logger.log(Level.FINE, "Created EventServiceConfig: " + eventServiceConfig);
            return eventServiceConfig;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to read events_service.properties. " + e.getMessage());
            e.printStackTrace();
            exit(1);
        }
        return null;
    }

    private static OptionsConfig loadOptionsConfig() {
        logger.log(Level.INFO, "Loading java.util.logging configuration from options.properties");

        try {
            Properties props = new Properties();
            props.load(Runner.class.getResourceAsStream("/options.properties"));

            int max_event_service_records_per_block;
            if ((props.getProperty("max_event_service_records_per_block").length() < 1) || (!props.getProperty("max_event_service_records_per_block").matches("[0-9]*"))) {
                throw new ConfigurationException("max_event_service_records_per_block MUST be set (Any number between 1 and 10000)");
            } else {
                max_event_service_records_per_block = Integer.parseInt(props.getProperty("max_event_service_records_per_block"));
                if ((max_event_service_records_per_block < 1) || (max_event_service_records_per_block > 10000)) {
                    throw new ConfigurationException("max_event_service_records_per_block MUST be set (Any number between 1 and 10000)");
                }
            }
            int time_lag_minutes;
            if ((props.getProperty("time_lag_minutes").length() < 1) || (!props.getProperty("time_lag_minutes").matches("[0-9]+"))) {
                throw new ConfigurationException("time_lag_minutes MUST be set (Any number between 1 and 60)");
            } else {
                time_lag_minutes = Integer.parseInt(props.getProperty("time_lag_minutes"));
                if ((time_lag_minutes < 1) || (time_lag_minutes > 60)) {
                    throw new ConfigurationException("time_lag_minutes MUST be a number between 1 and 60");
                }
            }
            int request_retries;
            if ((props.getProperty("request_retries").length() < 1) || (!props.getProperty("request_retries").matches("[0-9]+"))) {
                throw new ConfigurationException("request_retries MUST be set (Any number between 1 and 5)");
            } else {
                request_retries = Integer.parseInt(props.getProperty("request_retries"));
                if ((request_retries < 1) || (request_retries > 5)) {
                    throw new ConfigurationException("request_retries MUST be a number between 1 and 5");
                }
            }
            OptionsConfig optionsConfig = new OptionsConfig(max_event_service_records_per_block, time_lag_minutes, request_retries);
            logger.log(Level.FINE, "Created OptionsConfig: " + optionsConfig);
            return optionsConfig;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to read options.properties. " + e.getMessage());
            e.printStackTrace();
            exit(1);
        }
        return null;
    }

    private static ApigeeAPIConfig loadApigeeAPIConfig() {
        logger.log(Level.INFO, "Loading java.util.logging configuration from apigee_api.properties");

        try {
            Properties props = new Properties();
            props.load(Runner.class.getResourceAsStream("/apigee_api.properties"));
            Boolean api_product_dimension = Boolean.parseBoolean(props.getProperty("api_product_dimension"));
            Boolean client_id_dimension = Boolean.parseBoolean(props.getProperty("client_id_dimension"));
            Boolean developer_app_dimension = Boolean.parseBoolean(props.getProperty("developer_app_dimension"));
            Boolean proxy_basepath_dimension = Boolean.parseBoolean(props.getProperty("proxy_basepath_dimension"));
            Boolean enable_4xx_errors_request = Boolean.parseBoolean(props.getProperty("enable_4xx_errors_request"));
            Boolean enable_5xx_errors_request = Boolean.parseBoolean(props.getProperty("enable_5xx_errors_request"));
            String api_host;
            if ((props.getProperty("api_host").length() < 1) || (!props.getProperty("api_host").matches("https://(.*)/"))) {
                throw new ConfigurationException("api_host in apigee_api.properties MUST be set (Format \"https://(.*)/\")");
            } else {
                api_host = props.getProperty("api_host");
            }
            String organization;
            if (props.getProperty("organization").length() < 1) {
                throw new ConfigurationException("organization in apigee_api.properties MUST be set");
            } else {
                organization = props.getProperty("organization");
            }
            String environment;
            if (props.getProperty("environment").length() < 1) {
                throw new ConfigurationException("environment in apigee_api.properties MUST be set");
            } else {
                environment = props.getProperty("environment");
            }
            String api_proxies;
            if ((props.getProperty("api_proxies").length() > 0) && (!props.getProperty("api_proxies").matches("('[0-9a-zA-Z_\\-]*')(,'[0-9a-zA-Z_\\-]*')*"))) {
                throw new ConfigurationException("api_proxies if set must be in simple list, e.g: 'item1','item2','item3'");
            } else {
                api_proxies = props.getProperty("api_proxies");
            }
            String api_proxies_inverse;
            if ((props.getProperty("api_proxies_inverse").length() > 0) && (!props.getProperty("api_proxies_inverse").matches("('[0-9a-zA-Z_\\-]*')(,'[0-9a-zA-Z_\\-]*')*"))) {
                throw new ConfigurationException("api_proxies_inverse if set must be in simple list, e.g: 'item1','item2','item3'");
            } else {
                api_proxies_inverse = props.getProperty("api_proxies_inverse");
            }
            String username;
            if (props.getProperty("username").length() < 1) {
                throw new ConfigurationException("username in apigee_api.properties MUST be set");
            } else {
                username = props.getProperty("username");
            }
            String password;
            if (props.getProperty("password").length() < 1) {
                throw new ConfigurationException("password in apigee_api.properties MUST be set");
            } else {
                byte[] decodedBytes = Base64.getDecoder().decode(props.getProperty("password"));
                password = new String(decodedBytes);
            }
            ApigeeAPIConfig apigeeAPIConfig = new ApigeeAPIConfig(api_host, organization, environment, api_proxies, api_proxies_inverse, api_product_dimension, client_id_dimension, developer_app_dimension, proxy_basepath_dimension, enable_4xx_errors_request, enable_5xx_errors_request, username, password);
            logger.log(Level.FINE, "Created ApigeeAPIConfig: " + apigeeAPIConfig);
            return apigeeAPIConfig;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to read apigee_api.properties. " + e.getMessage());
            e.printStackTrace();
            exit(1);
        }
        return null;
    }
}
