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

import io.ballerina.runtime.internal.scheduling.Scheduler;
import io.ballerina.runtime.internal.scheduling.Strand;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.runtime.internal.scheduling.ItemGroup.getCreatedStrandGroupCount;

/**
 * Used to get the status of current Ballerina strands.
 *
 * @since 2201.2.0
 */
public class StrandDump {

    public static String getStrandDump() {
        Map<Integer, Strand> availableStrands = Scheduler.getCurrentStrands();
        int createdStrandGroupCount = getCreatedStrandGroupCount();
        int createdStrandCount = Strand.getCreatedStrandCount();
        int availableStrandCount = availableStrands.size();
        Map<Integer, List<String>> availableStrandGroups = new HashMap<>();
        populateAvailableStrandGroups(availableStrands, availableStrandGroups);

        String strandDumpOutput = generateOutput(availableStrandGroups, availableStrandCount, createdStrandGroupCount,
                createdStrandCount);
        cleanUp(availableStrands, availableStrandGroups);
        return strandDumpOutput;
    }

    private static String generateOutput(Map<Integer, List<String>> availableStrandGroups, int availableStrandCount,
                                         int createdStrandGroupCount, int createdStrandCount) {
        StringBuilder outputStr = new StringBuilder("Ballerina Strand Dump [");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.now();
        outputStr.append(dateTimeFormatter.format(localDateTime));
        outputStr.append("]\n===========================================\n\n");
        outputStr.append("Total strand group count \t:\t").append(createdStrandGroupCount).append("\n");
        outputStr.append("Total strand count       \t:\t").append(createdStrandCount).append("\n");
        outputStr.append("Active strand group count\t:\t").append(availableStrandGroups.size()).append("\n");
        outputStr.append("Active strand count      \t:\t").append(availableStrandCount).append("\n\n");
        availableStrandGroups.forEach((strandGroupId, strandList) -> {
            outputStr.append("group ").append(strandGroupId).append(" [").append(strandList.get(0)).append("]: [")
                    .append(strandList.size() - 1).append("]\n");
            strandList.subList(1, strandList.size()).forEach(outputStr::append);
        });
        outputStr.append("===========================================\n");
        return outputStr.toString();
    }

    private static void populateAvailableStrandGroups(Map<Integer, Strand> availableStrands,
                                                      Map<Integer, List<String>> availableStrandGroups) {
        for (Strand strand : availableStrands.values()) {
            int strandGroupId = strand.getStrandGroup().getId();
            String strandState = strand.dumpState();
            availableStrandGroups.computeIfAbsent(strandGroupId, k -> {
                ArrayList<String> strandDataList = new ArrayList<>();
                strandDataList.add(getStrandGroupStatus(strand.getStrandGroup().isScheduled()));
                return strandDataList;
            }).add(strandState);
        }
    }

    private static void cleanUp(Map<Integer, Strand> availableStrands,
                                Map<Integer, List<String>> availableStrandGroups) {
        availableStrands.clear();
        availableStrandGroups.clear();
    }

    private static String getStrandGroupStatus(boolean isStrandGroupScheduled) {
        if (isStrandGroupScheduled) {
            return "RUNNABLE";
        } else {
            return "QUEUED";
        }
    }

    private StrandDump() {}
 }
