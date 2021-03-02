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
package org.ballerinalang.test.documentation;

import org.ballerinalang.docgen.docs.BallerinaDocGenerator;
import org.ballerinalang.docgen.generator.model.BClass;
import org.ballerinalang.docgen.generator.model.BType;
import org.ballerinalang.docgen.generator.model.Client;
import org.ballerinalang.docgen.generator.model.DocPackage;
import org.ballerinalang.docgen.generator.model.Function;
import org.ballerinalang.docgen.generator.model.Listener;
import org.ballerinalang.docgen.generator.model.Module;
import org.ballerinalang.docgen.generator.model.ModuleDoc;
import org.ballerinalang.docgen.generator.model.Record;
import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * Test API Doc generation.
 */
public class DocModelTest {
    private Module testModule;

    @BeforeClass
    public void setup() throws IOException {
        String sourceRoot = "test-src" + File.separator + "documentation" + File.separator + "docerina_project";
        io.ballerina.projects.Project project = BCompileUtil.loadProject(sourceRoot);
        Map<String, ModuleDoc> moduleDocMap = BallerinaDocGenerator.generateModuleDocMap(project);
        DocPackage docerinaDocPackage = BallerinaDocGenerator.getDocsGenModel(moduleDocMap, project.currentPackage()
                .packageOrg().toString(), project.currentPackage().packageVersion().toString());
        testModule = docerinaDocPackage.modules.get(0);
    }

    @Test(description = "Test tuple type doc model")
    public void testTupleTypes() {
        Optional<BType> tupleType = testModule.types.stream()
                .filter(bType -> bType.name.equals("TimeDeltaStart")).findAny();
        Assert.assertTrue(tupleType.isPresent(), "TimeDeltaStart type not found");
        Assert.assertTrue(tupleType.get().isTuple, "isTuple must be true");
        Assert.assertEquals(tupleType.get().memberTypes.size(), 2, "Expected two member types");
        Assert.assertEquals(tupleType.get().memberTypes.get(0).name, "TIME_DELTA_START",
                "First membertype should be TIME_DELTA_START");
        Assert.assertEquals(tupleType.get().memberTypes.get(1).name, "int",
                "Second membertype should be int");
        Assert.assertEquals(tupleType.get().memberTypes.get(0).category, "constants",
                "First membertype category should be constants");
        Assert.assertEquals(tupleType.get().memberTypes.get(1).category, "builtin",
                "Second membertype category should be builtin");
        Assert.assertEquals(tupleType.get().memberTypes.get(0).orgName, "test_org",
                "First membertype orgName should be test_org");
        Assert.assertEquals(tupleType.get().memberTypes.get(0).version, "1.0.0",
                "First membertype version should be 1.0.0");
    }

    @Test(description = "Test intersection type doc model")
    public void testIntersectionTypes() {
        Optional<BType> intersectionType = testModule.types.stream()
                .filter(bType -> bType.name.equals("Block")).findAny();
        Assert.assertTrue(intersectionType.isPresent(), "Block type not found");
        Assert.assertTrue(intersectionType.get().isIntersectionType, "isIntersectionType must be true");
        Assert.assertEquals(intersectionType.get().memberTypes.size(), 2, "Expected two member types");
        Assert.assertEquals(intersectionType.get().memberTypes.get(0).name, "readonly",
                "First membertype should be readonly");
        Assert.assertTrue(intersectionType.get().memberTypes.get(1).isArrayType,
                "Second membertype should be an array");
        Assert.assertEquals(intersectionType.get().memberTypes.get(1).elementType.name, "byte",
                "Second membertype elementType name should be byte");
        Assert.assertEquals(intersectionType.get().memberTypes.get(1).elementType.category, "builtin",
                "Second membertype elementType category should be builtin");
    }

