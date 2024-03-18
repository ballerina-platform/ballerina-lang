/*
 *   Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
 *
 *   WSO2 LLC. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

package io.ballerina.runtime.internal.types.semtype;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.internal.types.BType;
import io.ballerina.runtime.internal.types.BUnionType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static io.ballerina.runtime.internal.types.semtype.Core.belongToBasicType;
import static io.ballerina.runtime.internal.types.semtype.Core.containsSimple;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.BasicTypeCodes.BT_BOOLEAN;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.BasicTypeCodes.BT_BTYPE;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.BasicTypeCodes.BT_DECIMAL;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.BasicTypeCodes.BT_FLOAT;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.BasicTypeCodes.BT_INT;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.BasicTypeCodes.BT_NIL;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.BasicTypeCodes.BT_STRING;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.BasicTypeCodes.N_TYPES;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.calculateDefaultValue;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.calculateTag;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.cardinality;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.isSet;

/**
 * Runtime representation to a type.
 *
 * @since 2201.10.0
 */
public class BSemType implements Type {

    // TODO: make these final (currently we need to mutate this to support various hacks to make BTypes work)
    public int all;
    public int some;
    // TODO: this is a workaround to make intersection work when the effective type is filled in later
    public boolean poisoned = false;
    // TODO: for the time being we are using a sparse array (acutally extra sparse where 0 is alway null), unlike
    //  nballerina to make things easier to implement. Consider using a compact array
    public final SubType[] subTypeData;
    private String name;
    private Module module;
    // NOTE: we are keeping these around instead of eagerly building the string representation to reduce the cost of
    // creating type. We need the string representation only to create errors (?). Need to check if this don't create
    // any undesired gc pressure
    private Type[] orderedUnionMembers = null;
    private int tag = -1;

    protected BSemType(int all, int some, SubType[] subtypeData) {
        this.all = all;
        this.some = some;
        this.subTypeData = subtypeData;
    }

    public void setIdentifiers(String name, Module module) {
        this.name = name;
        this.module = module;
    }

    @Deprecated
    public BType getBType() {
        if (!belongToBasicType(this, BT_BTYPE)) {
            throw new IllegalStateException("This semType does not contain only a BType");
        }
        return getBTypePart();
    }

    private BType getBTypePart() {
        BTypeComponent bTypeComponent = (BTypeComponent) subTypeData[SemTypeUtils.BasicTypeCodes.BT_BTYPE];
        if (this.name != null || this.module != null) {
            return bTypeComponent.getBTypeComponent(this.name, this.module);
        }
        return bTypeComponent.getBTypeComponent();
    }

    @Override
    public <V> V getZeroValue() {
        // TODO: need to think about a way to cache this result (one problem is some of these values seems to be
        //  mutable)
        return calculateDefaultValue(this);
    }

    @Override
    public <V> V getEmptyValue() {
        return calculateDefaultValue(this);
    }

    // TODO: eventually we need to get rid of dependency on tags since they overlap (ex:int subtypes and int singleton)
    @Override
    public int getTag() {
        if (tag == -1) {
            tag = calculateTag(this);
        }
        return tag;
    }

    @Override
    public boolean isNilable() {
        // TODO: can this be true?
        if (belongToBasicType(this, BT_BTYPE)) {
            return getBType().isNilable();
        }
        return containsSimple(this, BT_NIL);
    }

    @Override
    public String getName() {
        if (name == null) {
            if (cardinality(all) + cardinality(some) == 1) {
                if (isSet(all, BT_BOOLEAN)) {
                    return "boolean";
                }
                if (isSet(all, BT_STRING)) {
                    return "string";
                }
                if (isSet(all, BT_DECIMAL)) {
                    return "decimal";
                }
                if (isSet(all, BT_FLOAT)) {
                    return "float";
                }
                if (isSet(all, BT_INT)) {
                    return "int";
                }
            }
            return "";
        }
        return name;
    }

    @Override
    public String getQualifiedName() {
        if (name == null) {
            return "";
        }
        return module == null ? name : module.toString() + ":" + name;
    }

    @Override
    public Module getPackage() {
        return module;
    }

    @Override
    public boolean isPublic() {
        if (belongToBasicType(this, BT_BTYPE)) {
            return getBType().isPublic();
        }
        return false;
    }

    @Override
    public boolean isNative() {
        if (belongToBasicType(this, BT_BTYPE)) {
            return getBType().isNative();
        }
        return false;
    }

