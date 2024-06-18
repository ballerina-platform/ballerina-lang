/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.extensions.document;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.ballerinalang.langserver.extensions.LSExtensionTestUtil;
import org.ballerinalang.langserver.extensions.ballerina.document.BallerinaSyntaxTreeResponse;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Test json ST generation for configurables.
 */
public class ConfigurablesTest {
    private Endpoint serviceEndpoint;

    private final Path configurablesBalFile = FileUtils.RES_DIR.resolve("extensions")
            .resolve("document")
            .resolve("configurables")
            .resolve("configurables.bal");

    @BeforeClass
    public void startLangServer() {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "JSON ST Node generation for configurables")
    public void testConfigurables() throws IOException {
        Path inputFile = LSExtensionTestUtil.createTempFile(configurablesBalFile);
        TestUtil.openDocument(serviceEndpoint, inputFile);
        BallerinaSyntaxTreeResponse astResponse = LSExtensionTestUtil.getBallerinaSyntaxTree(
                inputFile.toString(), this.serviceEndpoint);
        JsonArray members = astResponse.getSyntaxTree().getAsJsonObject().getAsJsonArray("members");
        Assert.assertNotEquals(members.size(), 0);
        JsonObject secondMember = members.get(1).getAsJsonObject();
        Assert.assertEquals(secondMember.get("kind").getAsString(), "ModuleVarDecl");
        JsonArray qualifiers = secondMember.get("qualifiers").getAsJsonArray();
        JsonObject firstQualifier = qualifiers.get(0).getAsJsonObject();
        Assert.assertEquals(firstQualifier.get("kind").getAsString(), "ConfigurableKeyword");
        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }
}
