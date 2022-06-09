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
import java.util.HashMap;
import java.util.Map;

/**
 * Used to get the status of current Ballerina runtime.
 *
 * @since 2201.2.0
 */
public class Status {

    private static final PrintStream outStream = System.out;
    private static final Map<Integer, Strand> currentStrands = new HashMap<>();

    protected static void addToStrands(Integer strandId, Strand strand) {
        currentStrands.put(strandId, strand);
    }

    protected static void removeFromStrands(Integer strandId) {
        currentStrands.remove(strandId);
    }

    public static void initiateSignalListener() {
        Signal.handle(new Signal("USR1"), signal -> outStream.println(getRuntimeStateDump()));
    }

    private static String getRuntimeStateDump() {
        StringBuilder infoStr = new StringBuilder("Ballerina Runtime State Dump [");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.now();
        infoStr.append(dateTimeFormatter.format(localDateTime));
        infoStr.append("]\n==================================================\n\n");

        infoStr.append("Current strands: (Total " + currentStrands.size() + ")\n============================\n\n");
        for (Strand strand : currentStrands.values()) {
            infoStr.append(strand.dumpState());
            infoStr.append("\n");
        }
        infoStr.append("==================================================\n");
        return infoStr.toString();
    }

    private Status() {}
 }
