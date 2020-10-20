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

package toml.parser.test.core;

import io.ballerina.toml.api.Toml;
import io.ballerina.toml.semantic.ast.TomlLongValueNode;
import io.ballerina.toml.semantic.ast.TomlStringValueNode;
import io.ballerina.toml.semantic.ast.TomlValueNode;
import org.testng.Assert;
import org.testng.annotations.Test;

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
                .getResourceAsStream("syntax/core/table.toml");
        Toml read = Toml.read(inputStream);
        Long rootKey = ((TomlLongValueNode) read.get("rootKey")).getValue();
        String dotNotation = ((TomlStringValueNode) read.getTable("first").get("key")).getValue();
        Toml firstTable = read.getTable("first");
        String queryFromSubTable = ((TomlStringValueNode) firstTable.get("key")).getValue();

        String subDotNotation = ((TomlStringValueNode) read.getTable("first").getTable("sub").get("key")).getValue();
        Toml subTable = read.getTable("first").getTable("sub");
        String queryFromDeepSubTable = ((TomlStringValueNode) subTable.get("key")).getValue();

        Assert.assertEquals(rootKey, Long.valueOf(22L));
        Assert.assertEquals(dotNotation, "sdsad");
        Assert.assertEquals(queryFromSubTable, "sdsad");

        Assert.assertEquals(subDotNotation, "ewww");
        Assert.assertEquals(queryFromDeepSubTable, "ewww");
    }

    @Test
    public void testArrayOfTable() throws IOException {

        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("syntax/core/array-of-tables.toml");
        Toml read = Toml.read(inputStream);
        String valueInTable = ((TomlStringValueNode) read.getTable("products").get("hello1")).getValue();
        Assert.assertEquals(valueInTable, "hi");

        List<Toml> tables = read.getTable("products").getTables("hello");
        String firstElement = ((TomlStringValueNode) tables.get(0).get("name")).getValue();
        TomlValueNode nullElement = tables.get(1).get("name");
        String thridElement = ((TomlStringValueNode) tables.get(2).get("name")).getValue();

        Assert.assertEquals(firstElement, "Hammer");
        Assert.assertNull(nullElement);
        Assert.assertEquals(thridElement, "Nail");
    }
}
