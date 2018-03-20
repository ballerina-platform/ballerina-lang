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
package org.ballerinalang.test.expressions.fieldaccess;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.testng.annotations.Test;

public class FieldAccessExprTest {

    @Test
    public void testFieldAccess() {
        CompileResult compile = BCompileUtil.compile("test-src/expressions/fieldaccess/field-access.bal");
//        Assert.assertEquals(compile.getErrorCount(), 1);
        for(Diagnostic diag : compile.getDiagnostics()) {
            System.out.println(diag.getPosition() + diag.getMessage());
        }
    }

}
