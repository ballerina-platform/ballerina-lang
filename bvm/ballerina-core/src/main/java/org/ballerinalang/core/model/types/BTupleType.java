/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.core.model.types;

import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * {@code {@link BTupleType}} represents a tuple type in Ballerina.
 *
 * @since 0.970.0
 */
public class BTupleType extends BType {

    private List<BType> tupleTypes;

    /**
     * Create a {@code BUnionType} which represents the union type.
     *
     * @param typeList of the tuple type
     */
    public BTupleType(List<BType> typeList) {
        super(null, null, BValue.class);
        this.tupleTypes = typeList;
    }

    public List<BType> getTupleTypes() {
        return tupleTypes;
    }


    @Override
    public <V extends BValue> V getZeroValue() {
        return (V) new BValueArray(this);
    }

    @Override
    public <V extends BValue> V getEmptyValue() {
        return getZeroValue();
    }

    @Override
    public int getTag() {
        return TypeTags.TUPLE_TAG;
    }

    @Override
    public String toString() {
        List<String> list = tupleTypes.stream().map(BType::toString).collect(Collectors.toList());
        return "[" + String.join(",", list) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BTupleType)) {
            return false;
        }
        BTupleType that = (BTupleType) o;
        return Objects.equals(tupleTypes, that.tupleTypes);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), tupleTypes);
    }
}
