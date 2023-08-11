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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.ballerina.runtime.profiler.util.Constants.OUT;

/**
 * This class contains the JSON parser of the ballerina profiler.
 *
 * @since 2201.7.0
 */
public class JSONParser {

    private JSONParser() {
    }

    private static final String VALUE_KEY = "value";

    public static void initializeCPUParser(String skipFunctionString) {
        ArrayList<String> skipList = new ArrayList<>();
        skipList = skipFunctionString != null ? parseSkipFunctionStringToList(skipFunctionString) : skipList;
        skipList.add("$gen");
        skipList.add("getAnonType");
        cpuParser(skipList);
    }

    private static ArrayList<String> parseSkipFunctionStringToList(String skipFunctionString) {
        String[] elements = skipFunctionString.replace("[", "").replace("]", "").split(", ");
        return new ArrayList<>(Arrays.asList(elements));
    }

    public static int getTotalTime(JsonObject node) {
        int totalTime = 0; // Initialize total time
        JsonArray children = node.getAsJsonArray("children"); // Get the "children" array from the JSONObject
        if (children != null) {
            for (int i = 0; i < children.size(); i++) {
                if (children.get(i).getAsJsonObject().get(VALUE_KEY).getAsInt() != -1) {
                    totalTime += children.get(i).getAsJsonObject().get(VALUE_KEY).getAsInt(); // Add the value to the total time
                }
            }
        }
        return totalTime;
    }

    public static String readFileAsString(String file) throws IOException {
        return Files.readString(Paths.get(file)); // Read Files as a String
    }

    static void writer(String parsedJson) {
        parsedJson = "var data = " + parsedJson;
        try (FileWriter myWriter = new FileWriter("performance_report.json", StandardCharsets.UTF_8)) {
            myWriter.write(parsedJson); // Write the parsed json string to the file
            myWriter.flush(); // Flush the writer
        } catch (IOException e) {
            OUT.printf("An error occurred.%n"); // Print an error message
        }
    }

    public static boolean containsAnySkipList(String str, List<String> arrayList) {
        for (String s : arrayList) {
            if (str.contains(s)) {
                return true;
            }
        }
        return false;
    }

    private static void cpuParser(ArrayList<String> skipList) {
        try {
            String file = "CpuPre.json"; // File path of the Profiler Output json file
            String jsonInput = readFileAsString(file); // Read the json file as a string

            // Removes the trailing comma
            StringBuilder jsonInputStringBuffer = new StringBuilder(jsonInput);
            jsonInputStringBuffer.deleteCharAt(jsonInputStringBuffer.length() - 3);
            jsonInput = jsonInputStringBuffer.toString();
            // Populate the input list
            List<StackTraceItem> input = populateStackTraceItems(jsonInput);
            // Create a Data object to store the output
            Data output = new Data("Root", input.get(0).time, new ArrayList<>());
            // Iterate through the input list
            for (StackTraceItem stackTraceItem : input) {
                if (stackTraceItem.stackTrace.size() == 1) {
                    // Update the value of the root node
                    output.value = Math.max(output.value, stackTraceItem.time);
                } else {
                    // Iterate through the stack trace
                    analyseStackTraceItems(skipList, stackTraceItem, output);
                }
            }
            writeToValueJson(output);
        } catch (Exception throwable) {
            OUT.println(throwable + "%n");
        }
    }

    private static void analyseStackTraceItems(ArrayList<String> skipList, StackTraceItem stackTraceItem, Data output) {
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

    private static void writeToValueJson(Data output) {
        Gson gson = new Gson();
        String json = gson.toJson(output);
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        int totalTime = getTotalTime(jsonObject); // Calculate the total time
        jsonObject.remove(VALUE_KEY); // Remove the "value" key
        jsonObject.addProperty(VALUE_KEY, totalTime); // Add the total time as the value
        writer(jsonObject.toString()); // write the json object to a file
    }

    private static Data populateChildNodes(StackTraceItem stackTraceItem, Data current, String name) {
        boolean found = false;
        // Check if the child node already exists
        for (Data child : current.children) {
            if (child.name.equals(name)) {
                // Update the value of the existing child node
                child.value = Math.max(child.value, stackTraceItem.time);
                current = child;
                found = true;
                break;
            }
        }
        if (!found) {
            // Create a new child node if it doesn't exist
            Data newChild = new Data(name, stackTraceItem.time, new ArrayList<>());
            current.children.add(newChild);
            current = newChild;
        }
        return current;
    }

    private static List<StackTraceItem> populateStackTraceItems(String jsonInput) {
        Gson gson = new Gson();
        JsonArray jsonArr = gson.fromJson(jsonInput, JsonArray.class);
        List<StackTraceItem> stackTraceItems = new ArrayList<>();
        for (JsonElement jsonElement : jsonArr) {
            StackTraceItem person = gson.fromJson(jsonElement, StackTraceItem.class);
            stackTraceItems.add(person);
        }
        return stackTraceItems;
    }

    private static void removeChildrenByNodeName(Data node, String nodeName) {
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
     * @since 2201.7.0
     */
    public static class StackTraceItem {

        int time;
        List<String> stackTrace;

        public StackTraceItem(int time, List<String> stackTrace) {
            this.time = time;
            this.stackTrace = stackTrace;
        }
    }

    /**
     * This class is used as a custom data class.
     *
     * @since 2201.7.0
     */
    static class Data {

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