    @Test(description = "Test union type doc model")
    public void testUnionType() {
        Optional<BType> unionType = testModule.types.stream()
                .filter(bType -> bType.name.equals("RequestMessage")).findAny();
        Assert.assertTrue(unionType.isPresent(), "RequestMessage type not found");
        Assert.assertTrue(unionType.get().isAnonymousUnionType, "isAnonymousUnionType must be true");
        Assert.assertEquals(unionType.get().memberTypes.size(), 6, "Expected seven member types");

        Assert.assertEquals(unionType.get().memberTypes.get(0).name, "Person",
                "First membertype should be Person type");
        Assert.assertEquals(unionType.get().memberTypes.get(0).orgName, "test_org",
                "First membertype orgName should be test_org");
        Assert.assertEquals(unionType.get().memberTypes.get(0).moduleName, "docerina_project",
                "First membertype moduleName should be docerina_project");
        Assert.assertEquals(unionType.get().memberTypes.get(0).version, "1.0.0",
                "First membertype version should be 1.0.0");
        Assert.assertEquals(unionType.get().memberTypes.get(0).category, "classes",
                "First membertype category should be classes");

        Assert.assertEquals(unionType.get().memberTypes.get(1).category, "builtin",
                "Second membertype category should be builtin");
        Assert.assertEquals(unionType.get().memberTypes.get(1).name, "string",
                "Second membertype name should be string");

        Assert.assertEquals(unionType.get().memberTypes.get(2).category, "builtin",
                "Third membertype category should be builtin");
        Assert.assertEquals(unionType.get().memberTypes.get(2).name, "xml",
                "Third membertype name should be xml");

        Assert.assertEquals(unionType.get().memberTypes.get(3).category, "builtin",
                "Fourth membertype category should be builtin");
        Assert.assertEquals(unionType.get().memberTypes.get(3).name, "json",
                "Fourth membertype name should be json");

        Assert.assertEquals(unionType.get().memberTypes.get(4).name, "PersonZ",
                "Fifth membertype name should be MockObject");
        Assert.assertEquals(unionType.get().memberTypes.get(4).orgName, "test_org",
                "Fifth membertype orgName should be ballerina");
        Assert.assertEquals(unionType.get().memberTypes.get(4).moduleName, "docerina_project.world",
                "Fifth membertype moduleName should be test");
        Assert.assertEquals(unionType.get().memberTypes.get(4).category, "classes",
                "Fifth membertype category should be classes");

        Assert.assertEquals(unionType.get().memberTypes.get(5).category, "builtin",
                "Sixth membertype category should be builtin");
        Assert.assertEquals(unionType.get().memberTypes.get(5).name, "()",
                "Sixth membertype name should be ()");

    }

    @Test(description = "Test function doc model")
    public void testFunctionModel() {
        Optional<Function> function = testModule.functions.stream()
                .filter(func -> func.name.equals("addWealth")).findAny();

        Assert.assertTrue(function.isPresent(), "addWealth function not found");
        Assert.assertTrue(function.get().isIsolated, "isIsolated must be true");
        Assert.assertEquals(function.get().parameters.size(), 3, "Expected three parameters");
        Assert.assertEquals(function.get().returnParameters.size(), 1, "Expected one return parameter");

        Assert.assertEquals(function.get().parameters.get(0).name, "amt",
                "First parameter name should be amt");
        Assert.assertEquals(function.get().parameters.get(0).description, "Amount to be added\n",
                "First parameter description doesn't match.");
        Assert.assertEquals(function.get().parameters.get(0).defaultValue, "",
                "Second parameter default value should be empty");
        Assert.assertEquals(function.get().parameters.get(0).type.name, "int",
                "First parameter type should be int");
        Assert.assertTrue(function.get().parameters.get(0).type.isNullable, "First parameter should be optional");

        Assert.assertEquals(function.get().parameters.get(1).name, "rate",
                "Second parameter name should be rate");
        Assert.assertEquals(function.get().parameters.get(1).description, "Interest rate\n",
                "Second parameter description: Interest rate\n");
        Assert.assertEquals(function.get().parameters.get(1).defaultValue, "1.5",
                "Second parameter default value should be 1.5");
        Assert.assertEquals(function.get().parameters.get(1).type.name, "float",
                "Second parameter type should be float");

        Assert.assertEquals(function.get().parameters.get(2).name, "name",
                "Third parameter name should be name");
        Assert.assertTrue(function.get().parameters.get(2).type.isRestParam,
                "Third parameter should be a rest parameter");
        Assert.assertEquals(function.get().parameters.get(2).type.elementType.name, "string",
                "Third parameter element type name should be string");
        Assert.assertEquals(function.get().parameters.get(2).type.elementType.category, "builtin",
                "Third parameter element type category should be builtin");

        Assert.assertEquals(function.get().returnParameters.get(0).type.name, "RequestMessage",
                "Return parameter type name should be RequestMessage");
        Assert.assertEquals(function.get().returnParameters.get(0).description, "Req msg union type\n",
                "Return parameter description should be: Req msg union type\n");
        Assert.assertEquals(function.get().returnParameters.get(0).type.moduleName, "docerina_project",
                "Return parameter type moduleName should be docerina_project");
        Assert.assertEquals(function.get().returnParameters.get(0).type.category, "types",
                "Return parameter type category should be types");

    }

