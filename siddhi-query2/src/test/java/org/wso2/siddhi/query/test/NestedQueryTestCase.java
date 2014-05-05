package org.wso2.siddhi.query.test;

import org.antlr.runtime.RecognitionException;
import org.junit.Test;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

public class NestedQueryTestCase {

    @Test
    public void Test1() throws RecognitionException, SiddhiParserException {
        SiddhiCompiler.parse("from (\n" +
                             "\tfrom cseEventStream[ price >= 20]#window.length(50)\n" +
                             "select symbol, avg(price) as avgPrice\n" +
                             "group by symbol\n" +
                             "return) [symbol==\"IBM\"]\n" +
                             "select symbol, avgPrice\n" +
                             "insert into IBMStockQuote ");
    }

    //    from (
//           from cseEventStream[win.length(50)][ price >= 20]
//                    return  symbol, avg(price) as avgPrice
//                    group by symbol) [symbol=="IBM"]
//                    insert into IBMStockQuote symbol, avgPrice
    @Test
    public void Test2() throws RecognitionException, SiddhiParserException {
        SiddhiCompiler.parse("from (\n" +
                             "from MyStream [price >10]\n" +
                             "return \n" +
                             ") #window.length(1000)\n" +
                             "select symbol ,  avg(price) as avgPrice " +
                             "insert into StockQuote");
    }

}
