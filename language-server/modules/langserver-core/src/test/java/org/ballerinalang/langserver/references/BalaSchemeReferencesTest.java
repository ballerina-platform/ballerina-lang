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
package org.ballerinalang.langserver.references;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.capability.InitializationOptions;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Test goto definition language server feature.
 */
public class BalaSchemeReferencesTest extends ReferencesTest {

    private static final Logger log = LoggerFactory.getLogger(BalaSchemeReferencesTest.class);

    @Test(description = "Test reference", dataProvider = "testDataProvider")
    public void test(String configPath) throws IOException {
        super.test(configPath);
    }

    @Test(dataProvider = "testReferencesWithinStdLibDataProvider")
    public void testReferencesWithinStdLib(String configPath) throws IOException, URISyntaxException {
        super.testReferencesWithinStdLib(configPath);
    }

    @DataProvider
    protected Object[][] testDataProvider() {
        log.info("Test textDocument/definition for Basic Cases");
        return new Object[][]{
                {"ref_config1.json"},
                {"ref_error_types_config1.json"},
                {"ref_error_types_config2.json"},
                {"ref_error_types_config3.json"},
                {"ref_record_types_config1.json"},
                // TODO type Err error; user defined types from other modules are not detected due to their parent
                //  being set to lang.annotations.
                {"ref_package_alias_config1.json"},
                {"ref_retry_spec_config1.json"},
        };
    }

    @DataProvider
    protected Object[][] testReferencesWithinStdLibDataProvider() {
        log.info("Test textDocument/definition for within Std Lib Cases");
        return new Object[][]{
                {"ref_within_stdlib_config1.json"},
        };
    }

    @Override
    protected Endpoint getLanguageServerEndpoint() {
        return TestUtil.newLanguageServer()
                .withInitOption(InitializationOptions.KEY_BALA_SCHEME_SUPPORT, true)
                .build();
    }
    
    protected void alterActualStdLibUri(JsonArray actual) throws IOException, URISyntaxException {
        for (JsonElement jsonElement : actual) {
            JsonObject item = jsonElement.getAsJsonObject();
            String fileUri = item.get("uri").toString().replace("\"", "");

            // Check bala URI scheme
            URI uri = new URI(fileUri);
            Assert.assertEquals(uri.getScheme(), CommonUtil.URI_SCHEME_BALA, "Expected bala: URI scheme");
            fileUri = CommonUtil.convertUriSchemeFromBala(fileUri);
            uri = new URI(fileUri);
            Assert.assertEquals(uri.getScheme(), CommonUtil.URI_SCHEME_FILE,
                    "Expected file URI scheme after conversion");

            String canonicalPath = new File(URI.create(fileUri)).getCanonicalPath();
            item.remove("uri");
            item.addProperty("uri", canonicalPath);
        }
    }
}
