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

/**
 * Abstraction on top of bit set used to represent union of "all" of a given basic type
 *
 * @since 2201.12.0
 */
public sealed class BasicTypeBitSet permits SemType {

    private int all;

    public BasicTypeBitSet(int all) {
        this.all = all;
    }

    protected void setAll(int all) {
        this.all = all;
    }

    public final int all() {
        assert all != -1 : "SemType created by no arg constructor must be initialized with setAll";
        return all;
    }

    public BasicTypeBitSet union(BasicTypeBitSet basicTypeBitSet) {
        return new BasicTypeBitSet(all() | basicTypeBitSet.all());
    }

    public BasicTypeBitSet intersection(BasicTypeBitSet basicTypeBitSet) {
        return new BasicTypeBitSet(all() & basicTypeBitSet.all());
    }
}
