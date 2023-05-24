package libs;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.lang.System.exit;

public class EventServiceRecord {
    static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    public final long eventTimestamp;
    public final String organization;
    public final String environment;
    public final String api_product;
    public final String apiproxy;
    public final String client_id;
    public final String developer_app;
    public final String proxy_basepath;
    public final Float Count_4XXs;
    public final Float Count_5XXs;
    public final Float Average_Request_Processing_Latency;
    public final Float Average_Target_Response_Time;
    public final Float Average_Total_Response_Time;
    public final Float Maximum_Global_Request_Processing_Latency;
    public final Float Maximum_Target_Response_Time;
    public final Float Maximum_Total_Response_Time;
    public final Float Minimum_Global_Request_Processing_Latency;
    public final Float Minimum_Target_Response_Time;
    public final Float Minimum_Total_Response_Time;
    public final Float Sum_Request_Size;
    public final Float Average_Request_Size;
    public final Float Minimum_Request_Size;
    public final Float Maximum_Request_Size;
    public final Float Sum_Response_Size;
    public final Float Average_Response_Size;
    public final Float Minimum_Response_Size;
    public final Float Maximum_Response_Size;
    public final Float Total_Error_Count;
    public final Float Total_Message_Count;
    public final Float Total_Policy_Errors;
    public final Float Total_Target_Errors;

    public EventServiceRecord(long eventTimestamp, String organization, String environment, String api_product, String apiproxy, String client_id, String developer_app, String proxy_basepath, Float Count_4XXs, Float Count_5XXs, Float Average_Request_Processing_Latency, Float Average_Target_Response_Time, Float Average_Total_Response_Time, Float Maximum_Global_Request_Processing_Latency, Float Maximum_Target_Response_Time, Float Maximum_Total_Response_Time, Float Minimum_Global_Request_Processing_Latency, Float Minimum_Target_Response_Time, Float Minimum_Total_Response_Time, Float Sum_Request_Size, Float Average_Request_Size, Float Minimum_Request_Size, Float Maximum_Request_Size, Float Sum_Response_Size, Float Average_Response_Size, Float Minimum_Response_Size, Float Maximum_Response_Size, Float Total_Error_Count, Float Total_Message_Count, Float Total_Policy_Errors, Float Total_Target_Errors) {
        this.eventTimestamp = eventTimestamp;
        this.organization = organization;
        this.environment = environment;
        this.api_product = api_product;
        this.apiproxy = apiproxy;
        this.client_id = client_id;
        this.developer_app = developer_app;
        this.proxy_basepath = proxy_basepath;
        this.Count_4XXs = Count_4XXs;
        this.Count_5XXs = Count_5XXs;
        this.Average_Request_Processing_Latency = Average_Request_Processing_Latency;
        this.Average_Target_Response_Time = Average_Target_Response_Time;
        this.Average_Total_Response_Time = Average_Total_Response_Time;
        this.Maximum_Global_Request_Processing_Latency = Maximum_Global_Request_Processing_Latency;
        this.Maximum_Target_Response_Time = Maximum_Target_Response_Time;
        this.Maximum_Total_Response_Time = Maximum_Total_Response_Time;
        this.Minimum_Global_Request_Processing_Latency = Minimum_Global_Request_Processing_Latency;
        this.Minimum_Target_Response_Time = Minimum_Target_Response_Time;
        this.Minimum_Total_Response_Time = Minimum_Total_Response_Time;
        this.Sum_Request_Size = Sum_Request_Size;
        this.Average_Request_Size = Average_Request_Size;
        this.Minimum_Request_Size = Minimum_Request_Size;
        this.Maximum_Request_Size = Maximum_Request_Size;
        this.Sum_Response_Size = Sum_Response_Size;
        this.Average_Response_Size = Average_Response_Size;
        this.Minimum_Response_Size = Minimum_Response_Size;
        this.Maximum_Response_Size = Maximum_Response_Size;
        this.Total_Error_Count = Total_Error_Count;
        this.Total_Message_Count = Total_Message_Count;
        this.Total_Policy_Errors = Total_Policy_Errors;
        this.Total_Target_Errors = Total_Target_Errors;
    }

    @Override
    public String toString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(this);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to return String of EventServiceRecord. " + e.getMessage());
            e.printStackTrace();
            exit(1);
        }
        return null;
    }
}
