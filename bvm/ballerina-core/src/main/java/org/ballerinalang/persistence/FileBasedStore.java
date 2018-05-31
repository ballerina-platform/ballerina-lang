/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.persistence;

import org.apache.commons.io.FileUtils;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.bre.bvm.persistency.PersistenceUtils;
import org.ballerinalang.bre.bvm.persistency.SerializableState;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ResourceInfo;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangVMUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileBasedStore extends StateStore {

    private String basePath = "states";

    @Override
    public void persistState(String instanceId, State state) {

        SerializableState sState = new SerializableState(state.getContext());
        sState.setInstanceId(instanceId);
        String stateString = sState.serialize();

        File baseDir = new File(basePath);
        if (!baseDir.exists()) {
            baseDir.mkdir();
        }

        String workerName = state.getContext().workerInfo.getWorkerName();
        File stateFile = new File(baseDir, instanceId + "_" + workerName + ".json");
        try {
            FileUtils.write(stateFile, stateString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeStates(String instanceId) {
        File baseDir = new File(basePath);
        if (!baseDir.exists()) {
            return;
        }

        File[] stateFiles = baseDir.listFiles();
        String instancePrefix = instanceId + "_";
        for (File stateFile : stateFiles) {
            if (stateFile.getName().startsWith(instancePrefix)) {
                stateFile.delete();
            }
        }
    }

    @Override
    public void persistFaildState(String instanceId, State state) {
        try {
            File instanceDir = new File(instanceId);
            if (!instanceDir.exists()) {
                FileUtils.forceMkdir(instanceDir);
            }
            File file = new File(instanceDir, UUID.randomUUID().toString() + ".json");
            String jsonCtx = PersistenceUtils.getJson(state.getContext());
            PersistenceUtils.saveJsonFIle(jsonCtx, file);
        } catch (Exception e) {
            throw new BallerinaException("Failed to persist state for instance: " + instanceId, e);
        }
    }

    @Override
    public List<State> getStates(ProgramFile programFile) {
        List<State> states = new ArrayList<>();
        File baseDir = new File(basePath);
        if (!baseDir.exists()) {
            return states;
        }

        File[] stateFiles = baseDir.listFiles();
        for (File stateFile : stateFiles) {
            try {
                String jsonState = FileUtils.readFileToString(stateFile);
                SerializableState sState = SerializableState.deserialize(jsonState);
                WorkerExecutionContext context = sState.getExecutionContext(programFile);
                if (context.callableUnitInfo instanceof ResourceInfo) {
                    ResourceInfo resourceInfo = (ResourceInfo) context.callableUnitInfo;
                    BLangVMUtils.setServiceInfo(context, resourceInfo.getServiceInfo());
                }
                State state = new State(context);
                // have to decrement ip as CPU class increments it as soon as instruction is fetched
                context.ip--;
                state.setIp(context.ip);
                states.add(state);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        PersistenceUtils.clearTempRefTypes("s_");
        PersistenceUtils.clearTempContexts();
        PersistenceUtils.tempRespContexts.clear();
        return states;
    }

    @Override
    public List<State> getStates(String instanceId) {
        return null;
    }

    @Override
    public List<State> getFailedStates(String instanceId) {
//        try {
//            File instanceDir = new File(instanceId);
//            if (instanceDir.exists()) {
//                for (File state : instanceDir.listFiles()) {
//                    WorkerExecutionContext failedCtx = PersistenceUtils.reloadContext(null, state);
//                }
//
//                FileUtils.forceMkdir(instanceDir);
//            }
//            File file = new File(instanceDir, UUID.randomUUID().toString() + ".json");
//            String jsonCtx = PersistenceUtils.getJson(state.getContext());
//            PersistenceUtils.saveJsonFIle(jsonCtx, file);
//        } catch (Exception e) {
//            throw new BallerinaException("Failed to persist state for instance: " + instanceId, e);
//        }
        return null;
    }

    @Override
    public void removeFailedStates(String instanceId) {

    }
}
