/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.openapi.cmd;

import org.testng.Assert;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.IOException;

/**
 * OpenAPI client generation command test suit.
 */
public class OpenAPIGenClientCmdTest extends OpenAPICommandTest {
    @Test(description = "Test openapi gen client command with help flag",  enabled = false)
    public void testOpenAPIGenClientCmdHelp() throws IOException {
        String[] args = {"-h"};
        OpenApiGenClientCmd openApiGenClientCmd = new OpenApiGenClientCmd(printStream, this.tmpDir.toString());
        new CommandLine(openApiGenClientCmd).parseArgs(args);
        openApiGenClientCmd.execute();

        String output = readOutput(true);
        Assert.assertTrue(output.contains("ballerina-openapi-gen-client - Generate a mock Ballerina client\n" +
                                          "       for a given OpenAPI contract"));
    }
}
