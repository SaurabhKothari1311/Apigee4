package libs;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.lang.System.exit;

public class ApigeeAPIConfig {

    static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    public String api_host;
    public String organization;
    public String environment;
    public String api_proxies;
    public String api_proxies_inverse;
    public Boolean api_product_dimension;
    public Boolean client_id_dimension;
    public Boolean developer_app_dimension;
    public Boolean proxy_basepath_dimension;
    public Boolean enable_4xx_errors_request;
    public Boolean enable_5xx_errors_request;
    public String username;
    public String password;

    public ApigeeAPIConfig(String api_host, String organization, String environment, String api_proxies, String api_proxies_inverse, Boolean api_product_dimension, Boolean client_id_dimension, Boolean developer_app_dimension, Boolean proxy_basepath_dimension, Boolean enable_4xx_errors_request, Boolean enable_5xx_errors_request, String username, String password) {
        this.api_host = api_host;
        this.organization = organization;
        this.environment = environment;
        this.api_proxies = api_proxies;
        this.api_proxies_inverse = api_proxies_inverse;
        this.api_product_dimension = api_product_dimension;
        this.client_id_dimension = client_id_dimension;
        this.developer_app_dimension = developer_app_dimension;
        this.proxy_basepath_dimension = proxy_basepath_dimension;
        this.enable_4xx_errors_request = enable_4xx_errors_request;
        this.enable_5xx_errors_request = enable_5xx_errors_request;
        this.username = username;
        this.password = password;
    }

    @Override
    public String toString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(this);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to return String of ApigeeAPIConfig. " + e.getMessage());
            e.printStackTrace();
            exit(1);
        }
        return null;
    }
}