    @Test(description = "Test class doc model")
    public void testClassModel() {
        Optional<BClass> personClz = testModule.classes.stream()
                .filter(bClass -> bClass.name.equals("Person")).findAny();
        Optional<BClass> mainCntClz = testModule.classes.stream()
                .filter(bClass -> bClass.name.equals("MainController")).findAny();

        Assert.assertTrue(personClz.isPresent(), "Person class not found");

        Assert.assertNotNull(personClz.get().initMethod, "Person class should have a init method");
        Assert.assertEquals(personClz.get().initMethod.description, "Gets invoked to initialize the Person " +
                        "object\n\n",
                "Expected init method description for Person class: Gets invoked to initialize the Person object\n" +
                        "\n");
        Assert.assertTrue(personClz.get().isIsolated, "Person class should be isolated");
        Assert.assertFalse(personClz.get().isReadOnly, "Person class should be readonly");
        Assert.assertEquals(personClz.get().otherMethods.size(), 1,
                "Person class should have one other method");
        Assert.assertEquals(personClz.get().otherMethods.get(0).name, "addWealth",
                "Person class second method name should be addWealth");
        Assert.assertEquals(personClz.get().otherMethods.get(0).description, "Add wealth to person\n\n",
                "Person class method addWealth description should be: Add wealth to person\n\n");
        Assert.assertEquals(personClz.get().otherMethods.get(0).parameters.size(), 1,
                "Person class method addWealth should have one parameter");
        Assert.assertEquals(personClz.get().otherMethods.get(0).parameters.get(0).name, "amt",
                "Person class method addWealth parameter name should be amt");
        Assert.assertTrue(personClz.get().otherMethods.get(0).parameters.get(0).type.isArrayType,
                "Person class method addWealth parameter type should be array");
        Assert.assertEquals(personClz.get().otherMethods.get(0).parameters.get(0).type.elementType.name, "int",
                "Person class method addWealth parameter elementType name should be int");

        Assert.assertTrue(mainCntClz.isPresent(), "MainController class not found");
        Assert.assertNotNull(mainCntClz.get().initMethod, "MainController class should have a init method");
        Assert.assertTrue(mainCntClz.get().isReadOnly, "MainController class should be readonly");
        Assert.assertFalse(mainCntClz.get().isIsolated, "MainController class cannot be isolated");
        Assert.assertEquals(mainCntClz.get().otherMethods.size(), 1,
                "MainController class should have one other method");
        Assert.assertEquals(mainCntClz.get().otherMethods.get(0).name, "getId",
                "MainController class other method name should be getId");
        Assert.assertEquals(mainCntClz.get().otherMethods.get(0).returnParameters.size(), 1,
                "MainController class getId method name should have one return parameter");
        Assert.assertEquals(mainCntClz.get().otherMethods.get(0).returnParameters.get(0).type.name, "string",
                "MainController class getId method return type name should be string");
        Assert.assertEquals(mainCntClz.get().otherMethods.get(0).returnParameters.get(0).type.category, "builtin",
                "MainController class getId method return type category should be builtin");
        Assert.assertEquals(mainCntClz.get().otherMethods.get(0).returnParameters.get(0).description,
                "string builtin type\n",
                "MainController class getId method return description should be: string builtin type\n");
    }

