/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package toml.parser.test.api.core;

import io.ballerina.toml.api.Toml;
import io.ballerina.toml.semantic.ast.TomlKeyValueNode;
import io.ballerina.toml.semantic.ast.TomlLongValueNode;
import io.ballerina.toml.semantic.ast.TomlStringValueNode;
import io.ballerina.toml.semantic.ast.TomlTableArrayNode;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.tools.text.LineRange;
import org.testng.Assert;
import org.testng.annotations.Test;
import toml.parser.test.ParserTestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Basic tests for TOML Tables and Array of tables.
 */
public class TableTest {

    @Test
    public void testTable() throws IOException {

        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("syntax/tables/table.toml");
        Toml read = Toml.read(inputStream);
        Long rootKey = ((TomlLongValueNode) read.get("rootKey").get()).getValue();
        Assert.assertEquals(rootKey, Long.valueOf(22L));

        Toml firstTable = read.getTable("first").get();
        String queryFromSubTable = ((TomlStringValueNode) firstTable.get("key").get()).getValue();
        Assert.assertEquals(queryFromSubTable, "sdsad");

        String dotNotation = ((TomlStringValueNode) read.get("first.key").get()).getValue();
        Assert.assertEquals(dotNotation, "sdsad");

        String subDotNotation = ((TomlStringValueNode) read.get("first.sub.key").get()).getValue();
        Toml subTable = read.getTable("first.sub").get();
        Assert.assertEquals(subDotNotation, "ewww");

        String queryFromDeepSubTable = ((TomlStringValueNode) subTable.get("key").get()).getValue();
        Assert.assertEquals(queryFromDeepSubTable, "ewww");

        LineRange rootKeyRange = read.rootNode().entries().get("rootKey").location().lineRange();
        ParserTestUtils.assertLineRange(rootKeyRange, 16, 0, 16, 12);

        LineRange firstTableRange = read.rootNode().entries().get("first").location().lineRange();
        ParserTestUtils.assertLineRange(firstTableRange, 22, 0, 24, 13);

        LineRange tableKeyTableRange =
                ((TomlTableNode) read.rootNode().entries().get("first")).entries().get("key").location().lineRange();
        ParserTestUtils.assertLineRange(tableKeyTableRange, 23, 0, 23, 14);

        LineRange nestedTable =
                ((TomlTableNode) read.rootNode().entries().get("first")).entries().get("sub").location().lineRange();
        ParserTestUtils.assertLineRange(nestedTable, 18, 0, 20, 13);

        LineRange nestedTableKeyNode =
                ((TomlTableNode) ((TomlTableNode) read.rootNode().entries().get("first")).entries().get("sub"))
                        .entries().get("hey").location().lineRange();
        ParserTestUtils.assertLineRange(nestedTableKeyNode, 19, 0, 19, 14);

        LineRange nestedTableKeyValueNode =
                ((TomlKeyValueNode) ((TomlTableNode) ((TomlTableNode) read.rootNode().entries().get("first")).entries()
                        .get("sub")).entries().get("hey")).value().location().lineRange();
        ParserTestUtils.assertLineRange(nestedTableKeyValueNode, 19, 6, 19, 14);

    }

    @Test
    public void testArrayOfTable() throws IOException {

        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("syntax/tables/array-of-tables.toml");
        Toml read = Toml.read(inputStream);
        String valueInTable = ((TomlStringValueNode) read.getTable("products").get().get("hello1").get()).getValue();
        Assert.assertEquals(valueInTable, "hi");

        List<Toml> tables = read.getTable("products").get().getTables("hello");
        String firstElement = ((TomlStringValueNode) tables.get(0).get("name").get()).getValue();
        boolean nullElement = tables.get(1).get("name").isEmpty();
        String thirdElement = ((TomlStringValueNode) tables.get(2).get("name").get()).getValue();

        LineRange tableArrayRange =
                ((TomlTableNode) read.rootNode().entries().get("products")).entries().get("hello").location()
                        .lineRange();
        ParserTestUtils.assertLineRange(tableArrayRange, 19, 0, 21, 15);

        LineRange tableArrayKeyRange =
                ((TomlTableArrayNode) ((TomlTableNode) read.rootNode().entries().get("products")).entries()
                        .get("hello")).children().get(0).entries().get("name").location().lineRange();
        ParserTestUtils.assertLineRange(tableArrayKeyRange, 20, 0, 20, 15);

        Assert.assertEquals(firstElement, "Hammer");
        Assert.assertTrue(nullElement);
        Assert.assertEquals(thirdElement, "Nail");

        TomlStringValueNode generatedTableWithArray =
                (TomlStringValueNode) read.getTables("foo.bar").get(0).get("name").get();
        TomlStringValueNode generatedTableExplicitDecl = (TomlStringValueNode) read.get("foo.name").get();

        Assert.assertEquals(generatedTableWithArray.getValue(), "Alice");
        Assert.assertEquals(generatedTableExplicitDecl.getValue(), "Bob");

        List<Toml> fruits = read.getTables("fruits");
        Assert.assertEquals(fruits.size(), 2);

        Toml firstTable = fruits.get(0);
        TomlStringValueNode appleStr = (TomlStringValueNode) firstTable.get("name").get();
        Assert.assertEquals(appleStr.getValue(), "apple");

        Toml physical = firstTable.getTable("physical").get();
        TomlStringValueNode color = (TomlStringValueNode) physical.get("color").get();
        TomlStringValueNode shape = (TomlStringValueNode)  physical.get("shape").get();
        Assert.assertEquals(color.getValue(), "red");
        Assert.assertEquals(shape.getValue(), "round");

        List<Toml> firstVarieties = firstTable.getTables("varieties");
        Assert.assertEquals(firstVarieties.size(), 2);

        Toml firstVariant = firstVarieties.get(0);
        TomlStringValueNode red = (TomlStringValueNode) firstVariant.get("name").get();
        Assert.assertEquals(red.getValue(), "red delicious");

        Toml secondVariant = firstVarieties.get(1);
        TomlStringValueNode granny = (TomlStringValueNode) secondVariant.get("name").get();
        Assert.assertEquals(granny.getValue(), "granny smith");

        Toml secondTable = fruits.get(1);
        TomlStringValueNode banana = (TomlStringValueNode) secondTable.get("name").get();
        Assert.assertEquals(banana.getValue(), "banana");

        List<Toml> secondVarieties = secondTable.getTables("varieties");
        Assert.assertEquals(secondVarieties.size(), 1);

        Toml variant = secondVarieties.get(0);
        TomlStringValueNode plantain = (TomlStringValueNode) variant.get("name").get();
        Assert.assertEquals(plantain.getValue(), "plantain");
    }
}
