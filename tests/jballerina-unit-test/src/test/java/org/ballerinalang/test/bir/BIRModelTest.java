/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.bir;

import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

/**
 * This class contains unit tests to cover AST to BIR lowering.
 *
 * @since 0.980.0
 */
public class BIRModelTest {

    @Test(description = "Test AST to BIR lowering")
    public void testBIRGen() {
        CompileResult result = BCompileUtil.compileAndGetBIR("test-src/bir/bir_model.bal");
        Assert.assertEquals(result.getErrorCount(), 0);

        BIRNode.BIRPackage birPackage = ((BLangPackage) result.getAST()).symbol.bir;
    }
}
