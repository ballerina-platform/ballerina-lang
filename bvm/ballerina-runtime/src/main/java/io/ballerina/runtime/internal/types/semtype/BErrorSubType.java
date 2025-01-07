/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
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
 *
 */

package io.ballerina.runtime.internal.types.semtype;

import io.ballerina.runtime.api.types.semtype.Bdd;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.SubType;

import java.util.Objects;

import static io.ballerina.runtime.api.types.semtype.Bdd.bddEveryPositive;

/**
 * Runtime representation of a subtype of error type.
 *
 * @since 2201.11.0
 */
public class BErrorSubType extends SubType implements DelegatedSubType {

    public final Bdd inner;

    private BErrorSubType(Bdd inner) {
        super(inner.isAll(), inner.isNothing());
        this.inner = inner;
    }

    public static BErrorSubType createDelegate(SubType inner) {
        if (inner instanceof Bdd bdd) {
            return new BErrorSubType(bdd);
        } else if (inner.isAll() || inner.isNothing()) {
            throw new IllegalStateException("unimplemented");
        } else if (inner instanceof BErrorSubType bError) {
            return new BErrorSubType(bError.inner);
        }
        throw new IllegalArgumentException("Unexpected inner type");
    }

    @Override
    public SubType union(SubType other) {
        if (!(other instanceof BErrorSubType otherError)) {
            throw new IllegalArgumentException("union of different subtypes");
        }
        return createDelegate(inner.union(otherError.inner));
    }

    @Override
    public SubType intersect(SubType other) {
        if (!(other instanceof BErrorSubType otherError)) {
            throw new IllegalArgumentException("intersect of different subtypes");
        }
        return createDelegate(inner.intersect(otherError.inner));
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
        return cx.memoSubtypeIsEmpty(cx.mappingMemo, BErrorSubType::errorBddIsEmpty, b);
    }

    private static boolean errorBddIsEmpty(Context cx, Bdd b) {
        return bddEveryPositive(cx, b, null, null, BMappingSubType::mappingFormulaIsEmpty);
    }

    @Override
    public SubTypeData data() {
        return inner();
    }

    @Override
    public Bdd inner() {
        return inner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BErrorSubType that)) {
            return false;
        }
        return Objects.equals(inner, that.inner);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(inner);
    }

}
