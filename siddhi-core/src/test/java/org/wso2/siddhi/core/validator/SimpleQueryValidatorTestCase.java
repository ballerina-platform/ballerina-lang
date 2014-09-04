/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR ExpressionS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.siddhi.core.validator;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.exception.ValidatorException;
import org.wso2.siddhi.core.util.validate.InStreamValidator;
import org.wso2.siddhi.core.util.validate.OutStreamValidator;
import org.wso2.siddhi.core.util.validate.QueryValidator;
import org.wso2.siddhi.core.util.validate.SelectorValidator;
import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.JoinInputStream;
import org.wso2.siddhi.query.api.execution.query.output.stream.OutputStream;
import org.wso2.siddhi.query.api.execution.query.selection.Selector;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.condition.Compare;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleQueryValidatorTestCase {
    static final Logger log = Logger.getLogger(SimpleQueryValidatorTestCase.class);
    private Map<String, StreamDefinition> definitionMap;
    private Map<String, String> renameMap;
    private List<StreamDefinition> streamDefinitionList;

    private ExecutionPlan executionPlan;
    private Query query;

    private StreamDefinition testDefinition1;
    private StreamDefinition testDefinition2;
    private StreamDefinition testDefinition3;

    @Before
    public void init() {
        testDefinition1 = StreamDefinition.id("StockStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.INT).attribute("volume", Attribute.Type.FLOAT);
        testDefinition2 = StreamDefinition.id("OutStockStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.INT);
        testDefinition3 = StreamDefinition.id("TwitterStream").attribute("symbol", Attribute.Type.STRING).attribute("wordCount", Attribute.Type.INT);

        definitionMap = new HashMap<String, StreamDefinition>(2);
        definitionMap.put(testDefinition1.getId(), testDefinition1);
        definitionMap.put(testDefinition2.getId(), testDefinition2);
        definitionMap.put(testDefinition3.getId(), testDefinition3);

        streamDefinitionList = new ArrayList<StreamDefinition>();
        streamDefinitionList.add(testDefinition1);
        streamDefinitionList.add(testDefinition2);
        renameMap = new HashMap<String, String>(2);
        renameMap.put(testDefinition1.getId(), testDefinition1.getId());
        renameMap.put(testDefinition2.getId(), testDefinition2.getId());
        renameMap.put(testDefinition3.getId(), testDefinition3.getId());

        query = new Query();

        query.
                annotation(Annotation.annotation("info").element("name", "Query1").element("summary", "Test Query")).
                from(
                        InputStream.joinStream(
                                InputStream.stream("StockStream").
                                        window("lengthBatch", Expression.value(50)),
                                JoinInputStream.Type.JOIN,
                                InputStream.stream("TwitterStream").
                                        filter(
                                                Expression.compare(
                                                        Expression.value(50),
                                                        Compare.Operator.GREATER_THAN,
                                                        Expression.variable("wordCount").ofStream("TwitterStream")
                                                )


                                        ).window("lengthBatch", Expression.value(50)),
                                Expression.compare(
                                        Expression.variable("symbol").ofStream("StockStream"),
                                        Compare.Operator.EQUAL,
                                        Expression.variable("symbol").ofStream("TwitterStream"))
                        )
                ).
                select(
                        Selector.selector().
                                select("symbol", Expression.variable("symbol").ofStream("StockStream")).
                                select("price", Expression.variable("price")).
                                groupBy(Expression.variable("symbol").ofStream("StockStream")).
                                having(
                                        Expression.compare(
                                                Expression.value(50),
                                                Compare.Operator.GREATER_THAN,
                                                Expression.variable("price"))
                                )
                ).
                insertInto("OutStockStream", OutputStream.OutputEventType.EXPIRED_EVENTS);
    }

    @Test
    public void streamDefinitionValidatorWithValidStreamTest() throws ValidatorException {
        StreamDefinition testDefinition = StreamDefinition.id("OutStockStream1").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT);
        OutStreamValidator.validate(definitionMap, testDefinition);
    }

    @Test(expected = ValidatorException.class)
    public void streamDefinitionValidatorWithInvalidStreamTest() throws ValidatorException {
        StreamDefinition testDefinition = StreamDefinition.id("OutStockStream").attribute("symbol", Attribute.Type.STRING).attribute("price1", Attribute.Type.FLOAT);
        OutStreamValidator.validate(definitionMap, testDefinition);
    }

    @Test
    public void streamDefinitionValidatorWithSameStreamTest() throws ValidatorException {
        StreamDefinition testDefinition = StreamDefinition.id("OutStockStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.INT);
        OutStreamValidator.validate(definitionMap, testDefinition);
    }

    @Test
    public void inStreamValidatorTest() throws ValidatorException {
        Map<String, StreamDefinition> sampleRenameMap = new HashMap<String, StreamDefinition>();
        InStreamValidator.validate(query.getInputStream(), definitionMap, sampleRenameMap);
    }

    @Test
    public void SelectorValidatorTest() throws ValidatorException {
        Map<String, StreamDefinition> sampleRenameMap = new HashMap<String, StreamDefinition>();
        sampleRenameMap.put(testDefinition1.getId(), testDefinition1);
        sampleRenameMap.put(testDefinition3.getId(), testDefinition3);
        SelectorValidator.validate(query.getSelector(), sampleRenameMap);
        query.getSelector();
    }

    @Test(expected = ValidatorException.class)
    public void SelectorValidatorInvalidTest() throws ValidatorException {
        Selector selector = Selector.selector().
                select("symbol", Expression.variable("symbol1")).
                select("price", Expression.variable("price"));
        Map<String, StreamDefinition> sampleRenameMap = new HashMap<String, StreamDefinition>();
        sampleRenameMap.put("testDef", testDefinition1);
        SelectorValidator.validate(selector, sampleRenameMap);
    }

    @Test
    public void QueryValidatorTestCase() throws ValidatorException {
        QueryValidator.validate(query, definitionMap);
    }

    @Test(expected = ValidatorException.class)
    public void QueryValidatorWithInvalidQueryTestCase() throws ValidatorException {
        query.select(
                Selector.selector().
                        select("symbol_dummy", Expression.variable("symbol_dummy").ofStream("StockStream"))
        );
        QueryValidator.validate(query, definitionMap);
    }
}
