/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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
package io.ballerina.types;

import java.util.List;

/**
 * Represent a fixed length semtype member list similar to a tuple.
 * The length of the list is `fixedLength`, the last member of the `initial` is repeated to achieve this semantic.
 * { initial: [int], fixedLength: 3, } is same as { initial: [int, int, int], fixedLength: 3 }
 * { initial: [string, int], fixedLength: 100 } means `int` is repeated 99 times to get a list of 100 members.
 * `fixedLength` must be `0` when `inital` is empty and the `fixedLength` must be at least `initial.length()`
 */

public class FixedLengthArray {
    public List<SemType> initial;
    public int fixedLength;

    private FixedLengthArray(List<SemType> initial, int fixedLength) {
        this.initial = initial;
        this.fixedLength = fixedLength;
    }

    public static FixedLengthArray from(List<SemType> initial, int fixedLength) {
        return new FixedLengthArray(initial, fixedLength);
    }
}
