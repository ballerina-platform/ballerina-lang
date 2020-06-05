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

import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class to test xml constraint table type.
 *
 * @since 1.3.0
 */
public class TableWithXMLKeySpecifierTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/table/xml-type-table-key.bal");
    }

    @Test(description = "Test key specifier and key type constraint options")
    public void testKeySpecifierAndTypeConstraintOptions() {
        BRunUtil.invoke(result, "runKeySpecifierTestCases");
    }

    @Test(description = "Test member access")
    public void testMemberAccessExpr() {
        BRunUtil.invoke(result, "runMemberAccessTestCases");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.table\\}KeyNotFound message=cannot " +
                    "find key '<id>245</id>'.*")
    public void testMemberAccessWithInvalidSingleKey() {
        BRunUtil.invoke(result, "testMemberAccessWithInvalidXMLRecordKey");
        Assert.fail();
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.table\\}KeyNotFound message=cannot " +
                    "find key '<id>245</id> fname=Sanjiva lname=Weerawarana'.*")
    public void testMemberAccessWithInvalidMultiKey() {
        BRunUtil.invoke(result, "testMemberAccessWithInvalidXMLMultiKey");
        Assert.fail();
    }

    @Test(description = "Test Table with var type")
    public void testTableWithVarType() {
        BRunUtil.invoke(result, "runTableTestcasesWithVarType");
    }
}

