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
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class FileBasedStore extends StateStore {
    @Override
    public void persistState(String instanceId, State state) {

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
    public List<State> getStates() {
        return null;
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
