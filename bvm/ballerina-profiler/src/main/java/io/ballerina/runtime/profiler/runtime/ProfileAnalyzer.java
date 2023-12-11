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

import io.ballerina.runtime.internal.scheduling.State;
import io.ballerina.runtime.internal.scheduling.Strand;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import static io.ballerina.runtime.profiler.util.Constants.CPU_PRE_JSON;
import static io.ballerina.runtime.profiler.util.Constants.STRAND_PROFILER_STACK_PROPERTY;

/**
 * This class is used as the main profiler class for the Ballerina profiler.
 *
 * @since 2201.8.0
 */
public class ProfileAnalyzer {

    private final ConcurrentHashMap<String, Data> profiles = new ConcurrentHashMap<>();

    private static class ProfilerHolder {
        private static final ProfileAnalyzer PROFILER_INSTANCE = new ProfileAnalyzer();
    }

    public static ProfileAnalyzer getInstance() {
        return ProfilerHolder.PROFILER_INSTANCE;
    }

    private ProfileAnalyzer() {
        addProfilerShutDownHook();
    }

    public Data start(Strand strand, String className, String methodName) {
        Object stacksObj = strand.getProperty(STRAND_PROFILER_STACK_PROPERTY);
        String stackIndex = StackTraceMap.getStackIndex(className + "." + methodName) + "$";
        String stackKey;
        if (stacksObj != null) {
            stackKey = stacksObj + stackIndex;
        } else {
            stackKey = stackIndex;
        }
        Data data;
        if (this.profiles.containsKey(stackKey)) {
            data = this.profiles.get(stackKey);
        } else {
            data = new Data(stackIndex, stackKey);
            this.profiles.put(stackKey, data);
        }
        data.start(String.valueOf(strand.getId()));
        strand.setProperty(STRAND_PROFILER_STACK_PROPERTY, stackKey);
        return data;
    }

    public void stop(Strand strand, Data data) {
        if (strand.getState().equals(State.RUNNABLE)) {
            data.stop(String.valueOf(strand.getId()));
        }
        strand.setProperty(STRAND_PROFILER_STACK_PROPERTY, data.stackKey.substring(0,
                data.stackKey.length() - data.stackIndex.length()));
    }

    public final String getProfileStackString() {
        StringBuilder sb = new StringBuilder("[");
        ArrayList<Data> dataList = new ArrayList<>(this.profiles.values());
        for (int i = 0; i < (dataList.size() - 1); i++) {
            Data data = dataList.get(i);
            data.stackTrace = StackTraceMap.getCallStackString(data.stackKey);
            sb.append(data).append(",\n");
        }
        Data data = dataList.get(dataList.size() - 1);
        data.stackTrace = StackTraceMap.getCallStackString(data.stackKey);
        sb.append(data).append("\n");
        sb.append("]");
        return sb.toString();
    }

    private void printProfilerOutput(String dataStream) {
        try (Writer myWriter = new FileWriter(CPU_PRE_JSON, StandardCharsets.UTF_8)) {
            myWriter.write(dataStream);
        } catch (IOException e) {
            throw new ProfilerRuntimeException("Error occurred while writing to the " + CPU_PRE_JSON + " file");
        }
    }

    private void addProfilerShutDownHook() {
        // add a shutdown hook to stop the profiler and parse the output when the program is closed
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ProfileAnalyzer profiler = ProfileAnalyzer.getInstance();
            profiler.printProfilerOutput(profiler.getProfileStackString());
        }));
    }
}
