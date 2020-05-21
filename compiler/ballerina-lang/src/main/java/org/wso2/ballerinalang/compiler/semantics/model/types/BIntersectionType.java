/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerinalang.compiler.semantics.model.types;

import org.ballerinalang.model.types.IntersectionType;
import org.ballerinalang.model.types.Type;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * {@code BIntersectionType} represents an intersection type in Ballerina.
 *
 * @since 2.0.0
 */
public class BIntersectionType extends BType implements IntersectionType {

    public BType immutableType;

    private LinkedHashSet<BType> constituentTypes;
    private boolean nullable = false;
    private boolean isAnyData = true;
    private boolean isPureType = true;

    private BIntersectionType(BTypeSymbol tsymbol, LinkedHashSet<BType> constituentTypes) {
        super(TypeTags.UNION, tsymbol);
        this.constituentTypes = constituentTypes;

        for (BType constituentType : constituentTypes) {
            if (!this.nullable && constituentType.tag == TypeTags.NIL) {
                this.nullable = true;
            }

            if (this.isAnyData && !constituentType.isAnydata()) {
                this.isAnyData = false;
            }

            if (this.isPureType && !constituentType.isPureType()) {
                this.isPureType = false;
            }
        }
    }

    @Override
    public Set<BType> getConstituentTypes() {
        return Collections.unmodifiableSet(this.constituentTypes);
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.INTERSECTION;
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean isNullable() {
        return nullable;
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(getKind().typeName(), "(", ")");

        for (BType constituentType : this.constituentTypes) {
            if (constituentType.tag == TypeTags.NIL) {
                joiner.add("()");
                continue;
            }

            joiner.add(constituentType.toString());
        }

        return joiner.toString();
    }

    /**
     * Creates an intersection type using the types specified in the `types` set. The created intersection will not
     * have intersection types in its member types set.
     *
     * @param tsymbol Type symbol for the intersection.
     * @param types   The types to be used to define the intersection.
     * @return The created intersection type.
     */
    public static BIntersectionType create(BTypeSymbol tsymbol, LinkedHashSet<BType> types) {
        return new BIntersectionType(tsymbol, toFlatTypeSet(types));
    }

    /**
     * Creates an intersection type using the provided types.
     *
     * @param tsymbol Type symbol for the intersection.
     * @param types   The types to be used to define the intersection.
     * @return The created intersection type.
     */
    public static BIntersectionType create(BTypeSymbol tsymbol, BType... types) {
        return create(tsymbol, Arrays.stream(types).collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    @Override
    public boolean isAnydata() {
        return this.isAnyData;
    }

    @Override
    public boolean isPureType() {
        return this.isPureType;
    }

    private static LinkedHashSet<BType> toFlatTypeSet(LinkedHashSet<BType> types) {
        LinkedHashSet<BType> flatSet = new LinkedHashSet<>();

        for (BType type : types) {
            if (type.tag != TypeTags.INTERSECTION) {
                flatSet.add(type);
                continue;
            }

            flatSet.addAll(((BIntersectionType) type).constituentTypes);
        }

        return flatSet;
    }

    @Override
    public Type getImmutableType() {
        return this.immutableType;
    }
}
