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

package io.ballerina.runtime.api;

import io.ballerina.runtime.internal.scheduling.Scheduler;
import io.ballerina.runtime.internal.scheduling.Strand;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * External API to be used by the users to get status of Ballerina runtime.
 *
 * @since 2201.2.0
 */
public class Diagnostics {

    private static final Set<Scheduler> allSchedulers = new HashSet<>();
    private static final Map<Integer, Strand> allStrands = new HashMap<>();

    public static void addToSchedulers(Scheduler scheduler) {
        allSchedulers.add(scheduler);
    }

    public static void addToStrands(Integer strandId, Strand strand) {
        allStrands.put(strandId, strand);
    }

    public static void removeFromStrands(Integer strandId) {
        allStrands.remove(strandId);
    }

    public static String getAllSchedulerInfo() {
        StringBuilder infoStr = new StringBuilder();
        infoStr.append("No. of Schedulers: " + allSchedulers.size() + "\n");
        for (Scheduler scheduler: allSchedulers) {
            infoStr.append(scheduler.dumpState().toString());
        }
        return infoStr.toString();
    }

    public static String getAllStrandInfo() {
        StringBuilder infoStr = new StringBuilder();
        infoStr.append("No. of currently available Strands: " + allStrands.size() + "\n\n");
        for (Strand strand : allStrands.values()) {
            infoStr.append(strand.dumpState());
            infoStr.append("\n");
        }
        return infoStr.toString();
    }

    private Diagnostics() {}
 }
