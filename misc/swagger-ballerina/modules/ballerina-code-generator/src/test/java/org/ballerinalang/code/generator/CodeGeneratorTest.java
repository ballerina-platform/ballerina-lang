/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.code.generator.CodeGenerator;
import org.ballerinalang.code.generator.GeneratorConstants;

import org.ballerinalang.code.generator.exception.CodeGeneratorException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

public class CodeGeneratorTest {
    private String testResourceRoot;

    @BeforeClass()
    public void setup() {
        testResourceRoot = CodeGeneratorTest.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    }

    @Test(description = "Test Ballerina skeleton generation when destination path is provided")
    public void generateSkeletonWithDestination() {
        String outPath = testResourceRoot + File.separator + "service.bal";
        String definitionPath = testResourceRoot + File.separator + "petstore.yaml";
        CodeGenerator generator = new CodeGenerator();
        try {
            String ballerinaSource = "package passthroughservice.samples;\n" +
                    "\n" +
                    "import ballerina.net.http;\n" +
                    "\n" +
                    "@http:configuration {basePath:\"/nyseStock\"}\n" +
                    "service<http> nyseStockQuote {\n" +
                    "\n" +
                    "    @http:resourceConfig {\n" +
                    "        methods:[\"GET\"]\n" +
                    "    }\n" +
                    "    resource stocks (http:Connection conn, http:InRequest inReq) {\n" +
                    "        http:OutResponse res = {};\n" +
                    "        json payload = {\"exchange\":\"nyse\", \"name\":\"IBM\", \"value\":\"127.50\"};\n" +
                    "        res.setJsonPayload(payload);\n" +
                    "        _ = conn.respond(res);\n" +
                    "    }\n" +
                    "}\n";
            String serviceName = "nyseStockQuote";
            String generatedSource = generator.generate(GeneratorConstants.GenType.OPENAPI, ballerinaSource,
                    serviceName, outPath);
            Assert.assertNotNull(generatedSource);
        } catch (CodeGeneratorException e) {
            Assert.fail();

        }
    }
}
