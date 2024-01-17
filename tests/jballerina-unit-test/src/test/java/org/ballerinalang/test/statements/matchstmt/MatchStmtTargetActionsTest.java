// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.ballerinalang.test.statements.matchstmt;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test cases to verify the behaviour of the match-target.
 *
 * @since 2.0.0
 */
@Test
public class MatchStmtTargetActionsTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/matchstmt/match_target_actions.bal");
    }

    @DataProvider
    public Object[] dataToTestMatchTargets() {
        return new Object[]{
                "testMatchTargetWaitActionTest",
                "testMatchTargetWitchMatchGuardTest",
        };
    }

    @Test(dataProvider = "dataToTestMatchTargets")
    public void testMatchTargets(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
