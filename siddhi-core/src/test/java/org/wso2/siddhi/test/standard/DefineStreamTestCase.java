/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.siddhi.test.standard;

import junit.framework.Assert;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.exception.DifferentDefinitionAlreadyExistException;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.query.api.QueryFactory;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

public class DefineStreamTestCase {

    @Test
    public void testSingleDefinition() {
        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition streamDefinition = QueryFactory.createStreamDefinition();
        streamDefinition.name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.INT).attribute("volume", Attribute.Type.FLOAT);
        siddhiManager.defineStream(streamDefinition);
    }

    @Test(expected = DifferentDefinitionAlreadyExistException.class)
    public void testAddingTwoSameDefinition1() {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.INT).attribute("volume", Attribute.Type.FLOAT));
        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbols", Attribute.Type.STRING).attribute("price", Attribute.Type.INT).attribute("volume", Attribute.Type.FLOAT));
    }

    @Test
    public void testAddingTwoSameDefinition2() {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.INT).attribute("volume", Attribute.Type.FLOAT));
        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.INT).attribute("volume", Attribute.Type.FLOAT));
    }

    @Test
    public void testSingleOutputDefinition() {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");
        String queryReference = siddhiManager.addQuery("from cseEventStream " +
                                                       "select * " +
                                                       "insert into OutputStream ;");

        StreamDefinition outputStreamDefinition = siddhiManager.getStreamDefinition("OutputStream");
        Assert.assertEquals("OutputStream", outputStreamDefinition.getStreamId());
        Assert.assertEquals("symbol", outputStreamDefinition.getAttributeList().get(0).getName());
        Assert.assertEquals(Attribute.Type.FLOAT, outputStreamDefinition.getAttributeList().get(1).getType());
        Assert.assertEquals(3, outputStreamDefinition.getAttributeList().size());
    }

    @Test
    public void testRetrievalStreamID() {

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

        Assert.assertEquals("cseEventStream", inputHandler.getStreamId());

    }

    @Test(expected = DifferentDefinitionAlreadyExistException.class)
    public void testDifferentOutputDefinition() {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.defineStream("define stream OutputStream (symbol string, price float, volume long) ");
        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long, time long) ");
        String queryReference = siddhiManager.addQuery("from cseEventStream " +
                                                       "select * " +
                                                       "insert into OutputStream ;");

    }


}
