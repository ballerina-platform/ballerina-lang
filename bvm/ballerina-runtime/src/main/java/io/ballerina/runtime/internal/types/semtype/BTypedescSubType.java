/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.runtime.internal.types.semtype;

import io.ballerina.runtime.api.types.semtype.Bdd;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.SubType;

import java.util.Objects;

import static io.ballerina.runtime.api.types.semtype.Bdd.bddEveryPositive;

/**
 * Represents the subtype of a typedesc type.
 *
 * @since 2201.12.0
 */
public class BTypedescSubType extends SubType implements DelegatedSubType {

    private final Bdd inner;

    private BTypedescSubType(Bdd inner) {
        super(inner.isAll(), inner.isNothing());
        this.inner = inner;
    }

    public static BTypedescSubType createDelegate(SubType inner) {
        if (inner instanceof Bdd bdd) {
            return new BTypedescSubType(bdd);
        } else if (inner.isAll() || inner.isNothing()) {
            throw new IllegalStateException("unimplemented");
        } else if (inner instanceof BTypedescSubType other) {
            return new BTypedescSubType(other.inner);
        }
        throw new IllegalArgumentException("Unexpected inner type");
    }

    @Override
    public SubType union(SubType other) {
        if (!(other instanceof BTypedescSubType otherTypedesc)) {
            throw new IllegalArgumentException("union of different subtypes");
        }
        return createDelegate(inner.union(otherTypedesc.inner));
    }

    @Override
    public SubType intersect(SubType other) {
        if (!(other instanceof BTypedescSubType otherTypedesc)) {
            throw new IllegalArgumentException("union of different subtypes");
        }
        return createDelegate(inner.intersect(otherTypedesc.inner));
    }

    @Override
    public SubType complement() {
        return createDelegate(Builder.getBddSubtypeRo().diff(inner));
    }

    @Override
    public boolean isEmpty(Context cx) {
        Bdd b = inner;
        // The goal of this is to ensure that mappingFormulaIsEmpty call in errorBddIsEmpty beneath
        // does not get an empty posList, because it will interpret that
        // as `map<any|error>` rather than `readonly & map<readonly>`.
        b = b.posMaybeEmpty() ? (Bdd) b.intersect(Builder.getBddSubtypeRo()) : b;
        return cx.memoSubtypeIsEmpty(cx.mappingMemo, BTypedescSubType::typedescBddIsEmpty, b);
    }

    private static boolean typedescBddIsEmpty(Context cx, Bdd b) {
        return bddEveryPositive(cx, b, null, null, BMappingSubType::mappingFormulaIsEmpty);
    }

    @Override
    public SubTypeData data() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public SubType inner() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BTypedescSubType other)) {
            return false;
        }
        return Objects.equals(inner, other.inner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inner);
    }
}
