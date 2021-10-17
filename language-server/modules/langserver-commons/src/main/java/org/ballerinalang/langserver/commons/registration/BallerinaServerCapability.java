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

import org.eclipse.lsp4j.jsonrpc.validation.NonNull;

/**
 * Represents the interface for the server capabilities of the extension services.
 *
 * @since 2.0.0
 */
public class BallerinaServerCapability {
    @NonNull
    private String name;

    public BallerinaServerCapability(String name) {
        this.name = name;
    }

    /**
     * Unique name for the server capability.
     * The client capability and the server capability names MUST be the same.
     * Associated service name MUST be the same
     *
     * @return {@link String} name of the capability
     */
    protected String getName() {
        return this.name;
    }

    /**
     * Set the server capability name.
     *
     * @param name to be set
     */
    protected void setName(String name) {
        this.name = name;
    }
}
