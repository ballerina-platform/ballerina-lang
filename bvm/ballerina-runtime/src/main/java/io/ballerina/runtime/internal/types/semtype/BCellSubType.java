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
import io.ballerina.runtime.api.types.semtype.SubType;

// TODO: would making this a child class of say BddNode be faster than making this a delegate
//   -- Problem with this is modeling type operations (union, intersect, complement) since parent must return a Cell
//   as well
public class BCellSubType extends SubType {

    private final Bdd inner;

    private BCellSubType(Bdd inner) {
        super(inner.isAll(), inner.isNothing());
        this.inner = inner;
    }

    public static BCellSubType createDelegate(SubType inner) {
        if (inner instanceof Bdd bdd) {
            return new BCellSubType(bdd);
        } else if (inner.isAll() || inner.isNothing()) {
            // FIXME: if all or nothing do the same thing as cellSubtypeDataEnsureProper
            throw new IllegalStateException("unimplemented");
        } else if (inner instanceof BCellSubType bCell) {
            return new BCellSubType(bCell.inner);
        }
        throw new IllegalArgumentException("Unexpected inner type");
    }

    @Override
    public SubType union(SubType other) {
        if (!(other instanceof BCellSubType otherCell)) {
            throw new IllegalArgumentException("union of different subtypes");
        }
        return createDelegate(inner.union(otherCell.inner));
    }

    @Override
    public SubType intersect(SubType other) {
        if (!(other instanceof BCellSubType otherCell)) {
            throw new IllegalArgumentException("intersect of different subtypes");
        }
        return createDelegate(inner.intersect(otherCell.inner));
    }

    @Override
    public SubType complement() {
        return createDelegate(inner.complement());
    }

    @Override
    public boolean isEmpty() {
        throw new IllegalStateException("unimplemented");
    }

    @Override
    public SubTypeData data() {
        throw new IllegalStateException("unimplemented");
    }
}
