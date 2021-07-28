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

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

/**
 * Helper class to initialize functions related to help /TOPIC command.
 *
 */
public class HelpInitiator {

    private static final String BALLERINA_HOME =
            System.getProperty("ballerina.home");
    private static final String BBE_PATH = "/examples/";
    private static final String DESCRIPTION = ".description";

    private final HashMap<String, String> helpMap;
    private final List<String> listCommands;
    private final HashMap<String, String> urlList;

    public HelpInitiator() {
        helpMap = new HashMap<>();
        listCommands = new ArrayList<>();
        urlList = new HashMap<>();
        initHelpTopicFile();
    }

    private void initHelpTopicFile() {
        // System.getProperty("ballerina.home");
        String file = "./examples/index.json";
        String bbePrefix = "./examples/";
        String jsonString = readFileAsString(file).trim();

        Gson gson = new Gson();
        BbeTitle[] bbeTitles = gson.fromJson(jsonString, BbeTitle[].class);
        Stream<BbeTitle> streamList = Arrays.stream(bbeTitles);

        streamList.forEach((bbeTitle) -> {
            BbeRecord[] samples = bbeTitle.getSamples();
            Stream<BbeRecord> sampleList = Arrays.stream(samples);
            sampleList.forEach((bbeRecordElement) -> {
                String bbePath = bbePrefix + bbeRecordElement.getUrl() +
                        "/" + String.join("_", bbeRecordElement.getUrl().split("-"))
                        + DESCRIPTION;
                listCommands.add(bbeRecordElement.getName());
                urlList.put(bbeRecordElement.getName(), bbeRecordElement.getUrl());
                String description = readFileAsString(bbePath).trim();
                helpMap.put(bbeRecordElement.getName(), description.replaceAll("//", ""));
            });
        });
    }

    public List<String> getCommandList() {
        return listCommands;
    }

    public String getDescription(String topic) {
        return helpMap.get(topic);
    }

    public String getUrl(String topic) {
        return urlList.get(topic);
    }

    private static String readFileAsString(String file) {
        String content;
        try {
            content = Files.readString(Paths.get(file));
        } catch (IOException e) {
            content = "None";
        }
        return content;
    }
}
