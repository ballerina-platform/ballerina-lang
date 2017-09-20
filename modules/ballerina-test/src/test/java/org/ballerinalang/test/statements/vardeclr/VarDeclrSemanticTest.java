/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.statements.vardeclr;

import org.ballerinalang.test.utils.BTestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class VarDeclrSemanticTest {

    @BeforeClass
    public void setup() {
    }

    @Test
    public void varDeclrTest() {
        String[] errors = BTestUtils.compile("org/ballerinalang/test/statements/vardeclr/invalidVarDeclr.bal");

        Assert.assertEquals(errors.length, 1);
        Assert.assertEquals(errors[0],
                "error: invalidVarDeclr.bal:2:12: incompatible types: expected 'int', found 'string'");
    }
}
