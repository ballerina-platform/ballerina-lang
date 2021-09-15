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
package io.ballerina.types.typeops;

import io.ballerina.types.ComplexSemType;
import io.ballerina.types.SemType;
import io.ballerina.types.SubtypeData;
import io.ballerina.types.UniformSubtype;
import io.ballerina.types.UniformTypeBitSet;
import io.ballerina.types.UniformTypeCode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Iteration implementation of `SubtypePairIterator`.
 *
 * @since 2.0.0
 */
public class SubtypePairIterator implements Iterator<SubtypePair> {
    private int i1;
    private int i2;
    private final List<UniformSubtype> t1;
    private final List<UniformSubtype> t2;
    private final UniformTypeBitSet bits;

    private boolean doneIteration = false;
    private boolean shouldCalculate = true;
    private SubtypePair cache = null;

    public SubtypePairIterator(SemType t1, SemType t2, UniformTypeBitSet bits) {
        this.i1 = 0;
        this.i2 = 0;
        this.t1 = unpackToUniformSubtypes(t1);
        this.t2 = unpackToUniformSubtypes(t2);
        this.bits = bits;
    }

    private List<UniformSubtype> unpackToUniformSubtypes(SemType type) {
        if (type instanceof UniformTypeBitSet) {
            return new ArrayList<>();
        }
        return UnpackComplexSemType.unpack((ComplexSemType) type);
    }

    private boolean include(UniformTypeCode code) {
        return (this.bits.bitset & (1 << code.code)) != 0;
    }

    private UniformSubtype get1() {
        return this.t1.get(this.i1);
    }

    private UniformSubtype get2() {
        return this.t2.get(this.i2);
    }

    @Override
    public boolean hasNext() {
        if (this.doneIteration) {
            return false;
        }
        if (this.shouldCalculate) {
            SubtypePair cache = internalNext();
            if (cache == null) {
                this.doneIteration = true;
            }
            this.cache = cache;
            this.shouldCalculate = false;
        }
        return !this.doneIteration;
    }

    @Override
    public SubtypePair next() {
        if (this.doneIteration) {
            throw new NoSuchElementException("Exhausted iterator");
        }

        if (this.shouldCalculate) {
            SubtypePair cache = internalNext();
            if (cache == null) {
                // this.doneIteration = true;
                throw new IllegalStateException();
            }
            this.cache = cache;
        }
        this.shouldCalculate = true;
        return this.cache;
    }

    /*
     * This method corresponds to `next` method of SubtypePairIteratorImpl.
     */
    private SubtypePair internalNext() {
        while (true) {
            if (this.i1 >= this.t1.size()) {
                if (this.i2 >= this.t2.size()) {
                    break;
                }
                UniformSubtype t = get2();
                UniformTypeCode code = t.uniformTypeCode;
                SubtypeData data2 = t.subtypeData;
                this.i2 += 1;
                if (include(code)) {
                    return SubtypePair.create(code, null, data2);
                }
            } else if (this.i2 >= this.t2.size()) {
                UniformSubtype t = this.get1();
                this.i1 += 1;
                UniformTypeCode code = t.uniformTypeCode;
                SubtypeData data1 = t.subtypeData;
                if (include(code)) {
                    return SubtypePair.create(code, data1, null);
                }
            } else {
                UniformSubtype t1 = get1();
                UniformTypeCode code1 = t1.uniformTypeCode;
                SubtypeData data1 = t1.subtypeData;

                UniformSubtype t2 = get2();
                UniformTypeCode code2 = t2.uniformTypeCode;
                SubtypeData data2 = t2.subtypeData;

                if (code1.code == code2.code) {
                    this.i1 += 1;
                    this.i2 += 1;
                    if (include(code1)) {
                        return SubtypePair.create(code1, data1, data2);
                    }
                } else if (code1.code < code2.code) {
                    this.i1 += 1;
                    if (include(code1)) {
                        return SubtypePair.create(code1, data1, null);
                    }
                } else {
                    this.i2 += 1;
                    if (include(code2)) {
                        return SubtypePair.create(code2, null, data2);
                    }
                }
            }
        }
        return null;
    }
}
