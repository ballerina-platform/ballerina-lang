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
import org.ballerinalang.diagramutil.connector.models.connector.types.PrimitiveType;
import org.ballerinalang.diagramutil.connector.models.connector.types.RecordType;
import org.ballerinalang.diagramutil.connector.models.connector.types.StreamType;
import org.ballerinalang.diagramutil.connector.models.connector.types.UnionType;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Connector metadata generation test case.
 */
public class TestConnectorTypes {
    private final String balTypeString = "String";
    private final String balTypeBool = "boolean";

    private Type stringType;
    private Type boolType;
    private ArrayType arrayType;
    private EnumType enumType;
    private ErrorType errorType;
    private InclusionType inclusionType;
    private IntersectionType intersectionType;
    private PrimitiveType primitiveType;
    private RecordType recordType;
    private StreamType streamType;
    private UnionType unionType;

    @BeforeClass
    public void initConnectorModels() {
        stringType = new Type();
        stringType.name = balTypeString;
        stringType.optional = false;

        boolType = new Type();
        boolType.name = balTypeBool;
        boolType.optional = true;

        arrayType = new ArrayType(stringType);

        List<Type> members = Arrays.asList(stringType, boolType);
        enumType = new EnumType(members);

        errorType = new ErrorType();

        inclusionType = new InclusionType(stringType);

        intersectionType = new IntersectionType();

        primitiveType = new PrimitiveType(balTypeString);

        recordType = new RecordType(members, stringType);

        streamType = new StreamType(stringType, boolType);

        unionType = new UnionType();
        unionType.members = members;
    }


    @Test(description = "Test array type object")
    public void getArrayType() {
        Assert.assertEquals(arrayType.memberType.name, balTypeString);
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
        Assert.assertEquals(inclusionType.inclusionType.name, balTypeString);
    }

    @Test(description = "Test intersection type object")
    public void getIntersectionType() {
        Assert.assertEquals(intersectionType.typeName, "intersection");
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

    @Test(description = "Test stream type object")
    public void getStreamType() {
        Assert.assertEquals(streamType.typeName, "stream");
        Assert.assertEquals(streamType.leftTypeParam.name, balTypeString);
        Assert.assertEquals(streamType.rightTypeParam.name, balTypeBool);
    }

    @Test(description = "Test union type object")
    public void getUnionType() {
        Assert.assertEquals(unionType.typeName, "union");
        Assert.assertEquals(unionType.members.size(), 2);
    }
}
