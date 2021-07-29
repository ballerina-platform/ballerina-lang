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
import java.util.List;
import java.util.stream.Stream;

/**
 * Helper class to provide code completions related to /help topics.
 *
 */
public class BbeCompletionProvider {

    private static final String BALLERINA_HOME =
            System.getProperty("ballerina.home");
    private static final String BBE_PATH = "/examples/";
    private static final String INDEX_FILE = "index.json";

    private final List<String> topicList;

    public BbeCompletionProvider() {
        topicList = new ArrayList<>();
    }

    public List<String> getTopicList() {
        Gson gson = new Gson();

        String file = BALLERINA_HOME + BBE_PATH + INDEX_FILE;
        String jsonString = readFileAsString(file).trim();

        BbeTitle[] bbeTitles = gson.fromJson(jsonString, BbeTitle[].class);
        Stream<BbeTitle> streamList = Arrays.stream(bbeTitles);

        streamList.forEach((bbeTitle) -> {
            BbeRecord[] samples = bbeTitle.getSamples();
            Stream<BbeRecord> sampleList = Arrays.stream(samples);
            sampleList.forEach((bbeRecordElement) -> {
                topicList.add(bbeRecordElement.getName());
            });
        });
        return topicList;
    }

    private static String readFileAsString(String file) {
        String content;
        try {
            content = Files.readString(Paths.get(file));
        } catch (IOException e) {
            return null;
        }
        return content;
    }
}
