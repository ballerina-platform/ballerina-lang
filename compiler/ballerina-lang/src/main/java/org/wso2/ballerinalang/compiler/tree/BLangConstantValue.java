/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.tree;

import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

/**
 * A bean class to hold values of constants at compile time.
 * 
 * @since 1.0
 */
public class BLangConstantValue {

    public Object value;
    public BType type;

    public BLangConstantValue(Object value, BType type) {
        this.value = value;
        this.type = type;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (o instanceof BLangConstantValue) {
            BLangConstantValue that = (BLangConstantValue) o;

            if (this.type.tag != that.type.tag) {
                return false;
            }

            if (this.value == null) {
                return false;
            }

            return this.value.equals(that.value);
        }

        return this.value.equals(o);
    }
}
