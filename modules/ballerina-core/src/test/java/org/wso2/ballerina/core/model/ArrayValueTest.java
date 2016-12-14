/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerina.core.model;

import org.wso2.ballerina.core.model.types.IntType;
import org.wso2.ballerina.core.model.values.ArrayValue;

import static org.testng.AssertJUnit.assertEquals;

public class ArrayValueTest {
//    @BeforeTest
    public void setup() {
    }

//    @Test
    public void testStandardJavaArray() {
        // Standard Array
        IntType[] integers = new IntType[4];
        IntType int0 = new IntType();
        IntType int1 = new IntType();
        IntType int2 = new IntType();
        IntType int3 = new IntType();
        integers[0] = int0;
        integers[1] = int1;
        integers[2] = int2;
        integers[3] = int3;
        assertEquals(integers[2], int2);
    }

//    @Test
    public void testArrayTypeInsertGet() {
        // ArrayType
        ArrayValue<IntType> integers = new ArrayValue<>(4);
        IntType int0 = new IntType();
        IntType int1 = new IntType();
        IntType int2 = new IntType();
        IntType int3 = new IntType();
        integers.insert(0, int0);
        integers.insert(1, int1);
        integers.insert(2, int2);
        integers.insert(3, int3);
        assertEquals(integers.get(2), int2);
    }

//    @Test
    public void testArrayTypeInitializeWithValue() {
        // ArrayType
        IntType int0 = new IntType();
        IntType int1 = new IntType();
        IntType int2 = new IntType();
        IntType int3 = new IntType();
        ArrayValue<IntType> integers = new ArrayValue<>(int0, int1, int2, int3);
        assertEquals(integers.get(2), int2);
    }
}
