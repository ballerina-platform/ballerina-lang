/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.logging.util;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * This class is used for parsing the log format strings in Ballerina log configurations. This takes the Ballerina log
 * format string and builds the corresponding Java format string.
 *
 * @since 0.95.0
 */
public class FormatStringParser {

    private final Map<TokenType, Integer> placeholderMap;

    private FormatStringTokenizer tokenizer;
    private SimpleDateFormat dateFormat;

    public FormatStringParser(FormatStringTokenizer tokenizer, Map<TokenType, Integer> placeholderMap) {
        this.tokenizer = tokenizer;
        this.placeholderMap = placeholderMap;
    }

    public String buildJDKLogFormat() {
        StringBuilder fsBuilder = new StringBuilder();

        Token token;
        while (tokenizer.hasNext()) {
            token = tokenizer.nextToken();
            if (token.type == TokenType.TIMESTAMP) {
                String timestampFormat;
                try {
                    timestampFormat = token.text.substring(TokenTypes.FMT_TIMESTAMP.length() + 1,
                                                           token.text.length() - 1);
                    dateFormat = new SimpleDateFormat(timestampFormat);
                    fsBuilder.append("%1$s");
                } catch (Exception e) {
                    PrintStream console = System.err;
                    console.println("ballerina: invalid timestamp format");
                }
            } else if (token.type == TokenType.TEXT) {
                fsBuilder.append(token.text);
            } else {
                fsBuilder.append("%" + placeholderMap.get(token.type) + "$s");
            }
        }

        // TODO: Give the user the freedom to choose whether to end with a new line char or not
        return fsBuilder.append("\n").toString();
    }

    public SimpleDateFormat getDateFormat() {
        return dateFormat == null ? Constants.DEFAULT_TIMESTAMP_FORMAT : dateFormat;
    }
}
