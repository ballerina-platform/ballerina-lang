/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.test.cli;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.cli.CliSpec;
import io.ballerina.runtime.internal.cli.Operand;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigDecimal;

/**
 * Tests the options feature in bal cli commands.
 */
public class OperandTest {
    @Test
    public void testString() {
        Operand[] operands = {new Operand(false, "opString", PredefinedTypes.TYPE_STRING)};
        String val = "arg";
        Object[] args = new CliSpec(null, operands, val).getMainArgs();
        Assert.assertTrue(args[1] instanceof BString);
        Assert.assertEquals(((BString) args[1]).getValue(), val);
    }

    @Test
    public void testInt() {
        Operand[] operands = {new Operand(false, "opInt", PredefinedTypes.TYPE_INT)};
        Object[] args = new CliSpec(null, operands, "1").getMainArgs();
        Assert.assertTrue(args[1] instanceof Long);
        Assert.assertEquals(args[1], 1L);
    }

    @Test
    public void testFloat() {
        Operand[] operands = {new Operand(false, "opFloat", PredefinedTypes.TYPE_FLOAT)};
        Object[] args = new CliSpec(null, operands, "1.0").getMainArgs();
        Assert.assertTrue(args[1] instanceof Double);
        Assert.assertEquals(args[1], 1.0);
    }

    @Test
    public void testDecimal() {
        Operand[] operands = {new Operand(false, "opDecimal", PredefinedTypes.TYPE_DECIMAL)};
        String val = "9.999999999999999999999999999999999e6144";
        Object[] args = new CliSpec(null, operands, val).getMainArgs();
        Assert.assertTrue(args[1] instanceof BDecimal);
        Assert.assertEquals(((BDecimal) args[1]).value(), new BigDecimal(val));
    }

    @Test(expectedExceptions = BError.class,
          expectedExceptionsMessageRegExp = "the option 'opBoolean' of type 'boolean' is expected without a value")
    public void testBoolean() {
        Operand[] operands = {new Operand(false, "opBoolean", PredefinedTypes.TYPE_BOOLEAN)};
        String val = "true";
        new CliSpec(null, operands, val).getMainArgs();
    }

    @Test(expectedExceptions = BError.class,
          expectedExceptionsMessageRegExp = "unsupported type expected with main function '\\(string\\|int\\)'")
    public void testUnSupportedType() {
        Operand[] operands = {new Operand(false, "opUnsupported", TypeCreator
                .createUnionType(PredefinedTypes.TYPE_STRING, PredefinedTypes.TYPE_INT))};
        new CliSpec(null, operands, "arg").getMainArgs();
    }

    @Test
    public void testUnion() {
        Operand[] operands = {new Operand(false, "opUnion", TypeCreator
                .createUnionType(PredefinedTypes.TYPE_STRING, PredefinedTypes.TYPE_NULL))};
        String val = "arg";
        Object[] args = new CliSpec(null, operands, val).getMainArgs();
        Assert.assertTrue(args[1] instanceof BString);
        Assert.assertEquals(((BString) args[1]).getValue(), val);
    }

    @Test(expectedExceptions = BError.class,
          expectedExceptionsMessageRegExp = "unsupported type expected with main function '\\(string\\|int\\)\\?'")
    public void testUnSupportedUnion() {
        Operand[] operands = {new Operand(false, "opUnion", TypeCreator
                .createUnionType(PredefinedTypes.TYPE_STRING, PredefinedTypes.TYPE_NULL, PredefinedTypes.TYPE_INT))};
        String val = "arg";
        Object[] args = new CliSpec(null, operands, val).getMainArgs();
        Assert.assertTrue(args[1] instanceof BString);
        Assert.assertEquals(((BString) args[1]).getValue(), val);
    }

    @Test
    public void testIntArraySize0() {
        Operand[] operands = {new Operand(false, "arrInt", TypeCreator.createArrayType(PredefinedTypes.TYPE_INT))};
        Object[] args = new CliSpec(null, operands).getMainArgs();
        Assert.assertTrue(args[1] instanceof BArray);
    }

    @Test
    public void testIntArraySize1() {
        Operand[] operands = {new Operand(false, "arrInt", TypeCreator.createArrayType(PredefinedTypes.TYPE_INT))};
        Object[] args = new CliSpec(null, operands, "1").getMainArgs();
        Assert.assertTrue(args[1] instanceof BArray);
        Assert.assertEquals(((BArray) args[1]).get(0), 1L);
    }

    @Test
    public void testIntArraySize2() {
        Operand[] operands = {new Operand(false, "arrInt", TypeCreator.createArrayType(PredefinedTypes.TYPE_INT))};
        Object[] args = new CliSpec(null, operands, "1", "2").getMainArgs();
        Assert.assertTrue(args[1] instanceof BArray);
        BArray bArray = (BArray) args[1];
        Assert.assertEquals(bArray.get(0), 1L);
        Assert.assertEquals(bArray.get(1), 2L);
    }

