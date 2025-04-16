/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org)
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
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.query;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

/**
 * This contains methods to test query expressions with different input types.
 *
 * @since 2201.13.0
 */
public class QueryInputTypeTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/query/query_input_types.bal");
    }

    @Test(description = "Test query expression with array input")
    public void testArrayType() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testArrayQuery");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.size(), 3);
        Assert.assertEquals(returnValues.getInt(0), 1L);
        Assert.assertEquals(returnValues.getInt(1), 2L);
        Assert.assertEquals(returnValues.getInt(2), 3L);
    }

    @Test(description = "Test query expression with map input")
    public void testMapType() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testMapQuery");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.size(), 3);

        // Convert BArray to List for easier contains checking
        List<Long> values = Arrays.stream(returnValues.getIntArray())
                .boxed()
                .toList();
        Assert.assertTrue(values.contains(1L));
        Assert.assertTrue(values.contains(2L));
        Assert.assertTrue(values.contains(3L));
    }

    @Test(description = "Test query expression with record input")
    public void testRecordType() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testRecordQuery");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.size(), 2);

        BMap<?, ?> record1 = (BMap<?, ?>) returnValues.get(0);
        Assert.assertEquals(record1.get(StringUtils.fromString("id")).toString(), "1");
        Assert.assertEquals(record1.get(StringUtils.fromString("name")).toString(), "record1");

        BMap<?, ?> record2 = (BMap<?, ?>) returnValues.get(1);
        Assert.assertEquals(record2.get(StringUtils.fromString("id")).toString(), "2");
        Assert.assertEquals(record2.get(StringUtils.fromString("name")).toString(), "record2");
    }

    @Test(description = "Test query expression with string input")
    public void testStringType() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testStringQuery");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.size(), 9);
        Assert.assertEquals((returnValues.get(0).toString()), "B");
        Assert.assertEquals(returnValues.get(1).toString(), "a");
        Assert.assertEquals(returnValues.get(8).toString(), "a");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
