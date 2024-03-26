/*
 *  Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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
 * @param name   name of the field
 * @param type1  type of the field in the first mapping
 * @param type2  type of the field in the second mapping
 * @param index1 index of the field in the first mapping
 * @param index2 index of the field in the second mapping
 * @since 2201.10.0
 */
public record FieldPair(String name, CellSemType type1, CellSemType type2, Integer index1, Integer index2) {

    public static FieldPair create(String name, CellSemType type1, CellSemType type2, Integer index1,
                                   Integer index2) {
        return new FieldPair(name, type1, type2, index1, index2);
    }
}
