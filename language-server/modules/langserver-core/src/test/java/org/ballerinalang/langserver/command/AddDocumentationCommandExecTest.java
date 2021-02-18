/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.command;

import org.ballerinalang.langserver.command.executors.AddAllDocumentationExecutor;
import org.ballerinalang.langserver.command.executors.AddDocumentationExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Add Documentation command execution tests.
 */
public class AddDocumentationCommandExecTest extends AbstractCommandExecutionTest {

    private static final Logger log = LoggerFactory.getLogger(AddDocumentationCommandExecTest.class);

    @Test(dataProvider = "add-doc-data-provider", enabled = false)
    public void testAddSingleDocumentation(String config, String source) throws IOException {
        performTest(config, source, AddDocumentationExecutor.COMMAND);
    }

    @Test(dataProvider = "add-all-doc-data-provider", enabled = false)
    public void testAddAllDocumentation(String config, String source) throws IOException {
        performTest(config, source, AddAllDocumentationExecutor.COMMAND);
    }

    @DataProvider(name = "add-doc-data-provider")
    public Object[][] addDocDataProvider() {
        log.info("Test workspace/executeCommand for command {}", AddDocumentationExecutor.COMMAND);
        return new Object[][]{
                //TODO: Disabled failing doc tests
//                {"addSingleFunctionDocumentation1.json", "addSingleFunctionDocumentation1.bal"},
                {"addSingleFunctionDocumentation2.json", "commonDocumentation.bal"},
                {"addObjectFunctionDocumentation.json", "commonDocumentation.bal"},
                {"addSingleServiceDocumentation.json", "commonDocumentation.bal"},
                {"addSingleRecordDocumentation.json", "commonDocumentation.bal"},
                {"addSingleObjectDocumentation.json", "commonDocumentation.bal"},
                {"serviceDocumentationWithAnnotations.json", "serviceDocumentationWithAnnotations.bal"},
        };
    }

    @DataProvider(name = "add-all-doc-data-provider")
    public Object[][] addAllDocDataProvider() {
        log.info("Test workspace/executeCommand for command {}", AddAllDocumentationExecutor.COMMAND);
        return new Object[][]{
                {"addAllDocumentation.json", "commonDocumentation.bal"},
                {"addAllDocumentationWithAnnotations.json", "addAllDocumentationWithAnnotations.bal"}
        };
    }

    @Override
    protected String getSourceRoot() {
        return "add-documentation";
    }
}
