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

import io.ballerina.runtime.api.types.semtype.SemType;

import java.util.Arrays;
import java.util.Objects;

public record FixedLengthArray(SemType[] initial, int fixedLength) {

    public FixedLengthArray {
        for (SemType semType : initial) {
            if (semType == null) {
                throw new IllegalArgumentException("initial members can't be null");
            }
        }
    }

    private static final FixedLengthArray EMPTY = new FixedLengthArray(new SemType[0], 0);

    static FixedLengthArray normalized(SemType[] initial, int fixedLength) {
        if (initial.length < 2) {
            return new FixedLengthArray(initial, fixedLength);
        }
        int i = initial.length - 1;
        SemType last = initial[i];
        i -= 1;
        while (i >= 0) {
            if (last != initial[i]) {
                break;
            }
            i -= 1;
        }
        int length = Integer.max(1, i + 2);
        SemType[] buffer = new SemType[length];
        if (length == 1) {
            buffer[0] = last;
        } else {
            System.arraycopy(initial, 0, buffer, 0, length);
        }
        return new FixedLengthArray(buffer, fixedLength);
    }

    public static FixedLengthArray empty() {
        return EMPTY;
    }

    public FixedLengthArray shallowCopy() {
        // TODO: may be create a copy of write array instead
        return new FixedLengthArray(initial.clone(), fixedLength);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FixedLengthArray that)) {
            return false;
        }
        return fixedLength == that.fixedLength && Objects.deepEquals(initial, that.initial);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(initial), fixedLength);
    }
}
