/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;

public class ExternalTimeWindowTestCase {

    static final Logger log = Logger.getLogger(WindowTestCase.class);

    private int count;
    private long value;
    private boolean eventArrived;

    @Before
    public void init() {
        count = 0;
        value = 0;
        eventArrived = false;
    }

    @Test
    public void testWindowQuery1() throws InterruptedException {
        log.info("ExternalTime Window test1");

        SiddhiManager siddhiManager = new SiddhiManager();


        InputHandler inputHandler = siddhiManager.defineStream("define stream LoginEvents (timeStamp long, ip string) ");

        String queryReference = siddhiManager.addQuery("from LoginEvents#window.externalTime(timeStamp,5 sec) " +
                                                       "select timeStamp, ip " +
                                                       "insert into uniqueIps for all-events ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);

                if (inEvents != null) {
                    count++;
                } else {
                    value++;
                }
                eventArrived = true;
            }
        });

        System.out.println(System.currentTimeMillis());

        inputHandler.send(new Object[]{1366335804341l, "192.10.1.3"});
        inputHandler.send(new Object[]{1366335804342l, "192.10.1.4"});
        inputHandler.send(new Object[]{1366335814341l, "192.10.1.5"});
        inputHandler.send(new Object[]{1366335814345l, "192.10.1.6"});
        inputHandler.send(new Object[]{1366335824341l, "192.10.1.7"});

        Thread.sleep(1000);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("In Events ", 5, count);
        Assert.assertEquals("Remove Events ", 4, value);
        siddhiManager.shutdown();
    }

}
