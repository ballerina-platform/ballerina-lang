// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.ballerinalang.cli.module;

import io.ballerina.runtime.JSONParser;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import org.ballerinalang.cli.module.util.ErrorUtil;
import org.ballerinalang.cli.module.util.Utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import static org.ballerinalang.cli.module.util.CliModuleConstants.PUSH_ORGANIZATION;
import static org.ballerinalang.cli.module.util.Utils.convertToUrl;
import static org.ballerinalang.cli.module.util.Utils.createHttpUrlConnection;
import static org.ballerinalang.cli.module.util.Utils.getStatusCode;
import static org.ballerinalang.cli.module.util.Utils.initializeSsl;
import static org.ballerinalang.cli.module.util.Utils.setRequestMethod;

/**
 * This class is pushing the modules to ballerina central.
 *
 * @since 1.2.0
 */
public class Push {

    private static PrintStream errStream = System.err;
    private static PrintStream outStream = System.out;
    private static final int NO_OF_BYTES = 64;
    private static final int BUFFER_SIZE = 1024 * NO_OF_BYTES;

    private Push() {
    }

    /**
     * Execute push command.
     *
     * @param url           remote uri of the central
     * @param proxyHost     proxy host
     * @param proxyPort     proxy port
     * @param proxyUsername proxy username
     * @param proxyPassword proxy password
     * @param accessToken   access token of the org
     * @param orgName       org name
     * @param moduleName    module name
     * @param version       module version
     * @param baloPath      path to the balo
     */
    public static void execute(String url, String proxyHost, int proxyPort, String proxyUsername, String proxyPassword,
            String accessToken, String orgName, String moduleName, String version, Path baloPath) {
        initializeSsl();
        HttpURLConnection conn = createHttpUrlConnection(convertToUrl(url), proxyHost, proxyPort, proxyUsername,
                proxyPassword);
        conn.setInstanceFollowRedirects(false);
        setRequestMethod(conn, Utils.RequestMethod.POST);

        // Set headers
        conn.setRequestProperty(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        conn.setRequestProperty(PUSH_ORGANIZATION, orgName);
        conn.setRequestProperty(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM);

        conn.setDoOutput(true);
        conn.setChunkedStreamingMode(BUFFER_SIZE);

        try (DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream())) {
            // Send balo content by 1 kb chunks
            byte[] buffer = new byte[BUFFER_SIZE];
            int count;
            try (ProgressBar progressBar = new ProgressBar(
                    orgName + "/" + moduleName + ":" + version + " [project repo -> central]",
                    getTotalFileSizeInKB(baloPath), 1000, outStream, ProgressBarStyle.ASCII, " KB", 1);
                    FileInputStream fis = new FileInputStream(baloPath.toFile())) {
                while ((count = fis.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, count);
                    outputStream.flush();
                    progressBar.stepBy((long) NO_OF_BYTES);
                }
            }
        } catch (IOException e) {
            throw ErrorUtil.createCommandException("error occurred while uploading balo to central: " + e.getMessage());
        }

        handleResponse(conn, orgName, moduleName, version);
        Authenticator.setDefault(null);
    }

    /**
     * Handle the http response.
     *
     * @param conn       http connection
     * @param orgName    org name
     * @param moduleName module name
     * @param version    module version
     */
    private static void handleResponse(HttpURLConnection conn, String orgName, String moduleName, String version) {
        try {
            int statusCode = getStatusCode(conn);
            // 200 - Module pushed successfully
            // Other - Error occurred, json returned with the error message
            if (statusCode == HttpURLConnection.HTTP_OK) {
                outStream.println(orgName + "/" + moduleName + ":" + version + " pushed to central successfully");
            } else if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                errStream.println("unauthorized access token for organization: " + orgName);
            } else if (statusCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), Charset.defaultCharset()))) {
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    BMap payload = (BMap) JSONParser.parse(result.toString());
                    String message = payload.getStringValue(StringUtils.fromString("message")).getValue();
                    if (message.contains("module md file cannot be empty")) {
                        errStream.println(message);
                    } else {
                        throw ErrorUtil.createCommandException(message);
                    }
                } catch (IOException e) {
                    throw ErrorUtil.createCommandException(
                            "failed to push the module '" + orgName + "/" + moduleName + ":" + version
                                    + "' to the remote repository '" + conn.getURL() + "'");
                }
            } else {
                throw ErrorUtil.createCommandException(
                        "failed to push the module '" + orgName + "/" + moduleName + ":" + version
                                + "' to the remote repository '" + conn.getURL() + "'");
            }
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Get total file size in kb.
     *
     * @param filePath path to the file
     * @return size of the file in kb
     */
    private static long getTotalFileSizeInKB(Path filePath) {
        byte[] baloContent;
        try {
            baloContent = Files.readAllBytes(filePath);
            return baloContent.length / 1024;
        } catch (IOException e) {
            throw ErrorUtil.createCommandException("cannot read the balo content");
        }
    }
}
