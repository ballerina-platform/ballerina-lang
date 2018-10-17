/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.bir.model;

/**
 * Visibility of package level elements in BIR.
 *
 * @since 0.980.0
 */
public enum Visibility {
    /**
     * Symbols with this visibility are visible only within the package.
     */
    PACKAGE_PRIVATE((byte) 0),

    /**
     * This visibility is valid only for objects.
     */
    PRIVATE((byte) 1),

    /**
     * Visible across packages.
     */
    PUBLIC((byte) 2);

    private byte value;

    Visibility(byte value) {
        this.value = value;
    }

    public byte value() {
        return value;
    }
}
