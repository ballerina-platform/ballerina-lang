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
package org.ballerinalang.siddhi.core.stream.event;

import org.ballerinalang.siddhi.core.aggregation.AggregationRuntime;
import org.ballerinalang.siddhi.core.config.SiddhiAppContext;
import org.ballerinalang.siddhi.core.config.SiddhiContext;
import org.ballerinalang.siddhi.core.event.Event;
import org.ballerinalang.siddhi.core.event.SiddhiEventFactory;
import org.ballerinalang.siddhi.core.event.state.MetaStateEvent;
import org.ballerinalang.siddhi.core.event.stream.MetaStreamEvent;
import org.ballerinalang.siddhi.core.event.stream.StreamEvent;
import org.ballerinalang.siddhi.core.event.stream.StreamEventFactory;
import org.ballerinalang.siddhi.core.event.stream.StreamEventPool;
import org.ballerinalang.siddhi.core.event.stream.converter.SelectiveStreamEventConverter;
import org.ballerinalang.siddhi.core.event.stream.converter.SimpleStreamEventConverter;
import org.ballerinalang.siddhi.core.event.stream.converter.StreamEventConverter;
import org.ballerinalang.siddhi.core.event.stream.converter.StreamEventConverterFactory;
import org.ballerinalang.siddhi.core.event.stream.converter.ZeroStreamEventConverter;
import org.ballerinalang.siddhi.core.exception.OperationNotSupportedException;
import org.ballerinalang.siddhi.core.executor.ConstantExpressionExecutor;
import org.ballerinalang.siddhi.core.executor.ExpressionExecutor;
import org.ballerinalang.siddhi.core.executor.VariableExpressionExecutor;
import org.ballerinalang.siddhi.core.executor.condition.AndConditionExpressionExecutor;
import org.ballerinalang.siddhi.core.executor.condition.compare.greaterthan.GreaterThanCompareConditionExpressionExecutorIntInt;
import org.ballerinalang.siddhi.core.executor.condition.compare.lessthan.LessThanCompareConditionExpressionExecutorFloatFloat;
import org.ballerinalang.siddhi.core.executor.math.add.AddExpressionExecutorFloat;
import org.ballerinalang.siddhi.core.query.QueryRuntime;
import org.ballerinalang.siddhi.core.query.input.stream.single.SingleStreamRuntime;
import org.ballerinalang.siddhi.core.stream.input.source.Source;
import org.ballerinalang.siddhi.core.stream.output.sink.Sink;
import org.ballerinalang.siddhi.core.table.Table;
import org.ballerinalang.siddhi.core.util.ElementIdGenerator;
import org.ballerinalang.siddhi.core.util.SiddhiConstants;
import org.ballerinalang.siddhi.core.util.lock.LockSynchronizer;
import org.ballerinalang.siddhi.core.util.parser.QueryParser;
import org.ballerinalang.siddhi.core.util.parser.helper.QueryParserHelper;
import org.ballerinalang.siddhi.core.util.snapshot.SnapshotService;
import org.ballerinalang.siddhi.core.window.Window;
import org.ballerinalang.siddhi.query.api.annotation.Annotation;
import org.ballerinalang.siddhi.query.api.definition.AbstractDefinition;
import org.ballerinalang.siddhi.query.api.definition.Attribute;
import org.ballerinalang.siddhi.query.api.definition.StreamDefinition;
import org.ballerinalang.siddhi.query.api.execution.query.Query;
import org.ballerinalang.siddhi.query.api.execution.query.input.stream.InputStream;
import org.ballerinalang.siddhi.query.api.execution.query.selection.Selector;
import org.ballerinalang.siddhi.query.api.expression.Expression;
import org.ballerinalang.siddhi.query.api.expression.condition.Compare;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Testcase to validate stream events.
 */
public class EventTestCase {

    @Test
    public void testEventCreation() {
        SiddhiEventFactory siddhiEventFactory = new SiddhiEventFactory(2);
        AssertJUnit.assertEquals(2, siddhiEventFactory.newInstance().getData().length);

        StreamEventFactory streamEventFactory = new StreamEventFactory(2, 3, 4);
        StreamEvent streamEvent = streamEventFactory.newInstance();
        AssertJUnit.assertEquals(2, streamEvent.getBeforeWindowData().length);
        AssertJUnit.assertEquals(3, streamEvent.getOnAfterWindowData().length);
        AssertJUnit.assertEquals(4, streamEvent.getOutputData().length);
    }

