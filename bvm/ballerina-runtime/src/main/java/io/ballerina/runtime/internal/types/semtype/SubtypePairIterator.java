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

import io.ballerina.runtime.api.types.SemType.SemType;
import io.ballerina.runtime.api.types.SemType.SubType;

import java.util.Iterator;

public final class SubtypePairIterator implements Iterator<SubtypePair> {

    private int index = 0;
    private final int maxIndex;
    private final int bits;
    private final SemType t1;
    private final SemType t2;

    SubtypePairIterator(SemType t1, SemType t2, int bits) {
        maxIndex = Integer.max(t1.subTypeData().size(), t2.subTypeData().size());
        this.bits = bits;
        this.t1 = t1;
        this.t2 = t2;
        incrementIndex();
    }

    @Override
    public boolean hasNext() {
        return index < maxIndex;
    }

    private void incrementIndex() {
        while (index < maxIndex && (bits & (1 << index)) == 0) {
            index++;
        }
    }

    private SubType subTypeAtIndex(SemType t, int index) {
        if ((t.some() & (1 << index)) != 0) {
            return t.subTypeData().get(index);
        }
        return null;
    }

    @Override
    public SubtypePair next() {
        SubType subType1 = subTypeAtIndex(t1, index);
        SubType subType2 = subTypeAtIndex(t2, index);
        int typeCode = index;
        index++;
        incrementIndex();
        return new SubtypePair(typeCode, subType1, subType2);
    }
}
