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


package org.ballerinalang.var;

import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Var type assignment statement test.
 *
 * @since 0.88
 */
public class VarTypeAssignmentStmtTest {

    private ProgramFile bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.getProgramFile("lang/var/var-type-assign-stmt.bal");
    }

    @Test
    public void testIncompatibleJsonToStructWithErrors() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testIncompatibleJsonToStructWithErrors",
                new BValue[]{});
        Assert.assertNull(returns[0]);
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        String errorMsg = error.getStringField(0);
        Assert.assertEquals(errorMsg, "cannot convert 'json' to type 'Person': error while mapping" +
                " 'parent': incompatible types: expected 'json-object', found 'string'");
    }


    @Test
    public void testJsonToStructWithErrors() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testJsonToStructWithErrors",
                new BValue[]{});
        Assert.assertNull(returns[0]);
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        String errorMsg = error.getStringField(0);
        Assert.assertEquals(errorMsg, "cannot convert 'json' to type 'PersonA': " +
                "error while mapping 'age': no such field found");
    }

}
