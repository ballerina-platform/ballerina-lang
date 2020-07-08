/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.query;

import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This contains methods to test order by clause in query expression and query action.
 *
 * @since 2.0.0
 */
public class OrderByClauseTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/query/order-by-clause.bal");
    }

    @Test(description = "Test query expression with order by")
    public void testSimpleQueryExpr() {
        BValue[] returnValues = BRunUtil.invoke(result, "testSimpleQueryExpr");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 11, "Expected events are not received");

        BMap<String, BValue> student1 = (BMap<String, BValue>) returnValues[0];
        BMap<String, BValue> student2 = (BMap<String, BValue>) returnValues[1];
        BMap<String, BValue> student3 = (BMap<String, BValue>) returnValues[2];
        BMap<String, BValue> student4 = (BMap<String, BValue>) returnValues[3];
        BMap<String, BValue> student5 = (BMap<String, BValue>) returnValues[4];
        BMap<String, BValue> student6 = (BMap<String, BValue>) returnValues[5];
        BMap<String, BValue> student7 = (BMap<String, BValue>) returnValues[6];
        BMap<String, BValue> student8 = (BMap<String, BValue>) returnValues[7];
        BMap<String, BValue> student9 = (BMap<String, BValue>) returnValues[8];
        BMap<String, BValue> student10= (BMap<String, BValue>) returnValues[9];
        BMap<String, BValue> student11= (BMap<String, BValue>) returnValues[10];

        Assert.assertEquals(student1.get("firstname").stringValue(), "Ranjan");
        Assert.assertEquals(student2.get("lastname").stringValue(), "{lname:\"Herman\"}");
        Assert.assertEquals(student3.get("addressList").stringValue(), "[{addr:\"C\"}, {addr:\"A\"}]");
        Assert.assertEquals(student4.get("addressList").stringValue(), "[{addr:\"C\"}, {addr:\"B\"}]");
        Assert.assertEquals(student4.get("userTokens").stringValue(), "{\"one\":2.0, \"two\":3.0, \"three\":3.0}");
        Assert.assertEquals(student5.get("addressList").stringValue(), "[{addr:\"C\"}, {addr:\"B\"}]");
        Assert.assertEquals(student5.get("userTokens").stringValue(), "{\"one\":2.0, \"two\":3.0, \"three\":3.0}");
        Assert.assertEquals(student6.get("userTokens").stringValue(), "{\"one\":2.0, \"two\":3.0, \"three\":2.0}");
        Assert.assertEquals(student7.get("userTokens").stringValue(), "{\"one\":2.0, \"two\":2.0, \"three\":5.0}");
        Assert.assertEquals(student8.get("firstname").stringValue(), "Nina");
        Assert.assertEquals(student8.get("isUndergrad").stringValue(), "false");
        Assert.assertEquals(student9.get("firstname").stringValue(), "Nina");
        Assert.assertEquals(student9.get("isUndergrad").stringValue(), "true");
        Assert.assertEquals(student10.get("firstname").stringValue(), "Zander");
        Assert.assertEquals(student11.get("firstname").stringValue(), "Xyla");
    }
}
