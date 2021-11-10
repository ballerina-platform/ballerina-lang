/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.commons.registration;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Optional;

/**
 * Represents the abstract class for the client capability setter of the extension services.
 * Each extended service should have associated capability setter which will be loaded dynamically at the runtime.
 * 
 * @param <T> type of the capability
 *
 * @since 2.0.0
 */
public abstract class BallerinaClientCapabilitySetter<T extends BallerinaClientCapability> {
    private final Gson gson = new Gson();

    /**
     * Build the capability with the object model.
     * 
     * @return {@link BallerinaClientCapability} implementation
     */
    public Optional<T> build() {
        return Optional.empty();
    }

    /**
     * Build the client capability from a given json object.
     * 
     * @param config {@link JsonObject}
     * @return {@link BallerinaClientCapability} implementation
     */
    public Optional<T> build(JsonObject config) {
        return Optional.ofNullable(gson.fromJson(config, this.getCapability()));
    }

    /**
     * Get the capability name. This is a unique name which is the same as capability name.
     * The implementation of the particular service can decide how to uniquely maintain the name between the services
     * and the capabilities, without code duplication.
     * 
     * @return {@link String}
     */
    public abstract String getCapabilityName();

    /**
     * Get the class of the capability.
     * 
     * @return {@link Class}
     */
    public abstract Class<T> getCapability();
}
