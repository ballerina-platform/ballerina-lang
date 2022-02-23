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

/**
 * Object that holds the mapping of the data object to its type.
 */
public class MapperObject {
    private final String key;
    private final DataObject dataObject;

    protected MapperObject(String key, DataObject dataObject) {
        this.key = key;
        this.dataObject = dataObject;
    }

    protected String getKey() {
        return key;
    }

    protected DataObject getDataObject() {
        return dataObject;
    }
}
