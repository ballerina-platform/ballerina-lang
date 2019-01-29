/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.siddhi.core.query.window;

import org.ballerinalang.siddhi.core.SiddhiAppRuntime;
import org.ballerinalang.siddhi.core.SiddhiManager;
import org.testng.annotations.Test;

/**
 * Testcase for incremental stream processor queries.
 */
public class IncrementalStreamProcessorTestCase {

    @Test
    public void incrementalStreamProcessorTest1() {
        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                " define stream cseEventStream (arrival long, symbol string, price float, volume int); ";

        String query = "" +
                " @info(name = 'query1') " +
                " define aggregation cseEventAggregation " +
                " from cseEventStream " +
                " select sum(price) as sumPrice " +
                " aggregate by arrival every sec ... min";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);
    }

    @Test
    public void incrementalStreamProcessorTest2() {
        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                " define stream cseEventStream (arrival long, symbol string, price float, volume int); ";

        String query = "" +
                " @info(name = 'query2') " +
                " define aggregation cseEventAggregation " +
                " from cseEventStream " +
                " select sum(price) as sumPrice " +
                " aggregate every sec ... min";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);
    }

    @Test
    public void incrementalStreamProcessorTest3() {
        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                " define stream cseEventStream (arrival long, symbol string, price float, volume int); ";

        String query = "" +
                " @info(name = 'query3') " +
                " define aggregation cseEventAggregation " +
                " from cseEventStream " +
                " select sum(price) as sumPrice " +
                " group by price " +
                " aggregate every sec, min, hour, day";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);
    }
}
