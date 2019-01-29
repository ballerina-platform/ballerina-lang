/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.siddhi.query.test;

import org.ballerinalang.siddhi.query.api.annotation.Annotation;
import org.ballerinalang.siddhi.query.api.definition.Attribute;
import org.ballerinalang.siddhi.query.api.definition.TableDefinition;
import org.ballerinalang.siddhi.query.compiler.SiddhiCompiler;
import org.ballerinalang.siddhi.query.compiler.exception.SiddhiParserException;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * Testcase for defining event tables.
 */
public class DefineTableTestCase {

    @Test
    public void test1() throws SiddhiParserException {
        TableDefinition streamDefinition = SiddhiCompiler.parseTableDefinition("define table cseStream ( symbol " +
                "string, price int, volume float )");
        AssertJUnit.assertEquals(TableDefinition.
                        id("cseStream").
                        attribute("symbol", Attribute.Type.STRING).
                        attribute("price", Attribute.Type.INT).
                        attribute("volume", Attribute.Type.FLOAT).toString(),
                streamDefinition.toString());
    }

    @Test
    public void test2() throws SiddhiParserException {
        TableDefinition streamDefinition = SiddhiCompiler.parseTableDefinition("define table `define` ( `string` " +
                "string, price int, volume float );");
        AssertJUnit.assertEquals(TableDefinition.
                        id("define").
                        attribute("string", Attribute.Type.STRING).
                        attribute("price", Attribute.Type.INT).
                        attribute("volume", Attribute.Type.FLOAT).toString(),
                streamDefinition.toString());
    }

    @Test
    public void test3() throws SiddhiParserException {
        TableDefinition streamDefinition = SiddhiCompiler.parseTableDefinition("define table cseStream ( symbol " +
                "string, price int, volume float )");
        AssertJUnit.assertEquals(TableDefinition.
                        id("cseStream").
                        attribute("symbol", Attribute.Type.STRING).
                        attribute("price", Attribute.Type.INT).
                        attribute("volume", Attribute.Type.FLOAT).toString(),
                streamDefinition.toString());
    }

    @Test
    public void test4() throws SiddhiParserException {
        TableDefinition streamDefinition = SiddhiCompiler.parseTableDefinition("" +
                " @from(datasource='MyDatabase','CUSTOM')" +
                " define table cseStream ( symbol string, price int, volume float )");
        AssertJUnit.assertEquals(TableDefinition.
                        id("cseStream").
                        attribute("symbol", Attribute.Type.STRING).
                        attribute("price", Attribute.Type.INT).
                        attribute("volume", Attribute.Type.FLOAT).annotation(Annotation.annotation("from").element
                        ("datasource", "MyDatabase").element("CUSTOM")).toString(),
                streamDefinition.toString());
    }

}
