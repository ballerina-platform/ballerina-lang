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
package org.ballerinalang.model.types;

import org.ballerinalang.model.values.BValue;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * {@code BUnionType} represents a union type in Ballerina.
 *
 * @since 0.956
 */
public class BUnionType extends BType {

    private List<BType> memberTypes;
    private boolean nullable;

    /**
     * Create a {@code BUnionType} which represents the union type.
     */
    public BUnionType() {
        super(null, null, BValue.class);
    }

    /**
     * Create a {@code BUnionType} which represents the union type.
     *
     * @param memberTypes
     */
    public BUnionType(List<BType> memberTypes) {
        super(null, null, BValue.class);
        this.memberTypes = memberTypes;
        this.nullable = memberTypes.contains(BTypes.typeNull);
    }

    public List<BType> getMemberTypes() {
        return memberTypes;
    }

    public boolean isNullable() {
        return nullable;
    }

    @Override
    public <V extends BValue> V getZeroValue() {
        return null;
    }

    @Override
    public <V extends BValue> V getEmptyValue() {
        return null;
    }

    @Override
    public TypeSignature getSig() {
        return new TypeSignature(TypeSignature.SIG_UNION,
                memberTypes.stream().map(type -> type.getSig()).collect(Collectors.toList()));
    }

    @Override
    public int getTag() {
        return TypeTags.UNION_TAG;
    }

    @Override
    public String toString() {
        List<String> list = memberTypes.stream().map(BType::toString).collect(Collectors.toList());
        return String.join("|", list);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BUnionType)) {
            return false;
        }
        BUnionType that = (BUnionType) o;
        return Objects.equals(memberTypes, that.memberTypes);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), memberTypes);
    }
}
