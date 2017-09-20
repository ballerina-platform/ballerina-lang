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
package org.ballerinalang.test.statements.ifelse;

import org.ballerinalang.test.utils.BTestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class IfStmtSemanticTest {

    @BeforeClass
    public void setup() {
    }

    @Test
    public void ifStmtTest() {
        String[] errors = BTestUtils.compile("org/ballerinalang/test/statements/ifelse/invalidIfStmt.bal");

        Assert.assertEquals(errors.length, 2);
        Assert.assertEquals(errors[0],
                "error: invalidIfStmt.bal:2:6: incompatible types: expected 'boolean', found 'int'");

        Assert.assertEquals(errors[1], "warning: invalidIfStmt.bal:7:4: dead code");
    }
}
