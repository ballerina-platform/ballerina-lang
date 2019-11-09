/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * Test cases for Record Type.
 *
 * @since 1.0
 */
public class RecordTypeCompletionTest extends CompletionTest {

    private static final Logger log = LoggerFactory.getLogger(RecordTypeCompletionTest.class);

    @DataProvider(name = "completion-data-provider")
    @Override
    public Object[][] dataProvider() {
        log.info("Test textDocument/completion for Record Type Scope");
        return new Object[][] {
                {"recordTest1.json", "record"},
                {"recordTest2.json", "record"},
                {"recordTest3.json", "record"},
                {"recordTest4.json", "record"},
                {"recordTest5.json", "record"},
                {"recordTest6.json", "record"},
                {"recordTest7.json", "record"},
                {"recordTest8.json", "record"},
                {"recordTest9.json", "record"},
                {"recordTest10.json", "record"},
                {"recordTest11.json", "record"},
                {"recordTest12.json", "record"},
                {"recordTest13.json", "record"},
        };
    }
}
