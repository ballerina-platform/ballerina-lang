/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.persistence.serializable.serializer;

/**
 * Hold constant values used in JSON serialization.
 */
public class JsonSerializerConst {
    public static final String EXISTING_TAG = "#existing#";
    public static final String HASH_TAG = "#hash#";
    public static final String PAYLOAD_TAG = "payload";
    public static final String ENUM_TAG = "enum";
    public static final String LIST_TAG = "list";
    public static final String MAP_TAG = "map";
    public static final String TYPE_TAG = "type";
    public static final String COMPLEX_KEY_TAG = "complex_key";
    public static final String COMPLEX_KEY_MAP_TAG = "#complex_key_map#";
    public static final String ARRAY_TAG = "array";
    public static final String COMPONENT_TYPE = "componentType";
    public static final String BYTE_TAG = "byte";

    private JsonSerializerConst() {}
}
