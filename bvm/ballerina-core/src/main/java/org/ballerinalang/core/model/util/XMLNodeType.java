/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.core.model.util;

/**
 * Type of nodes a BXML represents.
 * 
 * @since 0.88
 */
public enum XMLNodeType {
    SEQUENCE("sequence"),
    ELEMENT("element"),
    TEXT("text"),
    COMMENT("comment"),
    PI("pi");
    
    String nodeType;

    XMLNodeType(String value) {
        nodeType = value;
    }

    public String value() {
        return nodeType;
    }
}