    @Override
    public boolean isAnydata() {
        // TODO: revisit this when we have error type as a semtype (Currently it is always a BType)
        if (!isSet(some, BT_BTYPE)) {
            return true;
        }
        BTypeComponent bTypeComponent = (BTypeComponent) subTypeData[SemTypeUtils.BasicTypeCodes.BT_BTYPE];
        if (bTypeComponent instanceof BSubType subTypeData) {
            // NOTE: this is because for cyclic types trying to get the BType going to put us to infinite loop
            // TODO: fix this with something similar to what we have in BUnionType
            if (subTypeData.isCyclic) {
                BSubType.BTypeClass typeClass = subTypeData.getTypeClass();
                return typeClass == BSubType.BTypeClass.BAnyData ||
                        typeClass == BSubType.BTypeClass.BJson;
            }
        }
        return bTypeComponent.getBTypeComponent().isAnydata();
    }

    @Override
    public boolean isPureType() {
        if (belongToBasicType(this, BT_BTYPE)) {
            return getBType().isPureType();
        }
        return false;
    }

    // TODO: revisit this when we have proper semtypes (ideally we should simply check isEmpty(T & readonly))
    @Override
    public boolean isReadOnly() {
        // If we have only basic types for the subset we have implmented it is always readonly
        if (isSet(some, BT_BTYPE)) {
            return getBTypePart().isReadOnly();
        }
        return true;
    }

    @Override
    public long getFlags() {
        return getBType().getFlags();
    }

    @Override
    public Type getImmutableType() {
        return getBType().getImmutableType();
    }

    @Override
    public void setImmutableType(IntersectionType immutableType) {
        getBType().setImmutableType(immutableType);
    }

    @Override
    public Module getPkg() {
        return getBType().getPkg();
    }

    @Override
    public String toString() {
        if (name != null) {
            return (module == null || module.getName() == null || module.getName().equals(".")) ? name :
                    module.toString() + ":" + name;
        }
        if (orderedUnionMembers != null) {
            return orderedMembersToString();
        }
        int nBasicTypes = cardinality(some) + cardinality(all);
        if (nBasicTypes == 0) {
            return "never";
        } else if (nBasicTypes == 1) {
            // TODO: once all test error messages has been updated to handle singleton types return the correct string
            if (containsSimple(this, BT_BOOLEAN)) {
                return "boolean";
            }
            if (containsSimple(this, BT_STRING)) {
                return "string";
            }
            if (containsSimple(this, BT_DECIMAL)) {
                return "decimal";
            }
            if (containsSimple(this, BT_FLOAT)) {
                return "float";
            }
            if (containsSimple(this, BT_INT)) {
                if (isSet(some, BT_INT)) {
                    IntSubType intSubType = (IntSubType) subTypeData[BT_INT];
                    if (intSubType.isByte) {
                        return "byte";
                    }
                }
                return "int";
            }
            if (containsSimple(this, BT_NIL)) {
                return "()";
            }
            if (containsSimple(this, BT_BTYPE)) {
                return getBTypePart().toString();
            }
            throw new IllegalStateException("Unexpected single type");
        }
        // TODO: try to avoid using the tag
        int tag = getTag();
        switch (tag) {
            case TypeTags.JSON_TAG:
                return "json";
            case TypeTags.READONLY_TAG:
                return "readonly";
            case TypeTags.ANY_TAG:
                return "any";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = BT_NIL + 1; i < N_TYPES; i++) {
            if (containsSimple(this, i)) {
                if (!sb.isEmpty()) {
                    sb.append("|");
                }
                switch (i) {
                    case BT_BOOLEAN -> sb.append(booleanPartToString());
                    case BT_STRING -> sb.append("\"").append(stringPartToString()).append("\"");
                    case BT_DECIMAL -> sb.append(decimalPartToString());
                    case BT_FLOAT -> sb.append(floatPartToString());
                    case BT_INT -> sb.append(intPartToString());
                    case BT_BTYPE -> {
                        String result = getBTypePart().toString();
                        if (result.contains("readonly")) {
                            return "readonly";
                        }
                        sb.append(result);
                    }
                }
            }
        }
        if (containsSimple(this, BT_NIL)) {
            return "(" + sb + ")?";
        }
        return "(" + sb + ")";
    }

    private String intPartToString() {
        if (isSet(all, BT_INT)) {
            return "int";
        } else {
            return subTypeData[BT_INT].toString();
        }
    }

