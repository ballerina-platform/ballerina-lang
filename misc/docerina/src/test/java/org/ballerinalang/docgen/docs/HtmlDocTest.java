/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.docgen.docs;


import org.apache.commons.io.FileUtils;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.docgen.Generator;
import org.ballerinalang.docgen.generator.model.Client;
import org.ballerinalang.docgen.generator.model.DefaultableVarible;
import org.ballerinalang.docgen.generator.model.Function;
import org.ballerinalang.docgen.generator.model.Module;
import org.ballerinalang.docgen.generator.model.Object;
import org.ballerinalang.docgen.generator.model.Record;
import org.ballerinalang.docgen.generator.model.Type;
import org.ballerinalang.langserver.compiler.LSCompiler;
import org.ballerinalang.langserver.compiler.LSCompilerException;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaFile;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

/**
 * HTML document writer test.
 */
public class HtmlDocTest {

    @BeforeClass()
    public void setup() {
    }

    @Test(description = "Empty module should give an empty page")
    public void testEmptyPackage() {
        BLangPackage bLangPackage = createPackage("");
        Module module = generateModule(bLangPackage);
        Assert.assertTrue(module.unionTypes.isEmpty());
        Assert.assertTrue(module.finiteTypes.isEmpty());
        Assert.assertTrue(module.errors.isEmpty());
        Assert.assertTrue(module.annotations.isEmpty());
        Assert.assertTrue(module.constants.isEmpty());
        Assert.assertTrue(module.functions.isEmpty());
        Assert.assertTrue(module.listeners.isEmpty());
        Assert.assertTrue(module.clients.isEmpty());
        Assert.assertTrue(module.objects.isEmpty());
        Assert.assertTrue(module.records.isEmpty());
    }

    @Test(description = "Optional Array Type args should be captured correctly")
    public void testOptionalArrayTypes() {
        BLangPackage bLangPackage = createPackage("public function find(string[]? names, string name)" +
                " returns (boolean) { return true; }");
        Module module = generateModule(bLangPackage);
        Assert.assertEquals(module.functions.size(), 1);
        Assert.assertEquals(module.functions.get(0).name, "find");
        Function function = module.functions.get(0);
        Assert.assertEquals(function.parameters.get(0).type.name, "string", "Invalid parameter type");
        Assert.assertTrue(function.parameters.get(0).type.isArrayType, "Invalid parameter type");
        Assert.assertTrue(function.parameters.get(0).type.isNullable, "Invalid parameter type");
        Assert.assertEquals(function.parameters.get(0).name, "names", "Invalid parameter name");
        Assert.assertEquals(function.returnParameters.get(0).type.name, "boolean", "Invalid return type");
    }

    @Test(description = "Functions in a module should be shown in the constructs")
    public void testFunctions() {
        BLangPackage bLangPackage = createPackage("public function hello(string name) returns (string)" +
                "{return \"a\";}");
        Module module = generateModule(bLangPackage);
        Assert.assertEquals(module.functions.size(), 1);
        Assert.assertEquals(module.functions.get(0).name, "hello");
        Function function = module.functions.get(0);
        Assert.assertEquals(function.parameters.get(0).type.name, "string", "Invalid parameter type");
        Assert.assertEquals(function.parameters.get(0).name, "name", "Invalid parameter name");
        Assert.assertEquals(function.returnParameters.get(0).type.name, "string", "Invalid return type");
    }

    @Test(description = "Return param should be generated correctly")
    public void testComplexReturn() {
        BLangPackage bLangPackage = createPackage("public function hello(string name) returns ((string[],int) | " +
                                                  "error)"
                + "{return ([\"a\"], 2);}");
        Module module = generateModule(bLangPackage);
        Assert.assertEquals(module.functions.size(), 1);
        Function function = module.functions.get(0);
        Assert.assertEquals(function.returnParameters.size(), 1);
        Type returnParamType = function.returnParameters.get(0).type;
        Assert.assertTrue(returnParamType.isAnonymousUnionType);
        Assert.assertEquals(returnParamType.memberTypes.size(), 2);
        Assert.assertTrue(returnParamType.memberTypes.get(0).isTuple);
        Assert.assertEquals(returnParamType.memberTypes.get(1).name, "error");
        Assert.assertEquals(returnParamType.memberTypes.get(0).memberTypes.size(), 2);
        Assert.assertTrue(returnParamType.memberTypes.get(0).memberTypes.get(0).isArrayType);
        Assert.assertEquals(returnParamType.memberTypes.get(0).memberTypes.get(0).name, "string");
        Assert.assertEquals(returnParamType.memberTypes.get(0).memberTypes.get(1).name, "int");
    }

