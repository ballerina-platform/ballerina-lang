/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.core.model.types;

import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;

/**
 * {@code BMapType} represents a type of a map in Ballerina.
 * <p>
 * Maps are defined using the map keyword as follows:
 * map mapName
 * <p>
 * All maps are unbounded in length and support key based indexing.
 *
 * @since 0.8.0
 */
public class BMapType extends BType implements BIndexedType {

    private BType constraint;

    /**
     * Create a type from the given name.
     *
     * @param typeName   string name of the type.
     * @param constraint constraint type which particular map is bound to.
     * @param pkgPath    package for the type.
     */
    public BMapType(String typeName, BType constraint, String pkgPath) {
        super(typeName, pkgPath, BMap.class);
        this.constraint = constraint;
    }

    public BMapType(BType constraint) {
        super(TypeConstants.MAP_TNAME, null, BMap.class);
        this.constraint = constraint;
    }

    /**
     * Returns element types which this map is constrained to.
     *
     * @return constraint type.
     */
    public BType getConstrainedType() {
        return constraint;
    }

    /**
     * Returns element type which this map contains.
     *
     * @return element type.
     * @deprecated use {@link #getConstrainedType()} instead.
     */
    @Deprecated
    public BType getElementType() {
        return constraint;
    }

    @Override
    public <V extends BValue> V getZeroValue() {
        return (V) new BMap(this);
    }

    @Override
    public <V extends BValue> V getEmptyValue() {
        return getZeroValue();
    }

    @Override
    public int getTag() {
        return TypeTags.MAP_TAG;
    }

    @Override
    public String toString() {
        if (constraint == BTypes.typeAny) {
            return super.toString();
        } else {
            return "map" + "<" + constraint.getName() + ">";
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj) || !(obj instanceof BMapType)) {
            return false;
        }

        BMapType other = (BMapType) obj;
        if (constraint == other.constraint) {
            return true;
        }

        return constraint.equals(other.constraint);
    }

}
