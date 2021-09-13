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
package io.ballerina.semtype.typeops;

import io.ballerina.semtype.Common;
import io.ballerina.semtype.MappingAtomicType;
import io.ballerina.semtype.SemType;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

/**
 * Iteration implementation of `MappingPairIterator`.
 *
 * @since 2.0.0
 */
public class MappingPairIterator implements Iterator<FieldPair> {
    private final String[] names1;
    private final String[] names2;
    private final SemType[] types1;
    private final SemType[] types2;
    private final int len1;
    private final int len2;
    private int i1 = 0;
    private int i2 = 0;
    private final SemType rest1;
    private final SemType rest2;

    private boolean doneIteration = false;
    private boolean shouldCalculate = true;
    private FieldPair cache = null;

    public MappingPairIterator(MappingAtomicType m1, MappingAtomicType m2) {
        this.names1 = m1.names;
        this.len1 = this.names1.length;
        this.types1 = m1.types;
        this.rest1 = m1.rest;
        this.names2 = m2.names;
        this.len2 = this.names2.length;
        this.types2 = m2.types;
        this.rest2 = m2.rest;
    }

    @Override
    public boolean hasNext() {
        if (this.doneIteration) {
            return false;
        }
        if (this.shouldCalculate) {
            FieldPair cache = internalNext();
            if (cache == null) {
                this.doneIteration = true;
            }
            this.cache = cache;
            this.shouldCalculate = false;
        }
        return !this.doneIteration;
    }

    @Override
    public FieldPair next() {
        if (this.doneIteration) {
            throw new NoSuchElementException("Exhausted iterator");
        }

        if (this.shouldCalculate) {
            FieldPair cache = internalNext();
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
     * This method corresponds to `next` method of MappingPairing.
     */
    private FieldPair internalNext() {
        FieldPair p;
        if (this.i1 >= this.len1) {
            if (this.i2 >= this.len2) {
                return null;
            }
            p = FieldPair.create(curName2(), this.rest1, curType2());
            this.i2 += 1;
        } else if (this.i2 >= this.len2) {
            p = FieldPair.create(curName1(), curType1(), this.rest2);
            this.i1 += 1;
        } else {
            String name1 = curName1();
            String name2 = curName2();
            if (Common.codePointCompare(name1, name2)) {
                p = FieldPair.create(name1, curType1(), this.rest2);
                this.i1 += 1;
            } else if (Common.codePointCompare(name2, name1)) {
                p = FieldPair.create(name2, this.rest1, curType2());
                this.i2 += 1;
            } else {
                p = FieldPair.create(name1, curType1(), curType2());
                this.i1 += 1;
                this.i2 += 1;
            }
        }
        return p;
    }

    private SemType curType1() {
        return this.types1[this.i1];
    }

    private String curName1() {
        return this.names1[this.i1];
    }

    private SemType curType2() {
        return this.types2[this.i2];
    }

    private String curName2() {
        return this.names2[this.i2];
    }

    public void reset() {
        this.i1 = 0;
        this.i2 = 0;
    }

    public Optional<Integer> index1(String name) {
        int i1Prev = this.i1 - 1;
        return i1Prev >= 0 && Objects.equals(this.names1[i1Prev], name) ? Optional.of(i1Prev) : Optional.empty();
    }
}
