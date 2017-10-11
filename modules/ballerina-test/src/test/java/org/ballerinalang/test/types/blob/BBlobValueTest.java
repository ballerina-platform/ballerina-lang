/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.test.types.blob;

import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This test class will test the blob values.
 */
public class BBlobValueTest {

    private CompileResult result;
    private final String funcName = "testIfStmt";

    @BeforeClass
    public void setup() {
        result = BTestUtils.compile("test-src/types/blob/blob-value.bal");
    }

    @Test(description = "Test blob value assignment")
    public void testBlobParameter() {
        byte[] bytes = "string".getBytes();
        BValue[] args = {new BBlob(bytes)};
        BValue[] returns = BTestUtils.invoke(result, "testBlobParameter", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBlob.class);
        BBlob blob = (BBlob) returns[0];
        Assert.assertEquals(blob.blobValue(), bytes, "Invalid byte value returned.");
    }

    @Test(description = "Test blob array")
    public void testBlobArray() {
        byte[] bytes1 = "string1".getBytes();
        byte[] bytes2 = "string2".getBytes();
        BValue[] args = {new BBlob(bytes1), new BBlob(bytes2)};
        BValue[] returns = BTestUtils.invoke(result, "testBlobParameterArray", args);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBlob.class);
        Assert.assertSame(returns[1].getClass(), BBlob.class);
        BBlob blob1 = (BBlob) returns[0];
        BBlob blob2 = (BBlob) returns[1];
        Assert.assertEquals(blob1.blobValue(), bytes1, "Invalid byte value returned.");
        Assert.assertEquals(blob2.blobValue(), bytes2, "Invalid byte value returned.");
    }

    @Test(description = "Test blob global variable")
    public void testBlobGlobalVariable() {
        byte[] bytes = "string".getBytes();
        BValue[] args = {new BBlob(bytes)};
        BValue[] returns = BTestUtils.invoke(result, "testGlobalVariable", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBlob.class);
        BBlob blob = (BBlob) returns[0];
        Assert.assertEquals(blob.blobValue(), bytes, "Invalid byte value returned.");
    }

    @Test(description = "Test blob global variable")
    public void testBlobField() {
        byte[] bytes = "string".getBytes();
        BValue[] args = {new BBlob(bytes)};
        BValue[] returns = BTestUtils.invoke(result, "testBlobField", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBlob.class);
        BBlob blob = (BBlob) returns[0];
        Assert.assertEquals(blob.blobValue(), bytes, "Invalid byte value returned.");
    }
}
