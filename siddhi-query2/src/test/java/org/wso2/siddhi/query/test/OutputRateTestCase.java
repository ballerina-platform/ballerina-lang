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
package org.wso2.siddhi.query.test;

import org.antlr.runtime.RecognitionException;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.siddhi.query.api.query.Query;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

public class OutputRateTestCase {

    @Test
    public void Test() throws RecognitionException, SiddhiParserException {
        Query query = SiddhiCompiler.parseQuery("from  cseEventStream#window.lenghtBatch(50) " +
                                                "select symbol, avg(price) as avgPrice " +
                                                "group by symbol " +
                                                "having (price >= 20)" +
                                                "output every 5 sec  " +
                                                "insert into StockQuote for all-events ; "
        );
        Assert.assertNotNull(query);

    }

//    @Test
//    public void Test1() throws RecognitionException, SiddhiParserException {
//        Query query = SiddhiCompiler.parseQuery("from  cseEventStream#window.lenghtBatch(50) " +
//                                                "select symbol, avg(price) as avgPrice " +
//                                                "group by symbol " +
//                                                "having (price >= 20)" +
//                                                "output all every 5 events " +
//                                                "order by price" +
//                                                "limit 10 " +
//                                                "insert into StockQuote for all-events ; "
//        );
//        Assert.assertNotNull(query);
//
//    }
//
    @Test
    public void Test2() throws RecognitionException, SiddhiParserException {
        Query query = SiddhiCompiler.parseQuery("from  cseEventStream#window.lenghtBatch(50) " +
                                                "select symbol, avg(price) as avgPrice " +
                                                "group by symbol " +
                                                "having (price >= 20)" +
                                                "output all every 5 events " +
                                                "insert into StockQuote for all-events ; "
        );
        Assert.assertNotNull(query);

    }
}
