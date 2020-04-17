/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.test.types.table;

import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class to test table type.
 *
 * @since 1.3.0
 */
public class BTableValueTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/table/table-value.bal");
    }

    @Test(description = "Test global table constructor expr")
    public void testGlobalTableConstructExpr1() {
        BValue[] values = BRunUtil.invoke(result, "testGlobalTableConstructExpr1", new BValue[]{});
        Assert.assertEquals(((BString) values[0]).value(), "name=AAA age=31 name=BBB age=34");
    }

    @Test(description = "Test key specifier and key type constraint options")
    public void testKeySpecifierAndTypeConstraintOptions() {
        BRunUtil.invoke(result, "runKeySpecifierTestcases");
    }

    //TODO:Ideally this should fail at compile time due to duplicate keys, currently it replaces older record
    @Test(description = "Test global table constructor expr with complex keys")
    public void testGlobalTableConstructExpr2() {
        BValue[] values = BRunUtil.invoke(result, "testGlobalTableConstructExpr2", new BValue[]{});
        Assert.assertEquals(((BString) values[0]).value(), "m=AAA=DDDD age=34");
    }
}
