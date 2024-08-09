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

import static io.ballerina.runtime.api.types.semtype.Bdd.bddEvery;

public class BStreamSubType extends SubType implements DelegatedSubType {

    public final Bdd inner;

    private BStreamSubType(Bdd inner) {
        super(inner.isAll(), inner.isNothing());
        this.inner = inner;
    }

    public static BStreamSubType createDelegate(SubType inner) {
        if (inner instanceof Bdd bdd) {
            return new BStreamSubType(bdd);
        } else if (inner.isAll() || inner.isNothing()) {
            throw new IllegalStateException("unimplemented");
        } else if (inner instanceof BStreamSubType bStreamSubType) {
            return new BStreamSubType(bStreamSubType.inner);
        }
        throw new IllegalArgumentException("Unexpected inner type");
    }

    @Override
    public SubType union(SubType other) {
        if (!(other instanceof BStreamSubType otherStream)) {
            throw new IllegalArgumentException("union of different subtypes");
        }
        return createDelegate(inner.union(otherStream.inner));
    }

    @Override
    public SubType intersect(SubType other) {
        if (!(other instanceof BStreamSubType otherStream)) {
            throw new IllegalArgumentException("intersect of different subtypes");
        }
        return createDelegate(inner.intersect(otherStream.inner));
    }

    @Override
    public SubType complement() {
        return createDelegate(Builder.listSubtypeTwoElement().diff(inner));
    }

    @Override
    public boolean isEmpty(Context cx) {
        Bdd b = inner;
        // The goal of this is to ensure that listSubtypeIsEmpty call beneath does
        // not get an empty posList, because it will interpret that
        // as `[any|error...]` rather than `[any|error, any|error]`.
        b = b.posMaybeEmpty() ? (Bdd) b.intersect(Builder.listSubtypeTwoElement()) : b;
        return cx.memoSubtypeIsEmpty(cx.listMemo,
                (context, bdd) -> bddEvery(context, bdd, null, null, BListSubType::listFormulaIsEmpty), b);
    }

    @Override
    public SubTypeData data() {
        return inner();
    }

    @Override
    public Bdd inner() {
        return inner;
    }
}
