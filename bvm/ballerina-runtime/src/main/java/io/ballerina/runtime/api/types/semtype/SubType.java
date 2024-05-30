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

import io.ballerina.runtime.internal.types.semtype.SubTypeData;

/**
 * Describe set of operation supported by each basic Type.
 *
 * @since 2201.10.0
 */
public abstract class SubType {

    private final boolean all;
    private final boolean nothing;

    protected SubType(boolean all, boolean nothing) {
        this.all = all;
        this.nothing = nothing;
    }

    public abstract SubType union(SubType other);

    public abstract SubType intersect(SubType other);

    public SubType diff(SubType other) {
        return this.intersect(other.complement());
    }

    public abstract SubType complement();

    public abstract boolean isEmpty(Context cx);

    public final boolean isAll() {
        return all;
    }

    public final boolean isNothing() {
        return nothing;
    }

    public abstract SubTypeData data();
}
