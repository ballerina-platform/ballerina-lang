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
import org.ballerinalang.docgen.generator.model.BObjectType;
import org.ballerinalang.docgen.generator.model.BType;
import org.ballerinalang.docgen.generator.model.Client;
import org.ballerinalang.docgen.generator.model.DefaultableVariable;
import org.ballerinalang.docgen.generator.model.Function;
import org.ballerinalang.docgen.generator.model.Listener;
import org.ballerinalang.docgen.generator.model.Module;
import org.ballerinalang.docgen.generator.model.ModuleDoc;
import org.ballerinalang.docgen.generator.model.Record;
import org.ballerinalang.docgen.generator.model.types.FunctionType;
import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
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
        List<Module> modulesList = BallerinaDocGenerator.getDocsGenModel(moduleDocMap, project.currentPackage()
                .packageOrg().toString(), project.currentPackage().packageVersion().toString());
        testModule = modulesList.get(0);
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
        Assert.assertEquals(function.get().parameters.get(0).description, "Amount to be added" + System.lineSeparator(),
                "First parameter description doesn't match.");
        Assert.assertEquals(function.get().parameters.get(0).defaultValue, "",
                "Second parameter default value should be empty");
        Assert.assertEquals(function.get().parameters.get(0).type.name, "int",
                "First parameter type should be int");
        Assert.assertTrue(function.get().parameters.get(0).type.isNullable, "First parameter should be optional");

        Assert.assertEquals(function.get().parameters.get(1).name, "rate",
                "Second parameter name should be rate");
        Assert.assertEquals(function.get().parameters.get(1).description, "Interest rate" +
                        System.lineSeparator(),
                "Second parameter description: Interest rate" + System.lineSeparator());
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
        Assert.assertEquals(function.get().returnParameters.get(0).description, "Req msg union type" +
                        System.lineSeparator(),
                "Return parameter description should be: Req msg union type" + System.lineSeparator());
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
                        "object" + System.lineSeparator() + "\n",
                "Expected init method description for Person class: Gets invoked to initialize the Person object"
                        + System.lineSeparator() + "\n");
        Assert.assertTrue(personClz.get().isIsolated, "Person class should be isolated");
        Assert.assertFalse(personClz.get().isReadOnly, "Person class should be readonly");
        Assert.assertEquals(personClz.get().otherMethods.size(), 1,
                "Person class should have one other method");
        Assert.assertEquals(personClz.get().otherMethods.get(0).name, "addWealth",
                "Person class second method name should be addWealth");
        Assert.assertEquals(personClz.get().otherMethods.get(0).description, "Add wealth to person" +
                        System.lineSeparator() + "\n",
                "Person class method addWealth description should be: Add wealth to person" +
                        System.lineSeparator() + "\n");
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
                "string builtin type" + System.lineSeparator(),
                "MainController class getId method return description should be: string builtin type" +
                        System.lineSeparator());
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
                "A `grpc:Error` if an error occurs while sending the response or else `()`"
                        + System.lineSeparator(),
                "Caller client remote method return description should be: A `grpc:Error` if an error " +
                        "occurs while sending the response or else `()`" + System.lineSeparator());
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
                "services can be registered. so that ballerina program can offer" + System.lineSeparator() +
                "service through this listener." + System.lineSeparator(),
                "Listener expected description: Represents server listener where one or more services can " +
                        "be registered. so that ballerina program can offer" + System.lineSeparator() +
                "service through this listener." + System.lineSeparator());
        Assert.assertEquals(listener.get().lifeCycleMethods.size(), 1,
                "Listener should have one life cycle method");

    }

    @Test(description = "Test record doc model")
    public void testRecordModel() {
        Optional<Record> humanRec = testModule.records.stream()
                .filter(record -> record.name.equals("Human")).findAny();

        Assert.assertTrue(humanRec.isPresent(), "Human record not found");
        Assert.assertTrue(humanRec.get().isClosed, "Human record should be closed");
        Assert.assertEquals(humanRec.get().description, "Represents a Human record." +
                        System.lineSeparator() + "\n",
                "Expected description for Human record: Represents a Human record." +
                        System.lineSeparator() + "\n");
        Assert.assertEquals(humanRec.get().fields.size(), 7, "Expected 7 fields in Human Record");

        Assert.assertEquals(humanRec.get().fields.get(0).name, "controller",
                "First field in Human Record should be controller");
        Assert.assertEquals(humanRec.get().fields.get(0).type.name, "MainController",
                "Type name of first field in Human Record should be MainController");
        Assert.assertEquals(humanRec.get().fields.get(0).type.category, "classes",
                "Category of first field in Human Record should be classes");
        Assert.assertEquals(humanRec.get().fields.get(0).description, "A MainController to control the human" +
                        System.lineSeparator(),
                "Description of first field in Human Record should be: A MainController to control the human" +
                        System.lineSeparator());

        Assert.assertEquals(humanRec.get().fields.get(1).name, "age",
                "Second field in Human Record should be age");
        Assert.assertEquals(humanRec.get().fields.get(1).defaultValue, "25",
                "Default value of second field in Human Record should be 25");
        Assert.assertEquals(humanRec.get().fields.get(1).type.name, "int",
                "Type name of second field in Human Record should be int");
        Assert.assertEquals(humanRec.get().fields.get(1).type.category, "builtin",
                "Category of second field in Human Record should be builtin");
        Assert.assertEquals(humanRec.get().fields.get(1).description, "Age of the human" +
                        System.lineSeparator(),
                "Description of first field in Human Record should be: Age of the human" +
                        System.lineSeparator());

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


    @Test(description = "Test typedesc")
    public void testTypedesc() {
        Optional<Function> testTypeDescFunc = testModule.functions.stream()
                .filter(func -> func.name.equals("testTypeDesc")).findAny();
        Assert.assertTrue(testTypeDescFunc.isPresent(), "testTypeDesc function not found");
        Assert.assertEquals(testTypeDescFunc.get().parameters.size(), 1);
        Assert.assertEquals(testTypeDescFunc.get().parameters.get(0).name, "rowType");
        Assert.assertEquals(testTypeDescFunc.get().parameters.get(0).description, "Typedesc with empty record"
                + System.lineSeparator());
        Assert.assertTrue(testTypeDescFunc.get().parameters.get(0).type.isTypeDesc);
        Assert.assertNotNull(testTypeDescFunc.get().parameters.get(0).type.elementType);
        Assert.assertEquals(testTypeDescFunc.get().parameters.get(0).type.elementType.category, "inline_record");
        Assert.assertEquals(testTypeDescFunc.get().parameters.get(0).type.elementType.memberTypes.size(), 0);

    }

    @Test(description = "Test readonly Objects and Records")
    public void testReadOnlyObjectsRecords() {
        Optional<Record> uuidRec = testModule.records.stream().filter(record -> record.name.equals("Uuid")).findAny();
        Assert.assertTrue(uuidRec.isPresent(), "Uuid record not found");
        Assert.assertTrue(uuidRec.get().isReadOnly);
        Assert.assertEquals(uuidRec.get().fields.get(0).name, "timeLow");
        Assert.assertEquals(uuidRec.get().fields.get(0).type.orgName, "ballerina");
        Assert.assertEquals(uuidRec.get().fields.get(0).type.moduleName, "lang.int");
        Assert.assertEquals(uuidRec.get().fields.get(0).type.version, "0.0.0");
        Assert.assertEquals(uuidRec.get().fields.get(0).type.name, "Unsigned32");
        Assert.assertEquals(uuidRec.get().fields.get(0).type.category, "types");

        Optional<BObjectType> controller = testModule.objectTypes.stream().filter(record -> record.name
                .equals("Controller")).findAny();
        Assert.assertTrue(controller.isPresent(), "Controller object type not found");
        Assert.assertTrue(controller.get().isReadOnly);
    }

    @Test(description = "Test links to readonly Objects and Records")
    public void testLinksToReadOnlyObjectsRecords() {
        Optional<Function> function = testModule.functions.stream()
                .filter(func -> func.name.equals("testReadonlyRecordObjectsLinks")).findAny();
        Assert.assertTrue(function.isPresent(), "testReadonlyRecordObjectsLinks function not found");

        Assert.assertEquals(function.get().parameters.size(), 2);
        Assert.assertEquals(function.get().parameters.get(0).name, "cnt");
        Assert.assertEquals(function.get().parameters.get(0).type.orgName, "test_org");
        Assert.assertEquals(function.get().parameters.get(0).type.moduleName, "docerina_project");
        Assert.assertEquals(function.get().parameters.get(0).type.version, "1.0.0");
        Assert.assertEquals(function.get().parameters.get(0).type.category, "objectTypes");

        Assert.assertEquals(function.get().parameters.get(1).name, "uuid");
        Assert.assertEquals(function.get().parameters.get(1).type.orgName, "test_org");
        Assert.assertEquals(function.get().parameters.get(1).type.moduleName, "docerina_project");
        Assert.assertEquals(function.get().parameters.get(1).type.version, "1.0.0");
        Assert.assertEquals(function.get().parameters.get(1).type.category, "records");
    }

    @Test(description = "Test deciaml type")
    public void testDecimalType() {
        Optional<BType> seconds = testModule.types.stream()
                .filter(bType -> bType.name.equals("Seconds")).findAny();
        Assert.assertTrue(seconds.isPresent(), "Seconds decimal type not found");
        Assert.assertEquals(seconds.get().memberTypes.get(0).category, "builtin");
    }

    @Test(description = "Test function type")
    public void testFunctionType() {
        Optional<BType> valuer = testModule.types.stream()
                .filter(bType -> bType.name.equals("Valuer")).findAny();
        Assert.assertTrue(valuer.isPresent(), "Valuer function type not found");
        Assert.assertTrue(valuer.get().memberTypes.get(0) instanceof FunctionType);
        Assert.assertTrue(((FunctionType) valuer.get().memberTypes.get(0)).isLambda);
        Assert.assertTrue(((FunctionType) valuer.get().memberTypes.get(0)).isIsolated);
        Assert.assertEquals(((FunctionType) valuer.get().memberTypes.get(0)).returnType.name, "anydata");
        Assert.assertEquals(((FunctionType) valuer.get().memberTypes.get(0)).returnType.category, "builtin");
    }

    @Test(description = "Test included record parameter ")
    public void testIncludedRecordParameter() {
        Optional<Function> function = testModule.functions.stream()
                .filter(func -> func.name.equals("printDebug")).findAny();
        Assert.assertTrue(function.isPresent(), "printDebug function not found");

        Assert.assertEquals(function.get().parameters.size(), 1);
        Assert.assertEquals(function.get().parameters.get(0).name, "keyValues");
        Assert.assertTrue(function.get().parameters.get(0).type.isInclusion);
        Assert.assertEquals(function.get().parameters.get(0).type.orgName, "test_org");
        Assert.assertEquals(function.get().parameters.get(0).type.moduleName, "docerina_project");
        Assert.assertEquals(function.get().parameters.get(0).type.version, "1.0.0");
        Assert.assertEquals(function.get().parameters.get(0).type.category, "records");
    }

    @Test(description = "Test distinct objects")
    public void testIDistinctObjects() {
        Optional<BObjectType> distObj = testModule.objectTypes.stream()
                .filter(func -> func.name.equals("DistObj")).findAny();
        Assert.assertTrue(distObj.isPresent(), "DistObj object not found");
        Assert.assertTrue(distObj.get().isDistinct);

        Optional<BType> linkToDistObj = testModule.types.stream()
                .filter(func -> func.name.equals("LinkToDistinctObj")).findAny();
        Assert.assertTrue(linkToDistObj.isPresent(), "DistObj object not found");
        Assert.assertEquals(linkToDistObj.get().memberTypes.get(0).name, "DistObj");
        Assert.assertEquals(linkToDistObj.get().memberTypes.get(0).category, "objectTypes");
        Assert.assertEquals(linkToDistObj.get().memberTypes.get(0).moduleName, "docerina_project");
        Assert.assertEquals(linkToDistObj.get().memberTypes.get(0).version, "1.0.0");
        Assert.assertEquals(linkToDistObj.get().memberTypes.get(0).orgName, "test_org");

    }

    @Test(description = "Test included record doc model")
    public void testIncludedRecordModel() {
        Optional<Record> cityRec = testModule.records.stream()
                .filter(record -> record.name.equals("City")).findAny();

        Assert.assertTrue(cityRec.isPresent(), "City record not found");
        Assert.assertEquals(cityRec.get().fields.size(), 2);

        Assert.assertNotNull(cityRec.get().fields.get(0).inclusionType);
        Assert.assertEquals(cityRec.get().fields.get(0).inclusionType.name, "Coordinates");
        Assert.assertEquals(cityRec.get().fields.get(0).inclusionType.moduleName, "docerina_project");
        Assert.assertEquals(cityRec.get().fields.get(0).inclusionType.version, "1.0.0");
        Assert.assertEquals(cityRec.get().fields.get(0).inclusionType.orgName, "test_org");
        Assert.assertEquals(cityRec.get().fields.get(0).inclusionType.category, "records");
        Assert.assertEquals(cityRec.get().fields.get(0).inclusionType.memberTypes.size(), 2);
        Assert.assertEquals(cityRec.get().fields.get(0).inclusionType.memberTypes.get(0).name, "latitude");
        Assert.assertEquals(cityRec.get().fields.get(0).inclusionType.memberTypes.get(1).name, "longitude");
    }

    @Test(description = "Test object type doc model")
    public void testObjectTypeDocModel() {
        Optional<BObjectType> personA = testModule.objectTypes.stream()
                .filter(client -> client.name.equals("PersonA")).findAny();

        Assert.assertTrue(personA.isPresent());
        Assert.assertEquals(personA.get().description, "Represents PersonA object type" +
                System.lineSeparator() + "\n");

        Assert.assertEquals(personA.get().methods.size(), 1);
        Assert.assertEquals(personA.get().methods.get(0).name, "getFullName");
        Assert.assertEquals(personA.get().methods.get(0).description, "Get full name method" +
                System.lineSeparator() + "\n");
        Assert.assertEquals(personA.get().methods.get(0).parameters.get(0).name, "middleName");
        Assert.assertEquals(personA.get().methods.get(0).parameters.get(0).description, "Middle name of person"
                + System.lineSeparator());
        Assert.assertEquals(personA.get().methods.get(0).parameters.get(0).type.name, "string");
        Assert.assertEquals(personA.get().methods.get(0).parameters.get(0).type.category, "builtin");
        Assert.assertEquals(personA.get().methods.get(0).returnParameters.get(0).description, "The full name"
                + System.lineSeparator());
        Assert.assertEquals(personA.get().methods.get(0).returnParameters.get(0).type.name, "string");
        Assert.assertEquals(personA.get().methods.get(0).returnParameters.get(0).type.category, "builtin");

        Assert.assertEquals(personA.get().fields.size(), 2);
        Assert.assertEquals(personA.get().fields.get(0).name, "firstName");
        Assert.assertEquals(personA.get().fields.get(0).description, "First name of the person" +
                System.lineSeparator());
        Assert.assertEquals(personA.get().fields.get(0).type.name, "string");
        Assert.assertEquals(personA.get().fields.get(0).type.category, "builtin");
    }

    @Test(description = "Test object inclusion")
    public void testObjectInclusion() {
        Optional<BObjectType> studentA = testModule.objectTypes.stream()
                .filter(client -> client.name.equals("StudentA")).findAny();

        Assert.assertTrue(studentA.isPresent());
        Assert.assertEquals(studentA.get().description, "Represents StudentA object type" + System.lineSeparator());
        Assert.assertEquals(studentA.get().fields.size(), 2);
        Assert.assertNotNull(studentA.get().fields.get(0).inclusionType);
        Assert.assertEquals(studentA.get().fields.get(0).inclusionType.name, "PersonA");
        Assert.assertEquals(studentA.get().fields.get(0).inclusionType.orgName, "test_org");
        Assert.assertEquals(studentA.get().fields.get(0).inclusionType.moduleName, "docerina_project");
        Assert.assertEquals(studentA.get().fields.get(0).inclusionType.version, "1.0.0");
        Assert.assertEquals(studentA.get().fields.get(0).inclusionType.category, "objectTypes");

        Assert.assertEquals(studentA.get().fields.get(0).inclusionType.memberTypes.get(0).name, "firstName");
        Assert.assertEquals(studentA.get().fields.get(0).inclusionType.memberTypes.get(1).name, "lastName");

        Assert.assertEquals(studentA.get().methods.size(), 1);
        Assert.assertEquals(studentA.get().methods.get(0).name, "getFullName");
        Assert.assertNotNull(studentA.get().methods.get(0).inclusionType);
        Assert.assertEquals(studentA.get().methods.get(0).inclusionType.name, "PersonA");
        Assert.assertEquals(studentA.get().methods.get(0).inclusionType.orgName, "test_org");
        Assert.assertEquals(studentA.get().methods.get(0).inclusionType.moduleName, "docerina_project");
        Assert.assertEquals(studentA.get().methods.get(0).inclusionType.version, "1.0.0");
        Assert.assertEquals(studentA.get().methods.get(0).inclusionType.category, "objectTypes");
        Assert.assertEquals(studentA.get().methods.get(0).parameters.get(0).name, "middleName");
        Assert.assertEquals(studentA.get().methods.get(0).parameters.get(0).type.name, "string");
        Assert.assertEquals(studentA.get().methods.get(0).parameters.get(0).type.category, "builtin");
        Assert.assertEquals(studentA.get().methods.get(0).returnParameters.get(0).type.name, "string");
        Assert.assertEquals(studentA.get().methods.get(0).returnParameters.get(0).type.category, "builtin");
    }

    @Test(description = "Test module variables")
    public void testModuleVariables() {
        Optional<DefaultableVariable> pubString = testModule.variables.stream()
                .filter(client -> client.name.equals("pubString")).findAny();
        Assert.assertTrue(pubString.isPresent());

        Assert.assertEquals(pubString.get().description, "A public variable of string type" +
                System.lineSeparator());
        Assert.assertEquals(pubString.get().defaultValue, "\"123\"");
        Assert.assertEquals(pubString.get().type.category, "builtin");
        Assert.assertEquals(pubString.get().type.name, "string");

        Optional<DefaultableVariable> tuple = testModule.variables.stream()
                .filter(client -> client.name.equals("[a,b]")).findAny();
        Assert.assertTrue(tuple.isPresent());
        Assert.assertEquals(tuple.get().description, "Docs for tuple module variable." +
                System.lineSeparator());
        Assert.assertEquals(tuple.get().defaultValue, "[1, 1.5]");
        Assert.assertTrue(tuple.get().type.isTuple);
        Assert.assertEquals(tuple.get().type.memberTypes.size(), 2);
        Assert.assertEquals(tuple.get().type.memberTypes.get(0).name, "int");
        Assert.assertEquals(tuple.get().type.memberTypes.get(0).category, "builtin");
        Assert.assertEquals(tuple.get().type.memberTypes.get(1).name, "float");
        Assert.assertEquals(tuple.get().type.memberTypes.get(1).category, "builtin");
    }

    @Test(description = "Test record rest field")
    public void testRecordRestField() {
        Optional<Record> keyValRec = testModule.records.stream()
                .filter(record -> record.name.equals("KeyValues")).findAny();

        Assert.assertTrue(keyValRec.isPresent(), "KeyValues record not found");
        Assert.assertEquals(keyValRec.get().fields.size(), 3);
        Assert.assertNotNull(keyValRec.get().fields.get(2).type);
        Assert.assertTrue(keyValRec.get().fields.get(2).type.isRestParam);
        Assert.assertNotNull(keyValRec.get().fields.get(2).type.elementType);
        Assert.assertEquals(keyValRec.get().fields.get(2).type.elementType.name, "json");
        Assert.assertEquals(keyValRec.get().fields.get(2).type.elementType.category, "builtin");
    }

    @Test(description = "Test type params and builtin subtype")
    public void testTypeParamAndBuiltinSubtype() {
        Optional<BType> typeParam = testModule.types.stream()
                .filter(bType -> bType.name.equals("TypeParam")).findAny();
        Optional<BType> charSubType = testModule.types.stream()
                .filter(bType -> bType.name.equals("Char")).findAny();

        Optional<BType> anyDataType = testModule.types.stream()
                .filter(bType -> bType.name.equals("AnydataType")).findAny();

        Assert.assertTrue(typeParam.isPresent());
        Assert.assertTrue(charSubType.isPresent());
        Assert.assertTrue(anyDataType.isPresent());

        Assert.assertEquals(typeParam.get().name, "TypeParam");
        Assert.assertEquals(typeParam.get().description, "A type param" + System.lineSeparator());
        Assert.assertTrue(typeParam.get().isAnonymousUnionType);
        Assert.assertEquals(typeParam.get().memberTypes.size(), 2);
        Assert.assertEquals(typeParam.get().memberTypes.get(0).name, "any");
        Assert.assertEquals(typeParam.get().memberTypes.get(0).category, "builtin");
        Assert.assertEquals(typeParam.get().memberTypes.get(1).name, "error");
        Assert.assertEquals(typeParam.get().memberTypes.get(1).category, "builtin");

        Assert.assertEquals(charSubType.get().name, "Char");
        Assert.assertEquals(charSubType.get().description,
                "Built-in subtype of string containing strings of length 1." + System.lineSeparator());
        Assert.assertEquals(charSubType.get().memberTypes.get(0).name, "string");
        Assert.assertEquals(charSubType.get().memberTypes.get(0).category, "builtin");

        Assert.assertTrue(anyDataType.get().isAnonymousUnionType);
        Assert.assertEquals(anyDataType.get().memberTypes.get(0).name, "anydata");
        Assert.assertEquals(anyDataType.get().memberTypes.get(0).category, "builtin");
    }

    @Test(description = "Test future type")
    public void testFutureType() {
        Optional<Function> cancelFutureFunc = testModule.functions.stream()
                .filter(bType -> bType.name.equals("cancelFuture")).findAny();

        Assert.assertTrue(cancelFutureFunc.isPresent());
        Assert.assertEquals(cancelFutureFunc.get().parameters.size(), 1);
        Assert.assertEquals(cancelFutureFunc.get().parameters.get(0).type.category, "future");
        Assert.assertTrue(cancelFutureFunc.get().parameters.get(0).type.elementType.isAnonymousUnionType);
        Assert.assertEquals(cancelFutureFunc.get().parameters.get(0).type.elementType.memberTypes.get(0).name,
                "any");
        Assert.assertEquals(cancelFutureFunc.get().parameters.get(0).type.elementType.memberTypes.get(1).name,
                "error");
    }

    @Test(description = "Test inline record")
    public void testInlineRecord() {
        Optional<Function> inlineRecordReturnFunc = testModule.functions.stream()
                .filter(bType -> bType.name.equals("inlineRecordReturn")).findAny();

        Assert.assertTrue(inlineRecordReturnFunc.isPresent());
        Assert.assertEquals(inlineRecordReturnFunc.get().returnParameters.get(0).type.category,
                "inline_record");
        Assert.assertEquals(inlineRecordReturnFunc.get().returnParameters.get(0).type.memberTypes.size(), 3);
        Assert.assertEquals(inlineRecordReturnFunc.get().returnParameters.get(0).type.memberTypes.get(0).name,
                "latitude");
        Assert.assertEquals(inlineRecordReturnFunc.get().returnParameters.get(0).type.memberTypes.get(0)
                        .elementType.name, "string");
        Assert.assertEquals(inlineRecordReturnFunc.get().returnParameters.get(0).type.memberTypes.get(1).name,
                "longitude");
        Assert.assertEquals(inlineRecordReturnFunc.get().returnParameters.get(0).type.memberTypes.get(1)
                        .elementType.name, "decimal");
        Assert.assertTrue(inlineRecordReturnFunc.get().returnParameters.get(0).type.memberTypes.get(2)
                .elementType.isRestParam);
        Assert.assertEquals(inlineRecordReturnFunc.get().returnParameters.get(0).type.memberTypes.get(2)
                .elementType.elementType.name, "json");
    }

    @Test(description = "Test private record inclusion")
    public void testPrivateRecordInclusion() {
        Optional<Record> subVeriSucRec = testModule.records.stream().filter(record ->
                record.name.equals("SubscriptionVerificationSuccess")).findAny();
        Assert.assertTrue(subVeriSucRec.isPresent());
        Assert.assertNull(subVeriSucRec.get().inclusionType);
        Assert.assertEquals(subVeriSucRec.get().fields.size(), 5);

        Assert.assertEquals(subVeriSucRec.get().fields.get(0).name, "headers");
        Assert.assertEquals(subVeriSucRec.get().fields.get(0).description,
                "Additional headers to be included in the `http:Response`");
        Assert.assertEquals(subVeriSucRec.get().fields.get(0).type.category, "map");
        Assert.assertTrue(subVeriSucRec.get().fields.get(0).type.constraint.isAnonymousUnionType);
        Assert.assertEquals(subVeriSucRec.get().fields.get(0).type.constraint.memberTypes.get(0).name, "string");
        Assert.assertEquals(subVeriSucRec.get().fields.get(0).type.constraint.memberTypes.get(0).category, "builtin");
        Assert.assertTrue(subVeriSucRec.get().fields.get(0).type.constraint.memberTypes.get(1).isArrayType);
        Assert.assertEquals(subVeriSucRec.get().fields.get(0).type.constraint.memberTypes.get(1).elementType.name,
                "string");
        Assert.assertEquals(subVeriSucRec.get().fields.get(0).type.constraint.memberTypes.get(1).elementType.category,
                "builtin");

        Assert.assertEquals(subVeriSucRec.get().fields.get(1).name, "settings");
        Assert.assertEquals(subVeriSucRec.get().fields.get(1).description,
                "Content to be included in the `http:Response` body");
        Assert.assertEquals(subVeriSucRec.get().fields.get(1).type.category, "records");
        Assert.assertEquals(subVeriSucRec.get().fields.get(1).type.name, "ClientHttp2Settings");
        Assert.assertEquals(subVeriSucRec.get().fields.get(1).type.memberTypes.size(), 0);
        Assert.assertTrue(subVeriSucRec.get().fields.get(1).type.isPublic);

        Assert.assertEquals(subVeriSucRec.get().fields.get(2).name, "timeLow");
        Assert.assertTrue(subVeriSucRec.get().fields.get(2).type.isAnonymousUnionType);
        Assert.assertEquals(subVeriSucRec.get().fields.get(2).type.memberTypes.size(), 2);
        Assert.assertEquals(subVeriSucRec.get().fields.get(2).type.memberTypes.get(0).name, "DistObj");
        Assert.assertEquals(subVeriSucRec.get().fields.get(2).type.memberTypes.get(0).category, "objectTypes");
        Assert.assertEquals(subVeriSucRec.get().fields.get(2).type.memberTypes.get(0).orgName, "test_org");
        Assert.assertEquals(subVeriSucRec.get().fields.get(2).type.memberTypes.get(0).moduleName, "docerina_project");
        Assert.assertEquals(subVeriSucRec.get().fields.get(2).type.memberTypes.get(0).version, "1.0.0");
        Assert.assertEquals(subVeriSucRec.get().fields.get(2).type.memberTypes.get(1).name, "Person");
        Assert.assertEquals(subVeriSucRec.get().fields.get(2).type.memberTypes.get(1).category, "classes");

        Assert.assertEquals(subVeriSucRec.get().fields.get(3).name, "clienthttpSettings");
        Assert.assertTrue(subVeriSucRec.get().fields.get(3).type.isIntersectionType);
        Assert.assertEquals(subVeriSucRec.get().fields.get(3).type.memberTypes.size(), 2);
        Assert.assertEquals(subVeriSucRec.get().fields.get(3).type.memberTypes.get(0).name, "ClientHttp2Settings");
        Assert.assertEquals(subVeriSucRec.get().fields.get(3).type.memberTypes.get(0).category, "records");
        Assert.assertEquals(subVeriSucRec.get().fields.get(3).type.memberTypes.get(0).orgName, "test_org");

        Assert.assertEquals(subVeriSucRec.get().fields.get(3).type.memberTypes.get(1).name, "readonly");
        Assert.assertEquals(subVeriSucRec.get().fields.get(3).type.memberTypes.get(1).category, "builtin");

        Assert.assertTrue(subVeriSucRec.get().fields.get(4).type.isRestParam);
        Assert.assertEquals(subVeriSucRec.get().fields.get(4).type.elementType.name, "string");
        Assert.assertEquals(subVeriSucRec.get().fields.get(4).type.elementType.category, "builtin");
    }

    @Test(description = "Test private record parameter")
    public void testPrivateRecordParameter() {
        Optional<Function> inlineRecordReturnFunc = testModule.functions.stream()
                .filter(bType -> bType.name.equals("inlineRecordReturn")).findAny();

        Assert.assertTrue(inlineRecordReturnFunc.isPresent());
        Assert.assertEquals(inlineRecordReturnFunc.get().parameters.size(), 2);
        Assert.assertEquals(inlineRecordReturnFunc.get().parameters.get(0).name, "prvtRecord");
        Assert.assertEquals(inlineRecordReturnFunc.get().parameters.get(0).type.category, "inline_record");
        Assert.assertEquals(inlineRecordReturnFunc.get().parameters.get(0).type.memberTypes.size(), 5);
        Assert.assertEquals(inlineRecordReturnFunc.get().parameters.get(0).type.memberTypes.get(0).name, "headers");
        Assert.assertEquals(inlineRecordReturnFunc.get().parameters.get(0).type.memberTypes.get(0).description,
                "Additional headers to be included in the `http:Response`");
        Assert.assertEquals(inlineRecordReturnFunc.get().parameters.get(0).type.memberTypes.get(0).elementType.
                category, "map");
        Assert.assertTrue(inlineRecordReturnFunc.get().parameters.get(0).type.memberTypes.get(0).elementType.
                constraint.isAnonymousUnionType);
        Assert.assertEquals(inlineRecordReturnFunc.get().parameters.get(0).type.memberTypes.get(0).elementType.
                constraint.memberTypes.get(0).name, "string");
        Assert.assertEquals(inlineRecordReturnFunc.get().parameters.get(0).type.memberTypes.get(0).elementType.
                constraint.memberTypes.get(0).category, "builtin");
        Assert.assertTrue(inlineRecordReturnFunc.get().parameters.get(0).type.memberTypes.get(0).elementType.
                constraint.memberTypes.get(1).isArrayType);
        Assert.assertEquals(inlineRecordReturnFunc.get().parameters.get(0).type.memberTypes.get(0).elementType.
                        constraint.memberTypes.get(1).elementType.name,
                "string");
        Assert.assertEquals(inlineRecordReturnFunc.get().parameters.get(0).type.memberTypes.get(0).elementType.
                        constraint.memberTypes.get(1).elementType.category,
                "builtin");

        Assert.assertEquals(inlineRecordReturnFunc.get().parameters.get(1).name, "publicRecord");
        Assert.assertEquals(inlineRecordReturnFunc.get().parameters.get(1).type.memberTypes.size(), 0);
    }
}
