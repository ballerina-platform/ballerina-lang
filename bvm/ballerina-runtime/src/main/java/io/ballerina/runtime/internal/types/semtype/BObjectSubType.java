/*
 *  Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.runtime.internal.types.semtype;

import io.ballerina.runtime.api.types.semtype.Bdd;
import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.SubType;

import java.util.Objects;

import static io.ballerina.runtime.api.types.semtype.Bdd.bddEveryPositive;

/**
 * Runtime representation of a subtype of object type.
 *
 * @since 2201.11.0
 */
public final class BObjectSubType extends SubType implements DelegatedSubType {

    public final Bdd inner;

    private BObjectSubType(Bdd inner) {
        super(inner.isAll(), inner.isNothing());
        this.inner = inner;
    }

    @Override
    public SubType union(SubType other) {
        if (!(other instanceof BObjectSubType otherObject)) {
            throw new IllegalArgumentException("union of different subtypes");
        }
        return createDelegate(inner.union(otherObject.inner));
    }

    @Override
    public SubType intersect(SubType other) {
        if (!(other instanceof BObjectSubType otherObject)) {
            throw new IllegalArgumentException("intersect of different subtypes");
        }
        return createDelegate(inner.intersect(otherObject.inner));
    }

    @Override
    public SubType complement() {
        return createDelegate(inner.complement());
    }

    @Override
    public boolean isEmpty(Context cx) {
        return cx.memoSubtypeIsEmpty(cx.mappingMemo,
                (context, bdd) -> bddEveryPositive(context, bdd, null, null, BMappingSubType::mappingFormulaIsEmpty),
                inner);
    }

    @Override
    public SubTypeData data() {
        throw new UnsupportedOperationException("Method not implemented");
    }

    public static BObjectSubType createDelegate(SubType inner) {
        if (inner instanceof Bdd bdd) {
            return new BObjectSubType(bdd);
        } else if (inner.isAll() || inner.isNothing()) {
            throw new IllegalStateException("unimplemented");
        } else if (inner instanceof BObjectSubType bMapping) {
            return new BObjectSubType(bMapping.inner);
        }
        throw new IllegalArgumentException("Unexpected inner type");
    }

    @Override
    public Bdd inner() {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public SubType diff(SubType other) {
        if (!(other instanceof BObjectSubType otherObject)) {
            throw new IllegalArgumentException("diff of different subtypes");
        }
        return createDelegate(inner.diff(otherObject.inner));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BObjectSubType that)) {
            return false;
        }
        return Objects.equals(inner, that.inner);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(inner);
    }

}
