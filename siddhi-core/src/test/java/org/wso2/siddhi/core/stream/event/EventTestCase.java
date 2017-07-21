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
package org.wso2.siddhi.core.stream.event;

import org.junit.Assert;
import org.junit.Test;
import org.wso2.siddhi.core.aggregation.AggregationRuntime;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.SiddhiEventFactory;
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventFactory;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.event.stream.converter.SelectiveStreamEventConverter;
import org.wso2.siddhi.core.event.stream.converter.SimpleStreamEventConverter;
import org.wso2.siddhi.core.event.stream.converter.StreamEventConverter;
import org.wso2.siddhi.core.event.stream.converter.StreamEventConverterFactory;
import org.wso2.siddhi.core.event.stream.converter.ZeroStreamEventConverter;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.executor.condition.AndConditionExpressionExecutor;
import org.wso2.siddhi.core.executor.condition.compare.greaterthan.GreaterThanCompareConditionExpressionExecutorIntInt;
import org.wso2.siddhi.core.executor.condition.compare.lessthan.LessThanCompareConditionExpressionExecutorFloatFloat;
import org.wso2.siddhi.core.executor.math.add.AddExpressionExecutorFloat;
import org.wso2.siddhi.core.query.QueryRuntime;
import org.wso2.siddhi.core.query.input.stream.single.SingleStreamRuntime;
import org.wso2.siddhi.core.stream.input.source.Source;
import org.wso2.siddhi.core.stream.output.sink.Sink;
import org.wso2.siddhi.core.table.Table;
import org.wso2.siddhi.core.util.ElementIdGenerator;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.core.util.lock.LockSynchronizer;
import org.wso2.siddhi.core.util.parser.QueryParser;
import org.wso2.siddhi.core.util.parser.helper.QueryParserHelper;
import org.wso2.siddhi.core.util.snapshot.SnapshotService;
import org.wso2.siddhi.core.window.Window;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.api.execution.query.selection.Selector;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.condition.Compare;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventTestCase {

    @Test
    public void testEventCreation() {
        SiddhiEventFactory siddhiEventFactory = new SiddhiEventFactory(2);
        Assert.assertEquals(2, siddhiEventFactory.newInstance().getData().length);

        StreamEventFactory streamEventFactory = new StreamEventFactory(2, 3, 4);
        StreamEvent streamEvent = streamEventFactory.newInstance();
        Assert.assertEquals(2, streamEvent.getBeforeWindowData().length);
        Assert.assertEquals(3, streamEvent.getOnAfterWindowData().length);
        Assert.assertEquals(4, streamEvent.getOutputData().length);
    }

    @Test
    public void testEventPool() {
        StreamEventPool streamEventPool = new StreamEventPool(2, 3, 1, 4);

        StreamEvent[] streamEvents = new StreamEvent[5];
        for (int i = 0; i < 5; i++) {
            streamEvents[i] = streamEventPool.borrowEvent();
        }
        Assert.assertEquals(0, streamEventPool.getBufferedEventsSize());

        for (int i = 0; i < 5; i++) {
            streamEventPool.returnEvents(streamEvents[i]);
        }
        Assert.assertEquals(4, streamEventPool.getBufferedEventsSize());

        streamEventPool.borrowEvent();
        Assert.assertEquals(3, streamEventPool.getBufferedEventsSize());

    }

    @Test
    public void testPassThroughStreamEventConverter() {
        Attribute symbol = new Attribute("symbol", Attribute.Type.STRING);
        Attribute price = new Attribute("price", Attribute.Type.DOUBLE);
        Attribute volume = new Attribute("volume", Attribute.Type.INT);

        MetaStreamEvent metaStreamEvent = new MetaStreamEvent();
        metaStreamEvent.addOutputData(symbol);
        metaStreamEvent.addOutputData(price);
        metaStreamEvent.addOutputData(volume);


        StreamDefinition streamDefinition = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.DOUBLE).attribute("volume", Attribute.Type.INT);
        Event event = new Event(System.currentTimeMillis(), new Object[]{"WSO2", 200.0, 50});

        metaStreamEvent.addInputDefinition(streamDefinition);

        StreamEventConverter converter = StreamEventConverterFactory.constructEventConverter(metaStreamEvent);
        StreamEventPool eventPool = new StreamEventPool(metaStreamEvent, 5);

        StreamEvent borrowedEvent = eventPool.borrowEvent();
        converter.convertEvent(event, borrowedEvent);

        Assert.assertTrue(converter instanceof ZeroStreamEventConverter);
        Assert.assertEquals(3, borrowedEvent.getOutputData().length);

        Assert.assertEquals("WSO2", borrowedEvent.getOutputData()[0]);
        Assert.assertEquals(200.0, borrowedEvent.getOutputData()[1]);
        Assert.assertEquals(50, borrowedEvent.getOutputData()[2]);
    }

    @Test
    public void testSimpleStreamEventConverter() {
        Attribute price = new Attribute("price", Attribute.Type.DOUBLE);
        Attribute symbol = new Attribute("symbol", Attribute.Type.STRING);

        MetaStreamEvent metaStreamEvent = new MetaStreamEvent();
        metaStreamEvent.addOutputData(symbol);
        metaStreamEvent.addOutputData(price);

        StreamDefinition streamDefinition = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.DOUBLE).attribute("volume", Attribute.Type.INT);
        Event event = new Event(System.currentTimeMillis(), new Object[]{"WSO2", 200, 50});

        metaStreamEvent.addInputDefinition(streamDefinition);
        StreamEventConverter converter = StreamEventConverterFactory.constructEventConverter(metaStreamEvent);
        StreamEventPool eventPool = new StreamEventPool(metaStreamEvent, 5);
        StreamEvent borrowedEvent = eventPool.borrowEvent();
        converter.convertEvent(event, borrowedEvent);

        Assert.assertTrue(converter instanceof SimpleStreamEventConverter);
        Assert.assertNull(borrowedEvent.getBeforeWindowData());
        Assert.assertNull(borrowedEvent.getOnAfterWindowData());
        Assert.assertEquals(2, borrowedEvent.getOutputData().length);

        Assert.assertEquals(200, borrowedEvent.getOutputData()[1]);
        Assert.assertEquals("WSO2", borrowedEvent.getOutputData()[0]);
    }

    @Test
    public void testStreamEventConverter() {
        Attribute price = new Attribute("price", Attribute.Type.DOUBLE);
        Attribute volume = new Attribute("volume", Attribute.Type.INT);
        Attribute symbol = new Attribute("symbol", Attribute.Type.STRING);

        MetaStreamEvent metaStreamEvent = new MetaStreamEvent();
        metaStreamEvent.addData(volume);
        metaStreamEvent.initializeAfterWindowData();
        metaStreamEvent.addData(price);
        metaStreamEvent.addOutputData(symbol);
        metaStreamEvent.addOutputData(null);        //complex attribute

        StreamDefinition streamDefinition = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.DOUBLE).attribute("volume", Attribute.Type.INT);
        Event event = new Event(System.currentTimeMillis(), new Object[]{"WSO2", 200, 50});

        metaStreamEvent.addInputDefinition(streamDefinition);
        StreamEventConverter converter = StreamEventConverterFactory.constructEventConverter(metaStreamEvent);
        StreamEventPool eventPool = new StreamEventPool(metaStreamEvent, 5);

        StreamEvent borrowedEvent = eventPool.borrowEvent();
        converter.convertEvent(event, borrowedEvent);

        Assert.assertTrue(converter instanceof SelectiveStreamEventConverter);
        Assert.assertEquals(1, borrowedEvent.getBeforeWindowData().length);      //volume
        Assert.assertEquals(1, borrowedEvent.getOnAfterWindowData().length);     //price
        Assert.assertEquals(2, borrowedEvent.getOutputData().length);            //symbol and avgPrice

        Assert.assertEquals(50, borrowedEvent.getBeforeWindowData()[0]);
        Assert.assertEquals(200, borrowedEvent.getOnAfterWindowData()[0]);
        Assert.assertEquals("WSO2", borrowedEvent.getOutputData()[0]);
    }

    @Test
    public void testExpressionExecutors() {
//        StreamDefinition streamDefinition = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute
// .Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT);

        VariableExpressionExecutor priceVariableExpressionExecutor = new VariableExpressionExecutor(new Attribute
                ("price", Attribute.Type.FLOAT), 0, 0);
        priceVariableExpressionExecutor.setPosition(new int[]{0, SiddhiConstants.UNKNOWN_STATE, SiddhiConstants
                .OUTPUT_DATA_INDEX, 1});

        ExpressionExecutor addExecutor = new AddExpressionExecutorFloat(new ConstantExpressionExecutor(10f, Attribute
                .Type.FLOAT), priceVariableExpressionExecutor);

        StreamEvent event = new StreamEvent(0, 0, 3);
        event.setOutputData(new Object[]{"WSO2", 10f, 5});

        Assert.assertEquals("Result of adding should be 20.0", 20f, addExecutor.execute(event));
    }

    @Test
    public void testConditionExpressionExecutors() {
//        StreamDefinition streamDefinition = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute
// .Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT);

        VariableExpressionExecutor priceVariableExpressionExecutor = new VariableExpressionExecutor(new Attribute
                ("price", Attribute.Type.FLOAT), 0, 0);
        priceVariableExpressionExecutor.setPosition(new int[]{0, SiddhiConstants.UNKNOWN_STATE, SiddhiConstants
                .OUTPUT_DATA_INDEX, 1});

        VariableExpressionExecutor volumeVariableExpressionExecutor = new VariableExpressionExecutor(new Attribute
                ("volume", Attribute.Type.INT), 0, 0);
        volumeVariableExpressionExecutor.setPosition(new int[]{0, SiddhiConstants.UNKNOWN_STATE, SiddhiConstants
                .OUTPUT_DATA_INDEX, 2});

        ExpressionExecutor compareLessThanExecutor = new LessThanCompareConditionExpressionExecutorFloatFloat(new
                ConstantExpressionExecutor(10f, Attribute.Type.FLOAT), priceVariableExpressionExecutor);
        ExpressionExecutor compareGreaterThanExecutor = new GreaterThanCompareConditionExpressionExecutorIntInt(new
                ConstantExpressionExecutor(10, Attribute.Type.INT), volumeVariableExpressionExecutor);
        ExpressionExecutor andExecutor = new AndConditionExpressionExecutor(compareLessThanExecutor,
                compareGreaterThanExecutor);

        int count = 0;
        for (int i = 0; i < 3; i++) {
            StreamEvent event = new StreamEvent(0, 0, 3);
            event.setOutputData(new Object[]{"WSO2", i * 11f, 5});
            if ((Boolean) andExecutor.execute(event)) {
                count++;
            }
        }

        Assert.assertEquals("Two events should pass through executor", 2, count);
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testConditionExpressionExecutorValidation() {
//        StreamDefinition streamDefinition = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute
// .Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT);

        VariableExpressionExecutor volumeVariableExpressionExecutor = new VariableExpressionExecutor(new Attribute
                ("volume", Attribute.Type.INT), 0, 0);
        volumeVariableExpressionExecutor.setPosition(new int[]{0, SiddhiConstants.UNKNOWN_STATE, SiddhiConstants
                .OUTPUT_DATA_INDEX, 2});

        ConstantExpressionExecutor constantExpressionExecutor = new ConstantExpressionExecutor(10f, Attribute.Type
                .FLOAT);
        ExpressionExecutor compareGreaterThanExecutor = new GreaterThanCompareConditionExpressionExecutorIntInt(new
                ConstantExpressionExecutor(10, Attribute.Type.INT), volumeVariableExpressionExecutor);
        ExpressionExecutor andExecutor = new AndConditionExpressionExecutor(constantExpressionExecutor,
                compareGreaterThanExecutor);
    }

    @Test
    public void testQueryParser() {
        StreamDefinition streamDefinition = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT);
        StreamDefinition outStreamDefinition = StreamDefinition.id("outputStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.FLOAT);
        Query query = new Query();
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"),
                Compare.Operator.NOT_EQUAL, Expression.value(50))));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression
                .variable("price")));
        query.insertInto("outputStream");
        Map<String, AbstractDefinition> tableDefinitionMap = new HashMap<>();
        Map<String, AbstractDefinition> windowDefinitionMap = new HashMap<>();
        Map<String, AbstractDefinition> aggregationDefinitionMap = new HashMap<>();
        Map<String, Table> tableMap = new HashMap<String, Table>();
        Map<String, Window> eventWindowMap = new HashMap<String, Window>();
        Map<String, AggregationRuntime> aggregationMap = new HashMap<String, AggregationRuntime>();
        Map<String, List<Source>> eventSourceMap = new HashMap<String, List<Source>>();
        Map<String, List<Sink>> eventSinkMap = new HashMap<String, List<Sink>>();
        Map<String, AbstractDefinition> streamDefinitionMap = new HashMap<String, AbstractDefinition>();
        LockSynchronizer lockSynchronizer = new LockSynchronizer();
        streamDefinitionMap.put("cseEventStream", streamDefinition);
        streamDefinitionMap.put("outputStream", outStreamDefinition);
        SiddhiContext siddhicontext = new SiddhiContext();
        SiddhiAppContext context = new SiddhiAppContext();
        context.setSiddhiContext(siddhicontext);
        context.setElementIdGenerator(new ElementIdGenerator(context.getName()));
        context.setSnapshotService(new SnapshotService(context));
        QueryRuntime runtime = QueryParser.parse(query, context, streamDefinitionMap, tableDefinitionMap,
                windowDefinitionMap, aggregationDefinitionMap, tableMap, aggregationMap, eventWindowMap,
                lockSynchronizer, "1");
        Assert.assertNotNull(runtime);
        Assert.assertTrue(runtime.getStreamRuntime() instanceof SingleStreamRuntime);
        Assert.assertNotNull(runtime.getSelector());
        Assert.assertTrue(runtime.getMetaComplexEvent() instanceof MetaStreamEvent);
    }

    @Test
    public void testUpdateMetaEvent() {
        StreamDefinition streamDefinition = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT);

        Attribute price = new Attribute("price", Attribute.Type.FLOAT);
        Attribute volume = new Attribute("volume", Attribute.Type.INT);
        Attribute symbol = new Attribute("symbol", Attribute.Type.STRING);

        MetaStreamEvent metaStreamEvent = new MetaStreamEvent();
        metaStreamEvent.addData(volume);
        metaStreamEvent.addData(price);
        metaStreamEvent.addData(symbol);
        metaStreamEvent.initializeAfterWindowData();
        metaStreamEvent.addData(price);
        metaStreamEvent.addOutputData(symbol);
        metaStreamEvent.addOutputData(null);
        MetaStateEvent metaStateEvent = new MetaStateEvent(1);
        metaStateEvent.addEvent(metaStreamEvent);

        VariableExpressionExecutor priceVariableExpressionExecutor = new VariableExpressionExecutor(new Attribute
                ("price", Attribute.Type.FLOAT), 0, 0);
        VariableExpressionExecutor volumeVariableExpressionExecutor = new VariableExpressionExecutor(new Attribute
                ("volume", Attribute.Type.INT), 0, 0);
        VariableExpressionExecutor symbolVariableExpressionExecutor = new VariableExpressionExecutor(new Attribute
                ("symbol", Attribute.Type.STRING), 0, 0);

        QueryParserHelper.reduceMetaComplexEvent(metaStateEvent);
        QueryParserHelper.updateVariablePosition(metaStateEvent, Arrays.asList(priceVariableExpressionExecutor,
                volumeVariableExpressionExecutor, symbolVariableExpressionExecutor));

        Assert.assertEquals(1, metaStreamEvent.getBeforeWindowData().size());
        Assert.assertEquals(1, metaStreamEvent.getOnAfterWindowData().size());
        Assert.assertEquals(2, metaStreamEvent.getOutputData().size());
        Assert.assertArrayEquals(new int[]{0, 0, 1, 0}, priceVariableExpressionExecutor.getPosition());
        Assert.assertArrayEquals(new int[]{0, 0, 0, 0}, volumeVariableExpressionExecutor.getPosition());
        Assert.assertArrayEquals(new int[]{0, 0, 2, 0}, symbolVariableExpressionExecutor.getPosition());
    }
}
