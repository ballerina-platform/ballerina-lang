package org.wso2.siddhi.core.query.window;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;

public class IncrementalStreamProcessorTestCase {
    private static final Logger log = Logger.getLogger(IncrementalStreamProcessorTestCase.class);

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
