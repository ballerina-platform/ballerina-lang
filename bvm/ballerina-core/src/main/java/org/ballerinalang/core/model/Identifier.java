/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.core.model;

/**
 * {@code Identifier} represents a identifier used in {@code BLangSymbol}.
 *
 * @since 0.87
 */
public class Identifier {
    private static final String VERTICAL_BAR = "|";

    private String name;

    private boolean isLiteral = false;

    public Identifier(String name) {
        if (name != null && name.startsWith(VERTICAL_BAR)) {
            name = name.substring(1, name.length() - 1);
            this.isLiteral = true;
        }
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isLiteral() {
        return isLiteral;
    }
}

