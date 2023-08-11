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

package io.ballerina.runtime.profiler.runtime;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class is used as the main profiler class for the ballerina profiler.
 *
 * @since 2201.8.0
 */
public class Profiler {
    private final HashMap<String, Data> profiles = new HashMap<>();
    private final ArrayList<Data> profilesStack = new ArrayList<>();
    private final ArrayList<String> blockedMethods = new ArrayList<>();
    private static final List<String> skippedList = new ArrayList<>();
    private static final Set<String> skippedClasses = new HashSet<>(skippedList);

    private static final String GENERATED_METHOD_PREFIX = "$gen$";

    protected Profiler() {
        shutDownHookProfiler();
        try {
            String content = Files.readString(Paths.get("usedPathsList.txt"));
            List<String> skippedListRead = new ArrayList<>(Arrays.asList(content.split(", ")));
            skippedList.addAll(skippedListRead);
            skippedClasses.addAll(skippedList);
        } catch (IOException ignored) {
            throw new ProfilerRuntimeException("Error occurred while reading the usedPathsList.txt file");
        }
    }

    private static class ProfilerHolder {
        private static final Profiler INSTANCE = new Profiler();
    }

    public static Profiler getInstance() {
        return ProfilerHolder.INSTANCE;
    }

    public String getMethodName() {
        final List<StackWalker.StackFrame> stack = StackWalker.getInstance().walk(s -> s.collect(Collectors.toList()));
        return stack.get(2).getMethodName() + "()";
    }

    public void removeDuplicates(List<String> list) {
        List<String> newList = new ArrayList<>();
        for (String element : list) {
            if (!newList.contains(element)) {
                newList.add(element);
            }
        }
        list.clear();
        list.addAll(newList);
    }

    public void start(int id) {
        String name = getStackTrace();
        if (!blockedMethods.contains(getMethodName() + id)) {
            Data p = this.profiles.get(name);
            if (p == null) {
                p = new Data(name);
                this.profiles.put(name, p);
                this.profilesStack.add(p);
            }
            p.start();
            removeDuplicates(blockedMethods);
        }
        blockedMethods.remove(getMethodName() + id);
    }

    public void start() {
        String name = getStackTrace();
        Data p = this.profiles.get(name);
        if (p == null) {
            p = new Data(name);
            this.profiles.put(name, p);
            this.profilesStack.add(p);
        }
        p.start();
        removeDuplicates(blockedMethods);
    }

    public void stop(String strandState, int id) {
        String name = getStackTrace();
        Data p = this.profiles.get(name);
        if (strandState.equals("RUNNABLE")) {
            if (p != null) {
                p.stop();
            }
        } else {
            blockedMethods.add(getMethodName() + id);
        }
    }

    public void stop() {
        String name = getStackTrace();
        Data p = this.profiles.get(name);
        if (p != null) {
            p.stop();
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < this.profilesStack.size(); i++) {
            sb.append(this.profilesStack.get(i)).append("\n");
        }
        sb.append("]");
        return sb.toString();
    }

    public void printProfilerOutput(String dataStream) {
        try (Writer myWriter = new FileWriter("CpuPre.json", StandardCharsets.UTF_8)) {
            myWriter.write(dataStream);
        } catch (IOException e) {
            throw new ProfilerRuntimeException("Error occurred while writing to the CpuPre.json file");
        }
    }

    public static void shutDownHookProfiler() {
        // add a shutdown hook to stop the profiler and parse the output when the program is closed
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Profiler profiler = Profiler.getInstance();
            profiler.printProfilerOutput(profiler.toString());
        }));
    }

    // This method returns a string representation of the current call stack in the form of a list of strings
    public String getStackTrace() {
        ArrayList<String> result = new ArrayList<>();

        final List<StackWalker.StackFrame> stack = StackWalker.getInstance().walk(s -> s.collect(Collectors.toList()));
        //Removes the first 2 stack frames (index 0 and 1) and reverses the order of the remaining stack frames
        stack.subList(0, 2).clear();
        Collections.reverse(stack); //Reverse the collection

        for (StackWalker.StackFrame frame : stack) {
            if (skippedClasses.contains(frame.getClassName())) {
                String frameString = frame.toString();

                if (frameString.contains("&")) {
                    frameString = "\"" + frameString + "\"";
                } else {
                    frameString = "\"" + frameString.replaceAll("\\(.*\\)", "") + "()" + "\"";
                    int lastDotIndex = frameString.lastIndexOf('.');
                    frameString = frameString.substring(0, lastDotIndex)
                            .replace('.', '/') + frameString.substring(lastDotIndex);
                }
                result.add(decodeIdentifier(frameString));
            }
        }

        return result.toString();
    }

    // This method takes an encoded identifier string as input and returns the decoded version of it
    public static String decodeIdentifier(String encodedIdentifier) {
        if (encodedIdentifier == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        int index = 0;
        while (index < encodedIdentifier.length()) {
            if (encodedIdentifier.charAt(index) == '$' && index + 4 < encodedIdentifier.length()) {
                if (isUnicodePoint(encodedIdentifier, index)) {
                    stringBuilder.append((char) Integer.parseInt(encodedIdentifier.substring(index + 1, index + 5)));
                    index += 5;
                } else {
                    stringBuilder.append(encodedIdentifier.charAt(index));
                    index++;
                }
            } else {
                stringBuilder.append(encodedIdentifier.charAt(index));
                index++;
            }
        }
        return decodeGeneratedMethodName(stringBuilder.toString());
    }

    // This method checks if the substring of the encoded identifier starting
    // from the given index represents a Unicode code point
    private static boolean isUnicodePoint(String encodedName, int index) {
        return (containsOnlyDigits(encodedName.substring(index + 1, index + 5)));
    }

    // This method takes a decoded method name as input and removes any generated method prefixes from it
    private static String decodeGeneratedMethodName(String decodedName) {
        return decodedName.startsWith(GENERATED_METHOD_PREFIX) ?
                decodedName.substring(GENERATED_METHOD_PREFIX.length()) : decodedName;
    }

    // This method checks if the given string contains only digits
    private static boolean containsOnlyDigits(String digitString) {
        for (int i = 0; i < digitString.length(); i++) {
            if (!Character.isDigit(digitString.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
