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
package org.ballerinalang.siddhi.query.api;

import org.ballerinalang.siddhi.query.api.annotation.Annotation;
import org.ballerinalang.siddhi.query.api.definition.Attribute;
import org.ballerinalang.siddhi.query.api.definition.StreamDefinition;
import org.ballerinalang.siddhi.query.api.exception.DuplicateAttributeException;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * Testcase for defining stream definition.
 */
public class DefineStreamTestCase {

    //define stream StockStream (symbol string, price int, volume float );

    @Test
    public void testCreatingStreamDefinition() {
        SiddhiApp.siddhiApp("Test").defineStream(StreamDefinition.id("StockStream").attribute("symbol",
                Attribute.Type.STRING).attribute("price", Attribute.Type.INT).attribute("volume", Attribute.Type
                .FLOAT));


    }

    @Test(expectedExceptions = DuplicateAttributeException.class)
    public void testCreatingStreamWithDuplicateAttribute() {
        StreamDefinition.id("StockStream").attribute("symbol", Attribute.Type.STRING).attribute("symbol", Attribute
                .Type.INT).attribute("volume", Attribute.Type.FLOAT);

    }

    @Test
    public void testCreatingStreamDefinitionWithObject() {
        StreamDefinition.id("StockStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute
                .Type.INT).attribute("volume", Attribute.Type.FLOAT).attribute("data", Attribute.Type.OBJECT);
    }

    @Test
    public void testAnnotatingStreamDefinition() {
        SiddhiApp.siddhiApp("Test").defineStream(StreamDefinition.id("StockStream").attribute("symbol",
                Attribute.Type.STRING).attribute("price", Attribute.Type.INT).attribute("volume", Attribute.Type
                .FLOAT).annotation(Annotation.annotation("distribute").element("true")));

    }

    @Test
    public void testAttribute() {
        StreamDefinition streamDefinition = StreamDefinition.id("StockStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.INT).attribute("volume", Attribute.Type.FLOAT);
        AssertJUnit.assertEquals(1, streamDefinition.getAttributePosition("price"));
        AssertJUnit.assertEquals(Attribute.Type.FLOAT, streamDefinition.getAttributeType("volume"));
    }

}
