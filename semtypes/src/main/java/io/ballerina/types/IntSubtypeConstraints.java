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
package io.ballerina.types;

import io.ballerina.types.subtypedata.AllOrNothingSubtype;
import io.ballerina.types.subtypedata.IntSubtype;

import static io.ballerina.types.Core.intSubtype;
import static io.ballerina.types.typeops.IntOps.intSubtypeMax;
import static io.ballerina.types.typeops.IntOps.intSubtypeMin;

/**
 * Port of:
 * <p>
 * {@code
 * // Constraints on a subtype of `int`.
 * public type IntSubtypeConstraints readonly & record {|
 *     // all values in the subtype are >= min
 *     int min;
 *     // all values in the subtype are <= max
 *     int max;
 *     // does the subtype contain all values between min and max?
 *     boolean all;
 * |};
 * }
 *
 * @since 2201.12.0
 */
public class IntSubtypeConstraints {
    final long min;
    final long max;
    final boolean all;

    private IntSubtypeConstraints(long min, long max, boolean all) {
        this.min = min;
        this.max = max;
        this.all = all;
    }

    // Returns `()` if `t` is not a proper, non-empty subtype of `int`.
    // i.e. returns `()` if `t` contains all or non of `int`.
    public static IntSubtypeConstraints intSubtypeConstraints(SemType t) {
        SubtypeData st = intSubtype(t);
        // JBUG can't flatten inner if-else
        if (st instanceof AllOrNothingSubtype) {
            return null;
        } else {
            IntSubtype ist = (IntSubtype) st;
            int len = ist.ranges.length;
            return new IntSubtypeConstraints(intSubtypeMin(ist), intSubtypeMax(ist), len == 1);
        }
    }
}
