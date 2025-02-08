/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime.internal.types;

import io.ballerina.runtime.api.constants.TypeConstants;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.TableType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.TypeTags;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.Core;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.api.types.semtype.ShapeAnalyzer;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.internal.types.semtype.TableUtils;
import io.ballerina.runtime.internal.values.ReadOnlyUtils;
import io.ballerina.runtime.internal.values.TableValue;
import io.ballerina.runtime.internal.values.TableValueImpl;

import java.util.Optional;
import java.util.Set;

/**
 * {@code BTableType} represents tabular data in Ballerina.
 *
 * @since 1.3.0
 */
public class BTableType extends BType implements TableType, TypeWithShape {

    private final Type constraint;
    private Type keyType;
    private String[] fieldNames = new String[]{};

    private final boolean readonly;
    private IntersectionType immutableType;
    private IntersectionType intersectionType = null;

    public BTableType(Type constraint, String[] fieldNames, boolean readonly) {
        this(constraint, readonly);
        this.fieldNames = fieldNames;
    }

    public BTableType(Type constraint, Type keyType, boolean readonly) {
        this(constraint, readonly);
        this.keyType = keyType;
    }

    public BTableType(Type constraint, boolean readonly) {
        super(TypeConstants.TABLE_TNAME, null, TableValue.class);
        this.constraint = readonly ? ReadOnlyUtils.getReadOnlyType(constraint) : constraint;
        this.readonly = readonly;
    }

    @Override
    public Type getConstrainedType() {
        return constraint;
    }

    public Type getKeyType() {
        return keyType;
    }

    @Override
    public String[] getFieldNames() {
        return fieldNames;
    }

    @Override
    public <V> V getZeroValue() {
        return (V) new TableValueImpl<BAnydataType, V>(new BTableType(constraint, readonly));
    }

    @Override
    public <V> V getEmptyValue() {
        return getZeroValue();
    }

    @Override
    public int getTag() {
        return TypeTags.TABLE_TAG;
    }

    @Override
    public String toString() {
        if (constraint == null) {
            return readonly ? super.toString().concat(" & readonly") : super.toString();
        }

        StringBuilder keyStringBuilder = new StringBuilder();
        String stringRep;
        if (fieldNames.length > 0) {
            for (String fieldName : fieldNames) {
                if (!keyStringBuilder.toString().isEmpty()) {
                    keyStringBuilder.append(", ");
                }
                keyStringBuilder.append(fieldName);
            }
            stringRep = super.toString() + "<" + constraint + "> key(" + keyStringBuilder.toString() + ")";
        } else {
            stringRep = super.toString() + "<" + constraint + ">" +
                    ((keyType != null) ? (" key<" + keyType + ">") : "");
        }

        return readonly ? stringRep.concat(" & readonly") : stringRep;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj) || !(obj instanceof BTableType other)) {
            return false;
        }

        if (constraint == other.constraint && keyType == other.keyType) {
            return true;
        }

        if (constraint == null || other.constraint == null) {
            return false;
        }

        if (keyType == null || other.keyType == null) {
            return false;
        }

        return constraint.equals(other.constraint) && keyType.equals(other.keyType);
    }

    @Override
    public boolean isReadOnly() {
        return this.readonly;
    }

    @Override
    public IntersectionType getImmutableType() {
        return this.immutableType;
    }

    @Override
    public void setImmutableType(IntersectionType immutableType) {
        this.immutableType = immutableType;
    }

    @Override
    public Optional<IntersectionType> getIntersectionType() {
        return this.intersectionType ==  null ? Optional.empty() : Optional.of(this.intersectionType);
    }

    @Override
    public void setIntersectionType(IntersectionType intersectionType) {
        this.intersectionType = intersectionType;
    }

    @Override
    public boolean isAnydata() {
        return this.constraint.isAnydata();
    }

    @Override
    public SemType createSemType(Context cx) {
        return createSemTypeWithConstraint(cx, tryInto(cx, constraint));
    }

    private SemType createSemTypeWithConstraint(Context cx, SemType constraintType) {
        SemType semType;
        if (fieldNames.length > 0) {
            semType = TableUtils.tableContainingKeySpecifier(cx, constraintType, fieldNames);
        } else if (keyType != null) {
            semType = TableUtils.tableContainingKeyConstraint(cx, constraintType, tryInto(cx, keyType));
        } else {
            semType = TableUtils.tableContaining(cx.env, constraintType);
        }

        if (isReadOnly()) {
            semType = Core.intersect(semType, Builder.getReadonlyType());
        }
        return semType;
    }

    @Override
    public Optional<SemType> inherentTypeOf(Context cx, ShapeSupplier shapeSupplier, Object object) {
        if (!couldInherentTypeBeDifferent()) {
            return Optional.of(getSemType(cx));
        }
        BTable<?, ?> table = (BTable<?, ?>) object;
        SemType cachedShape = table.shapeOf();
        if (cachedShape != null) {
            return Optional.of(cachedShape);
        }
        SemType semtype = valueShape(cx, shapeSupplier, table);
        return Optional.of(semtype);
    }

    @Override
    public boolean couldInherentTypeBeDifferent() {
        return isReadOnly();
    }

    @Override
    public Optional<SemType> shapeOf(Context cx, ShapeSupplier shapeSupplierFn, Object object) {
        return Optional.of(valueShape(cx, shapeSupplierFn, (BTable<?, ?>) object));
    }

    @Override
    public Optional<SemType> acceptedTypeOf(Context cx) {
        SemType constraintType = ShapeAnalyzer.acceptedTypeOf(cx, this.constraint).orElseThrow();
        SemType semType;
        if (fieldNames.length > 0) {
            semType = TableUtils.acceptedTypeContainingKeySpecifier(cx, constraintType, fieldNames);
        } else if (keyType != null) {
            SemType keyAcceptedType = ShapeAnalyzer.acceptedTypeOf(cx, keyType).orElseThrow();
            semType = TableUtils.acceptedTypeContainingKeyConstraint(cx, constraintType, keyAcceptedType);
        } else {
            semType = TableUtils.acceptedType(cx.env, constraintType);
        }
        return Optional.of(semType);
    }

    private SemType valueShape(Context cx, ShapeSupplier shapeSupplier, BTable<?, ?> table) {
        SemType constraintType = Builder.getNeverType();
        for (var value : table.values()) {
            SemType valueShape = shapeSupplier.get(cx, value).orElse(SemType.tryInto(cx, constraint));
            constraintType = Core.union(constraintType, valueShape);
        }
        return createSemTypeWithConstraint(cx, constraintType);
    }

    @Override
    public boolean shouldCache() {
        // TODO: remove this once we have fixed equals
        return false;
    }

    @Override
    protected boolean isDependentlyTypedInner(Set<MayBeDependentType> visited) {
        return constraint instanceof MayBeDependentType constraintType && constraintType.isDependentlyTyped(visited);
    }
}
