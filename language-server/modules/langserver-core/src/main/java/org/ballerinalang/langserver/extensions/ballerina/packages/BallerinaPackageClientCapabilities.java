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
package org.ballerinalang.langserver.extensions.ballerina.packages;

import org.ballerinalang.langserver.commons.registration.BallerinaClientCapability;

/**
 * Client capabilities for the ballerinaPackage service.
 *
 * @since 2.0.0
 */
public class BallerinaPackageClientCapabilities extends BallerinaClientCapability {

    private boolean metadata;

    private boolean components;

    private boolean configSchema;

    public boolean isMetadata() {
        return metadata;
    }

    public void setMetadata(boolean metadata) {
        this.metadata = metadata;
    }

    public boolean isComponents() {
        return components;
    }

    public void setComponents(boolean components) {
        this.components = components;
    }

    public boolean isConfigSchema() {
        return this.configSchema;
    }

    public void setConfigSchema(boolean configSchema) {
        this.configSchema = configSchema;
    }

    public BallerinaPackageClientCapabilities() {
        super(PackageServiceConstants.CAPABILITY_NAME);
    }
}
