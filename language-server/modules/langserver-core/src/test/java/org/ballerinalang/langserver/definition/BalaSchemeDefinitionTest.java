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
package org.ballerinalang.langserver.definition;

import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.capability.InitializationOptions;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Test goto definition language server feature with bala URI scheme enabled.
 */
public class BalaSchemeDefinitionTest extends DefinitionTest {
    
    @Test(description = "Test goto definitions", dataProvider = "testDataProvider")
    public void test(String configPath, String configDir) throws IOException {
        super.test(configPath, configDir);
    }

    @Test(description = "Test goto definitions for standard libs", dataProvider = "testStdLibDataProvider")
    public void testStdLibDefinition(String configPath, String configDir) throws IOException, URISyntaxException {
        super.testStdLibDefinition(configPath, configDir);
    }

    @Test(dataProvider = "testInterStdLibDataProvider")
    public void testInterStdLibDefinition(String configPath, String configDir) throws IOException, URISyntaxException {
        super.testInterStdLibDefinition(configPath, configDir);
    }
    
    @Override
    protected Endpoint getServiceEndpoint() {
        return TestUtil.newLanguageServer()
                .withInitOption(InitializationOptions.KEY_BALA_SCHEME_SUPPORT, true)
                .build();
    }

    protected String getExpectedUriScheme() {
        return CommonUtil.URI_SCHEME_BALA;
    }
}
