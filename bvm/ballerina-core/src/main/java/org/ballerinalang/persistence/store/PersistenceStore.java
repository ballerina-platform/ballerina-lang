/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.ballerinalang.persistence.store;

import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.model.util.serializer.JsonSerializer;
import org.ballerinalang.persistence.Deserializer;
import org.ballerinalang.persistence.RuntimeStates;
import org.ballerinalang.persistence.Serializer;
import org.ballerinalang.persistence.State;
import org.ballerinalang.persistence.serializable.SerializableState;
import org.ballerinalang.persistence.store.impl.FileStorageProvider;
import org.ballerinalang.runtime.Constants;
import org.ballerinalang.util.codegen.ProgramFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is used to persist @{@link SerializableState}s in given storage.
 *
 * @since 0.981.1
 */
public class PersistenceStore {

    private static final Logger log = LoggerFactory.getLogger(PersistenceStore.class);

    private static StorageProvider storageProvider = new FileStorageProvider();

    public static void persistState(WorkerExecutionContext ctx, int ip) {
        String stateId = (String) ctx.globalProps.get(Constants.STATE_ID);
        SerializableState state = RuntimeStates.get(stateId);
        if (state != null) {
            storageProvider.persistState(state.getId(), state.checkPoint(ctx, ip));
        } else {
            log.error("State doesn't exist for id : " + stateId);
        }
    }

    public static void removeStates(String stateId) {
        storageProvider.removeActiveState(stateId);
    }

    public static List<State> getStates(ProgramFile programFile, Deserializer deserializer) {
        List<String> serializedStates = storageProvider.getAllSerializedStates();
        if (serializedStates == null || serializedStates.isEmpty()) {
            return Collections.emptyList();
        }
        return serializedStates
                .stream()
                .map(s -> createState(deserialize(s), programFile, deserializer))
                .collect(Collectors.toList());
    }

    public static void setStorageProvider(StorageProvider storageProvider) {
        PersistenceStore.storageProvider = storageProvider;
    }

    private static State createState(SerializableState deSerializedState, ProgramFile programFile,
                                     Deserializer deserializer) {
        // we need to generate new state with deserialised state inorder to use newly created serializable data and
        // object hashes.
        List<WorkerExecutionContext> executableCtxList = deSerializedState.getExecutionContexts(programFile,
                                                                                                deserializer);
        SerializableState sState = new SerializableState(deSerializedState.getId(), executableCtxList);
        return new State(sState, executableCtxList);
    }

    public static SerializableState deserialize(String json) {
        JsonSerializer jsonSerializer = Serializer.getJsonSerializer();
        return jsonSerializer.deserialize(json, SerializableState.class);
    }
}
