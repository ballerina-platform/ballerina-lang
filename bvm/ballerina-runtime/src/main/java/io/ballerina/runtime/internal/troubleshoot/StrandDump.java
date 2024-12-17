/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package io.ballerina.runtime.internal.troubleshoot;

import com.sun.management.HotSpotDiagnosticMXBean;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.management.MBeanServer;


/**
 * Used to get the status of current Ballerina strands.
 *
 * @since 2201.2.0
 */
public final class StrandDump {
    private static final String HOT_SPOT_BEAN_NAME = "com.sun.management:type=HotSpotDiagnostic";
    private static final String WORKING_DIR = System.getProperty("user.dir") + "/";
    private static final String FILENAME = "threadDump" + LocalDateTime.now();
    private static final String VIRTUAL_THREAD_IDENTIFIER = "virtual";
    private static final String ISOLATED_IDENTIFIER = "io.ballerina.runtime.internal.scheduling." +
            "Scheduler.lambda$startIsolated";
    private static final String NON_ISOLATED_IDENTIFIER = "io.ballerina.runtime.internal.scheduling." +
            "Scheduler.lambda$startNonIsolated";
    private static final String JAVA_TRACE_PATTERN = "java\\.|\\.java(?::\\d+)?";    // .java, java., .java:(any number)
    private static final String BAL_TRACE_PATTERN = "\\.bal:\\d+";                  // .bal:(any number)
    private static volatile HotSpotDiagnosticMXBean hotSpotDiagnosticMXBean;

    public static String getStrandDump() {
        String dump;
        try {
            getStrandDump(WORKING_DIR + FILENAME);
            dump = new String(Files.readAllBytes(Paths.get(FILENAME)));
            File fileObj = new File(FILENAME);
            fileObj.delete();
        } catch (Exception e) {
            return "Error occurred during strand dump generation";
        }
        return generateOutput(dump);
    }

    private static String generateOutput(String dump) {
        String[] dumpItems = dump.split("\\n\\n");
        int id = 0;
        Set<Integer> isolatedStrandList = new HashSet<>();
        Set<Integer> nonIsolatedStrandList = new HashSet<>();
        Map<Integer, ArrayList<String>> balTraces = new HashMap<>();
        Pattern javaPattern = Pattern.compile(JAVA_TRACE_PATTERN);
        Pattern balPattern = Pattern.compile(BAL_TRACE_PATTERN);
        for (String item : dumpItems) {
            String[] lines = item.split("\\n");
            String[] subitems = lines[0].split("\" ");
            ArrayList<String> balTraceItems = new ArrayList<>();
            boolean isBalStrand = false;
            if (subitems.length > 1 && subitems[1].equals(VIRTUAL_THREAD_IDENTIFIER)) {
                balTraceItems.add("\tStrand " + lines[0].replace(VIRTUAL_THREAD_IDENTIFIER, ":") + "\n\t\tat");
                String prefix = " ";
                for (String line : lines) {
                    if (!javaPattern.matcher(line).find() && !line.contains("\" " + VIRTUAL_THREAD_IDENTIFIER)) {
                        balTraceItems.add(prefix + line + "\n");
                        prefix = "\t\t   ";
                        if (balPattern.matcher(line).find()) {
                            isBalStrand = true;
                        }
                    } else {
                        if (line.contains(ISOLATED_IDENTIFIER)) {
                            isolatedStrandList.add(id);
                        } else if (line.contains(NON_ISOLATED_IDENTIFIER)) {
                            nonIsolatedStrandList.add(id);
                        }
                    }
                }
                if (isBalStrand) {
                    balTraces.put(id, balTraceItems);
                } else {
                    isolatedStrandList.remove(id);
                    nonIsolatedStrandList.remove(id);
                }
                id++;
            }
        }
        StringBuilder outputStr = new StringBuilder("Ballerina Strand Dump [");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.now();
        outputStr.append(dateTimeFormatter.format(localDateTime));
        outputStr.append("]\n===============================================================\n\n");
        outputStr.append("Total Strand count       \t\t\t:\t").append(balTraces.size()).append("\n\n");
        outputStr.append("Total Isolated Strand count       \t\t:\t").append(isolatedStrandList.size()).append("\n\n");
        outputStr.append("Total Non Isolated Strand count       \t\t:\t").append(nonIsolatedStrandList.size()).
                append("\n\n");
        outputStr.append("================================================================\n");
        outputStr.append("\nIsolated Strands:\n\n");
        for (int strandId: isolatedStrandList) {
            if (balTraces.containsKey(strandId)) {
                balTraces.get(strandId).forEach(outputStr::append);
                outputStr.append("\n");
            }
        }
        outputStr.append("Non Isolated Strands:\n\n");
        for (int strandId: nonIsolatedStrandList) {
            if (balTraces.containsKey(strandId)) {
                balTraces.get(strandId).forEach(outputStr::append);
                outputStr.append("\n");
            }
        }
        return outputStr.toString();
    }

    private static void getStrandDump(String fileName) throws IOException {
        if (hotSpotDiagnosticMXBean == null) {
            hotSpotDiagnosticMXBean = getHotSpotDiagnosticMXBean();
        }
        hotSpotDiagnosticMXBean.dumpThreads(fileName, HotSpotDiagnosticMXBean.ThreadDumpFormat.TEXT_PLAIN);
    }

    private static HotSpotDiagnosticMXBean getHotSpotDiagnosticMXBean() throws IOException {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        return ManagementFactory.newPlatformMXBeanProxy(mBeanServer, HOT_SPOT_BEAN_NAME, HotSpotDiagnosticMXBean.class);
    }
}