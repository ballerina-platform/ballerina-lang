package org.ballerinalang.datamapper.codeaction;

import com.google.gson.JsonObject;
import org.ballerinalang.datamapper.util.FileUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

class codeActionTestUtil {
    private codeActionTestUtil(){}

    static void shutDownMockService() throws IOException {
        URL url = new URL(getMockServiceUrl());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        String schemasToSend = "test";
        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(schemasToSend.getBytes(StandardCharsets.UTF_8));
            try (InputStream inputStream = new BufferedInputStream(connection.getInputStream())) {
                connection.disconnect();
            } catch (ConnectException e){
                connection.disconnect();
            }
        }
    }

    private static String getMockServiceUrl(){
        String startConfigPath = "codeaction" + File.separator + "config" + File.separator + "endConfig.json";
        JsonObject configs = FileUtils.fileContentAsObject(startConfigPath);
        return configs.get("url").getAsString();
    }

}