    @Test(description = "Clients in a module should be shown in the clients list")
    public void testClients() throws IOException {
        String testClientBalPath = Paths.get("src", "test", "resources", "balFiles", "objects",
                "github_test_client.bal").toString();

        BLangPackage bLangPackage = createPackage(FileUtils.readFileToString(new File(testClientBalPath)));
        Module module = generateModule(bLangPackage);
        Assert.assertTrue(!module.clients.isEmpty());
        Assert.assertEquals(module.clients.size(), 1);

        Client client = module.clients.get(0);
        Assert.assertEquals(client.name, "GithubClient");
        List<Function> remoteMethods = client.remoteMethods;
        Assert.assertEquals(remoteMethods.size(), 2);
        Assert.assertEquals(remoteMethods.get(0).name, "testAction");
        Assert.assertEquals(remoteMethods.get(1).name, "testSend");
    }


    @Test(description = "Test records")
    public void testRecord() {
        BLangPackage bLangPackage = createPackage("# Record description.\n" +
                "#\n" +
                "# + name - name of the user\n" +
                "# + age - age of the user\n" +
                "public type User record {\n" +
                "    string name;\n" +
                "    int age;\n" +
                "};");
        Module module = generateModule(bLangPackage);
        List<Record> records = module.records;
        Assert.assertEquals(records.size(), 1);

        Record record = records.get(0);
        Assert.assertEquals(record.name, "User");

        List<DefaultableVarible> fields = record.fields;
        Assert.assertEquals(fields.size(), 2);

        DefaultableVarible field = fields.get(0);
        Assert.assertEquals(field.name, "name");
        Assert.assertEquals(field.description, "<p>name of the user</p>\n");

        field = fields.get(1);
        Assert.assertEquals(field.name, "age");
        Assert.assertEquals(field.description, "<p>age of the user</p>\n");
    }

    @Test(description = "Objects in a module should be shown in the objects list")
    public void testObjects() {
        String source = "# Object Test\n" +
                "public type Test abstract object {\n" +
                "    \n" +
                "    public string url;\n" +
                "    public string path;\n" +
                "    \n" +

                "    # Test Object function test1.\n" +
                "    # + return - whether successful or not\n" +
                "    public function test1() returns boolean;\n" +

                "    # Test Object function test2.\n" +
                "    # + ep - endpoint url\n" +
                "    # + return - whether successful or not\n" +
                "    public function test2(string ep) returns boolean;\n" +
                "};";
        BLangPackage bLangPackage = createPackage(source);
        Module module = generateModule(bLangPackage);
        List<Object> objects = module.objects;
        Assert.assertEquals(objects.size(), 1);
        Object object = objects.get(0);

        Assert.assertEquals(object.name, "Test");

        List<DefaultableVarible> fields = object.fields;
        Assert.assertEquals(fields.size(), 2);
        Assert.assertEquals(fields.get(0).name, "url");
        Assert.assertEquals(fields.get(1).name, "path");

        List<Function> methods = object.methods;
        Assert.assertEquals(methods.size(), 2);
        Assert.assertEquals(methods.get(0).name, "test1");
        Assert.assertEquals(methods.get(1).name, "test2");
    }


    @Test(description = "Annotation in a module should be shown in the constructs")
    public void testAnnotations() {
        BLangPackage bLangPackage = createPackage(" " +
                "public annotation ParameterInfo;" +
                "public annotation ReturnInfo;");
        Module module = generateModule(bLangPackage);
        Assert.assertEquals(module.annotations.size(), 2);
        Assert.assertEquals(module.annotations.get(0).name, "ParameterInfo");
        Assert.assertEquals(module.annotations.get(1).name, "ReturnInfo");
    }

    /**
     * Create the package from the bal file.
     *
     * @param source bal file which contains
     * @return BLangPackage
     */
    private BLangPackage createPackage(String source) {
        try {
            BallerinaFile ballerinaFile = LSCompiler.compileContent(source, CompilerPhase.TYPE_CHECK);

            ballerinaFile.getDiagnostics().ifPresent(
                    diagnostics -> {
                        if (!diagnostics.isEmpty()) {
                            diagnostics.forEach(System.err::println);
                            throw new IllegalStateException("Compilation errors detected.");
                        }
                    }
            );
            return ballerinaFile.getBLangPackage().orElse(null);
        } catch (LSCompilerException e) {
            throw new IllegalStateException("Compilation errors detected.", e);
        }
    }

    /**
     * Generate the api page using the package.
     *
     * @param balPackage bal package
     * @return page generated
     */
    private Module generateModule(BLangPackage balPackage) {
        Module module = new Module();
        module.id = balPackage.packageID.name.toString();
        Generator.generateModuleConstructs(module, balPackage);
        return module;
    }
}
