/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.jvm.types;

import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.api.BString;

/**
 * {@code BMapType} represents a type of a map in Ballerina.
 * <p>
 * Maps are defined using the map keyword as follows:
 * map mapName
 * <p>
 * All maps are unbounded in length and support key based indexing.
 *
 * @since 0.995.0
 */
@SuppressWarnings("unchecked")
public class BMapType extends BType {

    private BType constraint;
    private final boolean readonly;
    private BIntersectionType immutableType;

    /**
     * Create a type from the given name.
     *
     * @param typeName string name of the type.
     * @param constraint constraint type which particular map is bound to.
     * @param pkg package for the type.
     */
    public BMapType(String typeName, BType constraint, BPackage pkg) {
        super(typeName, pkg, MapValueImpl.class);
        this.constraint = constraint;
        this.readonly = false;
    }

    public BMapType(BType constraint) {
        this(constraint, false);
    }

    public BMapType(BType constraint, boolean readonly) {
        super(TypeConstants.MAP_TNAME, null, MapValueImpl.class);
        this.constraint = constraint;
        this.readonly = readonly;
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
    public <V extends Object> V getZeroValue() {
        return (V) new MapValueImpl<BString, V>(new BMapType(constraint));
    }

    @Override
    public <V extends Object> V getEmptyValue() {
        return getZeroValue();
    }

    @Override
    public int getTag() {
        return TypeTags.MAP_TAG;
    }

    @Override
    public String toString() {
        String stringRep;

        if (constraint == BTypes.typeAny) {
            stringRep = super.toString();
        } else {
            stringRep = "map" + "<" + constraint.toString() + ">";
        }

        return !readonly ? stringRep : stringRep.concat(" & readonly");
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj) || !(obj instanceof BMapType)) {
            return false;
        }

        BMapType other = (BMapType) obj;

        if (this.readonly != other.readonly) {
            return false;
        }

        if (constraint == other.constraint) {
            return true;
        }

        return constraint.equals(other.constraint);
    }

    @Override
    public boolean isAnydata() {
        return this.constraint.isPureType();
    }

    @Override
    public boolean isReadOnly() {
        return this.readonly;
    }

    @Override
    public BType getImmutableType() {
        return this.immutableType;
    }

    @Override
    public void setImmutableType(BIntersectionType immutableType) {
        this.immutableType = immutableType;
    }
}
