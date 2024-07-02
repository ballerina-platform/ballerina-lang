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

package org.wso2.ballerinalang.compiler.util;

/**
 * {@code DefaultValueLiteral} contains the default value and the type tag of a Ballerina literal.
 *
 * @since 0.985.0
 */
public class DefaultValueLiteral {
    private final Object defaultValue;
    private final int literalTypeTag;

    public DefaultValueLiteral(Object defaultValue, int literalTypeTag) {
        this.defaultValue = defaultValue;
        this.literalTypeTag = literalTypeTag;
    }

    public Object getValue() {
        return defaultValue;
    }

    public int getLiteralTypeTag() {
        return literalTypeTag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DefaultValueLiteral that = (DefaultValueLiteral) o;

        return literalTypeTag == that.literalTypeTag && defaultValue.equals(that.defaultValue);
    }

    @Override
    public int hashCode() {
        int result = defaultValue.hashCode();
        result = 31 * result + literalTypeTag;
        return result;
    }
}
