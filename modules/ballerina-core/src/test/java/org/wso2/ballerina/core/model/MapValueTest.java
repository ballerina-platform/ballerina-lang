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

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.model.values.MapValue;

import java.util.HashMap;
import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;


public class MapValueTest {
    @BeforeTest
    public void setup() {
    }

    @Test
    public void testStandardJavaMap() {
        // Standard Map
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("Chanaka", 1);
        map.put("Udaya", 2);
        map.put("Chanaka", 1);
        assertEquals((int) map.get("Chanaka"), 1);

        for (int i = 0; i < 100; i++) {
            map.put(String.valueOf(i), i);
        }
        assertEquals(map.size(), 102);
        assertEquals((int) map.get("51"), 51);

        map.remove("Chanaka");
        assertEquals(map.size(), 101);

        map.remove("WSO2");
        assertEquals(map.size(), 101);
    }

    @Test
    public void testMapTypeMap() {

        // MapType
        MapValue<String, Integer> map = new MapValue<String, Integer>();
        map.put("Chanaka", 1);
        map.put("Udaya", 2);
        map.put("Chanaka", 1);
        assertEquals((int) map.get("Chanaka"), 1);
        for (int i = 0; i < 100; i++) {
            map.put(String.valueOf(i), i);
        }
        assertEquals(map.size(), 102);
        assertEquals((int) map.get("51"), 51);

        map.remove("Chanaka");
        assertEquals(map.size(), 101);

        map.remove("WSO2");
        assertEquals(map.size(), 101);
    }
}
