package org.wso2.siddhi.query.test;

import junit.framework.Assert;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;
import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

import java.util.List;

public class ExecutionPlaneTestCase {


    @Test
    public void Test1() throws RecognitionException, SiddhiParserException {
        List<ExecutionPlan> executionPlans = SiddhiCompiler.parse("define stream cseStream ( symbol string, price int, volume float )");
        Assert.assertEquals(1, executionPlans.size());
    }

    @Test
    public void Test2() throws RecognitionException, SiddhiParserException {
        List<ExecutionPlan> executionPlans = SiddhiCompiler.parse("define stream cseStream ( symbol string, price int, volume float );" +
                                                                  "from  cseStream#window.lenghtBatch(50) " +
                                                                  "select symbol, avg(price) as avgPrice " +
                                                                  "group by symbol " +
                                                                  "having (price >= 20) " +
                                                                  "insert into StockQuote for all-events ; "
        );
        Assert.assertEquals(2, executionPlans.size());
    }

    @Test
    public void Test3() throws RecognitionException, SiddhiParserException {
        List<ExecutionPlan> executionPlans = SiddhiCompiler.parse("from  cseStream#window.lenghtBatch(50) " +
                                                                  "select symbol, avg(price) as avgPrice " +
                                                                  "group by symbol " +
                                                                  "having (price >= 20) " +
                                                                  "insert into StockQuote for all-events ; "
        );
        Assert.assertEquals(1, executionPlans.size());
    }

}
