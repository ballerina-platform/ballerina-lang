/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.profiler.ui;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.ballerina.runtime.profiler.util.Constants.CPU_PRE_JSON;
import static io.ballerina.runtime.profiler.util.Constants.OUT_STREAM;

/**
 * This class contains the JSON parser of the Ballerina profiler.
 *
 * @since 2201.8.0
 */
public class JsonParser {

    private static final String VALUE_KEY = "value";

    public void initializeCPUParser(String skipFunctionString) {
        ArrayList<String> skipList = new ArrayList<>();
        skipList = skipFunctionString != null ? parseSkipFunctionStringToList(skipFunctionString) : skipList;
        skipList.add("$gen");
        skipList.add("getAnonType");
        cpuParser(skipList);
    }

    private ArrayList<String> parseSkipFunctionStringToList(String skipFunctionString) {
        String[] elements = skipFunctionString.replace("[", "").replace("]", "").split(", ");
        return new ArrayList<>(Arrays.asList(elements));
    }

    private int getTotalTime(JsonObject node) {
        int totalTime = 0;
        JsonArray children = node.getAsJsonArray("children");
        if (children != null) {
            for (int i = 0; i < children.size(); i++) {
                if (children.get(i).getAsJsonObject().get(VALUE_KEY).getAsInt() != -1) {
                    totalTime += children.get(i).getAsJsonObject().get(VALUE_KEY).getAsInt();
                }
            }
        }
        return totalTime;
    }

    private void writePerformanceJson(String parsedJson) {
        parsedJson = "var data = " + parsedJson;
        try (FileWriter myWriter = new FileWriter("performance_report.json", StandardCharsets.UTF_8)) {
            myWriter.write(parsedJson);
            myWriter.flush();
        } catch (IOException e) {
            OUT_STREAM.printf("An error occurred.%n");
        }
    }

    private boolean containsAnySkipList(String str, List<String> arrayList) {
        for (String s : arrayList) {
            if (str.contains(s)) {
                return true;
            }
        }
        return false;
    }

    private void cpuParser(ArrayList<String> skipList) {
        try {
            String jsonInput = FileUtils.readFileAsString(CPU_PRE_JSON);
            StringBuilder jsonInputStringBuffer = new StringBuilder(jsonInput);
            if (jsonInputStringBuffer.length() > 3) {
                // Removes the trailing comma
                jsonInputStringBuffer.deleteCharAt(jsonInputStringBuffer.length() - 3);
            }
            jsonInput = jsonInputStringBuffer.toString();
            List<StackTraceItem> input = populateStackTraceItems(jsonInput);
            // Create a Data object to store the output
            Data output = new Data("Root", input.get(0).time, new ArrayList<>());
            for (StackTraceItem stackTraceItem : input) {
                if (stackTraceItem.stackTrace.size() == 1) {
                    output.value = Math.max(output.value, stackTraceItem.time);
                } else {
                    analyseStackTraceItems(skipList, stackTraceItem, output);
                }
            }
            writeToValueJson(output);
        } catch (Exception throwable) {
            OUT_STREAM.println(throwable + "%n");
        }
    }

    private void analyseStackTraceItems(ArrayList<String> skipList, StackTraceItem stackTraceItem, Data output) {
        Data current = output;
        for (int i = 1; i < stackTraceItem.stackTrace.size(); i++) {
            String name = stackTraceItem.stackTrace.get(i);
            if (name.contains("$configureInit()")) {
                removeChildrenByNodeName(output, name);
                break;
            }
            if (!containsAnySkipList(name, skipList)) {
                current = populateChildNodes(stackTraceItem, current, name);
            }
        }
    }

    private void writeToValueJson(Data output) {
        Gson gson = new Gson();
        String json = gson.toJson(output);
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        int totalTime = getTotalTime(jsonObject);
        jsonObject.remove(VALUE_KEY);
        jsonObject.addProperty(VALUE_KEY, totalTime);
        writePerformanceJson(jsonObject.toString());
    }

    private Data populateChildNodes(StackTraceItem stackTraceItem, Data current, String name) {
        boolean found = false;
        for (Data child : current.children) {
            if (child.name.equals(name)) {
                child.value = Math.max(child.value, stackTraceItem.time);
                current = child;
                found = true;
                break;
            }
        }
        if (!found) {
            Data newChild = new Data(name, stackTraceItem.time, new ArrayList<>());
            current.children.add(newChild);
            current = newChild;
        }
        return current;
    }

    private List<StackTraceItem> populateStackTraceItems(String jsonInput) {
        Gson gson = new Gson();
        JsonArray jsonArr = gson.fromJson(jsonInput, JsonArray.class);
        List<StackTraceItem> stackTraceItems = new ArrayList<>();
        for (JsonElement jsonElement : jsonArr) {
            StackTraceItem person = gson.fromJson(jsonElement, StackTraceItem.class);
            stackTraceItems.add(person);
        }
        return stackTraceItems;
    }

    private void removeChildrenByNodeName(Data node, String nodeName) {
        if (node.name != null && node.name.equals(nodeName)) {
            node.children.clear();
            return;
        }
        for (Data child : node.children) {
            removeChildrenByNodeName(child, nodeName);
        }
    }

    /**
     * This class is used as a custom data class.
     *
     * @since 2201.8.0
     */
    private static class StackTraceItem {

        int time;
        List<String> stackTrace;

        public StackTraceItem(int time, List<String> stackTrace) {
            this.time = time;
            this.stackTrace = new ArrayList<>(stackTrace);
        }
    }

    /**
     * This class is used as a custom data class.
     *
     * @since 2201.8.0
     */
    private static class Data {

        String name;
        int value;
        List<Data> children;

        Data(String name, int value, List<Data> children) {
            this.name = name;
            this.value = value;
            this.children = children;
        }
    }
}
