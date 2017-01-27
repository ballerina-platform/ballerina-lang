/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.lang.values;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.values.BArray;
import org.wso2.ballerina.core.model.values.BFloat;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.utils.Functions;
import org.wso2.ballerina.core.utils.ParserUtils;

/**
 *
 */
public class BArrayValueTest {

    private BallerinaFile bFile;

    @BeforeClass
    public void setup() {
        bFile = ParserUtils.parseBalFile("lang/values/array-value.bal");
    }

    @Test(description = "Test lazy array creation", expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "Array index out of range: Index: 0, Size: 0")
    public void testLazyArrayCreation() {
        Functions.invoke(bFile, "lazyInitThrowArrayIndexOutOfBound", new BValue[0]);
    }

    @Test(description = "Test lazy array initializer. Size should be zero")
    public void lazyInitSizeZero() {
        BValue[] args = {};
        BValue[] returns = Functions.invoke(bFile, "lazyInitSizeZero", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BArray.class);

        BArray<BString> arrayValue = (BArray<BString>) returns[0];
        Assert.assertEquals(arrayValue.size(), 0);
    }

    @Test(description = "Test add value operation on int array")
    public void addValueToIntegerArray() {
        BValue[] returns = Functions.invoke(bFile, "addValueToIntArray");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BArray.class);

        BArray<BInteger> arrayValue = (BArray<BInteger>) returns[0];
        Assert.assertEquals(arrayValue.size(), 200, "Invalid array size.");

        Assert.assertSame(arrayValue.get(0).getClass(), BInteger.class, "Invalid class type returned.");
        Assert.assertEquals(arrayValue.get(0).intValue(), (-10), "Invalid value returned.");

        Assert.assertEquals(arrayValue.get(15).getClass(), BInteger.class, "Invalid class type returned.");
        Assert.assertEquals(arrayValue.get(15).intValue(), 20, "Invalid value returned.");

        Assert.assertEquals(arrayValue.get(99).getClass(), BInteger.class, "Invalid class type returned.");
        Assert.assertEquals(arrayValue.get(99).intValue(), 2147483647, "Invalid value returned.");

        Assert.assertEquals(arrayValue.get(100).getClass(), BInteger.class, "Invalid class type returned.");
        Assert.assertEquals(arrayValue.get(100).intValue(), -4, "Invalid value returned.");

        Assert.assertEquals(arrayValue.get(115).getClass(), BInteger.class, "Invalid class type returned.");
        Assert.assertEquals(arrayValue.get(115).intValue(), -2147483647, "Invalid value returned.");

        Assert.assertEquals(arrayValue.get(199).getClass(), BInteger.class, "Invalid class type returned.");
        Assert.assertEquals(arrayValue.get(199).intValue(), 6, "Invalid value returned.");

    }

    @Test(description = "Test add value operation on float array")
    public void addValueToFloatArray() {
        BValue[] returns = Functions.invoke(bFile, "addValueToFloatArray");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BArray.class);

        BArray<BFloat> arrayValue = (BArray<BFloat>) returns[0];
        Assert.assertEquals(arrayValue.size(), 200, "Invalid array size.");

        Assert.assertSame(arrayValue.get(0).getClass(), BFloat.class, "Invalid class type returned.");
        Assert.assertEquals(arrayValue.get(0).floatValue(), new Float(-10.0), "Invalid value returned.");
        Assert.assertEquals(arrayValue.get(15).getClass(), BFloat.class, "Invalid class type returned.");
        Assert.assertEquals(arrayValue.get(15).floatValue(), new Float(2.5), "Invalid value returned.");

        Assert.assertEquals(arrayValue.get(99).getClass(), BFloat.class, "Invalid class type returned.");
        Assert.assertEquals(arrayValue.get(99).floatValue(), new Float(2147483647.1), "Invalid value returned.");

        Assert.assertEquals(arrayValue.get(100).getClass(), BFloat.class, "Invalid class type returned.");
        Assert.assertEquals(arrayValue.get(100).floatValue(), new Float(4.3), "Invalid value returned.");

        Assert.assertEquals(arrayValue.get(115).getClass(), BFloat.class, "Invalid class type returned.");
        Assert.assertEquals(arrayValue.get(115).floatValue(), new Float(-2147483647.7), "Invalid value returned.");

        Assert.assertEquals(arrayValue.get(199).getClass(), BFloat.class, "Invalid class type returned.");
        Assert.assertEquals(arrayValue.get(199).floatValue(), new Float(6.9), "Invalid value returned.");

    }
}
