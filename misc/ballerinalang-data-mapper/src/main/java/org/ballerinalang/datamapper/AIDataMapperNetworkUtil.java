package org.ballerinalang.datamapper;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.ballerinalang.datamapper.utils.HttpClientRequest;
import org.ballerinalang.datamapper.utils.HttpResponse;

import java.io.IOException;
import java.util.Map;

public class AIDataMapperNetworkUtil {
    private static Map<String, String> headers;
    private static String endpoint;
    private static JsonArray dataToSend;

    AIDataMapperNetworkUtil() {
//        endpoint = "";
//        headers = null;
//        dataToSend = new JsonArray();
    }

    void setEndpoint(String endpoint){
        AIDataMapperNetworkUtil.endpoint = endpoint;
    }

    void setHeaders(Map<String, String> headers){
        AIDataMapperNetworkUtil.headers = headers;
    }

    void setDataToSend(JsonArray dataToSend){
        AIDataMapperNetworkUtil.dataToSend = dataToSend;
    }

    public static String getMapping() throws IOException {
        try {
            HttpResponse response =
                    HttpClientRequest.doPost(endpoint, dataToSend.toString(), headers);
            int responseCode = response.getResponseCode();
            if (responseCode != 200) {
                if (responseCode == 422) {
                    throw new IOException("Error: Un-processable data");
                } else if (responseCode == 500) {
                    throw new IOException("Error: AI service error");
                }
            }
            JsonParser parser = new JsonParser();
            return parser.parse(response.getData()).getAsJsonObject().get("answer").getAsString();
        } catch (IOException e) {
            throw new IOException("Error connecting the AI service" + e.getMessage(), e);
        }
    }
}
