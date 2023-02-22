/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.diagramutil;

import org.ballerinalang.diagramutil.connector.models.connector.Type;
import org.ballerinalang.diagramutil.connector.models.connector.types.ArrayType;
import org.ballerinalang.diagramutil.connector.models.connector.types.EnumType;
import org.ballerinalang.diagramutil.connector.models.connector.types.ErrorType;
import org.ballerinalang.diagramutil.connector.models.connector.types.InclusionType;
import org.ballerinalang.diagramutil.connector.models.connector.types.IntersectionType;
import org.ballerinalang.diagramutil.connector.models.connector.types.MapType;
import org.ballerinalang.diagramutil.connector.models.connector.types.ObjectType;
import org.ballerinalang.diagramutil.connector.models.connector.types.PathParamType;
import org.ballerinalang.diagramutil.connector.models.connector.types.PrimitiveType;
import org.ballerinalang.diagramutil.connector.models.connector.types.RecordType;
import org.ballerinalang.diagramutil.connector.models.connector.types.StreamType;
import org.ballerinalang.diagramutil.connector.models.connector.types.TableType;
import org.ballerinalang.diagramutil.connector.models.connector.types.UnionType;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Connector metadata generation test case.
 */
public class TestConnectorTypes {
    private final String balTypeString = "String";
    private final String balTypeBool = "boolean";
    private final String name = "abc";

    private Type stringType;
    private Type boolType;
    private ArrayType arrayType;
    private EnumType enumType;
    private ErrorType errorType;
    private InclusionType inclusionType;
    private IntersectionType intersectionType;
    private MapType mapType;
    private ObjectType objectType;
    private PathParamType pathParamType;
    private PrimitiveType primitiveType;
    private RecordType recordType;
    private StreamType streamType;
    private TableType tableType;
    private UnionType unionType;


    @BeforeClass
    public void initConnectorModels() {
        boolType = new Type();
        boolType.typeName = balTypeBool;
        boolType.optional = true;

        stringType = new Type(name, balTypeString, false, null, true, "", null, null);

        arrayType = new ArrayType(stringType);

        List<Type> members = Arrays.asList(stringType, boolType);
        enumType = new EnumType(members);

        errorType = new ErrorType();

        inclusionType = new InclusionType(stringType);

        intersectionType = new IntersectionType();

        mapType = new MapType(stringType);

        objectType = new ObjectType(members);

        pathParamType = new PathParamType(name, balTypeString, true);

        primitiveType = new PrimitiveType(balTypeString);

        recordType = new RecordType(members, stringType);

        streamType = new StreamType(stringType, boolType);

        tableType = new TableType(stringType, Arrays.asList("foo", "bar"), boolType);

        unionType = new UnionType(members);
    }


    @Test(description = "Test array type object")
    public void getArrayType() {
        Assert.assertEquals(arrayType.memberType.typeName, balTypeString);
    }

    @Test(description = "Test enum type object")
    public void getEnumType() {
        Assert.assertEquals(enumType.typeName, "enum");
        Assert.assertEquals(enumType.members.size(), 2);
    }

    @Test(description = "Test error type object")
    public void getErrorType() {
        Assert.assertEquals(errorType.typeName, "error");
    }

    @Test(description = "Test inclusion type object")
    public void getInclusionType() {
        Assert.assertEquals(inclusionType.typeName, "inclusion");
        Assert.assertEquals(inclusionType.inclusionType.typeName, balTypeString);
    }

    @Test(description = "Test intersection type object")
    public void getIntersectionType() {
        Assert.assertEquals(intersectionType.typeName, "intersection");
    }

    @Test(description = "Test map type object")
    public void getMapType() {
        Assert.assertEquals(mapType.typeName, "map");
        Assert.assertEquals(mapType.paramType.typeName, balTypeString);
    }

    @Test(description = "Test object type object")
    public void getObjectType() {
        Assert.assertEquals(objectType.typeName, "object");
        Assert.assertEquals(objectType.fields.size(), 2);
    }

    @Test(description = "Test object type default construct")
    public void setObjectType() {
        objectType = new ObjectType();
        Assert.assertEquals(objectType.typeName, "object");
        Assert.assertEquals(objectType.fields.size(), 0);
    }


    @Test(description = "Test Path type object")
    public void getPathType() {
        Assert.assertEquals(pathParamType.name, name);
        Assert.assertEquals(pathParamType.typeName, balTypeString);
        Assert.assertTrue(pathParamType.isRestType);
    }

    @Test(description = "Test Path type default construct")
    public void setPathType() {
        pathParamType = new PathParamType();
        pathParamType.typeName = balTypeString;
        Assert.assertEquals(pathParamType.typeName, balTypeString);

        pathParamType = new PathParamType(name, balTypeBool);
        Assert.assertEquals(pathParamType.name, name);
        Assert.assertEquals(pathParamType.typeName, balTypeBool);
    }

    @Test(description = "Test primitive type object")
    public void getPrimitiveType() {
        Assert.assertEquals(primitiveType.typeName, balTypeString);
    }

    @Test(description = "Test record type object")
    public void getRecordType() {
        Assert.assertEquals(recordType.typeName, "record");
        Assert.assertEquals(recordType.fields.size(), 2);
    }

    @Test(description = "Test record type optional param construct")
    public void setRecordType() {
        recordType = new RecordType(recordType.fields, Optional.of(boolType));
        Assert.assertTrue(recordType.hasRestType);
    }

    @Test(description = "Test stream type object")
    public void getStreamType() {
        Assert.assertEquals(streamType.typeName, "stream");
        Assert.assertEquals(streamType.leftTypeParam.typeName, balTypeString);
        Assert.assertEquals(streamType.rightTypeParam.typeName, balTypeBool);
    }

    @Test(description = "Test stream type optional construct")
    public void setStreamType() {
        streamType = new StreamType(Optional.of(boolType), Optional.of(tableType));
        Assert.assertEquals(streamType.typeName, "stream");
        Assert.assertEquals(streamType.leftTypeParam.typeName, boolType.typeName);
        Assert.assertEquals(streamType.rightTypeParam.typeName, tableType.typeName);
    }

    @Test(description = "Test table type object")
    public void getTableType() {
        Assert.assertEquals(tableType.typeName, "table");
        Assert.assertEquals(tableType.rowType.typeName, balTypeString);
        Assert.assertEquals(tableType.keys.size(), 2);
        Assert.assertEquals(tableType.constraintType.typeName, balTypeBool);
    }

    @Test(description = "Test union type object")
    public void getUnionType() {
        Assert.assertEquals(unionType.typeName, "union");
        Assert.assertEquals(unionType.members.size(), 2);
    }
}
