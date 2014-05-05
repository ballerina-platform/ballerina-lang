package org.wso2.siddhi.query.test;

import junit.framework.Assert;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;
import org.wso2.siddhi.query.api.query.Query;
import org.wso2.siddhi.query.api.query.input.JoinStream;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

public class JoinTestCase {

    @Test
    public void Test1() throws RecognitionException, SiddhiParserException {
        Query query = SiddhiCompiler.parseQuery("from TickEvent as t unidirectional left outer join NewsEvent[symbol =='IBM'] as n \n" +
//        Query query = SiddhiCompiler.parseQuery("from TickEvent as t unidirectional left outer join NewsEvent[filter.unique(symbol)]as n \n" +
                                                "on t.symbol == n.symbol\n" +
                                                "insert into JoinStream ;");
        Assert.assertNotNull(query);
        Assert.assertTrue(((JoinStream) query.getInputStream()).getTrigger() == JoinStream.EventTrigger.LEFT);
    }

//    from TickEvent as t unidirectional left outer join NewsEvent[std.unique(symbol)]as n
//           on t.symbol == n.symbol
//           insert into JoinStream

    @Test
    public void Test2() throws RecognitionException, SiddhiParserException {
        Query query = SiddhiCompiler.parseQuery("from TickEvent#window.lastevent() join NewsEvent#window.lastevent()\n" +
                                                "insert into JoinStream ;");
        Assert.assertNotNull(query);
        Assert.assertTrue(((JoinStream) query.getInputStream()).getTrigger() == JoinStream.EventTrigger.ALL);

    }

    @Test
    public void Test3() throws RecognitionException, SiddhiParserException {
        Query query = SiddhiCompiler.parseQuery("from fastMovingStockQuotes#window.time(60000) as fastMovingStockQuotes join  highFrequentTweets#window.time(60000) as highFrequentTweets " +
                                                " on fastMovingStockQuotes.symbol == highFrequentTweets.company " +
                                                "select fastMovingStockQuotes.symbol as company, fastMovingStockQuotes.averagePrice as amount, highFrequentTweets.words as words " +
                                                "insert into predictedStockQuotes ");
        Assert.assertNotNull(query);
        Assert.assertTrue(((JoinStream) query.getInputStream()).getTrigger() == JoinStream.EventTrigger.ALL);

    }

    @Test
    public void Test4() throws RecognitionException, SiddhiParserException {
        Query query = SiddhiCompiler.parseQuery("from TickEvent as t left outer join NewsEvent[symbol =='IBM']as n unidirectional \n" +
                                                " on t.symbol == n.symbol\n" +
                                                "insert into JoinStream ;");
        Assert.assertNotNull(query);
        Assert.assertTrue(((JoinStream) query.getInputStream()).getTrigger() == JoinStream.EventTrigger.RIGHT);

    }

}
