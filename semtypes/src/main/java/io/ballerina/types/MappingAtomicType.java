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

// TODO: consider switching arrays to lists so if does the element wise comparison correctly, (or override equals)

import java.util.Arrays;

/**
 * MappingAtomicType node. {@code names} and {@code types} fields must be sorted.
 *
 * @param names names of the required members
 * @param types types of the required members
 * @param rest  for a given mapping type this represents the rest type. This is NEVER if the mapping don't have a rest
 *              type
 * @since 2201.8.0
 */
public record MappingAtomicType(String[] names, CellSemType[] types, CellSemType rest) implements AtomicType {

    public MappingAtomicType(String[] names, CellSemType[] types, CellSemType rest) {
        this.names = Arrays.copyOf(names, names.length);
        this.types = Arrays.copyOf(types, names.length);
        this.rest = rest;
    }

    // TODO: we can replace these with unmodifiable lists (which don't create new lists after changing parameters to
    //   lists)
    public String[] names() {
        return Arrays.copyOf(names, names.length);
    }

    public CellSemType[] types() {
        return Arrays.copyOf(types, types.length);
    }

    public static MappingAtomicType from(String[] names, CellSemType[] types, CellSemType rest) {
        return new MappingAtomicType(names, types, rest);
    }
}
