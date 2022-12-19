/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package org.ballerinalang.langserver.extensions.document;

import com.google.gson.JsonObject;
import org.ballerinalang.langserver.extensions.LSExtensionTestUtil;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;

/**
 * Test syntaxTreeByName call.
 * @since 2201.3.0
 */
public class RecordDefinition {
    private Endpoint serviceEndpoint;

    private final Path projectFile = FileUtils.RES_DIR.resolve("extensions")
            .resolve("document")
            .resolve("ast")
            .resolve("modify")
            .resolve("balProjectRecords")
            .resolve("main.bal");

    @BeforeClass
    public void startLanguageServer() throws Exception {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "Get the record ST model from position")
    public void getRecordDefinitionByPosition() throws Exception {
        TestUtil.openDocument(serviceEndpoint, projectFile);
//        Range range = new Range(new Position(4, 13), new Position(9, 10));
//
//        BallerinaRecordRequest request = new BallerinaRecordRequest(
//                "123", "123", "123", "123", false
//        );

//        Position cursor = new Position(9, 4);
        Position cursor = new Position(10, 4);

//        Position cursor = new Position(11, 11);
//        Position cursor = new Position(12, 4);
        JsonObject syntaxTreeByNameResponse = LSExtensionTestUtil.getSTNodeDefinitionByPosition(projectFile.toAbsolutePath().toString(),
                cursor, this.serviceEndpoint);
        TestUtil.closeDocument(this.serviceEndpoint, projectFile);
    }

    @AfterClass
    public void stopLangServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }
}
