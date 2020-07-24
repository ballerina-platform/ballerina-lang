/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Commercial License available at http://wso2.com/licenses.
 * For specific language governing the permissions and limitations under
 * this license, please see the license as well as any agreement you’ve
 * entered into with WSO2 governing the purchase of this software and any
 */
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
