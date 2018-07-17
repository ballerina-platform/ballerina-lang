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
package org.ballerinalang.persistence.store.impl;

import org.ballerinalang.persistence.states.State;
import org.ballerinalang.persistence.store.StorageProvider;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

/**
 * This class implements @{@link StorageProvider} for persist @{@link State}s as files.
 *
 * @since 0.976.0
 */
public class FileBasedProvider implements StorageProvider {

    private static final String BASE_PATH = "states";
    private static final Logger log = LoggerFactory.getLogger(FileBasedProvider.class);

    @Override
    public void persistState(String instanceId, String stateString) {
        File baseDir = new File(BASE_PATH);
        if (!baseDir.exists()) {
            baseDir.mkdir();
        }
        try {
            Files.write(Paths.get(baseDir.getPath() + File.separator + instanceId + ".json"),
                        stateString.getBytes());
        } catch (IOException e) {
            log.error("Error while persisting the state for instance id: {}", instanceId, e);
        }
    }

    @Override
    public void removeActiveState(String instanceId) {
        File baseDir = new File(BASE_PATH);
        if (!baseDir.exists()) {
            return;
        }
        try {
            Files.delete(Paths.get(baseDir.getPath() + File.separator + instanceId + ".json"));
        } catch (IOException e) {
            log.error("Error while removing the state for instance id: {}", instanceId, e);
        }
    }

    @Override
    public List<String> getAllSerializedStates() {
        List<String> states = new LinkedList<>();
        File baseDir = new File(BASE_PATH);
        if (!baseDir.exists()) {
            return states;
        }
        try (Stream<Path> stream = Files.list(Paths.get(BASE_PATH))) {
            stream.forEach(path -> {
                try {
                    states.add(new String(Files.readAllBytes(path)));
                } catch (IOException e) {
                    throw new BallerinaException("Error occurred while reading state from file path: " + path, e);
                }
            });
        } catch (IOException e) {
            throw new BallerinaException("Failed to retrieve states.", e);
        }
        return states;
    }
}
