/*
 * Copyright (c) 2023 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.documentation;

import org.ballerinalang.docgen.docs.BallerinaDocGenerator;
import org.ballerinalang.docgen.generator.model.BObjectType;
import org.ballerinalang.docgen.generator.model.BType;
import org.ballerinalang.docgen.generator.model.Error;
import org.ballerinalang.docgen.generator.model.MapType;
import org.ballerinalang.docgen.generator.model.Module;
import org.ballerinalang.docgen.generator.model.ModuleDoc;
import org.ballerinalang.docgen.generator.model.Record;
import org.ballerinalang.docgen.generator.model.TableType;
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
public class TypeModelTest {
    private Module testModule;

    @BeforeClass
    public void setup() throws IOException {
        String sourceRoot = "test-src" + File.separator + "documentation" + File.separator + "type_models_project";
        io.ballerina.projects.Project project = BCompileUtil.loadProject(sourceRoot);
        Map<String, ModuleDoc> moduleDocMap = BallerinaDocGenerator.generateModuleDocMap(project);
        List<Module> modulesList = BallerinaDocGenerator.getDocsGenModel(moduleDocMap, project.currentPackage()
                .packageOrg().toString(), project.currentPackage().packageVersion().toString());
        testModule = modulesList.get(0);
    }

    @Test(description = "Test type descriptor type doc model")
    public void testTypeDescriptorTypes() {
        Optional<BType> typeDescType = testModule.typeDescriptorTypes.stream()
                .filter(bType -> bType.name.equals("TypeDescType")).findAny();
        Assert.assertTrue(typeDescType.isPresent(), "TypeDescriptorType type not found");
        Assert.assertTrue(typeDescType.get().isTypeDesc, "isTypeDesc must be true");
    }

    @Test(description = "Test any type doc model")
    public void testAnyTypes() {
        Optional<BType> anyType = testModule.anyTypes.stream()
                .filter(bType -> bType.name.equals("AnyType")).findAny();
        Assert.assertTrue(anyType.isPresent(), "AnyType type not found");
        Assert.assertEquals(anyType.get().memberTypes.get(0).name, "any", "Member type name should be any");
        Assert.assertEquals(anyType.get().memberTypes.get(0).category, "builtin",
                "Member type name should be builtin");
    }

    @Test(description = "Test anydata type doc model")
    public void testAnyDataTypes() {
        Optional<BType> anyDataType = testModule.anyDataTypes.stream()
                .filter(bType -> bType.name.equals("AnyDataType")).findAny();
        Assert.assertTrue(anyDataType.isPresent(), "AnyDataType type not found");
        Assert.assertEquals(anyDataType.get().memberTypes.get(0).name, "anydata",
                "Member type name should be anydata");
        Assert.assertEquals(anyDataType.get().memberTypes.get(0).category, "builtin",
                "Member type name should be builtin");
    }

    @Test(description = "Test error type doc model")
    public void testErrorTypes() {
        Optional<Error> errorType = testModule.errors.stream()
                .filter(bType -> bType.name.equals("ErrorName")).findAny();
        Assert.assertTrue(errorType.isPresent(), "ErrorName type not found");
        Assert.assertEquals(errorType.get().detailType.category, "map", "detail type category must be map");
        Assert.assertEquals(errorType.get().detailType.constraint.name, "FileErrorDetail",
                "constraint type name must be FileErrorDetail");
        Assert.assertEquals(errorType.get().detailType.constraint.category, "records",
                "constraint type category must be record");

        Optional<Error> distinctErrorType = testModule.errors.stream()
                .filter(bType -> bType.name.equals("IOError")).findAny();
        Assert.assertTrue(distinctErrorType.isPresent(), "IOError type not found");
        Assert.assertTrue(distinctErrorType.get().isDistinct, "isDistinct must be true");
    }

    @Test(description = "Test table type doc model")
    public void testTableTypes() {
        Optional<TableType> tableType = testModule.tableTypes.stream()
                .filter(bType -> bType.name.equals("TableType")).findAny();
        Assert.assertTrue(tableType.isPresent(), "TableType type not found");
        Assert.assertNotNull(tableType.get().rowParameterType, "row parameter type not found");
        Assert.assertNull(tableType.get().keyConstraint, "key constraint must be null");
        Assert.assertEquals(tableType.get().rowParameterType.category, "table", "detail type category must be table");
        Assert.assertEquals(tableType.get().rowParameterType.constraint.name, "Student",
                "constraint type name must be Student");
        Assert.assertEquals(tableType.get().rowParameterType.constraint.category, "inline_record",
                "detail type category must be table");

        Optional<TableType> tableTypeWithKey = testModule.tableTypes.stream()
                .filter(bType -> bType.name.equals("TableTypeWithKey")).findAny();
        Assert.assertTrue(tableTypeWithKey.isPresent(), "TableType type not found");
        Assert.assertNotNull(tableTypeWithKey.get().rowParameterType, "row parameter type not found");
        Assert.assertNotNull(tableTypeWithKey.get().keyConstraint, "key constraint not found");
        Assert.assertEquals(tableTypeWithKey.get().keyConstraint.category, "key",
                "key constraint category must be key");
        Assert.assertEquals(tableTypeWithKey.get().keyConstraint.constraint.name, "firstName",
                "key constraint name must be firstName");
        Assert.assertEquals(tableTypeWithKey.get().keyConstraint.constraint.category, "types",
                "key constraint type category must be types");
    }

    @Test(description = "Test map type doc model")
    public void testMapTypes() {
        Optional<MapType> mapType = testModule.mapTypes.stream()
                .filter(bType -> bType.name.equals("MapType")).findAny();
        Assert.assertTrue(mapType.isPresent(), "MapType type not found");
        Assert.assertEquals(mapType.get().mapParameterType.category, "map", "detail type category must be map");
        Assert.assertEquals(mapType.get().mapParameterType.constraint.name, "int",
                "constraint type name must be int");
        Assert.assertEquals(mapType.get().mapParameterType.constraint.category, "builtin",
                "detail type category must be builtin");
    }

    @Test(description = "Test record doc model")
    public void testRecordModel() {
        Optional<Record> recordType = testModule.records.stream()
                .filter(record -> record.name.equals("RecordType")).findAny();

        Assert.assertTrue(recordType.isPresent(), "RecordType record not found");
        Assert.assertTrue(recordType.get().isClosed, "RecordType record should be closed");
        Assert.assertEquals(recordType.get().description, "Record type param" + System.lineSeparator() + "\n");
        Assert.assertEquals(recordType.get().fields.size(), 3, "Expected 3 fields in RecordType Record");

        Assert.assertEquals(recordType.get().fields.get(0).name, "firstName",
                "First field in RecordType record should be firstName");
        Assert.assertEquals(recordType.get().fields.get(0).type.name, "string",
                "Type name of first field in RecordType record should be string");
        Assert.assertEquals(recordType.get().fields.get(0).type.category, "builtin",
                "Category of first field in RecordType record should be builtin");
        Assert.assertEquals(recordType.get().fields.get(0).description, "first name of the student" +
                        System.lineSeparator(),
                "Description of first field in RecordType record should be: first name of the student" +
                        System.lineSeparator());

        Assert.assertEquals(recordType.get().fields.get(1).name, "lastName",
                "Second field in RecordType record should be lastName");
        Assert.assertEquals(recordType.get().fields.get(1).type.name, "string",
                "Type name of second field in RecordType record should be string");
        Assert.assertEquals(recordType.get().fields.get(1).type.category, "builtin",
                "Category of second field in RecordType record should be builtin");
        Assert.assertEquals(recordType.get().fields.get(1).description, "last name of the student" +
                        System.lineSeparator(),
                "Description of second field in RecordType record should be: last name of the student" +
                        System.lineSeparator());

        Assert.assertEquals(recordType.get().fields.get(2).name, "age",
                "Third field in RecordType record should be age");
        Assert.assertEquals(recordType.get().fields.get(2).type.name, "int",
                "Type name of third field in RecordType record should be int");
        Assert.assertEquals(recordType.get().fields.get(2).type.category, "builtin",
                "Category of third field in RecordType record should be builtin");
        Assert.assertEquals(recordType.get().fields.get(2).description, "age of the student" +
                        System.lineSeparator(),
                "Description of third field in RecordType Record should be: age of the student" +
                        System.lineSeparator());

        Optional<Record> readonlyRecordType = testModule.records.stream()
                .filter(record -> record.name.equals("ReadonlyRecordType")).findAny();

        Assert.assertTrue(readonlyRecordType.isPresent(), "ReadonlyRecordType record not found");
        Assert.assertTrue(readonlyRecordType.get().isClosed, "ReadonlyRecordType record should be closed");
        Assert.assertTrue(readonlyRecordType.get().isReadOnly, "ReadonlyRecordType record should be readonly");
        Assert.assertEquals(readonlyRecordType.get().description, "Readonly record type param" +
                System.lineSeparator() + "\n");
        Assert.assertEquals(readonlyRecordType.get().fields.size(), 3,
                "Expected 3 fields in ReadonlyRecordType Record");

        Optional<Record> recordReadonlyType = testModule.records.stream()
                .filter(record -> record.name.equals("RecordReadonlyType")).findAny();

        Assert.assertTrue(recordReadonlyType.isPresent(), "RecordReadonlyType record not found");
        Assert.assertTrue(recordReadonlyType.get().isClosed, "RecordReadonlyType record should be closed");
        Assert.assertTrue(recordReadonlyType.get().isReadOnly, "RecordReadonlyType record should be readonly");
        Assert.assertEquals(recordReadonlyType.get().description, "Readonly record type param 2" +
                System.lineSeparator() + "\n");
        Assert.assertEquals(recordReadonlyType.get().fields.size(), 3,
                "Expected 3 fields in RecordReadonlyType Record");
    }

    @Test(description = "Test intersection type doc model")
    public void testIntersectionTypes() {
        Optional<BType> errorIntersectionType = testModule.intersectionTypes.stream()
                .filter(bType -> bType.name.equals("FileIOError")).findAny();
        Assert.assertTrue(errorIntersectionType.isPresent(), "FileIOError type not found");
        Assert.assertTrue(errorIntersectionType.get().isIntersectionType, "isIntersectionType must be true");
        Assert.assertEquals(errorIntersectionType.get().memberTypes.size(), 2, "Expected two member types");
        Assert.assertEquals(errorIntersectionType.get().memberTypes.get(0).name, "IOError",
                "First membertype should be IOError");
        Assert.assertEquals(errorIntersectionType.get().memberTypes.get(0).category, "errors",
                "First membertype category should be errors");
        Assert.assertEquals(errorIntersectionType.get().memberTypes.get(1).name, "error",
                "First membertype should be error");
        Assert.assertEquals(errorIntersectionType.get().memberTypes.get(1).category, "builtin",
                "First membertype category should be builtin");

        Optional<BType> readonlyIntersectionType = testModule.intersectionTypes.stream()
                .filter(bType -> bType.name.equals("ReadonlyIntersectionType")).findAny();
        Assert.assertTrue(readonlyIntersectionType.isPresent(), "FileIOError type not found");
        Assert.assertTrue(readonlyIntersectionType.get().isIntersectionType, "isIntersectionType must be true");
        Assert.assertEquals(readonlyIntersectionType.get().memberTypes.size(), 2, "Expected two member types");
        Assert.assertEquals(readonlyIntersectionType.get().memberTypes.get(0).name, "readonly",
                "First membertype should be IOError");
        Assert.assertEquals(readonlyIntersectionType.get().memberTypes.get(0).category, "builtin",
                "First membertype category should be builtin");
        Assert.assertEquals(readonlyIntersectionType.get().memberTypes.get(1).name, "string",
                "First membertype should be string");
        Assert.assertEquals(readonlyIntersectionType.get().memberTypes.get(1).category, "builtin",
                "First membertype category should be builtin");
    }

    @Test(description = "Test array type doc model")
    public void testArrayTypes() {
        Optional<BType> arrayType = testModule.arrayTypes.stream()
                .filter(bType -> bType.name.equals("ArrayType")).findAny();
        Assert.assertTrue(arrayType.isPresent(), "ArrayType type not found");
        Assert.assertTrue(arrayType.get().memberTypes.get(0).isArrayType, "isArrayType must be true");
        Assert.assertEquals(arrayType.get().memberTypes.get(0).elementType.name, "int",
                "ArrayType array element type must be int");
        Assert.assertEquals(arrayType.get().memberTypes.get(0).elementType.category, "builtin",
                "ArrayType array element type category must be builtin");
    }

    @Test(description = "Test union type doc model")
    public void testUnionTypes() {
        Optional<BType> unionType = testModule.unionTypes.stream()
                .filter(bType -> bType.name.equals("UnionType")).findAny();
        Assert.assertTrue(unionType.isPresent(), "unionType type not found");
        Assert.assertTrue(unionType.get().isAnonymousUnionType, "isAnonymousUnionType must be true");
        Assert.assertEquals(unionType.get().memberTypes.size(), 2, "Expected two member types");
        Assert.assertEquals(unionType.get().memberTypes.get(0).name, "int",
                "First membertype should be int");
        Assert.assertEquals(unionType.get().memberTypes.get(0).category, "builtin",
                "First membertype category should be builtin");
        Assert.assertEquals(unionType.get().memberTypes.get(1).name, "error",
                "First membertype should be error");
        Assert.assertEquals(unionType.get().memberTypes.get(1).category, "builtin",
                "First membertype category should be builtin");
    }

    @Test(description = "Test tuple type doc model")
    public void testTupleTypes() {
        Optional<BType> tupleType = testModule.tupleTypes.stream()
                .filter(bType -> bType.name.equals("TupleType")).findAny();
        Assert.assertTrue(tupleType.isPresent(), "TupleType type not found");
        Assert.assertTrue(tupleType.get().isTuple, "isTuple must be true");
        Assert.assertEquals(tupleType.get().memberTypes.size(), 2, "Expected two member types");
        Assert.assertEquals(tupleType.get().memberTypes.get(0).name, "string",
                "First membertype should be string");
        Assert.assertEquals(tupleType.get().memberTypes.get(1).name, "int",
                "Second membertype should be int");
        Assert.assertEquals(tupleType.get().memberTypes.get(0).category, "builtin",
                "First membertype category should be builtin");
        Assert.assertEquals(tupleType.get().memberTypes.get(1).category, "builtin",
                "Second membertype category should be builtin");
    }

    @Test(description = "Test object type doc model")
    public void testObjectTypes() {
        Optional<BObjectType> objectType = testModule.objectTypes.stream()
                .filter(bType -> bType.name.equals("ObjectType")).findAny();
        Assert.assertTrue(objectType.isPresent(), "ObjectType type not found");
        Assert.assertEquals(objectType.get().methods.size(), 2, "Expected two methods");

        Assert.assertEquals(objectType.get().methods.get(0).name, "getName",
                "First method of ObjectType type should be getName");
        Assert.assertEquals(objectType.get().methods.get(0).description,
                "This method will be called to get name of the student" + System.lineSeparator(),
                "First method of ObjectType type should has description: This method will be called to " +
                        "get name of the student" + System.lineSeparator());
        Assert.assertEquals(objectType.get().methods.get(0).parameters.get(0).name, "id",
                "First method of ObjectType type should have parameter 'id'");
        Assert.assertEquals(objectType.get().methods.get(0).parameters.get(0).type.name, "int",
                "First method of ObjectType type should have parameter type int");
        Assert.assertEquals(objectType.get().methods.get(0).returnParameters.get(0).type.name, "string",
                "First method of ObjectType type should have return parameter type string");

        Assert.assertEquals(objectType.get().methods.get(1).name, "getClassName",
                "Second method of ObjectType type should be getName");
        Assert.assertEquals(objectType.get().methods.get(1).description,
                "This method will be called to get class name of the student" + System.lineSeparator(),
                "Second method of ObjectType type should has description: This method will be called to " +
                        "get class name of the student" + System.lineSeparator());
        Assert.assertEquals(objectType.get().methods.get(1).parameters.get(0).name, "id",
                "Second method of ObjectType type should have parameter 'id'");
        Assert.assertEquals(objectType.get().methods.get(1).parameters.get(0).type.name, "int",
                "Second method of ObjectType type should have parameter type int");
        Assert.assertEquals(objectType.get().methods.get(1).returnParameters.get(0).type.name, "string",
                "Second method of ObjectType type should have return parameter type string");

        Optional<BObjectType> distinctObjectType = testModule.objectTypes.stream()
                .filter(bType -> bType.name.equals("DistinctObjectType")).findAny();
        Assert.assertTrue(distinctObjectType.isPresent(), "DistinctObjectType type not found");
        Assert.assertTrue(distinctObjectType.get().isDistinct, "isDistinct must be true");

        Optional<BObjectType> clientObjectType = testModule.objectTypes.stream()
                .filter(bType -> bType.name.equals("ClientObjectType")).findAny();
        Assert.assertTrue(clientObjectType.isPresent(), "ClientObjectType type not found");

        Optional<BObjectType> serviceObjectType = testModule.serviceTypes.stream()
                .filter(bType -> bType.name.equals("ServiceObjectType")).findAny();
        Assert.assertTrue(serviceObjectType.isPresent(), "ServiceObjectType type not found");
    }

    @Test(description = "Test integer type doc model")
    public void testIntegerTypes() {
        Optional<BType> integerType = testModule.integerTypes.stream()
                .filter(bType -> bType.name.equals("IntegerType")).findAny();
        Assert.assertTrue(integerType.isPresent(), "IntegerType type not found");

        Assert.assertEquals(integerType.get().memberTypes.get(0).name, "int", "Member type name should be int");
        Assert.assertEquals(integerType.get().memberTypes.get(0).category, "builtin",
                "Member type name should be builtin");
    }

    @Test(description = "Test string type doc model")
    public void testStringTypes() {
        Optional<BType> stringType = testModule.stringTypes.stream()
                .filter(bType -> bType.name.equals("StringType")).findAny();
        Assert.assertTrue(stringType.isPresent(), "StringType type not found");

        Assert.assertEquals(stringType.get().memberTypes.get(0).name, "string",
                "Member type name should be string");
        Assert.assertEquals(stringType.get().memberTypes.get(0).category, "builtin",
                "Member type name should be builtin");
    }

    @Test(description = "Test decimal type doc model")
    public void testDecimalTypes() {
        Optional<BType> decimalType = testModule.decimalTypes.stream()
                .filter(bType -> bType.name.equals("DecimalType")).findAny();
        Assert.assertTrue(decimalType.isPresent(), "DecimalType type not found");

        Assert.assertEquals(decimalType.get().memberTypes.get(0).name, "decimal",
                "Member type name should be decimal");
        Assert.assertEquals(decimalType.get().memberTypes.get(0).category, "builtin",
                "Member type name should be builtin");
    }

    @Test(description = "Test stream type doc model")
    public void testStreamTypes() {
        Optional<BType> streamType = testModule.streamTypes.stream()
                .filter(bType -> bType.name.equals("StreamType")).findAny();
        Assert.assertTrue(streamType.isPresent(), "StreamType type not found");

        Assert.assertEquals(streamType.get().memberTypes.get(0).name, "stream",
                "Member type name should be stream");
        Assert.assertEquals(streamType.get().memberTypes.get(0).category, "stream",
                "Member type name should be stream");
    }

    @Test(description = "Test stream type doc model")
    public void testFunctionTypes() {
        Optional<BType> functionType = testModule.functionTypes.stream()
                .filter(bType -> bType.name.equals("FunctionType")).findAny();
        Assert.assertTrue(functionType.isPresent(), "FunctionType type not found");
    }
}
