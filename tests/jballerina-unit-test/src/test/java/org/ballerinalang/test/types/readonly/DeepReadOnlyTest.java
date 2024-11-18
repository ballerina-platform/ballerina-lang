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

package org.ballerinalang.test.types.readonly;

import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.types.PredefinedTypes;
import io.ballerina.runtime.api.types.TableType;
import io.ballerina.runtime.api.types.TupleType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.internal.types.BXmlType;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for recursive setting of readonly flag when creating runtime types.
 */
public class DeepReadOnlyTest {

    @Test
    public void testArray() {
        MapType mapType = TypeCreator.createMapType(PredefinedTypes.TYPE_INT);
        Assert.assertFalse(mapType.isReadOnly());
        MapType mapOfMapType = TypeCreator.createMapType(mapType);
        Assert.assertFalse(mapOfMapType.isReadOnly());
        ArrayType arrayType = TypeCreator.createArrayType(mapOfMapType, true);
        Assert.assertTrue(arrayType.getElementType().isReadOnly());
        Assert.assertTrue(((MapType) arrayType.getElementType()).getConstrainedType().isReadOnly());
    }

    @Test
    public void testMap() {
        ArrayType arrayType = TypeCreator.createArrayType(PredefinedTypes.TYPE_INT);
        Assert.assertFalse(arrayType.isReadOnly());
        MapType mapType = TypeCreator.createMapType(arrayType, true);
        Assert.assertTrue(mapType.getConstrainedType().isReadOnly());
    }

    @Test
    public void testTable() {
        MapType mapType = TypeCreator.createMapType(PredefinedTypes.TYPE_INT);
        Assert.assertFalse(mapType.isReadOnly());
        TableType tableType = TypeCreator.createTableType(mapType, true);
        Assert.assertTrue(tableType.getConstrainedType().isReadOnly());
    }

    @Test
    public void testTuple() {
        MapType mapType = TypeCreator.createMapType(PredefinedTypes.TYPE_INT);
        Assert.assertFalse(mapType.isReadOnly());
        ArrayType arrayType = TypeCreator.createArrayType(PredefinedTypes.TYPE_INT);
        Assert.assertFalse(arrayType.isReadOnly());
        List<Type> ls = new ArrayList<>();
        ls.add(mapType);
        TupleType tupleType = TypeCreator.createTupleType(ls, arrayType, 0, true);
        Assert.assertTrue(tupleType.getTupleTypes().get(0).isReadOnly());
        Assert.assertTrue(tupleType.getRestType().isReadOnly());
    }

    @Test
    public void testUnion() {
        MapType mapType = TypeCreator.createMapType(PredefinedTypes.TYPE_INT);
        Assert.assertFalse(mapType.isReadOnly());
        ArrayType arrayType = TypeCreator.createArrayType(PredefinedTypes.TYPE_INT);
        Assert.assertFalse(arrayType.isReadOnly());
        List<Type> ls = new ArrayList<>();
        ls.add(mapType);
        ls.add(arrayType);
        UnionType unionType = TypeCreator.createUnionType(ls, true);
        Assert.assertTrue(unionType.getMemberTypes().get(0).isReadOnly());
        Assert.assertTrue(unionType.getMemberTypes().get(1).isReadOnly());
    }

    @Test
    public void testXml() {
        ArrayType arrayType = TypeCreator.createArrayType(PredefinedTypes.TYPE_INT);
        Assert.assertFalse(arrayType.isReadOnly());
        BXmlType xmlType = (BXmlType) TypeCreator.createXMLType(arrayType, true);
        Assert.assertTrue(xmlType.constraint.isReadOnly());
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "map cannot be a readonly type.")
    public void testInvalid() {
        MapType mapType = TypeCreator.createMapType(PredefinedTypes.TYPE_FUTURE);
        MapType mapMapType = TypeCreator.createMapType(mapType);
        TypeCreator.createArrayType(mapMapType, true);
    }
}
