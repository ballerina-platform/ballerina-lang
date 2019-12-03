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
package org.ballerinalang.langserver.completion.definitions;

import org.ballerinalang.langserver.completion.CompletionTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;

/**
 * Test cases for Object Type.
 *
 * @since 0.982.0
 */
public class ObjectTypeCompletionTest extends CompletionTest {

    private static final Logger log = LoggerFactory.getLogger(ObjectTypeCompletionTest.class);

    @DataProvider(name = "completion-data-provider")
    @Override
    public Object[][] dataProvider() {
        log.info("Test textDocument/completion for Object Type Scope");
        return new Object[][] {
                {"objectTest1.json", "object"},
                {"objectTest2.json", "object"},
                {"objectTest3.json", "object"},
                {"objectTest4.json", "object"},
                {"objectTest5.json", "object"},
                {"objectTest6.json", "object"},
                {"objectTest7.json", "object"},
                {"objectTest8.json", "object"},
                {"objectTest10.json", "object"},
                {"objectTest11.json", "object"},
                {"objectTest12.json", "object"},
                {"objectTest13.json", "object"},
                {"objectTest14.json", "object"},
                {"objectTest15.json", "object"},
        };
    }
}
