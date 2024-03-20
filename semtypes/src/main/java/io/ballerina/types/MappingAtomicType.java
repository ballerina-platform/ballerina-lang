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

import java.util.Arrays;

import static io.ballerina.types.PredefinedType.CELL_SEMTYPE_INNER;

/**
 * MappingAtomicType node.
 *
 * @since 2201.8.0
 */
public class MappingAtomicType implements AtomicType {
    // sorted
    public final String[] names;
    public final CellSemType[] types;
    public final CellSemType rest;

    public static final MappingAtomicType MAPPING_ATOMIC_INNER = from(
            new String[]{}, new CellSemType[]{}, CELL_SEMTYPE_INNER
    );

    private MappingAtomicType(String[] names, CellSemType[] types, CellSemType rest) {
        this.names = names;
        this.types = types;
        this.rest = rest;
    }

    public static MappingAtomicType from(String[] names, CellSemType[] types, CellSemType rest) {
        return new MappingAtomicType(names, types, rest);
    }

    @Override
    public String toString() {
        return "MappingAtomicType{names=" + Arrays.toString(names) + ", types=" + Arrays.toString(types) + ", rest=" +
                rest + '}';
    }
}
