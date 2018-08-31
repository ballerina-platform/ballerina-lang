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

import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.persistence.serializable.SerializableState;
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
 * This class implements @{@link StorageProvider} for persist @{@link SerializableState}s as files.
 *
 * @since 0.981.1
 */
public class FileStorageProvider implements StorageProvider {

    // System property of directory path for storing serialized states.
    public static final String INTERRUPTIBLE_STATES_FILE_PATH = "ballerina.interruptible.directory.path";

    private static String stateStoreDirPath = "ballerina-states";

    private static final Logger log = LoggerFactory.getLogger(FileStorageProvider.class);

    static {
        String pathPropValue = ConfigRegistry.getInstance().getAsString(INTERRUPTIBLE_STATES_FILE_PATH);
        if (pathPropValue != null) {
            stateStoreDirPath = pathPropValue;
        }
    }

    @Override
    public void persistState(String stateId, String stateString) {
        File baseDir = new File(stateStoreDirPath);
        if (!baseDir.exists()) {
            baseDir.mkdir();
        }
        try {
            /*
            save the context to a temp file and rename to json. This is to make the serializable state
            is completely written to file before saving it as a json.
             */
            String fileName = baseDir.getPath() + File.separator + stateId;
            Path path = Paths.get(fileName + ".tmp");
            Files.write(path, stateString.getBytes());
            path.toFile().renameTo(new File(fileName + ".json"));
        } catch (IOException e) {
            log.error("Error while persisting the state for state id: {}", stateId, e);
        }
    }

    @Override
    public void removeActiveState(String stateId) {
        File baseDir = new File(stateStoreDirPath);
        if (!baseDir.exists()) {
            return;
        }
        try {
            Files.delete(Paths.get(baseDir.getPath() + File.separator + stateId + ".json"));
        } catch (IOException e) {
            log.error("Error while removing the state for state id: {}", stateId, e);
        }
    }

    @Override
    public List<String> getAllSerializedStates() {
        List<String> states = new LinkedList<>();
        File baseDir = new File(stateStoreDirPath);
        if (!baseDir.exists()) {
            return states;
        }
        try (Stream<Path> stream = Files.list(Paths.get(stateStoreDirPath))) {
            stream.forEach(path -> {
                try {
                    if (path.toString().endsWith(".json")) {
                        states.add(new String(Files.readAllBytes(path)));
                    } else {
                        // Remove temp files which are partially written state files.
                        Files.delete(path);
                    }
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
