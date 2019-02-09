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

import org.ballerinalang.bre.old.WorkerExecutionContext;
import org.ballerinalang.persistence.Deserializer;
import org.ballerinalang.persistence.serializable.SerializableState;
import org.ballerinalang.persistence.states.State;
import org.ballerinalang.persistence.store.impl.FileStorageProvider;
import org.ballerinalang.runtime.Constants;
import org.ballerinalang.util.codegen.ProgramFile;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is used to persist @{@link State}s in given storage.
 *
 * @since 0.981.1
 */
public class PersistenceStore {

    private static StorageProvider storageProvider = new FileStorageProvider();

    public static void persistState(State state) {
        SerializableState sState = new SerializableState(state.getContext(), state.getIp());
        sState.setId(state.getId());
        String stateString = sState.serialize();
        storageProvider.persistState(state.getId(), stateString);
    }

    public static void removeStates(String stateId) {
        storageProvider.removeActiveState(stateId);
    }

    public static List<State> getStates(ProgramFile programFile) {
        List<String> serializedStates = storageProvider.getAllSerializedStates();
        if (serializedStates.isEmpty()) {
            return Collections.emptyList();
        }
        Deserializer deserializer = new Deserializer();
        List<State> states = new LinkedList<>();
        for (String serializedState : serializedStates) {
            SerializableState sState = SerializableState.deserialize(serializedState);
            WorkerExecutionContext context = sState.getExecutionContext(programFile, deserializer);
            State state = new State(context, (String) context.globalProps.get(Constants.STATE_ID));
            // have to decrement ip as CPU class increments it as soon as instruction is fetched
            context.ip--;
            state.setIp(context.ip);
            states.add(state);
        }
        deserializer.cleanUpDeserializer();
        return states;
    }

    public static void setStorageProvider(StorageProvider storageProvider) {
        PersistenceStore.storageProvider = storageProvider;
    }
}