    @Test(description = "Test client doc model")
    public void testClientModel() {
        Optional<Client> caller = testModule.clients.stream()
                .filter(client -> client.name.equals("Caller")).findAny();

        Assert.assertTrue(caller.isPresent(), "Caller client not found");

        Assert.assertEquals(caller.get().remoteMethods.size(), 1,
                "Caller client should have one remote method");
        Assert.assertTrue(caller.get().remoteMethods.get(0).isRemote,
                "Caller client method, `complete` should be a remote method");
        Assert.assertEquals(caller.get().remoteMethods.get(0).name, "complete",
                "Caller client should have one remote method name complete");
        Assert.assertEquals(caller.get().remoteMethods.get(0).returnParameters.size(), 1,
                "Caller client should have one remote method name complete");
        Assert.assertEquals(caller.get().remoteMethods.get(0).returnParameters.get(0).description,
                "A `grpc:Error` if an error occurs while sending the response or else `()`\n",
                "Caller client remote method return description should be: " +
                        "A `grpc:Error` if an error occurs while sending the response or else `()`\n");
        Assert.assertTrue(caller.get().remoteMethods.get(0).returnParameters.get(0).type.isAnonymousUnionType,
                "Caller client remote method return type should be union");
        Assert.assertEquals(caller.get().remoteMethods.get(0).returnParameters.get(0).type.memberTypes.size(), 2,
                "Caller client remote method return type should have to membertypes");
        Assert.assertEquals(caller.get().remoteMethods.get(0).returnParameters.get(0).type.memberTypes.get(0).name,
                "error",
                "Caller client remote method return type first member should be error");
        Assert.assertEquals(caller.get().remoteMethods.get(0).returnParameters.get(0).type.memberTypes.get(0).category,
                "builtin",
                "Caller client remote method return type first member type should be builtin");
        Assert.assertEquals(caller.get().remoteMethods.get(0).returnParameters.get(0).type.memberTypes.get(1).name,
                "int",
                "Caller client remote method return type second member should be int");
    }

    @Test(description = "Test listener doc model")
    public void testListenerModel() {
        Optional<Listener> listener = testModule.listeners.stream()
                .filter(listnr -> listnr.name.equals("Listener")).findAny();

        Assert.assertTrue(listener.isPresent(), "Listener not found");
        Assert.assertEquals(listener.get().description, "Represents server listener where one or more " +
                "services can be registered. so that ballerina program can offer\n" +
                "service through this listener.\n", "Listener expected description: Represents server " +
                "listener where one or more services can be registered. so that ballerina program can offer\n" +
                "service through this listener.\n");
        Assert.assertEquals(listener.get().lifeCycleMethods.size(), 1,
                "Listener should have one life cycle method");

    }

