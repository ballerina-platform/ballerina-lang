/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.composer.service.ballerina.launcher.service.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ballerinalang.composer.service.ballerina.launcher.service.LaunchManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * LogParser.
 */
public class LogParser {

    static LogParser logParserInstance;
    static ServerSocket listenSocket;
    static BufferedReader logReader;

    static final Pattern ID_PATTERN = Pattern.compile("id: ([a-z0-9]*)");
    static final Pattern DIRECTION = Pattern.compile("(INBOUND|OUTBOUND)");
    static final Pattern HEADER = Pattern.compile("(?:INBOUND|OUTBOUND): ([\\s\\S]*)");
    static final Pattern HTTP_METHOD = Pattern.compile("(GET|POST|HEAD|POST|PUT|DELETE|CONNECT|OPTIONS|TRACE|PATCH)");
    static final Pattern PATH = Pattern.compile("(?:GET|POST|HEAD|POST|PUT|DELETE|CONNECT|OPTIONS|TRACE|PATCH)"
            + " ([^\\s]+)");

    static final Pattern HEADER_PARSABLE =  Pattern.compile("^(?:[A-Z]*?).*\\)[\\s\\S],(.*)");

    public static LogParser getLogParserInstance() {
        if (logParserInstance == null) {
            logParserInstance = new LogParser();
        }
        return logParserInstance;
    }

    public void startListner(LaunchManager launchManagerInstance) {
        try {
            listenSocket = new ServerSocket(5010);
            Socket dataSocket = listenSocket.accept();
            logReader = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
            String line;
            while ((line = logReader.readLine()) != null) {
                JsonElement jelement = new JsonParser().parse(line);
                JsonObject jobject = jelement.getAsJsonObject();
                String rawRecord;
                try {
                    rawRecord = jobject.get("record").getAsJsonObject().get("message").getAsString();
                } catch (Exception e) {
                    rawRecord = jelement.getAsString();
                }
                jobject.addProperty("meta", parseLogLine(rawRecord));

                launchManagerInstance.pushLogToClient(jobject.toString());
            }
        } catch (Exception e) {
            stopListner();
        }
    }

    public void stopListner() {
        try {
            logReader.close();
            listenSocket.close();
        } catch (IOException e) {

        }
    }

    private String getId(String logLine) {
        Matcher matcher = ID_PATTERN.matcher(logLine);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "";
        }
    }

    private String getDirection(String logLine) {
        Matcher matcher = DIRECTION.matcher(logLine);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "";
        }
    }

    private String getHeader(String logLine) {
        Matcher matcher = HEADER.matcher(logLine);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "";
        }
    }

    private String getHttpMethod(String logLine) {
        Matcher matcher = HTTP_METHOD.matcher(logLine);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "";
        }
    }
    private String getPath(String logLine) {
        Matcher matcher = PATH.matcher(logLine);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "";
        }
    }


    private String parseLogLine(String logLine) {

        Gson gson = new Gson();
        LogDTO log = new LogDTO();
        log.setId(getId(logLine));
        log.setDirection(getDirection(logLine));
        log.setHeaders(getHeader(logLine));
        String rawHeader = getHeader(logLine);
        log.setHttpMethod(getHttpMethod(logLine));
        log.setParsedHeader(parseHeader(rawHeader));
        log.setPath(getPath(logLine));
        String json = gson.toJson(log);
        return json;
    }

    private List<String> parseHeader(String header) {
        Matcher matcher = HEADER_PARSABLE.matcher(header);
        String parsableRawHeader;
        List<String> parsedHeader = new ArrayList<>();
        if (matcher.find()) {
            parsableRawHeader = matcher.group(1);
            String[] rawHeaderArray = parsableRawHeader.split(",");
            StringBuilder logLineBuilder = new StringBuilder();
            for (int i = 0; i < rawHeaderArray.length; i++) {
                if (rawHeaderArray[i].split(": ").length > 1) {
                    parsedHeader.add(logLineBuilder.toString());
                    logLineBuilder.setLength(0);
                    logLineBuilder.append(rawHeaderArray[i] + ",");
                } else {
                    logLineBuilder.append(rawHeaderArray[i]);
                    if (i == rawHeaderArray.length - 1) {
                        parsedHeader.add(logLineBuilder.toString());
                    }
                }
            }
        }
        return parsedHeader;
    }
}
