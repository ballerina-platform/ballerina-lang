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
package io.ballerina.types;

/**
 * Represent a recursive type atom.
 *
 * @since 2201.8.0
 */
public class RecAtom implements Atom {
    public final int index;
    private static final RecAtom zero = new RecAtom(0);

    private RecAtom(int index) {
        this.index = index;
    }

    public static RecAtom createRecAtom(int index) {
        if (index == 0) {
            return zero;
        }
        return new RecAtom(index);
    }
}