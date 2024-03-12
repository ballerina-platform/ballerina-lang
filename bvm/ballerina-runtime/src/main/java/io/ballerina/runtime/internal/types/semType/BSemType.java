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

package io.ballerina.runtime.internal.types.semType;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.constants.RuntimeConstants;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.internal.types.BAnyType;
import io.ballerina.runtime.internal.types.BIntersectionType;
import io.ballerina.runtime.internal.types.BJsonType;
import io.ballerina.runtime.internal.types.BType;
import io.ballerina.runtime.internal.types.BUnionType;
import io.ballerina.runtime.internal.values.DecimalValue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;

import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.UniformTypeCodes.N_TYPES;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.UniformTypeCodes.UT_BOOLEAN;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.UniformTypeCodes.UT_BTYPE;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.UniformTypeCodes.UT_DECIMAL;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.UniformTypeCodes.UT_FLOAT;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.UniformTypeCodes.UT_INT;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.UniformTypeCodes.UT_NIL;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.UniformTypeCodes.UT_STRING;

public class BSemType implements Type {

    public final BitSet all;
    public final BitSet some;
    public boolean poisoned = false;
    // TODO: for the time being we are using a sparse array (acutally extra sparse where 0 is alway null), unlike
    // nballerina to make things easier to implement. Consider using a compact array
    public final SubType[] subTypeData;
    private String name;
    private Module module;
    // NOTE: we are keeping these around instead of eagerly building the string representation to reduce the cost of
    // creating type. We need the string representation only to create errors (?). Need to check if this don't create
    // any undesired gc pressure
    private Type[] orderedUnionMembers = null;
    private int tag = -1;

    protected BSemType() {
        this.all = new BitSet(N_TYPES);
        this.some = new BitSet(N_TYPES);
        this.subTypeData = new SubType[N_TYPES];
    }

    protected BSemType(BitSet all, BitSet some, SubType[] subtypeData) {
        this.all = all;
        this.some = some;
        this.subTypeData = subtypeData;
    }

    private boolean onlyBType() {
        return some.cardinality() == 1 && some.get(SemTypeUtils.UniformTypeCodes.UT_BTYPE) && all.cardinality() == 0;
    }

    public void setIdentifiers(String name, Module module) {
        this.name = name;
        this.module = module;
    }

    @Deprecated
    public BType getBType() {
        if (!onlyBType()) {
            throw new IllegalStateException("This semType does not contain only a BType");
        }
        return getBTypePart();
    }

    private BType getBTypePart() {
        BTypeComponent bTypeComponent = (BTypeComponent) subTypeData[SemTypeUtils.UniformTypeCodes.UT_BTYPE];
        if (this.name != null || this.module != null) {
            return bTypeComponent.getBTypeComponent(this.name, this.module);
        }
        return bTypeComponent.getBTypeComponent();
    }

    @Override
    public <V> V getZeroValue() {
        // TODO: for string type if we don't consider subtyping we get and error but if we do consider subtyping for
        //  float we get an error. Need to check if this is the expected behavior
        if (onlyBType()) {
            return getBType().getZeroValue();
        }
        if (isUniformType(UT_BOOLEAN)) {
            return (V) Boolean.valueOf(false);
        } else if (isUniformType(UT_STRING)) {
            if (all.get(UT_STRING)) {
                return (V) RuntimeConstants.STRING_EMPTY_VALUE;
            } else {
                StringSubType stringSubType = (StringSubType) subTypeData[UT_STRING];
                return (V) StringUtils.fromString(stringSubType.defaultValue());
            }
        } else if (isUniformType(UT_DECIMAL)) {
            return (V) new DecimalValue(BigDecimal.ZERO);
        } else if (isUniformType(UT_FLOAT)) {
            return (V) new Double(0);
        } else if (isUniformType(UT_INT)) {
            if (some.get(UT_INT)) {
                IntSubType intSubType = (IntSubType) subTypeData[UT_INT];
                if (intSubType.isByte) {
                    return (V) new Integer(0);
                }
            }
            return (V) new Long(0);
        }
        return null;
    }

