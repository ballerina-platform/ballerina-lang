/*
 *  Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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
package io.ballerina.types.typeops;

// TODO: introduce some sort of generic here
/**
 * Used to return two values from a method.
 *
 * @since 2201.8.0
 */
public final class TwoTuple<E1, E2> {

    E1 item1;
    E2 item2;

    private TwoTuple(E1 item1, E2 item2) {
        this.item1 = item1;
        this.item2 = item2;
    }

    public static <E1, E2> TwoTuple<E1, E2> from(E1 item1, E2 item2) {
        return new TwoTuple<>(item1, item2);
    }
}
