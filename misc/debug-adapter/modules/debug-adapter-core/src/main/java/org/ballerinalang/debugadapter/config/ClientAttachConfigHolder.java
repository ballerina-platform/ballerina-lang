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

package org.ballerinalang.debugadapter.config;

import java.util.Map;
import java.util.Optional;

/**
 * Client configuration holder implementation for attach-mode.
 *
 * @since 2.0.0
 */
public class ClientAttachConfigHolder extends ClientConfigHolder {

    // The host name or IP address of remote debuggee.
    private String hostName;

    public ClientAttachConfigHolder(Map<String, Object> args) {
        super(args, ClientConfigKind.ATTACH_CONFIG);
        hostName = null;
    }

    public Optional<String> getHostName() {
        if (hostName == null) {
            if (!clientRequestArgs.containsKey(ARG_DEBUGGEE_HOST)) {
                return Optional.empty();
            }
            hostName = clientRequestArgs.get(ARG_DEBUGGEE_HOST).toString();
        }
        return Optional.ofNullable(hostName);
    }
}
