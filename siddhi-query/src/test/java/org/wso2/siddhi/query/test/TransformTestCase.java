package org.wso2.siddhi.query.test;

import org.antlr.runtime.RecognitionException;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.siddhi.query.api.query.Query;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

public class TransformTestCase {

    @Test
    public void Test() throws RecognitionException, SiddhiParserException {
        Query query = SiddhiCompiler.parseQuery("from  cseEventStream#transform.avgStream(price) " +
                                                "insert into StockQuote ;"

        );
        Assert.assertNotNull(query);
    }

    @Test
    public void Test1() throws RecognitionException, SiddhiParserException {
        Query query = SiddhiCompiler.parseQuery("from  cseEventStream#transform.exthj:avgStream(price) " +
                                                "insert into StockQuote ;"

        );
        Assert.assertNotNull(query);
    }


}


