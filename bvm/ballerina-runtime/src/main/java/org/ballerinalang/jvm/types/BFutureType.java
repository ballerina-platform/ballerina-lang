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

import org.ballerinalang.jvm.TypeChecker;

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
     * @param pkg of the type
     */
    public BFutureType(String typeName, BPackage pkg) {
        super(typeName, pkg, Object.class);
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

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj) || !(obj instanceof BTableType)) {
            return false;
        }

        BFutureType other = (BFutureType) obj;
        if (constraint == other.constraint) {
            return true;
        }

        return TypeChecker.checkIsType(constraint, other.constraint);
    }

}
