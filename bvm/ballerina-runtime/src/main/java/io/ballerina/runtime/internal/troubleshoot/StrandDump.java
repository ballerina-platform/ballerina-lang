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

import javax.management.MBeanServer;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Used to get the status of current Ballerina strands.
 *
 * @since 2201.2.0
 */
public class StrandDump {
    private static final String hotSpotBeanName = "com.sun.management:type=HotSpotDiagnostic";
    private static volatile HotSpotDiagnosticMXBean hotSpotDiagnosticMXBean;

    public static String getStrandDump() {
        final String WORKING_DIR = System.getProperty("user.dir") + "/";
        final String FILENAME = "thread_dump" + LocalDateTime.now();
        String dump;
        try {
            getStrandDump( WORKING_DIR + FILENAME);
            dump = new String(Files.readAllBytes(Paths.get(FILENAME)));
            File fileObj = new File(FILENAME);
            fileObj.delete();
        } catch (Exception e) {
            return "Error occurred during strand dump generation";
        }
        return generateOutput(dump);
    }

    private static String generateOutput(String dump) {
        final String VIRTUAL_THREAD_IDENTIFIER = "virtual";
        final String ISOLATED_WORKER_IDENTIFIER = "Scheduler.lambda$startIsolatedWorker";
        final String NON_ISOLATED_WORKER_IDENTIFIER = "Scheduler.lambda$startNonIsolatedWorker";
        final String JAVA_TRACE_ITEM_IDENTIFIER = "java";
        final String BAL_TRACE_ITEM_IDENTIFIER = ".bal";
        String[] dump_items = dump.split("\\n\\n");
        int id = 0;
        Set<Integer> isolatedWorkerList = new HashSet<>();
        Set<Integer> nonIsolatedWorkerList = new HashSet<>();
        ArrayList<ArrayList<String>> balTraces = new ArrayList<>();
        for(String item : dump_items) {
            String[] lines = item.split("\\n");
            String[] subitems = lines[0].split("\" ");
            ArrayList<String> balTraceItems = new ArrayList<>();
            boolean bal_strand = false;
            if(subitems.length > 1 && subitems[1].equals(VIRTUAL_THREAD_IDENTIFIER)) {
                balTraceItems.add("\tStrand " + lines[0].replace(VIRTUAL_THREAD_IDENTIFIER, ":") + "\n\t\tat");
                String prefix = " ";
                for(String line : lines) {
                    if(line.contains(ISOLATED_WORKER_IDENTIFIER)) {
                        isolatedWorkerList.add(id);
                    }
                    if(line.contains(NON_ISOLATED_WORKER_IDENTIFIER)) {
                        nonIsolatedWorkerList.add(id);
                    }
                    if(!line.contains(JAVA_TRACE_ITEM_IDENTIFIER) && !line.contains("\" " + VIRTUAL_THREAD_IDENTIFIER)){
                        balTraceItems.add(prefix + line + "\n");
                        prefix = "\t\t   ";
                    }
                    if(line.contains(BAL_TRACE_ITEM_IDENTIFIER)) {
                        bal_strand = true;
                    }
                }
                if(bal_strand) {
                    balTraces.add(balTraceItems);
                }else {
                    isolatedWorkerList.remove(id);
                    nonIsolatedWorkerList.remove(id);
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
        outputStr.append("Total Isolated Worker count       \t\t:\t").append(isolatedWorkerList.size()).append("\n\n");
        outputStr.append("Total Non Isolated Worker count       \t\t:\t").append(nonIsolatedWorkerList.size()).append("\n\n");
        outputStr.append("================================================================\n");
        outputStr.append("\nIsolated Workers:\n\n");
        for(int strandId: isolatedWorkerList){
            balTraces.get(strandId).forEach(outputStr::append);
            outputStr.append("\n");
        }
        outputStr.append("Non Isolated Workers:\n\n");
        for(int strandId: nonIsolatedWorkerList){
            balTraces.get(strandId).forEach(outputStr::append);
            outputStr.append("\n");
        }
        return outputStr.toString();
    }

    private static void getStrandDump(String fileName) throws IOException {
        if (hotSpotDiagnosticMXBean == null) {
            synchronized (StrandDump.class) {
                hotSpotDiagnosticMXBean = getHotSpotDiagnosticMXBean();
            }
        }
        hotSpotDiagnosticMXBean.dumpThreads(fileName, HotSpotDiagnosticMXBean.ThreadDumpFormat.TEXT_PLAIN);
    }

    private static HotSpotDiagnosticMXBean getHotSpotDiagnosticMXBean() throws IOException {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        return ManagementFactory.newPlatformMXBeanProxy(mBeanServer, hotSpotBeanName, HotSpotDiagnosticMXBean.class);
    }
 }
