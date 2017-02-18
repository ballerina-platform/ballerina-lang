/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.model.statements;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BDouble;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BLong;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class VariableDefinitionTest {
    private BLangProgram bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("lang/statements/variable-definition-stmt.bal");
    }

    @Test
    public void testVariableDefaultValue() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "variableDefaultValue");
        Assert.assertEquals(returns.length, 6);

        Assert.assertSame(returns[0].getClass(), BInteger.class);
        int i = ((BInteger) returns[0]).intValue();
        Assert.assertEquals(i, 0);

        Assert.assertSame(returns[1].getClass(), BLong.class);
        long l = ((BLong) returns[1]).longValue();
        Assert.assertEquals(l, 0);

        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        boolean b = ((BBoolean) returns[2]).booleanValue();
        Assert.assertEquals(b, false);

        Assert.assertSame(returns[3].getClass(), BString.class);
        String s = ((BString) returns[3]).stringValue();
        Assert.assertEquals(s, "");

        Assert.assertSame(returns[4].getClass(), BFloat.class);
        float f = ((BFloat) returns[4]).floatValue();
        Assert.assertEquals(f, 0.0f);

        Assert.assertSame(returns[5].getClass(), BDouble.class);
        double d = ((BDouble) returns[5]).doubleValue();
        Assert.assertEquals(d, 0.0);

    }

    @Test
    public void testInlineVarInit() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "inlineVarInit");
        Assert.assertEquals(returns.length, 6);

        Assert.assertSame(returns[0].getClass(), BInteger.class);
        int i = ((BInteger) returns[0]).intValue();
        Assert.assertEquals(i, 10);

        Assert.assertSame(returns[1].getClass(), BLong.class);
        long l = ((BLong) returns[1]).longValue();
        Assert.assertEquals(l, 5);

        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        boolean b = ((BBoolean) returns[2]).booleanValue();
        Assert.assertEquals(b, true);

        Assert.assertSame(returns[3].getClass(), BString.class);
        String s = ((BString) returns[3]).stringValue();
        Assert.assertEquals(s, "hello");

        Assert.assertSame(returns[4].getClass(), BFloat.class);
        float f = ((BFloat) returns[4]).floatValue();
        Assert.assertEquals(f, 2.6f);

        Assert.assertSame(returns[5].getClass(), BDouble.class);
        double d = ((BDouble) returns[5]).doubleValue();
        Assert.assertEquals(d, 3.14159265359);

    }

    @Test
    public void testUpdateDefaultValue() {
        int v1 = 56;
        long v2 = 46;
        boolean v3 = false;
        String v4 = "newstr";
        float v5 = 68.3325f;
        double v6 = 45.32514;

        BValue[] args = {
                new BInteger(v1), new BLong(v2), new BBoolean(v3), new BString(v4), new BFloat(v5), new BDouble(v6)
        };

        BValue[] returns = BLangFunctions.invoke(bLangProgram, "updateVarValue", args);
        Assert.assertEquals(returns.length, 6);

        Assert.assertSame(returns[0].getClass(), BInteger.class);
        int i = ((BInteger) returns[0]).intValue();
        Assert.assertEquals(i, v1);

        Assert.assertSame(returns[1].getClass(), BLong.class);
        long l = ((BLong) returns[1]).longValue();
        Assert.assertEquals(l, v2);

        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        boolean b = ((BBoolean) returns[2]).booleanValue();
        Assert.assertEquals(b, v3);

        Assert.assertSame(returns[3].getClass(), BString.class);
        String s = ((BString) returns[3]).stringValue();
        Assert.assertEquals(s, v4);

        Assert.assertSame(returns[4].getClass(), BFloat.class);
        float f = ((BFloat) returns[4]).floatValue();
        Assert.assertEquals(f, v5);

        Assert.assertSame(returns[5].getClass(), BDouble.class);
        double d = ((BDouble) returns[5]).doubleValue();
        Assert.assertEquals(d, v6);
    }

    @Test
    public void testUpdateVarValue() {
        int v1 = 56;
        long v2 = 46;
        boolean v3 = false;
        String v4 = "newstr";
        float v5 = 68.3325f;
        double v6 = 45.32514;

        BValue[] args = {
                new BInteger(v1), new BLong(v2), new BBoolean(v3), new BString(v4), new BFloat(v5), new BDouble(v6)
        };

        BValue[] returns = BLangFunctions.invoke(bLangProgram, "updateVarValue", args);
        Assert.assertEquals(returns.length, 6);

        Assert.assertSame(returns[0].getClass(), BInteger.class);
        int i = ((BInteger) returns[0]).intValue();
        Assert.assertEquals(i, v1);

        Assert.assertSame(returns[1].getClass(), BLong.class);
        long l = ((BLong) returns[1]).longValue();
        Assert.assertEquals(l, v2);

        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        boolean b = ((BBoolean) returns[2]).booleanValue();
        Assert.assertEquals(b, v3);

        Assert.assertSame(returns[3].getClass(), BString.class);
        String s = ((BString) returns[3]).stringValue();
        Assert.assertEquals(s, v4);

        Assert.assertSame(returns[4].getClass(), BFloat.class);
        float f = ((BFloat) returns[4]).floatValue();
        Assert.assertEquals(f, v5);

        Assert.assertSame(returns[5].getClass(), BDouble.class);
        double d = ((BDouble) returns[5]).doubleValue();
        Assert.assertEquals(d, v6);

    }


}