    @Override
    public <V> V getEmptyValue() {
        if (onlyBType()) {
            return getBType().getEmptyValue();
        }
        if (isUniformType(UT_BOOLEAN)) {
            return (V) Boolean.valueOf(false);
        } else if (isUniformType(UT_STRING)) {
            if (all.get(UT_STRING)) {
                return (V) RuntimeConstants.STRING_EMPTY_VALUE;
            } else {
                StringSubType stringSubType = (StringSubType) subTypeData[UT_STRING];
                return (V) StringUtils.fromString(stringSubType.defaultValue());
            }
        } else if (isUniformType(UT_DECIMAL)) {
            if (all.get(UT_DECIMAL)) {
                return (V) new DecimalValue(BigDecimal.ZERO);
            } else {
                DecimalSubType decimalSubType = (DecimalSubType) subTypeData[UT_DECIMAL];
                return (V) new DecimalValue(decimalSubType.defaultValue());
            }
        } else if (isUniformType(UT_FLOAT)) {
            if (all.get(UT_FLOAT)) {
                return (V) new Double(0);
            } else {
                FloatSubType floatSubType = (FloatSubType) subTypeData[UT_FLOAT];
                return (V) floatSubType.defaultValue();
            }
        } else if (isUniformType(UT_INT)) {
            if (some.get(UT_INT)) {
                IntSubType intSubType = (IntSubType) subTypeData[UT_INT];
                if (intSubType.isByte) {
                    return (V) new Integer(0);
                }
            }
            return (V) new Long(0);
        }
        return null;
    }

    @Override
    public int getTag() {
        if (tag == -1) {
            tag = calculateTag();
        }
        return tag;
    }

    private boolean isUniformType(int uniformTypeTag) {
        return this.all.cardinality() + this.some.cardinality() == 1 &&
                (this.all.get(uniformTypeTag) || this.some.get(uniformTypeTag));
    }

    private int calculateTag() {
        // If we have BType
        if (some.get(UT_BTYPE)) {
            BTypeComponent bTypeComponent = (BTypeComponent) subTypeData[SemTypeUtils.UniformTypeCodes.UT_BTYPE];
            if (bTypeComponent instanceof BSubType subTypeData) {
                switch (subTypeData.getTypeClass()) {
                    case BAnyData -> {
                        return TypeTags.ANYDATA_TAG;
                    }
                    case BJson -> {
                        return TypeTags.JSON_TAG;
                    }
                }
            } else if (bTypeComponent instanceof BAnyType) {
                return TypeTags.ANY_TAG;
            } else if (bTypeComponent instanceof BIntersectionType) {
                return TypeTags.INTERSECTION_TAG;
            } else if (bTypeComponent instanceof BJsonType) {
                return TypeTags.JSON_TAG;
            }
            if (some.cardinality() + all.cardinality() > 1) {
                return TypeTags.UNION_TAG;
            } else {
                return bTypeComponent.getBTypeComponent().getTag();
            }
        }
        // Pure SemType
        int nBasicTypes = some.cardinality() + all.cardinality();
        if (nBasicTypes == 0) {
            return TypeTags.NEVER_TAG;
        } else if (nBasicTypes > 1) {
            return TypeTags.UNION_TAG;
        }

        // Single type
        if (isUniformType(UT_NIL)) {
            return TypeTags.NULL_TAG;
        } else if (isUniformType(UT_BOOLEAN)) {
            return TypeTags.BOOLEAN_TAG;
        } else if (isUniformType(UT_STRING)) {
            if (some.get(UT_STRING)) {
                StringSubType stringSubType = (StringSubType) subTypeData[UT_STRING];
                if (stringSubType.data instanceof StringSubType.StringSubTypeData stringSubTypeData) {
                    var chars = stringSubTypeData.chars();
                    var nonChars = stringSubTypeData.nonChars();
                    if (!chars.allowed() && chars.values().length == 0 && nonChars.allowed() &&
                            nonChars.values().length == 0) {
                        return TypeTags.CHAR_STRING_TAG;
                    }
                }
            }
            return TypeTags.STRING_TAG;
        } else if (isUniformType(UT_DECIMAL)) {
            return TypeTags.DECIMAL_TAG;
        } else if (isUniformType(UT_FLOAT)) {
            return TypeTags.FLOAT_TAG;
        } else if (isUniformType(UT_INT)) {
            if (some.get(UT_INT)) {
                IntSubType intSubType = (IntSubType) subTypeData[UT_INT];
                Optional<Integer> subtypeTag = intSubType.getTag();
                if (subtypeTag.isPresent()) {
                    return subtypeTag.get();
                }
            }
            return TypeTags.INT_TAG;
        }
        throw new IllegalStateException("Unable to calculate tag for the given SemType: " + this);
    }