    @Test
    public void testIntFollowingArray() {
        Operand[] operands = {new Operand(false, "arrInt", TypeCreator.createArrayType(PredefinedTypes.TYPE_INT)),
                new Operand(false, "opString", PredefinedTypes.TYPE_INT)};
        Object[] args = new CliSpec(null, operands, "1", "2").getMainArgs();
        Assert.assertTrue(args[1] instanceof BArray);
        BArray bArray = (BArray) args[1];
        Assert.assertEquals(bArray.get(0), 1L);
        Assert.assertTrue(args[2] instanceof Long);
        Assert.assertEquals(args[2], 2L);
    }

    @Test
    public void testIntFollowingArrayWith2Args() {
        Operand[] operands = {new Operand(false, "arrInt", TypeCreator.createArrayType(PredefinedTypes.TYPE_INT)),
                new Operand(false, "opString", PredefinedTypes.TYPE_INT)};
        Object[] args = new CliSpec(null, operands, "1", "2", "3").getMainArgs();
        Assert.assertTrue(args[1] instanceof BArray);
        BArray bArray = (BArray) args[1];
        Assert.assertEquals(bArray.get(0), 1L);
        Assert.assertEquals(bArray.get(1), 2L);
        Assert.assertTrue(args[2] instanceof Long);
        Assert.assertEquals(args[2], 3L);
    }

    @Test
    public void testTwoIntsFollowingArray() {
        Operand[] operands = {new Operand(false, "arrInt", TypeCreator.createArrayType(PredefinedTypes.TYPE_INT)),
                new Operand(false, "opInt1", PredefinedTypes.TYPE_INT), new Operand(false, "opInt2",
                                                                                    PredefinedTypes.TYPE_INT)};
        Object[] args = new CliSpec(null, operands, "1", "2", "3").getMainArgs();
        Assert.assertTrue(args[1] instanceof BArray);
        BArray bArray = (BArray) args[1];
        Assert.assertEquals(bArray.get(0), 1L);
        Assert.assertTrue(args[2] instanceof Long);
        Assert.assertEquals(args[2], 2L);
        Assert.assertTrue(args[3] instanceof Long);
        Assert.assertEquals(args[3], 3L);
    }

    @Test
    public void testOperandIntArrayString() {
        Operand[] operands = {new Operand(false, "intArr", TypeCreator.createArrayType(PredefinedTypes.TYPE_INT)),
                new Operand(false, "opString", PredefinedTypes.TYPE_STRING)};
        String val = "arg";
        Object[] args = new CliSpec(null, operands, val).getMainArgs();
        Assert.assertTrue(args[1] instanceof BArray);
        Assert.assertEquals(((BArray) args[1]).size(), 0);
        Assert.assertTrue(args[2] instanceof BString);
        Assert.assertEquals(((BString) args[2]).getValue(), val);
    }

    @Test
    public void testStringFollowingIntsArray() {
        Operand[] operands = {new Operand(false, "intArr", TypeCreator.createArrayType(PredefinedTypes.TYPE_INT)),
                new Operand(false, "opString", PredefinedTypes.TYPE_STRING)};
        Object[] args = new CliSpec(null, operands, "1", "2").getMainArgs();
        Assert.assertTrue(args[1] instanceof BArray);
        BArray arr = ((BArray) args[1]);
        Assert.assertEquals(arr.size(), 1);
        Assert.assertEquals(arr.get(0).toString(), "1");
        Assert.assertTrue(args[2] instanceof BString);
        Assert.assertEquals(((BString) args[2]).getValue(), "2");
    }

    @Test
    public void testStringIntArray() {
        Operand[] operands = {new Operand(false, "opString", PredefinedTypes.TYPE_STRING),
                new Operand(false, "arrInt", TypeCreator.createArrayType(PredefinedTypes.TYPE_INT))};
        String val = "arg";
        Object[] args = new CliSpec(null, operands, val).getMainArgs();
        Assert.assertTrue(args[1] instanceof BString);
        Assert.assertEquals(((BString) args[1]).getValue(), val);
        Assert.assertTrue(args[2] instanceof BArray);
        Assert.assertEquals(((BArray) args[2]).size(), 0);
    }

    @Test
    public void testStringUnionNil() {
        Operand[] operands = {new Operand(false, "opString", PredefinedTypes.TYPE_STRING),
                new Operand(false, "opUnion",
                            TypeCreator.createUnionType(PredefinedTypes.TYPE_STRING, PredefinedTypes.TYPE_NULL))};
        String val = "arg";
        Object[] args = new CliSpec(null, operands, val).getMainArgs();
        Assert.assertTrue(args[1] instanceof BString);
        Assert.assertEquals(((BString) args[1]).getValue(), val);
    }

    @Test(expectedExceptions = BError.class,
          expectedExceptionsMessageRegExp = "missing operand arguments for parameter 'opString' of type 'string'")
    public void testMissingOperand() {
        Operand[] operands = {new Operand(false, "opString", PredefinedTypes.TYPE_STRING)};
         new CliSpec(null, operands).getMainArgs();
    }
}
