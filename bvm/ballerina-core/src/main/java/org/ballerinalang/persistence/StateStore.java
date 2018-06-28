/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.persistence.states.State;
import org.ballerinalang.persistence.store.StorageProvider;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * This class implements @{@link StorageProvider} for persist @{@link State}s as files.
 *
 * @since 0.976.0
 */
public class StateStore implements StorageProvider {

    private static final String BASE_PATH = "states";
    private static final Logger log = LoggerFactory.getLogger(StateStore.class);

    @Override
    public void persistState(String instanceId, String workerName, String stateString) {
        File baseDir = new File(BASE_PATH);
        if (!baseDir.exists()) {
            baseDir.mkdir();
        }
        File stateFile = new File(baseDir, instanceId + "_" + workerName + ".json");
        try {
            FileUtils.write(stateFile, stateString);
        } catch (IOException e) {
            log.error("Error while persisting state for instance id: {}", instanceId + "_" + workerName, e);
        }
    }

    @Override
    public void removeStates(String instanceId) {
        File baseDir = new File(BASE_PATH);
        if (!baseDir.exists()) {
            return;
        }
        File[] stateFiles = baseDir.listFiles();
        String instancePrefix = instanceId + "_";
        if (Objects.nonNull(stateFiles)) {
            for (File stateFile : stateFiles) {
                if (stateFile.getName().startsWith(instancePrefix)) {
                    stateFile.delete();
                }
            }
        }
    }

    @Override
    public List<String> getAllSerializedStates() {
        List<String> states = new LinkedList<>();
        File baseDir = new File(BASE_PATH);
        if (!baseDir.exists()) {
            return states;
        }
        File[] stateFiles = baseDir.listFiles();
        if (stateFiles != null) {
            for (File stateFile : stateFiles) {
                try {
                    String jsonState = FileUtils.readFileToString(stateFile);
                    states.add(jsonState);
                } catch (IOException e) {
                    throw new BallerinaException("Failed to retrieve states.", e);
                }
            }
        }
        return states;
    }

    @Override
    public void removeFailedStates(String instanceId) {
    }
}