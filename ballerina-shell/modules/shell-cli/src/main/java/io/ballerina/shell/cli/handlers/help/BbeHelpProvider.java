/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.cli.handlers.help;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Helper class to initialize functions related to help /TOPIC command.
 *
 */
public class BbeHelpProvider {

    private static final String BALLERINA_HOME = System.getProperty("ballerina.home");
    private static final String EXAMPLES = "examples";
    private static final String DESCRIPTION = ".description";

    public BbeHelpProvider() {
    }

    public String getDescription(String topic) throws HelpProviderException {
        String topicUrl = topic.replace(" ", "-");
        String bbePrefix = BALLERINA_HOME + File.separator + EXAMPLES + File.separator;
        String bbePath = bbePrefix + topicUrl + File.separator + String.join("_", topicUrl.split("-")) + DESCRIPTION;
        String description = readFileAsString(bbePath).trim();
        return description.replaceAll("//", "");
    }

    private static String readFileAsString(String file) throws HelpProviderException {
        String content;
        try {
            content = Files.readString(Paths.get(file));
        } catch (IOException e) {
            throw new HelpProviderException("Error occurred while executing the command");
        }
        return content;
    }
}