    @Test(description = "Test record doc model")
    public void testRecordModel() {
        Optional<Record> humanRec = testModule.records.stream()
                .filter(record -> record.name.equals("Human")).findAny();

        Assert.assertTrue(humanRec.isPresent(), "Human record not found");
        Assert.assertTrue(humanRec.get().isClosed, "Human record should be closed");
        Assert.assertEquals(humanRec.get().description, "Represents a Human record.\n\n",
                "Expected description for Human record: Represents a Human record.\n\n");
        Assert.assertEquals(humanRec.get().fields.size(), 7, "Expected 7 fields in Human Record");

        Assert.assertEquals(humanRec.get().fields.get(0).name, "controller",
                "First field in Human Record should be controller");
        Assert.assertEquals(humanRec.get().fields.get(0).type.name, "MainController",
                "Type name of first field in Human Record should be MainController");
        Assert.assertEquals(humanRec.get().fields.get(0).type.category, "classes",
                "Category of first field in Human Record should be classes");
        Assert.assertEquals(humanRec.get().fields.get(0).description, "A MainController to control the human\n",
                "Description of first field in Human Record should be: A MainController to control the human\n");

        Assert.assertEquals(humanRec.get().fields.get(1).name, "age",
                "Second field in Human Record should be age");
        Assert.assertEquals(humanRec.get().fields.get(1).defaultValue, "25",
                "Default value of second field in Human Record should be 25");
        Assert.assertEquals(humanRec.get().fields.get(1).type.name, "int",
                "Type name of second field in Human Record should be int");
        Assert.assertEquals(humanRec.get().fields.get(1).type.category, "builtin",
                "Category of second field in Human Record should be builtin");
        Assert.assertEquals(humanRec.get().fields.get(1).description, "Age of the human\n",
                "Description of first field in Human Record should be: Age of the human\n");

        Assert.assertEquals(humanRec.get().fields.get(2).name, "b",
                "Third field in Human Record should be b");
        Assert.assertEquals(humanRec.get().fields.get(2).defaultValue, "\"ballerina\"",
                "Default value of third field in Human Record should be \"ballerina\"");
        Assert.assertEquals(humanRec.get().fields.get(2).type.name, "readonly",
                "Type name of third field in Human Record should be readonly");
        Assert.assertEquals(humanRec.get().fields.get(2).type.category, "builtin",
                "Category of third field in Human Record should be builtin");

        Assert.assertEquals(humanRec.get().fields.get(3).name, "listnr",
                "Fourth field in Human Record should be listnr");
        Assert.assertEquals(humanRec.get().fields.get(3).type.name, "Listener",
                "Type name of fourth field in Human Record should be Listener");
        Assert.assertEquals(humanRec.get().fields.get(3).type.category, "listeners",
                "Category of fourth field in Human Record should be listeners");
        Assert.assertTrue(humanRec.get().fields.get(3).type.isNullable,
                "Fourth field in Human Record should be optional");

        Assert.assertEquals(humanRec.get().fields.get(4).name, "caller",
                "Fifth field in Human Record should be caller");
        Assert.assertEquals(humanRec.get().fields.get(4).type.name, "Caller",
                "Type name of fifth field in Human Record should be Caller");
        Assert.assertEquals(humanRec.get().fields.get(4).type.category, "clients",
                "Category of fifth field in Human Record should be clients");

        Assert.assertEquals(humanRec.get().fields.get(5).name, "userID",
                "Sixth field in Human Record should be userID");
        Assert.assertEquals(humanRec.get().fields.get(5).defaultValue,
                "{\"user\": \"Ballerina\", \"ID\": \"1234\"}",
                "Expected default value of the sixth field in Human Record: " +
                        "{\"user\": \"Ballerina\", \"ID\": \"1234\"}");
        Assert.assertEquals(humanRec.get().fields.get(5).type.name, "map",
                "Type name of sixth field in Human Record should be map");
        Assert.assertEquals(humanRec.get().fields.get(5).type.category, "map",
                "Category of sixth field in Human Record should be map");
        Assert.assertEquals(humanRec.get().fields.get(5).type.constraint.name, "string",
                "Type constraint name of sixth field in Human Record should be string");
        Assert.assertEquals(humanRec.get().fields.get(5).type.constraint.category, "builtin",
                "Type constraint category of sixth field in Human Record should be builtin");

        Assert.assertEquals(humanRec.get().fields.get(6).name, "settings",
                "Seventh field in Human Record should be settings");
        Assert.assertEquals(humanRec.get().fields.get(6).type.name, "ClientHttp2Settings",
                "Type name of seventh field in Human Record should be map");
        Assert.assertEquals(humanRec.get().fields.get(6).type.category, "records",
                "Category of seventh field in Human Record should be map");
    }
}
