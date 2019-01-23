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

import java.util.Collection;
import java.util.Iterator;
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
     * @param memberTypes of the union type
     */
    public BUnionType(List<BType> memberTypes) {
        super(String.join("|", memberTypes.stream().map(BType::toString).collect(Collectors.toList())), null,
              BValue.class);
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
        if (nullable || memberTypes.stream().anyMatch(BTypes::isImplicitInitValueNil)) {
            return null;
        }

        return memberTypes.get(0).getZeroValue();
//        validateBasicType(firstType);
//
//        for (int i = 1; i < memberTypes.size(); i++) {
//            BType type = memberTypes.get(i);
//            if (!checkIsSameType(firstType, type)) {
//                throw new IllegalStateException(
//                        "Union type '" + this.typeName + "' does not have an implicit initial value.");
//            }
//        }
    }

    @Override
    public <V extends BValue> V getEmptyValue() {
        if (nullable || memberTypes.stream().anyMatch(BTypes::isImplicitInitValueNil)) {
            return null;
        }

        return memberTypes.get(0).getZeroValue();
//        BType firstType = memberTypes.get(0);
//        validateBasicType(firstType);
//
//        for (int i = 1; i < memberTypes.size(); i++) {
//            BType type = memberTypes.get(i);
//            if (!firstType.equals(type)) {
//                throw new IllegalStateException(
//                        "Union type '" + this.typeName + "' does not have an implicit initial value.");
//            }
//        }
//
//        return firstType.getEmptyValue();
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
        return memberTypes.containsAll(that.memberTypes) && that.memberTypes.containsAll(memberTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), memberTypes);
    }

    private void validateBasicType(BType type) {
        if (type.getTag() == TypeTags.UNION_TAG) {
            ((BUnionType) type).memberTypes.forEach(memType -> {
                if (!BTypes.isBasicType(memType)) {
                    throw new IllegalStateException(
                            "Component type '" + memType.getName() + "' of the union type '" + type.typeName +
                                    "' is not a basic type.");
                }
            });
        } else if (type.getTag() == TypeTags.FINITE_TYPE_TAG) {
            ((BFiniteType) type).valueSpace.forEach(value -> {
                if (value != null && !BTypes.isBasicType(value.getType())) {
                    throw new IllegalStateException(
                            "Component type '" + value.getType().getName() + "' of the union type '" + type.typeName +
                                    "' is not a basic type.");
                }
            });
        } else if (!BTypes.isBasicType(type)) {
            throw new IllegalStateException(
                    "Component type '" + type.getName() + "' of the union type '" + this.typeName +
                            "' is not a basic type.");
        }
    }

    private boolean checkIsSameType(BType source, BType target) {
        switch (target.getTag()) {
            case TypeTags.UNION_TAG:
                BUnionType unionType = (BUnionType) target;
                return checkIsSameType(unionType.memberTypes) && unionType.memberTypes.get(0).equals(source);
            case TypeTags.FINITE_TYPE_TAG:
                BFiniteType finiteType = (BFiniteType) target;
                return checkIsSameType(finiteType.valueSpace.stream()
                                               .map(val -> val == null ? BTypes.typeNull : val.getType())
                                               .collect(Collectors.toSet()))
                        && finiteType.valueSpace.iterator().next().getType().equals(source);
            default:
                return target.equals(source);
        }
    }

    private boolean checkIsSameType(Collection<BType> types) {
        boolean isSameType = true;
        Iterator<BType> valueIterator = types.iterator();
        BType firstValType = valueIterator.next();

        while (valueIterator.hasNext()) {
            BType valType = valueIterator.next();
            isSameType = isSameType && checkIsSameType(firstValType, valType);
        }

        return isSameType;
    }
}

