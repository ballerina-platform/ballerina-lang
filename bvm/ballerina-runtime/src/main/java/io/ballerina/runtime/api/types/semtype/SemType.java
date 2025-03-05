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

package io.ballerina.runtime.api.types.semtype;

import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.internal.types.semtype.ImmutableSemType;
import io.ballerina.runtime.internal.types.semtype.MutableSemType;

/**
 * Represent a type in runtime.
 *
 * @since 2201.12.0
 */
public sealed class SemType extends BasicTypeBitSet
        permits io.ballerina.runtime.internal.types.BType, ImmutableSemType {

    private int some;
    private SubType[] subTypeData;
    private static final SemType NEVER = new SemType(0, 0, null);

    protected SemType(int all, int some, SubType[] subTypeData) {
        super(all);
        this.some = some;
        this.subTypeData = subTypeData;
    }

    protected SemType() {
        this(-1, -1, null);
    }

    public static SemType from(int all) {
        if (all == 0) {
            return NEVER;
        }
        return new SemType(all, 0, null);
    }

    public static SemType from(int all, int some, SubType[] subTypes) {
        return new SemType(all, some, subTypes);
    }

    public final int some() {
        return some;
    }

    public final SubType[] subTypeData() {
        return subTypeData;
    }

    public final SubType subTypeByCode(int code) {
        if ((some() & (1 << code)) == 0) {
            return null;
        }
        int someMask = (1 << code) - 1;
        int some = some() & someMask;
        return subTypeData()[Integer.bitCount(some)];
    }

    protected void setSome(int some, SubType[] subTypeData) {
        this.some = some;
        this.subTypeData = subTypeData;
    }

    public static SemType tryInto(Context cx, Type type) {
        if (type instanceof MutableSemType mutableSemType) {
            mutableSemType.updateInnerSemTypeIfNeeded(cx);
        }

        return (SemType) type;
    }
}
