/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.runtime.profiler.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JSONParser {
    public static void initializeCPUParser(String skipFunctionString) {
        ArrayList<String> skipList = new ArrayList<>();
        skipList = skipFunctionString != null ? parseSkipFunctionStringToList(skipFunctionString) : skipList;
        skipList.add("$gen");
        skipList.add("getAnonType");
        cpuParser(skipList);
    }

    private static ArrayList<String> parseSkipFunctionStringToList(String skipFunctionString) {
        ArrayList<String> skipList = new ArrayList<>();
        String[] elements = skipFunctionString.replace("[", "").replace("]", "").split(", ");
        skipList.addAll(Arrays.asList(elements));
        return skipList;
    }

    public static int getTotalTime(JSONObject node) {
        int totalTime = 0; // Initialize total time
        JSONArray children = node.optJSONArray("children"); // Get the "children" array from the JSONObject
        if (children != null) {
            for (int i = 0; i < children.length(); i++) {
                if (children.getJSONObject(i).getInt("value") != -1) {
                    totalTime += children.getJSONObject(i).getInt("value"); // Add the value to the total time
                }
            }
        }
        return totalTime;
    }

    public static String readFileAsString(String file) throws Exception {
        return new String(Files.readAllBytes(Paths.get(file))); // Read Files as a String
    }

    static void writer(String parsedJson) {
        parsedJson = "var data = " + parsedJson;
        try {
            FileWriter myWriter = new FileWriter("performance_report.json"); // Create a FileWriter object to write to the specified file
            myWriter.write(parsedJson); // Write the parsed json string to the file
            myWriter.flush(); // Flush the writer
        } catch (IOException e) {
            System.out.println("An error occurred."); // Print an error message
        }
    }

    public static boolean containsAnySkipList(String str, ArrayList<String> arrayList) {
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
            StringBuffer jsonInputStringBuffer = new StringBuffer(jsonInput);
            jsonInputStringBuffer.deleteCharAt(jsonInputStringBuffer.length() - 3);
            jsonInput = jsonInputStringBuffer.toString();

            ObjectMapper mapper = new ObjectMapper(); // Create an ObjectMapper object to map json to Java objects
            List<Item> input = mapper.readValue(jsonInput, new TypeReference<List<Item>>() {
            }); // Map the json input to a list of Item objects

            // Create a Data object to store the output
            Data output = new Data();
            output.name = "Root";
            output.value = input.get(0).time;
            output.children = new ArrayList<>();

            // Iterate through the input list
            for (Item item : input) {
                if (item.stackTrace.size() == 1) {
                    output.value = Math.max(output.value, item.time); // Update the value of the root node
                } else {
                    Data current = output;
                    // Iterate through the stack trace
                    for (int i = 1; i < item.stackTrace.size(); i++) {
                        String name = item.stackTrace.get(i);
                        if (name.contains("$configureInit()")) {
                            removeChildrenByNodeName(output, name);
                            break;
                        }
                        if (!containsAnySkipList(name, skipList)) {

                            boolean found = false;
                            // Check if the child node already exists
                            for (Data child : current.children) {
                                if (child.name.equals(name)) {
                                    // Update the value of the existing child node
                                    child.value = Math.max(child.value, item.time);
                                    current = child;
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                // Create a new child node if it doesn't exist
                                Data newChild = new Data();
                                newChild.name = name;
                                newChild.value = item.time;
                                newChild.children = new ArrayList<>();
                                current.children.add(newChild);
                                current = newChild;
                            }
                        }
                    }
                }
            }

            String jsonString = mapper.writeValueAsString(output); // Convert the output data object to a json string
            JSONObject jsonObject = new JSONObject(jsonString); // Convert the json string to a JSONObject
            int totalTime = getTotalTime(jsonObject); // Calculate the total time
            jsonObject.remove("value"); // Remove the "value" key
            jsonObject.put("value", totalTime); // Add the total time as the value
            writer(jsonObject.toString()); // write the json object to a file
        } catch (Exception | Error throwable) {
            System.out.println(throwable);
        }
    }

    private static void removeChildrenByNodeName(Data node, String nodeName) {
        if (node.name.equals(nodeName)) {
            node.children.clear();
            return;
        }
        for (Data child : node.children) {
            removeChildrenByNodeName(child, nodeName);
        }
    }

    private static class Item {
        public int time;
        public List<String> stackTrace;
    }

    private static class Data {
        public String name;
        public int value;
        public List<Data> children;
    }
}