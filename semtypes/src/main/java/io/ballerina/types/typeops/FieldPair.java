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
package io.ballerina.types.typeops;

import io.ballerina.types.CellSemType;

/**
 * Represent the FieldPair record.
 *
 * @since 2201.8.0
 */
public class FieldPair {
    public final String  name;
    public final CellSemType type1;
    public final CellSemType type2;
    Integer index1;
    Integer index2;


    public FieldPair(String name, CellSemType type1, CellSemType type2, Integer index1, Integer index2) {
        this.name = name;
        this.type1 = type1;
        this.type2 = type2;
        this.index1 = index1;
        this.index2 = index2;
    }

    public static FieldPair create(String name, CellSemType type1, CellSemType type2, Integer index1,
                                   Integer index2) {
        return new FieldPair(name, type1, type2, index1, index2);
    }
}