    @Test
    public void testEventPool() {
        StreamEventPool streamEventPool = new StreamEventPool(2, 3, 1, 4);

        StreamEvent[] streamEvents = new StreamEvent[5];
        for (int i = 0; i < 5; i++) {
            streamEvents[i] = streamEventPool.borrowEvent();
        }
        AssertJUnit.assertEquals(0, streamEventPool.getBufferedEventsSize());

        for (int i = 0; i < 5; i++) {
            streamEventPool.returnEvents(streamEvents[i]);
        }
        AssertJUnit.assertEquals(4, streamEventPool.getBufferedEventsSize());

        streamEventPool.borrowEvent();
        AssertJUnit.assertEquals(3, streamEventPool.getBufferedEventsSize());

    }

    @Test
    public void testPassThroughStreamEventConverter() {
        Attribute symbol = new Attribute("symbol", Attribute.Type.STRING);
        Attribute price = new Attribute("price", Attribute.Type.DOUBLE);
        Attribute volume = new Attribute("volume", Attribute.Type.INT);

        MetaStreamEvent metaStreamEvent = new MetaStreamEvent();
        metaStreamEvent.addOutputDataAllowingDuplicate(symbol);
        metaStreamEvent.addOutputDataAllowingDuplicate(price);
        metaStreamEvent.addOutputDataAllowingDuplicate(volume);


        StreamDefinition streamDefinition = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.DOUBLE).attribute("volume", Attribute.Type.INT);
        Event event = new Event(System.currentTimeMillis(), new Object[]{"WSO2", 200.0, 50});

        metaStreamEvent.addInputDefinition(streamDefinition);

        StreamEventConverter converter = StreamEventConverterFactory.constructEventConverter(metaStreamEvent);
        StreamEventPool eventPool = new StreamEventPool(metaStreamEvent, 5);

        StreamEvent borrowedEvent = eventPool.borrowEvent();
        converter.convertEvent(event, borrowedEvent);

        AssertJUnit.assertTrue(converter instanceof ZeroStreamEventConverter);
        AssertJUnit.assertEquals(3, borrowedEvent.getOutputData().length);

        AssertJUnit.assertEquals("WSO2", borrowedEvent.getOutputData()[0]);
        AssertJUnit.assertEquals(200.0, borrowedEvent.getOutputData()[1]);
        AssertJUnit.assertEquals(50, borrowedEvent.getOutputData()[2]);
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

        AssertJUnit.assertTrue(converter instanceof SimpleStreamEventConverter);
        AssertJUnit.assertNull(borrowedEvent.getBeforeWindowData());
        AssertJUnit.assertNull(borrowedEvent.getOnAfterWindowData());
        AssertJUnit.assertEquals(2, borrowedEvent.getOutputData().length);

        AssertJUnit.assertEquals(200, borrowedEvent.getOutputData()[1]);
        AssertJUnit.assertEquals("WSO2", borrowedEvent.getOutputData()[0]);
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

        AssertJUnit.assertTrue(converter instanceof SelectiveStreamEventConverter);
        AssertJUnit.assertEquals(1, borrowedEvent.getBeforeWindowData().length);      //volume
        AssertJUnit.assertEquals(1, borrowedEvent.getOnAfterWindowData().length);     //price
        AssertJUnit.assertEquals(2, borrowedEvent.getOutputData().length);            //symbol and avgPrice

        AssertJUnit.assertEquals(50, borrowedEvent.getBeforeWindowData()[0]);
        AssertJUnit.assertEquals(200, borrowedEvent.getOnAfterWindowData()[0]);
        AssertJUnit.assertEquals("WSO2", borrowedEvent.getOutputData()[0]);
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

        AssertJUnit.assertEquals("Result of adding should be 20.0", 20f, addExecutor.execute(event));
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

        AssertJUnit.assertEquals("Two events should pass through executor", 2, count);
    }

    @Test(expectedExceptions = OperationNotSupportedException.class)
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
        AssertJUnit.assertNotNull(runtime);
        AssertJUnit.assertTrue(runtime.getStreamRuntime() instanceof SingleStreamRuntime);
        AssertJUnit.assertNotNull(runtime.getSelector());
        AssertJUnit.assertTrue(runtime.getMetaComplexEvent() instanceof MetaStreamEvent);
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

        AssertJUnit.assertEquals(1, metaStreamEvent.getBeforeWindowData().size());
        AssertJUnit.assertEquals(1, metaStreamEvent.getOnAfterWindowData().size());
        AssertJUnit.assertEquals(2, metaStreamEvent.getOutputData().size());
        AssertJUnit.assertArrayEquals(new int[]{0, 0, 1, 0}, priceVariableExpressionExecutor.getPosition());
        AssertJUnit.assertArrayEquals(new int[]{0, 0, 0, 0}, volumeVariableExpressionExecutor.getPosition());
        AssertJUnit.assertArrayEquals(new int[]{0, 0, 2, 0}, symbolVariableExpressionExecutor.getPosition());
    }
}
