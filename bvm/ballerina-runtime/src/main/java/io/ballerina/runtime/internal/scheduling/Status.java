/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.internal.scheduling;

import sun.misc.Signal;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Used to get the status of current Ballerina runtime.
 *
 * @since 2201.2.0
 */
public class Status {

    private static final PrintStream outStream = System.out;
    private static final ConcurrentHashMap<Integer, Strand> currentStrands = new ConcurrentHashMap<>();

    protected static void addToStrands(Integer strandId, Strand strand) {
        currentStrands.put(strandId, strand);
    }

    protected static void removeFromStrands(Integer strandId) {
        currentStrands.remove(strandId);
    }

    public static void initiateSignalListener() {
        try {
            Signal.handle(new Signal("TRAP"), signal -> outStream.println(getStrandDump()));
        } catch (IllegalArgumentException ignored) {
            // In some Operating Systems like Windows, "TRAP" POSIX signal is not supported.
            // There getting the strand dump using kill signals is not expected, hence this exception is ignored.
        }
    }

    private static String getStrandDump() {
        Map<Integer, Strand> availableStrands = new HashMap<>(currentStrands);
        int availableStrandCount = availableStrands.size();
        Map<Integer, List<String>> availableStrandGroups = new HashMap<>();
        for (Strand strand : availableStrands.values()) {
            int strandGroupHashCode = strand.strandGroup.hashCode();
            String strandState = strand.dumpState();
            availableStrandGroups.computeIfAbsent(strandGroupHashCode, k -> new ArrayList<>()).add(strandState);
        }
        availableStrands.clear();

        StringBuilder infoStr = new StringBuilder("Ballerina Strand Dump [");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.now();
        infoStr.append(dateTimeFormatter.format(localDateTime));
        infoStr.append("]\n===========================================\n\n");
        infoStr.append("Current no. of strand groups\t:\t").append(availableStrandGroups.size()).append("\n");
        infoStr.append("Current no. of strands      \t:\t").append(availableStrandCount).append("\n\n");
        availableStrandGroups.forEach((groupHashCode, strandList) -> {
            infoStr.append("group@").append(Integer.toHexString(groupHashCode)).append(": [")
                    .append(strandList.size()).append("]\n");
            strandList.forEach(infoStr::append);
        });
        availableStrandGroups.clear();
        infoStr.append("===========================================\n");
        return infoStr.toString();
    }

    private Status() {}
 }
