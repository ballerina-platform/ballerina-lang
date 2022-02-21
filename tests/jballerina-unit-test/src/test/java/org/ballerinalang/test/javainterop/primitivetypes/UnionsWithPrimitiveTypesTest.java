/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.javainterop.primitivetypes;

import io.ballerina.runtime.internal.values.HandleValue;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * This class contains test cases to test Java interop functions returning union types with simple types .
 *
 * @since 1.0.0
 */
public class UnionsWithPrimitiveTypesTest {
    private CompileResult result;
    private boolean aBoolean = true;
    private byte aByte = 8;
    private short aShort = 567;
    private char aChar = 'A';
    private int anInt = 345866;
    private long aLong = 42343242;
    private float aFloat = 234242423.0f;
    private double aDouble = 8781233342322.324234;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/javainterop/primitive_types/unions_with_primitive_types.bal");
    }

    @Test(description = "Test a function that reads from a java.io.DataInputStream instance")
    public void testFunctionsReturningUnionsWithSimpleTypes() {
        DataInputStream dataIS;
        try {
            dataIS = getDataInputStream();
        } catch (IOException e) {
            throw new IllegalStateException("Failed create the DataInputStream instance.");
        }

        Object[] args = new Object[1];
        args[0] = new HandleValue(dataIS);

        Object returns = BRunUtil.invoke(result, "readBoolean", args);
        Assert.assertEquals(returns, aBoolean);

        returns = BRunUtil.invoke(result, "readByte", args);
        Assert.assertEquals(((Integer) returns).byteValue(), aByte);

        returns = BRunUtil.invoke(result, "readShort", args);
        Assert.assertEquals(((Long) returns).shortValue(), aShort);

        returns = BRunUtil.invoke(result, "readChar", args);
        Assert.assertEquals((char) ((Long) returns).intValue(), aChar);

        returns = BRunUtil.invoke(result, "readInt", args);
        Assert.assertEquals(((Long) returns).intValue(), anInt);

        returns = BRunUtil.invoke(result, "readLong", args);
        Assert.assertEquals(returns, aLong);

        returns = BRunUtil.invoke(result, "readFloat", args);
        Assert.assertEquals(((Double) returns).floatValue(), aFloat);

        returns = BRunUtil.invoke(result, "readDouble", args);
        Assert.assertEquals(returns, aDouble);
    }

    private DataInputStream getDataInputStream() throws IOException {
        ByteArrayOutputStream byteAOS = new ByteArrayOutputStream(1000);
        DataOutputStream dataOS = new DataOutputStream(byteAOS);
        dataOS.writeBoolean(aBoolean);
        dataOS.writeByte(aByte);
        dataOS.writeShort(aShort);
        dataOS.writeChar(aChar);
        dataOS.writeInt(anInt);
        dataOS.writeLong(aLong);
        dataOS.writeFloat(aFloat);
        dataOS.writeDouble(aDouble);
        ByteArrayInputStream byteAIS = new ByteArrayInputStream(byteAOS.toByteArray());
        return new DataInputStream(byteAIS);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
