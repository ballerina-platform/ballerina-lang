/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.core.query;

import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.util.parser.SiddhiAppParser;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;

public class SimpleQueryValidatorTestCase {
    private SiddhiContext siddhiContext;

    @Before
    public void init() {
        siddhiContext = new SiddhiContext();
    }

    @Test(expected = SiddhiAppValidationException.class)
    public void testQueryWithNotExistingAttributes() throws InterruptedException {

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume long);";
        String query = "@info(name = 'query1') from cseEventStream[volume >= 50] select symbol1,price,volume insert " +
                "into outputStream ;";

        SiddhiAppParser.parse(SiddhiCompiler.parse(cseEventStream + query), siddhiContext);
    }

    @Test(expected = SiddhiAppValidationException.class)
    public void testQueryWithDuplicateDefinition() throws InterruptedException {
        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume long);";
        String duplicateStream = "define stream outputStream (symbol string, price float);";
        String query = "@info(name = 'query1') from cseEventStream[volume >= 50] select symbol,price,volume insert " +
                "into outputStream ;";

        SiddhiAppParser.parse(SiddhiCompiler.parse(cseEventStream + duplicateStream + query), siddhiContext);
    }

    @Test(expected = SiddhiAppValidationException.class)
    public void testInvalidFilterCondition1() throws InterruptedException {
        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume long);";
        String query = "@info(name = 'query1') from cseEventStream[volume >= 50 and volume] select symbol,price," +
                "volume insert into outputStream ;";

        SiddhiAppParser.parse(SiddhiCompiler.parse(cseEventStream + query), siddhiContext);
    }

    @Test(expected = SiddhiAppValidationException.class)
    public void testInvalidFilterCondition2() throws InterruptedException {
        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume long);";
        String query = "@info(name = 'query1') from cseEventStream[not(price)] select symbol,price,volume insert into" +
                " outputStream ;";

        SiddhiAppParser.parse(SiddhiCompiler.parse(cseEventStream + query), siddhiContext);

    }

    @Test
    public void testComplexFilterQuery1() throws InterruptedException {
        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume long, available " +
                "bool);";
        String query = "@info(name = 'query1') from cseEventStream[available] select symbol,price,volume insert into " +
                "outputStream ;";

        SiddhiAppParser.parse(SiddhiCompiler.parse(cseEventStream + query), siddhiContext);
    }

    @Test
    public void testComplexFilterQuery2() throws InterruptedException {
        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume long, available " +
                "bool);";
        String query = "@info(name = 'query1') from cseEventStream[available and price>50] select symbol,price,volume" +
                " insert into outputStream ;";

        SiddhiAppParser.parse(SiddhiCompiler.parse(cseEventStream + query), siddhiContext);
    }
}
