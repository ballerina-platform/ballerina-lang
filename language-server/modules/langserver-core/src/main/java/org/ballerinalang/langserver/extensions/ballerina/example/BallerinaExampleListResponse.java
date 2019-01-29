/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.extensions.ballerina.example;

import java.util.List;

/**
 * Represents a Ballerina example list response.
 *
 * @since 0.981.2
 */
public class BallerinaExampleListResponse {

    private List<BallerinaExampleCategory> samples;

    public List<BallerinaExampleCategory> getSamples() {
        return samples;
    }

    public void setSamples(List<BallerinaExampleCategory> samples) {
        this.samples = samples;
    }
}
