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

package org.ballerinalang.langserver.extensions.ballerina.traces;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * LogParser.
 */
public class LogParser {

    static final Pattern ID_PATTERN = Pattern.compile("id: ([a-z0-9]*)");
    static final Pattern DIRECTION = Pattern.compile("(INBOUND|OUTBOUND)");
    static final Pattern HEADER = Pattern.compile("(?:INBOUND|OUTBOUND): (.*[\\n\\r])([\\s\\S]*)");
    static final Pattern HTTP_METHOD = Pattern.compile("(GET|POST|HEAD|POST|PUT|DELETE|CONNECT|OPTIONS|TRACE|PATCH)");
    static final Pattern PATH = Pattern.compile("(?:GET|POST|HEAD|POST|PUT|DELETE|CONNECT|OPTIONS|TRACE|PATCH)"
            + " ([^\\s]+)");
    static final Pattern CONTENT_TYPE = Pattern.compile("(?:content-type): ?(.*)",  Pattern.CASE_INSENSITIVE);
    static final Pattern PAYLOAD_REQUEST =
            Pattern.compile("(?:DefaultLastHttpContent)(?:.*[\\n\\r])([\\s\\S]*)");
    static final Pattern PAYLOAD_RESPONSE =
            Pattern.compile("(?:DefaultFullHttpResponse)(?:.*[\\n\\r])(?:[\\s\\S]*)(?:[\\n\\r])([\\s\\S]*)");

    private static String getId(String logLine) {
        Matcher matcher = ID_PATTERN.matcher(logLine);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static String getDirection(String logLine) {
        Matcher matcher = DIRECTION.matcher(logLine);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static String getHeaderType(String logLine) {
        Matcher matcher = HEADER.matcher(logLine);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static String getHeader(String logLine) {
        Matcher matcher = HEADER.matcher(logLine);
        return matcher.find() ? matcher.group(2) : "";
    }

    private static String getHttpMethod(String logLine) {
        Matcher matcher = HTTP_METHOD.matcher(logLine);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static String getPath(String logLine) {
        Matcher matcher = PATH.matcher(logLine);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static String getContentType(String logLine) {
        Matcher matcher = CONTENT_TYPE.matcher(logLine);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static String getPayload(String logLine) {
        String payload = "";
        Matcher requestMatcher = PAYLOAD_REQUEST.matcher(logLine);
        Matcher responseMatcher = PAYLOAD_RESPONSE.matcher(logLine);
        if (requestMatcher.find()) {
            payload = requestMatcher.group(1);
        }
        if (responseMatcher.find()) {
            payload = responseMatcher.group(1);
        }
        return payload;
    }

    /**
     * Remove payload from header.
     * @param header String
     * @param payload String
     * @return header String
     */
    private static String removePayload(String header, String payload) {
        return header.substring(0, header.length() - payload.length());
    }

    /**
     * Parse log line to a Trace object.
     * @param logLine String
     * @return log Trace
     */
    static Message fromString(String logLine) {
        String id = getId(logLine);
        String direction = getDirection(logLine);
        String header = getHeader(logLine);
        String headerType = getHeaderType(logLine);
        String payload = getPayload(logLine);
        String headers = removePayload(header, payload);
        String httpMethod = getHttpMethod(logLine);
        String path = getPath(logLine);
        String contentType = getContentType(logLine);

        return new Message(id, direction, headers, httpMethod, path, contentType, payload, headerType);
    }
}
