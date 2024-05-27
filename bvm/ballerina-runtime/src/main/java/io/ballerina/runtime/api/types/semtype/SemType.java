/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

import io.ballerina.runtime.internal.types.BSemTypeWrapper;
import io.ballerina.runtime.internal.types.semtype.PureSemType;

/**
 * Runtime representation of SemType.
 *
 * @since 2201.10.0
 */
public abstract sealed class SemType implements BasicTypeBitSet permits BSemTypeWrapper, PureSemType {

    final int all;
    final int some;
    private final SubType[] subTypeData;
    private static final SubType[] EMPTY_SUBTYPE_DATA = new SubType[0];

    protected SemType(int all, int some, SubType[] subTypeData) {
        this.all = all;
        this.some = some;
        this.subTypeData = subTypeData;
    }

    protected SemType(int all) {
        this.all = all;
        this.some = 0;
        this.subTypeData = EMPTY_SUBTYPE_DATA;
    }

    protected SemType(SemType semType) {
        this(semType.all(), semType.some(), semType.subTypeData());
    }

    public static SemType from(int all, int some, SubType[] subTypeData) {
        return new PureSemType(all, some, subTypeData);
    }

    public static SemType from(int all) {
        return new PureSemType(all);
    }

    @Override
    public String toString() {
        return SemTypeHelper.stringRepr(this);
    }

    public final int all() {
        return all;
    }

    public final int some() {
        return some;
    }

    public final SubType[] subTypeData() {
        return subTypeData;
    }
}