    @Override
    public boolean isNilable() {
        // TODO: can this be true?
        if (onlyBType()) {
            return getBType().isNilable();
        }
        return all.get(UT_NIL);
    }

    @Override
    public String getName() {
        if (name == null) {
            if (all.cardinality() + some.cardinality() == 1) {
                if (all.get(UT_BOOLEAN)) {
                    return "boolean";
                }
                if (all.get(UT_STRING)) {
                    return "string";
                }
                if (all.get(UT_DECIMAL)) {
                    return "decimal";
                }
                if (all.get(UT_FLOAT)) {
                    return "float";
                }
                if (all.get(UT_INT)) {
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
        // FIXME:
        if (onlyBType()) {
            return getBType().isPublic();
        }
        return false;
    }

    @Override
    public boolean isNative() {
        if (onlyBType()) {
            return getBType().isNative();
        }
        return false;
    }

    @Override
    public boolean isAnydata() {
        // Error type is always a BType
        if (!some.get(SemTypeUtils.UniformTypeCodes.UT_BTYPE)) {
            return true;
        }
        BTypeComponent bTypeComponent = (BTypeComponent) subTypeData[SemTypeUtils.UniformTypeCodes.UT_BTYPE];
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
        if (onlyBType()) {
            return getBType().isPureType();
        }
        return false;
    }

    @Override
    public boolean isReadOnly() {
        if (onlyBType()) {
            return getBType().isReadOnly();
        }
        // If we have only basic types for the subset we have implmented it is always readonly
        if (some.get(UT_BTYPE)) {
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
        int nBasicTypes = some.cardinality() + all.cardinality();
        if (nBasicTypes == 0) {
            return "never";
        } else if (nBasicTypes == 1) {
            // TODO: once all test error messages has been updated to handle singleton types return the correct string
            if (some.get(UT_BOOLEAN) || all.get(UT_BOOLEAN)) {
                return "boolean";
            }
            if (some.get(UT_STRING) || all.get(UT_STRING)) {
                return "string";
            }
            if (some.get(UT_DECIMAL) || all.get(UT_DECIMAL)) {
                return "decimal";
            }
            if (some.get(UT_FLOAT) || all.get(UT_FLOAT)) {
                return "float";
            }
            if (some.get(UT_INT) || all.get(UT_INT)) {
                if (some.get(UT_INT)) {
                    IntSubType intSubType = (IntSubType) subTypeData[UT_INT];
                    if (intSubType.isByte) {
                        return "byte";
                    }
                }
                return "int";
            }
            if (all.get(UT_NIL)) {
                return "()";
            }
            if (some.get(UT_BTYPE)) {
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
        for (int i = UT_NIL + 1; i < N_TYPES; i++) {
            if (all.get(i) || some.get(i)) {
                if (!sb.isEmpty()) {
                    sb.append("|");
                }
                switch (i) {
                    case UT_BOOLEAN -> sb.append(booleanPartToString());
                    case UT_STRING -> sb.append("\"").append(stringPartToString()).append("\"");
                    case UT_DECIMAL -> sb.append(decimalPartToString());
                    case UT_FLOAT -> sb.append(floatPartToString());
                    case UT_INT -> sb.append(intPartToString());
                    case UT_BTYPE -> {
                        String result = getBTypePart().toString();
                        if (result.contains("readonly")) {
                            return "readonly";
                        }
                        sb.append(result);
                    }
                }
            }
        }
        if (all.get(UT_NIL)) {
            return "(" + sb + ")?";
        }
        return "(" + sb + ")";
    }

    private String intPartToString() {
        if (all.get(UT_INT)) {
            return "int";
        } else {
            return subTypeData[UT_INT].toString();
        }
    }

    private String floatPartToString() {
        if (all.get(UT_FLOAT)) {
            return "float";
        } else {
            return subTypeData[UT_FLOAT].toString();
        }
    }

    private String decimalPartToString() {
        if (all.get(UT_DECIMAL)) {
            return "decimal";
        } else {
            return subTypeData[UT_DECIMAL].toString();
        }
    }

    private String booleanPartToString() {
        if (all.get(UT_BOOLEAN)) {
            return "boolean";
        } else {
            return subTypeData[UT_BOOLEAN].toString();
        }
    }

    private String stringPartToString() {
        if (all.get(UT_STRING)) {
            return "string";
        } else {
            return subTypeData[UT_STRING].toString();
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
            if (memberStr.length() > 2 && memberStr.charAt(0) == '(' &&
                    memberStr.charAt(memberStr.length() - 1) == ')') {
                sb.append(memberStr, 1, memberStr.length() - 1);
            } else {
                sb.append(memberStr);
            }
            addPrefix = true;
        }
        return containsNull ? "(" + sb + ")?" : "(" + sb + ")";
    }

    public void addCyclicMembers(List<Type> members) {
        if (!some.get(UT_BTYPE)) {
            some.set(UT_BTYPE);
            subTypeData[UT_BTYPE] = new BSubType(List.of());
        }
        BTypeComponent bTypeComponent = (BTypeComponent) subTypeData[UT_BTYPE];
        if (!(bTypeComponent instanceof BUnionType) && bTypeComponent instanceof BType singleType) {
            BSubType newBTypeComponent = new BSubType(List.of(singleType));
            subTypeData[UT_BTYPE] = newBTypeComponent;
            bTypeComponent = newBTypeComponent;
        }
        bTypeComponent.addCyclicMembers(members);
    }

    public void setByteClass() {
        if (some.get(UT_INT)) {
            IntSubType intSubType = (IntSubType) subTypeData[UT_INT];
            intSubType.isByte = true;
            return;
        }
        throw new IllegalStateException("Trying to set byte class to a non int type");
    }

    public void setBTypeClass(BSubType.BTypeClass typeClass) {
        if (!some.get(UT_BTYPE)) {
            some.set(UT_BTYPE);
            subTypeData[UT_BTYPE] = new BSubType(List.of());
        } else if (subTypeData[UT_BTYPE] instanceof BType bType) {
            subTypeData[UT_BTYPE] = new BSubType(List.of(bType));
        }
        BSubType bTypeComponent = (BSubType) subTypeData[UT_BTYPE];
        bTypeComponent.setBTypeClass(typeClass);
    }

    public void setReadonly(boolean readonly) {
        SubType subTypeData = this.subTypeData[UT_BTYPE];
        if (subTypeData instanceof BSubType bTypeComponent) {
            bTypeComponent.setReadonly(readonly);
        }
    }

    public void setOrderedUnionMembers(Type[] orderedUnionMembers) {
        this.orderedUnionMembers = orderedUnionMembers;
    }

    @Override
    public int hashCode() {
        return all.hashCode() + 31 * some.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BSemType other) {
            return all.equals(other.all) && some.equals(other.some) && Arrays.equals(subTypeData, other.subTypeData);
        }
        return false;
    }
}
