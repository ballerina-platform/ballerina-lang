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
package org.wso2.ballerina.lang.values;

import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.exception.SemanticException;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BMap;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.utils.ParserUtils;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * Test class for ballerina exception initialization..
 */
public class BExceptionValueTest {

    private BallerinaFile bFile;

    @Test
    public void testExceptionValueInit() {
        bFile = ParserUtils.parseBalFile("lang/values/exception-value.bal");
    }

    @Test(expectedExceptions = {ParseCancellationException.class})
    public void testInvalidExceptionValueInit() {
        ParserUtils.parseBalFile("lang/values/invalid-exception-init.bal");
    }

    @Test(expectedExceptions = {ParseCancellationException.class})
    public void testInvalidExceptionValueInitTwo() {
        ParserUtils.parseBalFile("lang/values/invalid-exception-init2.bal");
    }

    @Test(expectedExceptions = SemanticException.class,
            expectedExceptionsMessageRegExp = ".*'exception' cannot be cast to 'string'.*")
    public void testInvalidExceptionCast() {
        ParserUtils.parseBalFile("lang/values/invalid-exception-cast.bal");
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
    public void testBMap() {

        BMap<BString, BInteger> map = new BMap<>();
        map.put(new BString("Chanaka"), new BInteger(1));
        map.put(new BString("Udaya"), new BInteger(2));
        map.put(new BString("Chanaka"), new BInteger(1));
        assertEquals(map.get(new BString("Chanaka")), new BInteger(1));
        for (int i = 0; i < 100; i++) {
            map.put(new BString(String.valueOf(i)), new BInteger(i));
        }
        assertEquals(map.size(), 102);
        assertEquals(map.get(new BString("51")), new BInteger(51));

        map.remove(new BString("Chanaka"));
        assertEquals(map.size(), 101);

        map.remove(new BString("Chanaka"));
        assertEquals(map.size(), 101);
    }
}
