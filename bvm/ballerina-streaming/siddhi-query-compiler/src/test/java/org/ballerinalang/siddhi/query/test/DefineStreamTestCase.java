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
import org.ballerinalang.siddhi.query.api.definition.StreamDefinition;
import org.ballerinalang.siddhi.query.compiler.SiddhiCompiler;
import org.ballerinalang.siddhi.query.compiler.exception.SiddhiParserException;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * Stream definition testing testcase
 */
public class DefineStreamTestCase {

    @Test
    public void test1() throws SiddhiParserException {
        StreamDefinition streamDefinition = SiddhiCompiler.parseStreamDefinition("define stream cseStream ( symbol " +
                "string, price int, volume float )");
        AssertJUnit.assertEquals(StreamDefinition.
                        id("cseStream").
                        attribute("symbol", Attribute.Type.STRING).
                        attribute("price", Attribute.Type.INT).
                        attribute("volume", Attribute.Type.FLOAT).toString(),
                streamDefinition.toString());
    }

    @Test
    public void test2() throws SiddhiParserException {
        StreamDefinition streamDefinition = SiddhiCompiler.parseStreamDefinition("define stream `define` ( `string` " +
                "string, price int, volume float );");
        AssertJUnit.assertEquals(StreamDefinition.
                        id("define").
                        attribute("string", Attribute.Type.STRING).
                        attribute("price", Attribute.Type.INT).
                        attribute("volume", Attribute.Type.FLOAT).toString(),
                streamDefinition.toString());
    }

    //define stream StockStream (symbol string, price int, volume float );

    @Test
    public void testCreatingStreamDefinition() {
        StreamDefinition streamDefinition = SiddhiCompiler.parseStreamDefinition("define stream StockStream ( symbol " +
                "string, price int, volume float );");
        StreamDefinition api = StreamDefinition.id("StockStream").attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.INT).attribute("volume", Attribute.Type.FLOAT);
        AssertJUnit.assertEquals(api, streamDefinition);
    }

    @Test(expectedExceptions = SiddhiParserException.class)
    public void testCreatingStreamWithDuplicateAttribute() {
        StreamDefinition streamDefinition = SiddhiCompiler.parseStreamDefinition("define stream StockStream ( symbol " +
                "string, symbol int, volume float );");
//        StreamDefinition.id("StockStream").attribute("symbol", Attribute.Type.STRING).attribute("symbol", Attribute
// .Type.INT).attribute("volume", Attribute.Type.FLOAT);
    }

    @Test
    public void testCreatingStreamDefinition2() {
        StreamDefinition streamDefinition = SiddhiCompiler.parseStreamDefinition("define stream StockStream ( symbol " +
                "string, price int, volume double, data Object );");
        StreamDefinition api = StreamDefinition.id("StockStream").attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.INT).attribute("volume", Attribute.Type.DOUBLE).attribute("data",
                        Attribute.Type.OBJECT);
        AssertJUnit.assertEquals(api, streamDefinition);
    }

    @Test
    public void testEqualObjects() throws SiddhiParserException {
        StreamDefinition streamDefinition = SiddhiCompiler.parseStreamDefinition("@Foo(name='bar','Custom')define " +
                "stream cseStream ( symbol string, price int, volume float )");
        AssertJUnit.assertEquals(StreamDefinition.
                        id("cseStream").
                        attribute("symbol", Attribute.Type.STRING).
                        attribute("price", Attribute.Type.INT).
                        attribute("volume", Attribute.Type.FLOAT).annotation(Annotation.annotation("Foo").element
                        ("name", "bar").element("Custom")),
                streamDefinition);
    }

    @Test
    public void testMultilevelNestedAnnotations1() throws SiddhiParserException {
        StreamDefinition streamDefinition = SiddhiCompiler.parseStreamDefinition(
                "@sink(url='http://foo.com/test/{{data}}', " +
                        "   @map(type='xml', " +
                        "@payload('<test><time>{{time}}</time></test>')" +
                        "   )" +
                        ") " +
                        "define stream fooStream (id int, name string);"
        );

        AssertJUnit.assertEquals(
                StreamDefinition
                        .id("fooStream")
                        .attribute("id", Attribute.Type.INT)
                        .attribute("name", Attribute.Type.STRING)
                        .annotation(Annotation.annotation("sink")
                                .element("url", "http://foo.com/test/{{data}}")
                                .annotation(Annotation.annotation("map")
                                        .element("type", "xml")
                                        .annotation(Annotation.annotation("payload")
                                                .element("<test><time>{{time}}</time></test>")))),
                streamDefinition);
    }

    @Test
    public void testMultilevelNestedAnnotations2() throws SiddhiParserException {
        StreamDefinition streamDefinition = SiddhiCompiler.parseStreamDefinition("" +
                "@source(" +
                "   type='http', " +
                "   context='/test', " +
                "   transport='http,https', " +
                "   @map(" +
                "       type='xml', " +
                "       namespace = \"h=uri, a=uri\", " +
                "       @attributes(" +
                "           '//h:time', " +
                "           '//h:data'" +
                "       )" +
                "   )" +
                ") " +
                "define stream fooStream (id int, name string);");

        AssertJUnit.assertEquals(
                StreamDefinition
                        .id("fooStream")
                        .attribute("id", Attribute.Type.INT)
                        .attribute("name", Attribute.Type.STRING)
                        .annotation(Annotation.annotation("source")
                                .element("type", "http")
                                .element("context", "/test")
                                .element("transport", "http,https")
                                .annotation(Annotation.annotation("map")
                                        .element("type", "xml")
                                        .element("namespace", "h=uri, a=uri")
                                        .annotation(Annotation.annotation("attributes")
                                                .element("//h:time")
                                                .element("//h:data")
                                        )
                                )
                        ),
                streamDefinition);
    }
}
