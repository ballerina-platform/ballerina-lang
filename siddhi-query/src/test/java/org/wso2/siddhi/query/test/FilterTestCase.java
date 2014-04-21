package org.wso2.siddhi.query.test;

import org.antlr.runtime.RecognitionException;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.siddhi.query.api.query.Query;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

public class FilterTestCase {

    @Test
    public void Test() throws RecognitionException, SiddhiParserException {
        Query query = SiddhiCompiler.parseQuery("from  cseEventStream[price>3]#window.lenghtBatch(50) " +
                                                "select symbol, avg(price) as avgPrice " +
                                                "group by symbol " +
                                                "having (price >= 20) " +
                                                "insert into StockQuote for all-events ; "
        );
        Assert.assertNotNull(query);

    }


    @Test
    public void Test1() throws RecognitionException, SiddhiParserException {
        Query query = SiddhiCompiler.parseQuery("from  cseEventStream [price >= 20]#window.lengthBatch(50) " +
                                                "select symbol, avg(price) as avgPrice " +
                                                "group by symbol " +
                                                "having avgPrice>50 " +
                                                "insert into StockQuote; "
        );
        Assert.assertNotNull(query);
    }

    //        from cseEventStream[win.lenghtBatch(50)][price >= 20]
//            insert into StockQuote symbol, avg(price) as avgPrice
//            group by symbol
//            having avgPrice>50;
    @Test
    public void Test2() throws RecognitionException, SiddhiParserException {
        SiddhiCompiler.parse("from  cseEventStream [price >= 20]#window.length(50) " +
                             "select symbol, avg(price) as avgPricegroup " +
                             "group by symbol " +
                             "having avgPrice>50 " +
                             "insert into StockQuote; ");
    }

    @Test
    public void Test3() throws RecognitionException, SiddhiParserException {
        SiddhiCompiler.parse("from allStockQuotes#window.time(600000)\n" +
                             "select symbol as symbol, price, avg(price) as averagePrice \n" +
                             "group by symbol \n" +
                             "having ( price > ( averagePrice*1.02) ) or ( averagePrice > price ) " +
                             "insert into fastMovingStockQuotes \n;");
//                             "having ( price > ( averagePrice*1.02) ) or ( averagePrice*0.98 > price ); ");
//                             "having ( price >  (averagePrice +5)) or ( averagePrice > price ); ");
        ;
    }

    @Test
    public void Test4() throws RecognitionException, SiddhiParserException {
        Query query = SiddhiCompiler.parseQuery("from  cseEventStream#window.lenghtBatch(50)  " +
                                                "select symbol, avg(price) as avgPrice " +
                                                "return "
        );
        Assert.assertNotNull(query);
    }

    @Test
    public void Test5() throws RecognitionException, SiddhiParserException {
        Query query = SiddhiCompiler.parseQuery("from  cseEventStream#window.lenghtBatch(50)  " +
                                                "select symbol, avg(price) as avgPrice " +
                                                " "
        );
        Assert.assertNotNull(query);
    }

    @Test
    public void Test6() throws RecognitionException, SiddhiParserException {
        Query query = SiddhiCompiler.parseQuery("from  cseEventStream[price==foo.price and foo.try>5 in foo]  " +
                                                "select symbol, avg(price) as avgPrice "
        );
        Assert.assertNotNull(query);
    }

    @Test
    public void Test7() throws RecognitionException, SiddhiParserException {
        Query query = SiddhiCompiler.parseQuery("from  cseEventStream[(price==foo.price and foo.try>5) in foo]  " +
                                                "select symbol, avg(price) as avgPrice "
        );
        Assert.assertNotNull(query);
    }

    @Test
    public void Test9() throws RecognitionException, SiddhiParserException {
        Query query = SiddhiCompiler.parseQuery("from  cseEventStream[(price==foo.price and foo.try>5) in foo]  " +
                                                "select symbol, min(price) as minPrice "
        );
        Assert.assertNotNull(query);
    }

}


