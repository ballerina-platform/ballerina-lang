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
package org.ballerinalang.langserver.telemetry;

import org.ballerinalang.langserver.commons.LSOperation;

/**
 * Represents a telemetry event for language server.
 *
 * @since 2.0.0
 */
public abstract class LSTelemetryEvent {

    /** Sub types of this class. */
    public static final String TYPE_ERROR_EVENT = "ErrorTelemetryEvent";
    public static final String TYPE_FEATURE_USAGE_EVENT = "FeatureUsageTelemetryEvent";

    protected static final String LS_PACKAGE_NAME = "org.ballerinalang.langserver";
    protected static final String LS_TELEMETRY_COMPONENT_NAME = "component.langserver";
    
    /** This field is added for the plugin to differentiate event types. */
    private final String type;
    private final String component;
    private final String version;

    protected LSTelemetryEvent(String type, String component, String version) {
        this.type = type;
        this.component = component;
        this.version = version;
    }

    protected static String getComponentName(LSOperation operation) {
        if (operation == null) {
            return LS_TELEMETRY_COMPONENT_NAME;
        }
        return LS_TELEMETRY_COMPONENT_NAME + "." + operation.getName().replace("/", "_");
    }

    public String getComponent() {
        return component;
    }

    public String getVersion() {
        return version;
    }

    public String getType() {
        return type;
    }
}
