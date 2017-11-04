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
package org.ballerinalang.model.values;

import org.ballerinalang.model.types.BEnumType;
import org.ballerinalang.model.types.BType;

/**
 * {@code BEnumerator} represents an enumerator of an enum.
 *
 * @since 0.95
 */
public class BEnumerator implements BRefType {

    private String name;
    private BEnumType type;

    public BEnumerator(String name, BEnumType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    @Override
    public String stringValue() {
        return name;
    }

    @Override
    public BType getType() {
        return type;
    }

    @Override
    public BValue copy() {
        return this;
    }

    @Override
    public Object value() {
        return name;
    }
}
