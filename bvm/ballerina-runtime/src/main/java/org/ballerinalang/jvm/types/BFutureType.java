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
package org.ballerinalang.jvm.types;

/**
 * {@code BFutureType} represents a future value in Ballerina.
 *
 * @since 0.995.0
 */
public class BFutureType extends BType {

    private BType constraint;

    /**
     * Create a {@code {@link BFutureType}} which represents the future value.
     *
     * @param typeName string name of the type
     * @param pkgPath of the type
     */
    public BFutureType(String typeName, String pkgPath) {
        super(typeName, pkgPath, Object.class);
    }

    public BFutureType(BType constraint) {
        super(TypeConstants.FUTURE_TNAME, null, Object.class);
        this.constraint = constraint;
    }

    public BType getConstrainedType() {
        return constraint;
    }

    @Override
    public <V extends Object> V getZeroValue() {
        return null;
    }

    @Override
    public <V extends Object> V getEmptyValue() {
        return null;
    }

    @Override
    public int getTag() {
        return TypeTags.FUTURE_TAG;
    }

}
