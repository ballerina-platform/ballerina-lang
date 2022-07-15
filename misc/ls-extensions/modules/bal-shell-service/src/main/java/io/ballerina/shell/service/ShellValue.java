/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.shell.service;

import io.ballerina.runtime.api.types.Type;
import io.ballerina.shell.service.util.TypeUtils;

import static io.ballerina.runtime.api.utils.TypeUtils.getType;

/**
 * Format for the generated result by execution.
 *
 * @since 2201.1.1
 */
public class ShellValue {
    private final String value;
    private final String mimeType;
    private final String type;

    public ShellValue(Object value) {
        Type type = getType(value);
        this.value = TypeUtils.convertToJsonIfAcceptable(value);
        this.type = type.toString();
        this.mimeType = TypeUtils.getMimeTypeFromName(type);
    }

    public String getValue() {
        return value;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getType() {
        return type;
    }
}
