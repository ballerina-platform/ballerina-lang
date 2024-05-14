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

package io.ballerina.runtime.internal.types.semtype;

import io.ballerina.runtime.api.types.semtype.SubType;
import io.ballerina.runtime.internal.types.BType;

public class BSubType extends SubType {

    private final BType data;

    private BSubType(BType innerType) {
        super(false, false);
        data = innerType;
    }

    public static BSubType wrap(BType innerType) {
        return new BSubType(innerType);
    }

    // NOTE: we are allowing isAll() and isNothing() (from the parent) so we can get the union of PureSemTypes and
    // PureBTypes. All other operations are unsupported for BSubType
    @Override
    public SubType union(SubType other) {
        throw new IllegalArgumentException("BSubType don't support semType operations");
    }

    @Override
    public SubType intersect(SubType other) {
        throw new IllegalArgumentException("BSubType don't support semType operations");
    }

    @Override
    public SubType diff(SubType other) {
        throw new IllegalArgumentException("BSubType don't support semType operations");
    }

    @Override
    public SubType complement() {
        throw new IllegalArgumentException("BSubType don't support semType operations");
    }

    @Override
    public boolean isEmpty() {
        throw new IllegalArgumentException("BSubType don't support semType operations");
    }

    @Override
    public SubTypeData data() {
        return data;
    }
}
