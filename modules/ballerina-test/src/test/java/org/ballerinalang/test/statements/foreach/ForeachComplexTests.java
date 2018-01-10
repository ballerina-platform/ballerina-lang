/*
*   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.ballerinalang.test.statements.foreach;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Complex test cases written for foreach statement.
 *
 * @since 0.96.0
 */
public class ForeachComplexTests {

    private CompileResult program;

    @BeforeClass
    public void setup() {
        program = BCompileUtil.compile("test-src/statements/foreach/foreach-complex.bal");
    }

    @Test
    public void testNestedForeach() {
        String[] days = {"mon", "tue", "wed", "thu", "fri"};
        String[] people = {"tom", "bob", "sam"};
        StringBuilder sb = new StringBuilder();
        int i = -1;
        for (String day : days) {
            sb.append(++i).append(":").append(day).append(" ");
            for (String person : people) {
                sb.append(i).append(":").append(person).append(" ");
            }
            sb.append("\n");
        }
        BValue[] returns = BRunUtil.invoke(program, "testNestedForeach");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), sb.toString());
    }
}
