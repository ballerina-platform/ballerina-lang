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
package io.ballerina.runtime.types;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.IntegerType;

/**
 * {@code BIntegerType} represents an integer which is a 32-bit signed number.
 *
 * @since 0.995.0
 */
@SuppressWarnings("unchecked")
public class BIntegerType extends BType implements IntegerType {

    private final int tag;

    /**
     * Create a {@code BIntegerType} which represents the boolean type.
     *
     * @param typeName string name of the type
     */
    public BIntegerType(String typeName, Module pkg) {
        super(typeName, pkg, Long.class);
        tag = TypeTags.INT_TAG;
    }

    public BIntegerType(String typeName, Module pkg, int tag) {
        super(typeName, pkg, Long.class);
        this.tag = tag;
    }

    @Override
    public <V extends Object> V getZeroValue() {
        return (V) new Long(0);
    }

    @Override
    public <V extends Object> V getEmptyValue() {
        return (V) new Long(0);
    }

    @Override
    public int getTag() {
        return tag;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }
}
