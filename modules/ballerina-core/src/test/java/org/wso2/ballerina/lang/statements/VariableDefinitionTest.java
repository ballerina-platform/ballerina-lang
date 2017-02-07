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

package org.wso2.ballerina.lang.statements;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.values.BBoolean;
import org.wso2.ballerina.core.model.values.BDouble;
import org.wso2.ballerina.core.model.values.BFloat;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BLong;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.utils.ParserUtils;
import org.wso2.ballerina.lang.util.Functions;

public class VariableDefinitionTest {
    private BallerinaFile bFile;

    @BeforeClass
    public void setup() {
        bFile = ParserUtils.parseBalFile("lang/statements/variable-definition-stmt.bal");
    }

    @Test
    public void testVariableDefaultValue() {
        BValue[] returns = Functions.invoke(bFile, "variableDefaultValue");
        Assert.assertEquals(returns.length, 6);

        Assert.assertSame(returns[0].getClass(), BInteger.class);
        int i = ((BInteger) returns[0]).intValue();
        Assert.assertEquals(i, 0);

        Assert.assertSame(returns[1].getClass(), BLong.class);
        long l = ((BLong) returns[1]).longValue();
        Assert.assertEquals(i, 0);

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
}