    private String floatPartToString() {
        if (isSet(all, BT_FLOAT)) {
            return "float";
        } else {
            return subTypeData[BT_FLOAT].toString();
        }
    }

    private String decimalPartToString() {
        if (isSet(all, BT_DECIMAL)) {
            return "decimal";
        } else {
            return subTypeData[BT_DECIMAL].toString();
        }
    }

    private String booleanPartToString() {
        if (isSet(all, BT_BOOLEAN)) {
            return "boolean";
        } else {
            return subTypeData[BT_BOOLEAN].toString();
        }
    }

    private String stringPartToString() {
        if (isSet(all, BT_STRING)) {
            return "string";
        } else {
            return subTypeData[BT_STRING].toString();
        }
    }

    private String orderedMembersToString() {
        boolean containsNull = false;
        boolean addPrefix = false;
        StringBuilder sb = new StringBuilder();
        for (Type member : orderedUnionMembers) {
            // TODO: avoid using the tag
            if (member.getTag() == TypeTags.NULL_TAG) {
                containsNull = true;
                continue;
            }
            String memberStr = member.toString();
            if (addPrefix) {
                sb.append("|");
            }
            if (memberStr.length() > 2 && memberStr.charAt(0) == '(') {
                if (memberStr.charAt((memberStr.length() - 1)) == ')') {
                    sb.append(memberStr, 1, memberStr.length() - 1);
                } else {
                    sb.append(memberStr, 1, memberStr.length() - 2);
                    containsNull = true;
                }
            } else {
                sb.append(memberStr);
            }
            addPrefix = true;
        }
        return containsNull ? "(" + sb + ")?" : "(" + sb + ")";
    }

    // Hacks to make BType work
    @Deprecated
    public void addCyclicMembers(List<Type> members) {
        if (!isSet(some, BT_BTYPE)) {
            some |= 1 << BT_BTYPE;
            subTypeData[BT_BTYPE] = new BSubType(List.of());
        }
        BTypeComponent bTypeComponent = (BTypeComponent) subTypeData[BT_BTYPE];
        if (!(bTypeComponent instanceof BUnionType) && bTypeComponent instanceof BType singleType) {
            BSubType newBTypeComponent = new BSubType(List.of(singleType));
            subTypeData[BT_BTYPE] = newBTypeComponent;
            bTypeComponent = newBTypeComponent;
        }
        bTypeComponent.addCyclicMembers(members);
    }

    // TODO: This is to ensure we give the correct type tag as byte, which is important because at runtime we
    //  represent byte as Integer while integers (include those in the same range) as Long. Need think about a way to
    //  avoid needing set explicitly set this tag
    @Deprecated
    public void setByteClass() {
        if (isSet(some, BT_INT)) {
            IntSubType intSubType = (IntSubType) subTypeData[BT_INT];
            intSubType.isByte = true;
            return;
        }
        throw new IllegalStateException("Trying to set byte class to a non int type");
    }

    @Deprecated
    public void setBTypeClass(BSubType.BTypeClass typeClass) {
        if (!isSet(some, BT_BTYPE)) {
            some |= 1 << BT_BTYPE;
            subTypeData[BT_BTYPE] = new BSubType(List.of());
        } else if (subTypeData[BT_BTYPE] instanceof BType bType) {
            subTypeData[BT_BTYPE] = new BSubType(List.of(bType));
        }
        BSubType bTypeComponent = (BSubType) subTypeData[BT_BTYPE];
        bTypeComponent.setBTypeClass(typeClass);
    }

    @Deprecated
    public void setReadonly(boolean readonly) {
        SubType subTypeData = this.subTypeData[BT_BTYPE];
        if (subTypeData instanceof BSubType bTypeComponent) {
            bTypeComponent.setReadonly(readonly);
        }
    }

    public void setOrderedUnionMembers(Type[] orderedUnionMembers) {
        this.orderedUnionMembers = orderedUnionMembers;
    }

    @Override
    public int hashCode() {
        return all + 31 * some;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BSemType other) {
            return all == other.all && some == other.some && Arrays.equals(subTypeData, other.subTypeData);
        }
        return false;
    }

    public Optional<List<Type>> getOrderedUnionMembers() {
        if (orderedUnionMembers == null) {
            return Optional.empty();
        }
        return Optional.of(List.of(orderedUnionMembers));
    }
}
