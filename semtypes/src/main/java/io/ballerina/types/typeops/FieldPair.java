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

import io.ballerina.types.SemType;

import java.util.Optional;

/**
 * Represent the FieldPair record.
 *
 * @since 3.0.0
 */
public class FieldPair {
    public final String  name;
    public final SemType type1;
    public final SemType type2;
    Integer index1;
    Integer index2;


    public FieldPair(String name, SemType type1, SemType type2, Integer index1, Integer index2) {
        this.name = name;
        this.type1 = type1;
        this.type2 = type2;
        this.index1 = index1;
        this.index2 = index2;
    }

    public static FieldPair create(String name, SemType type1, SemType type2, Integer index1,
                                   Integer index2) {
        return new FieldPair(name, type1, type2, index1, index2);
    }
}
