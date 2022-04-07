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

import java.util.List;

/**
 * Represents the input parameters for the initBalServices API in the language server.
 *
 * @since 2.0.0
 */
public class BallerinaInitializeParams {
    private List<Object> ballerinaClientCapabilities;

    public BallerinaInitializeParams() {
    }

    /**
     * Set the list of client capabilities.
     * 
     * @param ballerinaClientCapabilities {@link List}
     */
    public void setExtendedServerCapabilities(List<Object> ballerinaClientCapabilities) {
        this.ballerinaClientCapabilities = ballerinaClientCapabilities;
    }

    /**
     * Get the client capabilities for the extended services.
     * 
     * @return {@link List}
     */
    public List<Object> getBallerinaClientCapabilities() {
        return ballerinaClientCapabilities;
    }
}
