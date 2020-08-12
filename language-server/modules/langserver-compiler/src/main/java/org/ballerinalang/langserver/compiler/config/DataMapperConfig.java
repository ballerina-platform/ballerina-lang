/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.compiler.config;

/**
 * Ballerina Data Mapper code action Configuration.
 */
public class DataMapperConfig {
    private final boolean enabled;
    private final String url;

    DataMapperConfig() {
        this.enabled = false;
        this.url = "";
    }

    /**
     * Returns True if data mapper is enabled, False otherwise.
     *
     * @return True or False
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Returns the URL set for the data mapper back end service.
     *
     * @return string URL
     */
    public String getUrl() {
        return url;
    }
}
