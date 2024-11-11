/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.runtime.api.types;

/**
 * Type of nodes a BXML represents.
 * 
 * @since 0.995.0
 */
public enum XmlNodeType {
    SEQUENCE("sequence"),
    ELEMENT("element"),
    TEXT("text"),
    COMMENT("comment"),
    PI("pi");

    final String nodeType;

    XmlNodeType(String value) {
        nodeType = value;
    }

    public String value() {
        return nodeType;
    }
}
