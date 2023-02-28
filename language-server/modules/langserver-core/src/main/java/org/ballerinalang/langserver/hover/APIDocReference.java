/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.hover;

/**
 * Hover Provider for Ballerina.
 *
 * @since 2201.2.0
 */
public class APIDocReference {

    private static final String BASE_URL = "https://lib.ballerina.io/";

    public static String from(String orgName,
                              String moduleName,
                              String version,
                              String constructReference) {
        String url = BASE_URL + String.join("/", orgName, moduleName, version);
        return url + "#" + constructReference;
    }
}
