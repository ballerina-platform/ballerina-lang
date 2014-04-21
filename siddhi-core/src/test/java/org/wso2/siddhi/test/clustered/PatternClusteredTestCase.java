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
package org.wso2.siddhi.test.clustered;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.config.SiddhiConfiguration;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.query.api.QueryFactory;
import org.wso2.siddhi.query.api.condition.Condition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.query.Query;
import org.wso2.siddhi.query.api.query.input.pattern.Pattern;

public class PatternClusteredTestCase {
    static final Logger log = Logger.getLogger(PatternClusteredTestCase.class);
    int eventCount;

    @Before
    public void inti() {
        eventCount = 0;
    }

    @Test
    public void testQuery1() throws InterruptedException {
        log.info("testPatternClustered1 - OUT 1");

        String siddhiPlanId = "siddhi7-cluster-" + System.currentTimeMillis();

        SiddhiManager siddhiManager1 = createInstanceQuery1("node1", siddhiPlanId);
        InputHandler stream11 = siddhiManager1.getInputHandler("Stream1");
        InputHandler stream12 = siddhiManager1.getInputHandler("Stream2");

        SiddhiManager siddhiManager2 = createInstanceQuery1("node2", siddhiPlanId);
        InputHandler stream21 = siddhiManager2.getInputHandler("Stream1");
        InputHandler stream22 = siddhiManager2.getInputHandler("Stream2");
        try {

            stream11.send(new Object[]{"WSO2", 25.6f, 100});
            Thread.sleep(2500);
            stream21.send(new Object[]{"GOOG", 47.6f, 100});
            Thread.sleep(1500);
            stream21.send(new Object[]{"GOOG", 13.7f, 100});
            Thread.sleep(500);
            stream11.send(new Object[]{"GOOG", 47.8f, 100});
            Thread.sleep(500);
            stream12.send(new Object[]{"IBM", 45.7f, 100});
            Thread.sleep(500);
            stream22.send(new Object[]{"IBM", 55.7f, 100});
            Thread.sleep(500);

        } finally {
            siddhiManager1.shutdown();
            siddhiManager2.shutdown();
        }

        Assert.assertEquals(1, eventCount);

    }

    private SiddhiManager createInstanceQuery1(String instanceId, String planId) {

        SiddhiManager siddhiManager = new SiddhiManager(new SiddhiConfiguration().setInstanceIdentifier(instanceId).setQueryPlanIdentifier(planId).setDistributedProcessing(true));


        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream1").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));
        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream2").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.patternStream(
                        Pattern.followedBy(
                                Pattern.count(
                                        QueryFactory.inputStream("e1", "Stream1").filter(
                                                Condition.compare(Expression.variable("price"),
                                                        Condition.Operator.GREATER_THAN,
                                                        Expression.value(20))), 2, 5),
                                QueryFactory.inputStream("e2", "Stream2").filter(
                                        Condition.compare(Expression.variable("price"),
                                                Condition.Operator.GREATER_THAN,
                                                Expression.value(20))))));

        query.select(
                QueryFactory.outputSelector().
                        select("price1.1", Expression.variable("e1", 0, "price")).
                        select("price1.2", Expression.variable("e1", 1, "price")).
                        select("price1.3", Expression.variable("e1", 2, "price")).
                        select("price1.4", Expression.variable("e1", 3, "price")).
                        select("price2", Expression.variable("e2", "price"))

        );
        query.insertInto("OutStream");


        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertArrayEquals(new Object[]{25.6f, 47.6f, 47.8f, null, 45.7f}, inEvents[0].getData());
                eventCount++;
            }
        });
        return siddhiManager;
    }


}
