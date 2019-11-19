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
package org.ballerinalang.langserver.completion.packageimport;

import org.ballerinalang.langserver.completion.CompletionTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;

/**
 * Tests the auto-completion suggestions.
 *
 */
public class PackageImportCompletionTest extends CompletionTest {

    private static final Logger log = LoggerFactory.getLogger(PackageImportCompletionTest.class);

    @DataProvider(name = "completion-data-provider")
    @Override
    public Object[][] dataProvider() {
        log.info("Test textDocument/completion for import statements");
        return new Object[][]{
                {"packageImport1.json", "packageimport"},
                {"packageImport3.json", "packageimport"},
//                {"packageImport4.json", "packageimport"},
//                {"packageImport2.json", "packageimport"}
        };
    }
}
