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

import io.ballerina.toml.Toml;
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

        Toml toml = new Toml();
        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("core/table.toml");
        Toml read = toml.read(inputStream);
        Long rootKey = read.getLong("rootKey");
        String dotNotation = read.getString("first.key");
        Toml firstTable = read.getTable("first");
        String queryFromSubTable = firstTable.getString("key");

        String subDotNotation = read.getString("first.sub.key");
        Toml subTable = read.getTable("first.sub");
        String queryFromDeepSubTable = subTable.getString("key");

        Assert.assertEquals(rootKey, new Long(22L));
        Assert.assertEquals(dotNotation, "sdsad");
        Assert.assertEquals(queryFromSubTable, "sdsad");

        Assert.assertEquals(subDotNotation, "ewww");
        Assert.assertEquals(queryFromDeepSubTable, "ewww");
    }

    @Test
    public void testArrayOfTable() throws IOException {

        Toml toml = new Toml();
        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("core/array-of-tables.toml");
        Toml read = toml.read(inputStream);
        String valueInTable = read.getString("products.hello1");
        Assert.assertEquals(valueInTable, "hi");

        List<Toml> tables = read.getTables("products.hello");
        String firstElement = tables.get(0).getString("name");
        String nullElement = tables.get(1).getString("name");
        String thridElement = tables.get(2).getString("name");

        Assert.assertEquals(firstElement, "Hammer");
        Assert.assertNull(nullElement);
        Assert.assertEquals(thridElement, "Nail");
    }
}
