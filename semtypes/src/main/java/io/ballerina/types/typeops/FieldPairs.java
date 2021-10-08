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

import io.ballerina.types.MappingAtomicType;

import java.util.Iterator;

/**
 * Ballerina iterator is similar to an iterable in Java.
 * This class implements the iterable for `MappingPairing`
 *
 * @since 3.0.0
 */
public class FieldPairs implements Iterable<FieldPair> {

    MappingAtomicType m1;
    MappingAtomicType m2;
    public MappingPairIterator itr;

    public FieldPairs(MappingAtomicType m1, MappingAtomicType m2) {
        this.m1 = m1;
        this.m2 = m2;
    }

    @Override
    public Iterator<FieldPair> iterator() {
        itr = new MappingPairIterator(m1, m2);
        return itr;
    }
}
