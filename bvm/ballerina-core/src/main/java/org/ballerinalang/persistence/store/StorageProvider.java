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
package org.ballerinalang.persistence.store;

import org.ballerinalang.persistence.serializable.SerializableState;

import java.util.List;

/**
 * Representation of storage provider which will be used to persist @{@link SerializableState}s.
 *
 * @since 0.981.1
 */
public interface StorageProvider {

    /**
     * Persist the serialized runtime state in the storage .
     *
     * @param stateId     Identifier of the runtime state
     * @param stateString Serialized runtime state
     */
    void persistState(String stateId, String stateString);

    /**
     * Remove serialized state from the given storage.
     *
     * @param stateId Identifier of the runtime state
     */
    void removeActiveState(String stateId);

    /**
     * Provides all serialized states of the system.
     *
     * @return List of serialized state as strings
     */
    List<String> getAllSerializedStates();
}
