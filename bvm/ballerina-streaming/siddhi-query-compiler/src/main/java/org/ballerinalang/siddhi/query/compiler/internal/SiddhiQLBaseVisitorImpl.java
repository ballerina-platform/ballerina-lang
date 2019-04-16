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
package org.ballerinalang.siddhi.query.compiler.internal;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import org.ballerinalang.siddhi.query.api.SiddhiApp;
import org.ballerinalang.siddhi.query.api.SiddhiElement;
import org.ballerinalang.siddhi.query.api.aggregation.TimePeriod;
import org.ballerinalang.siddhi.query.api.aggregation.Within;
import org.ballerinalang.siddhi.query.api.annotation.Annotation;
import org.ballerinalang.siddhi.query.api.annotation.Element;
import org.ballerinalang.siddhi.query.api.definition.AggregationDefinition;
import org.ballerinalang.siddhi.query.api.definition.Attribute;
import org.ballerinalang.siddhi.query.api.definition.FunctionDefinition;
import org.ballerinalang.siddhi.query.api.definition.StreamDefinition;
import org.ballerinalang.siddhi.query.api.definition.TableDefinition;
import org.ballerinalang.siddhi.query.api.definition.TriggerDefinition;
import org.ballerinalang.siddhi.query.api.definition.WindowDefinition;
import org.ballerinalang.siddhi.query.api.execution.ExecutionElement;
import org.ballerinalang.siddhi.query.api.execution.partition.Partition;
import org.ballerinalang.siddhi.query.api.execution.partition.PartitionType;
import org.ballerinalang.siddhi.query.api.execution.partition.RangePartitionType;
import org.ballerinalang.siddhi.query.api.execution.partition.ValuePartitionType;
import org.ballerinalang.siddhi.query.api.execution.query.Query;
import org.ballerinalang.siddhi.query.api.execution.query.StoreQuery;
import org.ballerinalang.siddhi.query.api.execution.query.input.handler.Filter;
import org.ballerinalang.siddhi.query.api.execution.query.input.handler.StreamFunction;
import org.ballerinalang.siddhi.query.api.execution.query.input.handler.StreamHandler;
import org.ballerinalang.siddhi.query.api.execution.query.input.handler.Window;
import org.ballerinalang.siddhi.query.api.execution.query.input.state.AbsentStreamStateElement;
import org.ballerinalang.siddhi.query.api.execution.query.input.state.CountStateElement;
import org.ballerinalang.siddhi.query.api.execution.query.input.state.EveryStateElement;
import org.ballerinalang.siddhi.query.api.execution.query.input.state.NextStateElement;
import org.ballerinalang.siddhi.query.api.execution.query.input.state.State;
import org.ballerinalang.siddhi.query.api.execution.query.input.state.StateElement;
import org.ballerinalang.siddhi.query.api.execution.query.input.state.StreamStateElement;
import org.ballerinalang.siddhi.query.api.execution.query.input.store.InputStore;
import org.ballerinalang.siddhi.query.api.execution.query.input.store.Store;
import org.ballerinalang.siddhi.query.api.execution.query.input.stream.AnonymousInputStream;
import org.ballerinalang.siddhi.query.api.execution.query.input.stream.BasicSingleInputStream;
import org.ballerinalang.siddhi.query.api.execution.query.input.stream.InputStream;
import org.ballerinalang.siddhi.query.api.execution.query.input.stream.JoinInputStream;
import org.ballerinalang.siddhi.query.api.execution.query.input.stream.SingleInputStream;
import org.ballerinalang.siddhi.query.api.execution.query.input.stream.StateInputStream;
import org.ballerinalang.siddhi.query.api.execution.query.output.ratelimit.EventOutputRate;
import org.ballerinalang.siddhi.query.api.execution.query.output.ratelimit.OutputRate;
import org.ballerinalang.siddhi.query.api.execution.query.output.ratelimit.SnapshotOutputRate;
import org.ballerinalang.siddhi.query.api.execution.query.output.ratelimit.TimeOutputRate;
import org.ballerinalang.siddhi.query.api.execution.query.output.stream.DeleteStream;
import org.ballerinalang.siddhi.query.api.execution.query.output.stream.InsertIntoStream;
import org.ballerinalang.siddhi.query.api.execution.query.output.stream.OutputStream;
import org.ballerinalang.siddhi.query.api.execution.query.output.stream.ReturnStream;
import org.ballerinalang.siddhi.query.api.execution.query.output.stream.UpdateOrInsertStream;
import org.ballerinalang.siddhi.query.api.execution.query.output.stream.UpdateSet;
import org.ballerinalang.siddhi.query.api.execution.query.output.stream.UpdateStream;
import org.ballerinalang.siddhi.query.api.execution.query.selection.BasicSelector;
import org.ballerinalang.siddhi.query.api.execution.query.selection.OrderByAttribute;
import org.ballerinalang.siddhi.query.api.execution.query.selection.OutputAttribute;
import org.ballerinalang.siddhi.query.api.execution.query.selection.Selector;
import org.ballerinalang.siddhi.query.api.expression.AttributeFunction;
import org.ballerinalang.siddhi.query.api.expression.Expression;
import org.ballerinalang.siddhi.query.api.expression.Variable;
import org.ballerinalang.siddhi.query.api.expression.condition.Compare;
import org.ballerinalang.siddhi.query.api.expression.constant.BoolConstant;
import org.ballerinalang.siddhi.query.api.expression.constant.Constant;
import org.ballerinalang.siddhi.query.api.expression.constant.DoubleConstant;
import org.ballerinalang.siddhi.query.api.expression.constant.FloatConstant;
import org.ballerinalang.siddhi.query.api.expression.constant.IntConstant;
import org.ballerinalang.siddhi.query.api.expression.constant.LongConstant;
import org.ballerinalang.siddhi.query.api.expression.constant.StringConstant;
import org.ballerinalang.siddhi.query.api.expression.constant.TimeConstant;
import org.ballerinalang.siddhi.query.api.util.SiddhiConstants;
import org.ballerinalang.siddhi.query.compiler.SiddhiQLBaseVisitor;
import org.ballerinalang.siddhi.query.compiler.SiddhiQLParser;
import org.ballerinalang.siddhi.query.compiler.exception.SiddhiParserException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Siddhi Query visitor implementation converting query to Siddhi Query Objects.
 */

public class SiddhiQLBaseVisitorImpl extends SiddhiQLBaseVisitor {

    private Set<String> activeStreams = new HashSet<String>();

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Object visitParse(@NotNull SiddhiQLParser.ParseContext ctx) {
        return visit(ctx.siddhi_app());
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public SiddhiApp visitSiddhi_app(@NotNull SiddhiQLParser.Siddhi_appContext ctx) {
        SiddhiApp siddhiApp = SiddhiApp.siddhiApp();
        for (SiddhiQLParser.App_annotationContext annotationContext : ctx.app_annotation()) {
            siddhiApp.annotation((Annotation) visit(annotationContext));
        }
        for (SiddhiQLParser.Definition_streamContext streamContext : ctx.definition_stream()) {
            siddhiApp.defineStream((StreamDefinition) visit(streamContext));
        }
        for (SiddhiQLParser.Definition_tableContext tableContext : ctx.definition_table()) {
            siddhiApp.defineTable((TableDefinition) visit(tableContext));
        }
        for (SiddhiQLParser.Definition_functionContext functionContext : ctx.definition_function()) {
            siddhiApp.defineFunction((FunctionDefinition) visit(functionContext));
        }
        for (SiddhiQLParser.Definition_windowContext windowContext : ctx.definition_window()) {
            siddhiApp.defineWindow((WindowDefinition) visit(windowContext));
        }
        for (SiddhiQLParser.Definition_aggregationContext aggregationContext : ctx.definition_aggregation()) {
            siddhiApp.defineAggregation((AggregationDefinition) visit(aggregationContext));
        }
        for (SiddhiQLParser.Execution_elementContext executionElementContext : ctx.execution_element()) {
            ExecutionElement executionElement = (ExecutionElement) visit(executionElementContext);
            if (executionElement instanceof Partition) {
                siddhiApp.addPartition((Partition) executionElement);

            } else if (executionElement instanceof Query) {
                siddhiApp.addQuery((Query) executionElement);

            } else {
                throw newSiddhiParserException(ctx);
            }
        }
        for (SiddhiQLParser.Definition_triggerContext triggerContext : ctx.definition_trigger()) {
            siddhiApp.defineTrigger((TriggerDefinition) visit(triggerContext));
        }
        populateQueryContext(siddhiApp, ctx);
        return siddhiApp;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Object visitDefinition_stream_final(@NotNull SiddhiQLParser.Definition_stream_finalContext ctx) {
        return visit(ctx.definition_stream());
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public StreamDefinition visitDefinition_stream(@NotNull SiddhiQLParser.Definition_streamContext ctx) {
        Source source = (Source) visit(ctx.source());
        if (source.isInnerStream) {
            throw newSiddhiParserException(ctx, " InnerStreams cannot be defined!");
        }
        try {
            StreamDefinition streamDefinition = StreamDefinition.id(source.streamId);
            populateQueryContext(streamDefinition, ctx);
            List<SiddhiQLParser.Attribute_nameContext> attribute_names = ctx.attribute_name();
            List<SiddhiQLParser.Attribute_typeContext> attribute_types = ctx.attribute_type();
            for (int i = 0; i < attribute_names.size(); i++) {
                SiddhiQLParser.Attribute_nameContext attributeNameContext = attribute_names.get(i);
                SiddhiQLParser.Attribute_typeContext attributeTypeContext = attribute_types.get(i);
                streamDefinition.attribute((String) visit(attributeNameContext), (Attribute.Type) visit
                        (attributeTypeContext));

            }
            for (SiddhiQLParser.AnnotationContext annotationContext : ctx.annotation()) {
                streamDefinition.annotation((Annotation) visit(annotationContext));
            }
            return streamDefinition;
        } catch (Throwable t) {
            throw newSiddhiParserException(ctx, t.getMessage(), t);
        }
    }

    @Override
    public Object visitDefinition_function_final(@NotNull SiddhiQLParser.Definition_function_finalContext ctx) {
        return visit(ctx.definition_function());
    }

    @Override
    public FunctionDefinition visitDefinition_function(@NotNull SiddhiQLParser.Definition_functionContext ctx) {
        String functionName = (String) visitFunction_name(ctx.function_name());
        String languageName = (String) visitLanguage_name(ctx.language_name());
        Attribute.Type attributeType = (Attribute.Type) visit(ctx.attribute_type());
        String functionBody = (String) visitFunction_body(ctx.function_body());

        FunctionDefinition functionDefinition = new FunctionDefinition();
        functionDefinition.id(functionName).language(languageName).
                type(attributeType).body(functionBody);
        populateQueryContext(functionDefinition, ctx);
        return functionDefinition;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Object visitFunction_name(@NotNull SiddhiQLParser.Function_nameContext ctx) {
        return visitId(ctx.id());
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Object visitFunction_body(@NotNull SiddhiQLParser.Function_bodyContext ctx) {
        String bodyBlock = ctx.SCRIPT().getText();
        return bodyBlock.substring(1, bodyBlock.length() - 2);
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Object visitLanguage_name(@NotNull SiddhiQLParser.Language_nameContext ctx) {
        return visitId(ctx.id());
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Object visitDefinition_trigger_final(@NotNull SiddhiQLParser.Definition_trigger_finalContext ctx) {
        return visitDefinition_trigger(ctx.definition_trigger());
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Object visitDefinition_trigger(@NotNull SiddhiQLParser.Definition_triggerContext ctx) {
        TriggerDefinition triggerDefinition = TriggerDefinition.id((String) visitTrigger_name(ctx.trigger_name()));
        if (ctx.time_value() != null) {
            triggerDefinition.atEvery(visitTime_value(ctx.time_value()).value());
        } else {
            triggerDefinition.at(visitString_value(ctx.string_value()).getValue());
        }
        populateQueryContext(triggerDefinition, ctx);
        return triggerDefinition;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Object visitTrigger_name(@NotNull SiddhiQLParser.Trigger_nameContext ctx) {
        return visitId(ctx.id());
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Object visitDefinition_table_final(@NotNull SiddhiQLParser.Definition_table_finalContext ctx) {
        return visit(ctx.definition_table());
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public TableDefinition visitDefinition_table(@NotNull SiddhiQLParser.Definition_tableContext ctx) {

//        definition_table
//        : annotation* DEFINE TABLE source '(' attribute_name attribute_type (',' attribute_name attribute_type )*
// ')' definition_store?
//        ;

        Source source = (Source) visit(ctx.source());
        if (source.isInnerStream) {
            throw newSiddhiParserException(ctx, "'#' cannot be used, because Tables can't be defined as " +
                    "InnerStream!");
        }
        TableDefinition tableDefinition = TableDefinition.id(source.streamId);
        List<SiddhiQLParser.Attribute_nameContext> attribute_names = ctx.attribute_name();
        List<SiddhiQLParser.Attribute_typeContext> attribute_types = ctx.attribute_type();
        for (int i = 0; i < attribute_names.size(); i++) {
            SiddhiQLParser.Attribute_nameContext attributeNameContext = attribute_names.get(i);
            SiddhiQLParser.Attribute_typeContext attributeTypeContext = attribute_types.get(i);
            tableDefinition.attribute((String) visit(attributeNameContext), (Attribute.Type) visit
                    (attributeTypeContext));

        }
        for (SiddhiQLParser.AnnotationContext annotationContext : ctx.annotation()) {
            tableDefinition.annotation((Annotation) visit(annotationContext));
        }
        populateQueryContext(tableDefinition, ctx);
        return tableDefinition;

    }

    @Override
    public Object visitDefinition_window_final(@NotNull SiddhiQLParser.Definition_window_finalContext ctx) {
        return visit(ctx.definition_window());
    }

    @Override
    public Object visitDefinition_window(@NotNull SiddhiQLParser.Definition_windowContext ctx) {
        Source source = (Source) visit(ctx.source());
        if (source.isInnerStream) {
            throw newSiddhiParserException(ctx, "'#' cannot be used, because Windows can't be defined as InnerStream!");
        }
        WindowDefinition windowDefinition = WindowDefinition.id(source.streamId);
        List<SiddhiQLParser.Attribute_nameContext> attribute_names = ctx.attribute_name();
        List<SiddhiQLParser.Attribute_typeContext> attribute_types = ctx.attribute_type();
        for (int i = 0; i < attribute_names.size(); i++) {
            SiddhiQLParser.Attribute_nameContext attributeNameContext = attribute_names.get(i);
            SiddhiQLParser.Attribute_typeContext attributeTypeContext = attribute_types.get(i);
            windowDefinition.attribute((String) visit(attributeNameContext), (Attribute.Type) visit
                    (attributeTypeContext));

        }
        for (SiddhiQLParser.AnnotationContext annotationContext : ctx.annotation()) {
            windowDefinition.annotation((Annotation) visit(annotationContext));
        }
        AttributeFunction attributeFunction = (AttributeFunction) visit(ctx.function_operation());
        Window window = new Window(attributeFunction.getNamespace(), attributeFunction.getName(), attributeFunction
                .getParameters());
        windowDefinition.window(window);

        // Optional output event type
        if (ctx.output_event_type() != null) {
            windowDefinition.setOutputEventType((OutputStream.OutputEventType) visit(ctx.output_event_type()));
        }
        populateQueryContext(windowDefinition, ctx);
        return windowDefinition;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Object visitPartition_final(@NotNull SiddhiQLParser.Partition_finalContext ctx) {
        return visit(ctx.partition());
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Partition visitPartition(@NotNull SiddhiQLParser.PartitionContext ctx) {
        Partition partition = Partition.partition();
        for (SiddhiQLParser.AnnotationContext annotationContext : ctx.annotation()) {
            partition.annotation((Annotation) visit(annotationContext));
        }
        for (SiddhiQLParser.Partition_with_streamContext with_streamContext : ctx.partition_with_stream()) {
            partition.with((PartitionType) visit(with_streamContext));
        }
        for (SiddhiQLParser.QueryContext queryContext : ctx.query()) {
            partition.addQuery((Query) visit(queryContext));
        }
        populateQueryContext(partition, ctx);
        return partition;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public PartitionType visitPartition_with_stream(@NotNull SiddhiQLParser.Partition_with_streamContext ctx) {

        String streamId = (String) visit(ctx.stream_id());

        activeStreams.add(streamId);
        try {
            if (ctx.condition_ranges() != null) {
                PartitionType partitionType = new RangePartitionType(streamId,
                        (RangePartitionType.RangePartitionProperty[]) visit(ctx.condition_ranges()));
                populateQueryContext(partitionType, ctx);
                return partitionType;
            } else if (ctx.attribute() != null) {
                PartitionType partitionType = new ValuePartitionType(streamId, (Expression) visit(ctx.attribute()));
                populateQueryContext(partitionType, ctx);
                return partitionType;
            } else {
                throw newSiddhiParserException(ctx);
            }
        } finally {
            activeStreams.clear();
        }
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public RangePartitionType.RangePartitionProperty[] visitCondition_ranges(
            @NotNull SiddhiQLParser.Condition_rangesContext ctx) {
        RangePartitionType.RangePartitionProperty[] rangePartitionProperties = new RangePartitionType
                .RangePartitionProperty[ctx.condition_range().size()];
        List<SiddhiQLParser.Condition_rangeContext> condition_range = ctx.condition_range();
        for (int i = 0; i < condition_range.size(); i++) {
            SiddhiQLParser.Condition_rangeContext rangeContext = condition_range.get(i);
            rangePartitionProperties[i] = (RangePartitionType.RangePartitionProperty) visit(rangeContext);
        }
        return rangePartitionProperties;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Object visitCondition_range(@NotNull SiddhiQLParser.Condition_rangeContext ctx) {
        RangePartitionType.RangePartitionProperty rangePartitionProperty = new RangePartitionType.
                RangePartitionProperty((String) ((StringConstant) visit(ctx.string_value()))
                .getValue(), (Expression) visit(ctx.expression()));
        populateQueryContext(rangePartitionProperty, ctx);
        return rangePartitionProperty;

    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Object visitQuery_final(@NotNull SiddhiQLParser.Query_finalContext ctx) {
        return visit(ctx.query());
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Query visitQuery(@NotNull SiddhiQLParser.QueryContext ctx) {

//        query
//        : annotation* query_input query_section? output_rate? (query_output | query_publish)
//        ;

        try {
            Query query = Query.query().from((InputStream) visit(ctx.query_input()));

            if (ctx.query_section() != null) {
                query.select((Selector) visit(ctx.query_section()));
            }
            if (ctx.output_rate() != null) {
                query.output((OutputRate) visit(ctx.output_rate()));
            }
            for (SiddhiQLParser.AnnotationContext annotationContext : ctx.annotation()) {
                query.annotation((Annotation) visit(annotationContext));
            }
            if (ctx.query_output() != null) {
                query.outStream((OutputStream) visit(ctx.query_output()));
            }
            populateQueryContext(query, ctx);
            return query;
        } finally {
            activeStreams.clear();
        }
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Annotation visitApp_annotation(@NotNull SiddhiQLParser.App_annotationContext ctx) {
        Annotation annotation = new Annotation((String) visit(ctx.name()));

        for (SiddhiQLParser.Annotation_elementContext elementContext : ctx.annotation_element()) {
            annotation.element((Element) visit(elementContext));
        }
        populateQueryContext(annotation, ctx);
        return annotation;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Annotation visitAnnotation(@NotNull SiddhiQLParser.AnnotationContext ctx) {
        Annotation annotation = Annotation.annotation((String) visit(ctx.name()));

        for (SiddhiQLParser.Annotation_elementContext elementContext : ctx.annotation_element()) {
            annotation.element((Element) visit(elementContext));
        }

        for (SiddhiQLParser.AnnotationContext annotationContext : ctx.annotation()) {
            annotation.annotation((Annotation) visit(annotationContext));
        }
        populateQueryContext(annotation, ctx);
        return annotation;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Element visitAnnotation_element(@NotNull SiddhiQLParser.Annotation_elementContext ctx) {
        Element element;
        if (ctx.property_name() != null) {
            element = new Element((String) visit(ctx.property_name()), ((StringConstant) visit(ctx.property_value()))
                    .getValue());
        } else {
            element = new Element(null, ((StringConstant) visit(ctx.property_value())).getValue());
        }
        populateQueryContext(element, ctx);
        return element;

    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public SingleInputStream visitStandard_stream(@NotNull SiddhiQLParser.Standard_streamContext ctx) {

//        standard_stream
//        : io (basic_source_stream_handler)* window? (basic_source_stream_handler)*
//        ;

        Source source = (Source) visit(ctx.source());

        BasicSingleInputStream basicSingleInputStream = new BasicSingleInputStream(null, source.streamId,
                source.isInnerStream);

        if (ctx.pre_window_handlers != null) {
            basicSingleInputStream.addStreamHandlers((List<StreamHandler>) visit(ctx.pre_window_handlers));
        }

        if (ctx.window() == null && ctx.post_window_handlers == null) {
            populateQueryContext(basicSingleInputStream, ctx);
            return basicSingleInputStream;
        } else if (ctx.window() != null) {
            SingleInputStream singleInputStream = new SingleInputStream(basicSingleInputStream, (Window) visit(ctx
                    .window()));
            if (ctx.post_window_handlers != null) {
                singleInputStream.addStreamHandlers((List<StreamHandler>) visit(ctx.post_window_handlers));
            }
            populateQueryContext(singleInputStream, ctx);
            return singleInputStream;
        } else {
            throw newSiddhiParserException(ctx);
        }

    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Object visitJoin_stream(@NotNull SiddhiQLParser.Join_streamContext ctx) {

//        join_stream
//        :left_source=join_source join right_source=join_source right_unidirectional=UNIDIRECTIONAL (ON expression)?
// within_time?
//        |left_source=join_source join right_source=join_source (ON expression)? within_time?
//        |left_source=join_source left_unidirectional=UNIDIRECTIONAL join right_source=join_source (ON expression)?
// within_time?
//        ;

        SingleInputStream leftStream = (SingleInputStream) visit(ctx.left_source);
        SingleInputStream rightStream = (SingleInputStream) visit(ctx.right_source);
        JoinInputStream.Type joinType = (JoinInputStream.Type) visit(ctx.join());
        JoinInputStream.EventTrigger eventTrigger = null;
        Expression onCondition = null;
        Within within = null;
        Expression per = null;

        if (ctx.within_time_range() != null) {
            within = (Within) visit(ctx.within_time_range());
        }

        if (ctx.per() != null) {
            per = (Expression) visit(ctx.per());
        }

        if (ctx.expression() != null) {
            onCondition = (Expression) visit(ctx.expression());
        }

        if (ctx.right_unidirectional != null) {
            eventTrigger = JoinInputStream.EventTrigger.RIGHT;
        } else if (ctx.left_unidirectional != null) {
            eventTrigger = JoinInputStream.EventTrigger.LEFT;
        } else {
            eventTrigger = JoinInputStream.EventTrigger.ALL;
        }

        InputStream inputStream = InputStream.joinStream(leftStream, joinType, rightStream, onCondition,
                eventTrigger, within, per);
        populateQueryContext(inputStream, ctx);
        return inputStream;

    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Object visitJoin_source(@NotNull SiddhiQLParser.Join_sourceContext ctx) {

//        join_source
//        :io (basic_source_stream_handler)* window? (AS alias)?
//        ;

        Source source = (Source) visit(ctx.source());

        String streamAlias = null;
        if (ctx.alias() != null) {
            streamAlias = (String) visit(ctx.alias());
            activeStreams.remove(ctx.source().getText());
            activeStreams.add(streamAlias);
        }
        BasicSingleInputStream basicSingleInputStream = new BasicSingleInputStream(streamAlias, source.streamId,
                source.isInnerStream);

        if (ctx.basic_source_stream_handlers() != null) {
            basicSingleInputStream.addStreamHandlers((List<StreamHandler>) visit(ctx.basic_source_stream_handlers()));
        }

        if (ctx.window() != null) {
            SingleInputStream inputStream = new SingleInputStream(basicSingleInputStream, (Window) visit(ctx.window()));
            populateQueryContext(inputStream, ctx);
            return inputStream;
        } else {
            populateQueryContext(basicSingleInputStream, ctx);
            return basicSingleInputStream;
        }
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Object visitPattern_stream(@NotNull SiddhiQLParser.Pattern_streamContext ctx) {
//    pattern_stream
//    : every_pattern_source_chain
//    | every_absent_pattern_source_chain
//    ;

        StateElement stateElement;
        if (ctx.every_pattern_source_chain() != null) {
            stateElement = ((StateElement) visit(ctx.every_pattern_source_chain()));
        } else {
            stateElement = ((StateElement) visit(ctx.absent_pattern_source_chain()));
        }
        StateInputStream stateInputStream = new StateInputStream(StateInputStream.Type.PATTERN, stateElement);
        populateQueryContext(stateInputStream, ctx);
        return stateInputStream;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Object visitEvery_pattern_source_chain(@NotNull SiddhiQLParser.Every_pattern_source_chainContext ctx) {
//        every_pattern_source_chain
//        : '('every_pattern_source_chain')' within_time?
//        | EVERY '('pattern_source_chain ')' within_time?
//        | every_pattern_source_chain  '->' every_pattern_source_chain
//        | pattern_source_chain
//        | EVERY pattern_source within_time?
//        ;

        if (ctx.every_pattern_source_chain().size() == 1) { // '('every_pattern_source_chain')' within_time?
            StateElement stateElement = ((StateElement) visit(ctx.every_pattern_source_chain(0)));
            if (ctx.within_time() != null) {
                stateElement.setWithin((TimeConstant) visit(ctx.within_time()));
            }
            populateQueryContext(stateElement, ctx);
            return stateElement;
        } else if (ctx.every_pattern_source_chain().size() == 2) { // every_pattern_source_chain  '->'
            // every_pattern_source_chain
            NextStateElement nextStateElement = new NextStateElement(((StateElement) visit(
                    ctx.every_pattern_source_chain(0))),
                    ((StateElement) visit(ctx.every_pattern_source_chain(1))));
            populateQueryContext(nextStateElement, ctx);
            return nextStateElement;
        } else if (ctx.EVERY() != null) {
            if (ctx.pattern_source_chain() != null) { // EVERY '('pattern_source_chain ')' within_time?
                EveryStateElement everyStateElement = new EveryStateElement((StateElement) visit(ctx
                        .pattern_source_chain()));
                if (ctx.within_time() != null) {
                    everyStateElement.setWithin((TimeConstant) visit(ctx.within_time()));
                }
                populateQueryContext(everyStateElement, ctx);
                return everyStateElement;
            } else if (ctx.pattern_source() != null) { // EVERY pattern_source within_time?
                EveryStateElement everyStateElement = new EveryStateElement((StateElement) visit(ctx.pattern_source()));
                if (ctx.within_time() != null) {
                    everyStateElement.setWithin((TimeConstant) visit(ctx.within_time()));
                }
                populateQueryContext(everyStateElement, ctx);
                return everyStateElement;
            } else {
                throw newSiddhiParserException(ctx);
            }
        } else if (ctx.pattern_source_chain() != null) {  // pattern_source_chain
            StateElement stateElement = ((StateElement) visit(ctx.pattern_source_chain()));
            if (ctx.within_time() != null) {
                stateElement.setWithin((TimeConstant) visit(ctx.within_time()));
            }
            populateQueryContext(stateElement, ctx);
            return stateElement;
        } else {
            throw newSiddhiParserException(ctx);
        }
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Object visitPattern_source_chain(@NotNull SiddhiQLParser.Pattern_source_chainContext ctx) {
//    pattern_source_chain
//    : '('pattern_source_chain')' within_time?
//    | pattern_source_chain  '->' pattern_source_chain
//    | pattern_source within_time?
//    ;

        if (ctx.pattern_source_chain().size() == 1) {
            StateElement stateElement = ((StateElement) visit(ctx.pattern_source_chain(0)));
            if (ctx.within_time() != null) {
                stateElement.setWithin((TimeConstant) visit(ctx.within_time()));
            }
            populateQueryContext(stateElement, ctx);
            return stateElement;
        } else if (ctx.pattern_source_chain().size() == 2) {
            NextStateElement nextStateElement = new NextStateElement((
                    (StateElement) visit(ctx.pattern_source_chain(0))),
                    ((StateElement) visit(ctx.pattern_source_chain(1))));
            populateQueryContext(nextStateElement, ctx);
            return nextStateElement;
        } else if (ctx.pattern_source() != null) {
            StateElement stateElement = ((StateElement) visit(ctx.pattern_source()));
            if (ctx.within_time() != null) {
                stateElement.setWithin((TimeConstant) visit(ctx.within_time()));
            }
            populateQueryContext(stateElement, ctx);
            return stateElement;
        } else {
            throw newSiddhiParserException(ctx);
        }
    }

    @Override
    public Object visitAbsent_pattern_source_chain(SiddhiQLParser.Absent_pattern_source_chainContext ctx) {
//    absent_pattern_source_chain
//    : EVERY? '('absent_pattern_source_chain')' within_time?
//    | every_absent_pattern_source
//    | left_absent_pattern_source
//    | right_absent_pattern_source
//    ;

        if (ctx.absent_pattern_source_chain() != null) {
            StateElement stateElement = (StateElement) visit(ctx.absent_pattern_source_chain());
            if (ctx.EVERY() != null) {
                stateElement = new EveryStateElement(stateElement);
            }
            if (ctx.within_time() != null) {
                stateElement.setWithin((TimeConstant) visit(ctx.within_time()));
            }
            populateQueryContext(stateElement, ctx);
            return stateElement;
        } else {
            return visit(ctx.getChild(0));
        }
    }

    @Override
    public Object visitLeft_absent_pattern_source(SiddhiQLParser.Left_absent_pattern_sourceContext ctx) {
//    left_absent_pattern_source
//    : EVERY? '('left_absent_pattern_source')' within_time?
//    | every_absent_pattern_source '->' every_pattern_source_chain
//    | left_absent_pattern_source '->' left_absent_pattern_source
//    | left_absent_pattern_source '->' every_absent_pattern_source
//    | every_pattern_source_chain '->' left_absent_pattern_source
//    ;

        if (ctx.left_absent_pattern_source().size() == 1 && ctx.every_absent_pattern_source() == null &&
                ctx.every_pattern_source_chain() == null) {
            // EVERY? '('left_absent_pattern_source')' within_time?
            StateElement stateElement = (StateElement) visit(ctx.left_absent_pattern_source(0));
            if (ctx.EVERY() != null) {
                stateElement = new EveryStateElement(stateElement);
            }
            if (ctx.within_time() != null) {
                stateElement.setWithin((TimeConstant) visit(ctx.within_time()));
            }
            populateQueryContext(stateElement, ctx);
            return stateElement;
        } else {
            NextStateElement nextStateElement = new NextStateElement((StateElement) visit(ctx.getChild(0)),
                    (StateElement) visit(ctx.getChild(2)));
            populateQueryContext(nextStateElement, ctx);
            return nextStateElement;
        }
    }

    @Override
    public Object visitRight_absent_pattern_source(SiddhiQLParser.Right_absent_pattern_sourceContext ctx) {
//    right_absent_pattern_source
//    : EVERY? '('right_absent_pattern_source')' within_time?
//    | every_pattern_source_chain '->' every_absent_pattern_source
//    | right_absent_pattern_source '->' right_absent_pattern_source
//    | every_absent_pattern_source '->' right_absent_pattern_source
//    | right_absent_pattern_source '->' every_pattern_source_chain
//    ;
        if (ctx.right_absent_pattern_source().size() == 1 && ctx.every_absent_pattern_source() == null &&
                ctx.every_pattern_source_chain() == null) {
            // EVERY? '('right_absent_pattern_source')' within_time?
            StateElement stateElement = (StateElement) visit(ctx.right_absent_pattern_source(0));
            if (ctx.EVERY() != null) {
                stateElement = new EveryStateElement(stateElement);
            }
            if (ctx.within_time() != null) {
                stateElement.setWithin((TimeConstant) visit(ctx.within_time()));
            }
            populateQueryContext(stateElement, ctx);
            return stateElement;
        } else {
            NextStateElement nextStateElement = new NextStateElement((StateElement) visit(ctx.getChild(0)),
                    (StateElement) visit(ctx.getChild(2)));
            populateQueryContext(nextStateElement, ctx);
            return nextStateElement;
        }
    }

    @Override
    public Object visitEvery_absent_pattern_source(SiddhiQLParser.Every_absent_pattern_sourceContext ctx) {
//    every_absent_pattern_source
//    : EVERY? basic_absent_pattern_source
//    ;

        StateElement stateElement = (StateElement) visit(ctx.basic_absent_pattern_source());
        if (ctx.EVERY() != null) {
            stateElement = new EveryStateElement(stateElement);
        }
        populateQueryContext(stateElement, ctx);
        return stateElement;
    }

    @Override
    public Object visitFor_time(SiddhiQLParser.For_timeContext ctx) {
//    for_time
//    : FOR time_value
//    ;

        return visit(ctx.time_value());
    }

    @Override
    public Object visitBasic_absent_pattern_source(SiddhiQLParser.Basic_absent_pattern_sourceContext ctx) {
//    basic_absent_pattern_source
//    :NOT basic_source for_time
//    ;

        AbsentStreamStateElement stateElement = State.logicalNot(new StreamStateElement((BasicSingleInputStream)
                visit(ctx
                        .basic_source())));
        stateElement.waitingTime((TimeConstant) visit(ctx.for_time()));
        populateQueryContext(stateElement, ctx);
        return stateElement;
    }

    @Override
    public Object visitLogical_absent_stateful_source(SiddhiQLParser.Logical_absent_stateful_sourceContext ctx) {
//    logical_absent_stateful_source
//    : '(' logical_absent_stateful_source ')'
//    | standard_stateful_source AND NOT basic_source
//    | NOT basic_source AND standard_stateful_source
//    | standard_stateful_source AND basic_absent_pattern_source
//    | basic_absent_pattern_source AND standard_stateful_source
//    | basic_absent_pattern_source AND basic_absent_pattern_source
//    | standard_stateful_source OR basic_absent_pattern_source
//    | basic_absent_pattern_source OR standard_stateful_source
//    | basic_absent_pattern_source OR basic_absent_pattern_source
//    ;
        if (ctx.logical_absent_stateful_source() != null) {
            return visit(ctx.logical_absent_stateful_source());
        } else if (ctx.AND() != null) {
            if (ctx.basic_absent_pattern_source().size() == 2) {
                StateElement stateElement = State.logicalNotAnd(
                        (AbsentStreamStateElement) visit(ctx.basic_absent_pattern_source(0)),
                        (AbsentStreamStateElement) visit(ctx.basic_absent_pattern_source(1)));
                populateQueryContext(stateElement, ctx);
                return stateElement;
            } else {
                StreamStateElement presentStreamState = (StreamStateElement) visit(ctx.standard_stateful_source());
                AbsentStreamStateElement absentStreamState;
                if (!ctx.basic_absent_pattern_source().isEmpty()) {
                    absentStreamState = (AbsentStreamStateElement) visit(ctx.basic_absent_pattern_source(0));
                } else {
                    absentStreamState = State.logicalNot(new StreamStateElement((BasicSingleInputStream) visit(ctx
                            .basic_source())));
                }
                StateElement stateElement = State.logicalNotAnd(absentStreamState, presentStreamState);
                populateQueryContext(stateElement, ctx);
                return stateElement;
            }
        } else if (ctx.OR() != null) {
            if (ctx.basic_absent_pattern_source().size() == 2) {
                return State.logicalOr((AbsentStreamStateElement) visit(ctx.basic_absent_pattern_source(0)),
                        (AbsentStreamStateElement) visit(ctx.basic_absent_pattern_source(1)));
            } else {
                StreamStateElement streamStateElement1 = (StreamStateElement) visit(ctx.standard_stateful_source());
                AbsentStreamStateElement streamStateElement2 = (AbsentStreamStateElement) visit(ctx
                        .basic_absent_pattern_source(0));
                StateElement stateElement = State.logicalOr(streamStateElement2, streamStateElement1);
                populateQueryContext(stateElement, ctx);
                return stateElement;
            }
        } else {
            throw newSiddhiParserException(ctx);
        }
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Object visitLogical_stateful_source(@NotNull SiddhiQLParser.Logical_stateful_sourceContext ctx) {

//        logical_stateful_source
//        :NOT standard_stateful_source (AND standard_stateful_source) ?
//        |standard_stateful_source AND standard_stateful_source
//        |standard_stateful_source OR standard_stateful_source
//        ;

        if (ctx.AND() != null) {
            StreamStateElement streamStateElement1 = (StreamStateElement) visit(ctx.standard_stateful_source(0));
            StreamStateElement streamStateElement2 = (StreamStateElement) visit(ctx.standard_stateful_source(1));
            StateElement stateElement = State.logicalAnd(streamStateElement1, streamStateElement2);
            populateQueryContext(stateElement, ctx);
            return stateElement;
        } else if (ctx.OR() != null) {
            StreamStateElement streamStateElement1 = (StreamStateElement) visit(ctx.standard_stateful_source(0));
            StreamStateElement streamStateElement2 = (StreamStateElement) visit(ctx.standard_stateful_source(1));
            StateElement stateElement = State.logicalOr(streamStateElement1, streamStateElement2);
            populateQueryContext(stateElement, ctx);
            return stateElement;
        } else {
            throw newSiddhiParserException(ctx);
        }

    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public CountStateElement visitPattern_collection_stateful_source(
            @NotNull SiddhiQLParser.Pattern_collection_stateful_sourceContext ctx) {

        StreamStateElement streamStateElement = (StreamStateElement) visit(ctx.standard_stateful_source());

        if (ctx.collect() != null) {
            Object[] minMax = (Object[]) visit(ctx.collect());
            int min = CountStateElement.ANY;
            int max = CountStateElement.ANY;
            if (minMax[0] != null) {
                min = (Integer) minMax[0];
            }
            if (minMax[1] != null) {
                max = (Integer) minMax[1];
            }
            CountStateElement stateElement = new CountStateElement(streamStateElement, min, max);
            populateQueryContext(stateElement, ctx);
            return stateElement;
        } else {
            throw newSiddhiParserException(ctx);
        }
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public StateInputStream visitSequence_stream(@NotNull SiddhiQLParser.Sequence_streamContext ctx) {
//        sequence_stream
//        :every_sequence_source_chain
//        |every_absent_sequence_source_chain
//        ;
        if (ctx.every_sequence_source_chain() != null) {
            StateInputStream stateInputStream = (StateInputStream)
                    visitEvery_sequence_source_chain(ctx.every_sequence_source_chain());
            populateQueryContext(stateInputStream, ctx);
            return stateInputStream;
        } else {
            StateInputStream stateInputStream = (StateInputStream)
                    visitEvery_absent_sequence_source_chain(ctx.every_absent_sequence_source_chain());
            populateQueryContext(stateInputStream, ctx);
            return stateInputStream;
        }
    }

    @Override
    public Object visitEvery_sequence_source_chain(SiddhiQLParser.Every_sequence_source_chainContext ctx) {
//        every_sequence_source_chain
//        : EVERY? sequence_source  within_time?  ',' sequence_source_chain
//        ;

        StateElement stateElement1;
        if (ctx.EVERY() != null) {
            stateElement1 = new EveryStateElement((StateElement) visit(ctx.sequence_source()));
        } else {
            stateElement1 = (StateElement) visit(ctx.sequence_source());
        }
        if (ctx.within_time() != null) {
            stateElement1.setWithin((TimeConstant) visit(ctx.within_time()));
        }
        populateQueryContext(stateElement1, ctx);
        NextStateElement nextStateElement = new NextStateElement(stateElement1,
                ((StateElement) visit(ctx.sequence_source_chain())));
        populateQueryContext(nextStateElement, ctx);
        StateInputStream stateInputStream = new StateInputStream(StateInputStream.Type.SEQUENCE, nextStateElement);
        populateQueryContext(stateInputStream, ctx);
        return stateInputStream;
    }

    @Override
    public Object visitEvery_absent_sequence_source_chain(SiddhiQLParser.Every_absent_sequence_source_chainContext
                                                                  ctx) {
//        every_absent_sequence_source_chain
//        : EVERY? absent_sequence_source_chain  within_time? ',' sequence_source_chain
//        | EVERY? sequence_source  within_time? ',' absent_sequence_source_chain
//        ;
        StateElement stateElement1;
        StateElement stateElement2;
        if (ctx.EVERY() != null) {
            stateElement1 = new EveryStateElement((StateElement) visit(ctx.getChild(1)));
        } else {
            stateElement1 = (StateElement) visit(ctx.getChild(0));
        }
        if (ctx.within_time() != null) {
            stateElement1.setWithin((TimeConstant) visit(ctx.within_time()));
        }
        stateElement2 = (StateElement) visit(ctx.getChild(ctx.getChildCount() - 1));
        populateQueryContext(stateElement1, ctx);
        populateQueryContext(stateElement2, ctx);
        NextStateElement nextStateElement = new NextStateElement(stateElement1, stateElement2);
        populateQueryContext(nextStateElement, ctx);
        StateInputStream stateInputStream = new StateInputStream(StateInputStream.Type.SEQUENCE, nextStateElement);
        populateQueryContext(stateInputStream, ctx);
        return stateInputStream;
    }

    @Override
    public Object visitAbsent_sequence_source_chain(SiddhiQLParser.Absent_sequence_source_chainContext ctx) {
//        absent_sequence_source_chain
//        : '('absent_sequence_source_chain')' within_time?
//        | basic_absent_pattern_source
//        | left_absent_sequence_source
//        | right_absent_sequence_source
//        ;

        StateElement stateElement;
        if (ctx.absent_sequence_source_chain() != null) {
            stateElement = (StateElement) visit(ctx.absent_sequence_source_chain());
            if (ctx.within_time() != null) {
                stateElement.setWithin((TimeConstant) visit(ctx.within_time()));
            }
        } else {
            stateElement = (StateElement) visit(ctx.getChild(0));
        }
        populateQueryContext(stateElement, ctx);
        return stateElement;
    }

    @Override
    public Object visitLeft_absent_sequence_source(SiddhiQLParser.Left_absent_sequence_sourceContext ctx) {

//        left_absent_sequence_source
//        : '('left_absent_sequence_source')' within_time?
//        | basic_absent_pattern_source ',' sequence_source_chain
//        | left_absent_sequence_source ',' left_absent_sequence_source
//        | left_absent_sequence_source ',' basic_absent_pattern_source
//        | sequence_source_chain ',' left_absent_sequence_source
//        ;

        if (ctx.left_absent_sequence_source().size() == 1 && ctx.basic_absent_pattern_source() == null &&
                ctx.sequence_source_chain() == null) {
            // '('left_absent_pattern_source')' within_time?
            StateElement stateElement = (StateElement) visit(ctx.left_absent_sequence_source(0));
            if (ctx.within_time() != null) {
                stateElement.setWithin((TimeConstant) visit(ctx.within_time()));
            }
            populateQueryContext(stateElement, ctx);
            return stateElement;
        } else {
            NextStateElement nextStateElement = new NextStateElement((StateElement) visit(ctx.getChild(0)),
                    (StateElement) visit(ctx.getChild(2)));
            populateQueryContext(nextStateElement, ctx);
            return nextStateElement;
        }
    }

    @Override
    public Object visitRight_absent_sequence_source(SiddhiQLParser.Right_absent_sequence_sourceContext ctx) {
//        right_absent_sequence_source
//        : '('right_absent_sequence_source')' within_time?
//        | sequence_source_chain ',' basic_absent_pattern_source
//        | right_absent_sequence_source ',' right_absent_sequence_source
//        | basic_absent_pattern_source ',' right_absent_sequence_source
//        | right_absent_sequence_source ',' sequence_source_chain
//        ;

        if (ctx.right_absent_sequence_source().size() == 1 && ctx.basic_absent_pattern_source() == null &&
                ctx.sequence_source_chain() == null) {
            // '('right_absent_pattern_source')' within_time?
            StateElement stateElement = (StateElement) visit(ctx.right_absent_sequence_source(0));
            if (ctx.within_time() != null) {
                stateElement.setWithin((TimeConstant) visit(ctx.within_time()));
            }
            populateQueryContext(stateElement, ctx);
            return stateElement;
        } else {
            NextStateElement nextStateElement = new NextStateElement((StateElement) visit(ctx.getChild(0)),
                    (StateElement) visit(ctx.getChild(2)));
            populateQueryContext(nextStateElement, ctx);
            return nextStateElement;
        }
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public StateElement visitSequence_source_chain(@NotNull SiddhiQLParser.Sequence_source_chainContext ctx) {
//        sequence_source_chain
//        :'('sequence_source_chain ')' within_time?
//        | sequence_source_chain ',' sequence_source_chain
//        | sequence_source  within_time?
//        ;

        if (ctx.sequence_source_chain().size() == 1) {
            StateElement stateElement = ((StateElement) visit(ctx.sequence_source_chain(0)));
            if (ctx.within_time() != null) {
                stateElement.setWithin((TimeConstant) visit(ctx.within_time()));
            }
            populateQueryContext(stateElement, ctx);
            return stateElement;
        } else if (ctx.sequence_source_chain().size() == 2) {
            NextStateElement nextStateElement = new NextStateElement(((StateElement)
                    visit(ctx.sequence_source_chain(0))), ((StateElement) visit(ctx.sequence_source_chain(1))));
            populateQueryContext(nextStateElement, ctx);
            return nextStateElement;
        } else if (ctx.sequence_source() != null) {
            StateElement stateElement = ((StateElement) visit(ctx.sequence_source()));
            if (ctx.within_time() != null) {
                stateElement.setWithin((TimeConstant) visit(ctx.within_time()));
            }
            populateQueryContext(stateElement, ctx);
            return stateElement;
        } else {
            throw newSiddhiParserException(ctx);
        }
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public BasicSingleInputStream visitBasic_source(@NotNull SiddhiQLParser.Basic_sourceContext ctx) {

//        basic_source
//        : io (basic_source_stream_handler)*
//        ;

        Source source = (Source) visit(ctx.source());

        BasicSingleInputStream basicSingleInputStream =
                new BasicSingleInputStream(null, source.streamId, source.isInnerStream);

        if (ctx.basic_source_stream_handlers() != null) {
            basicSingleInputStream.addStreamHandlers((List<StreamHandler>) visit(ctx.basic_source_stream_handlers()));
        }
        populateQueryContext(basicSingleInputStream, ctx);
        return basicSingleInputStream;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public List<StreamHandler> visitBasic_source_stream_handlers(
            @NotNull SiddhiQLParser.Basic_source_stream_handlersContext ctx) {
        List<StreamHandler> streamHandlers = new ArrayList<StreamHandler>();
        for (SiddhiQLParser.Basic_source_stream_handlerContext handlerContext : ctx.basic_source_stream_handler()) {
            streamHandlers.add((StreamHandler) visit(handlerContext));
        }
        return streamHandlers;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public String visitEvent(@NotNull SiddhiQLParser.EventContext ctx) {
        return ctx.getText();
    }


    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public StreamStateElement visitStandard_stateful_source(
            @NotNull SiddhiQLParser.Standard_stateful_sourceContext ctx) {

//        standard_stateful_source
//        : (event '=')? basic_source
//        ;

        if (ctx.event() != null) {
            activeStreams.add(visitEvent(ctx.event()));
        }
        BasicSingleInputStream basicSingleInputStream = (BasicSingleInputStream) visit(ctx.basic_source());
        if (ctx.event() != null) {
            if (basicSingleInputStream.isInnerStream()) {
                activeStreams.remove("#" + basicSingleInputStream.getStreamId());
            } else {
                activeStreams.remove(basicSingleInputStream.getStreamId());
            }
            StreamStateElement streamStateElement = new StreamStateElement(basicSingleInputStream.as((String)
                    visit(ctx.event())));
            populateQueryContext(streamStateElement, ctx);
            return streamStateElement;
        } else {
            StreamStateElement streamStateElement = new StreamStateElement(basicSingleInputStream);
            populateQueryContext(streamStateElement, ctx);
            return streamStateElement;
        }
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public CountStateElement visitSequence_collection_stateful_source(
            @NotNull SiddhiQLParser.Sequence_collection_stateful_sourceContext ctx) {

//        sequence_collection_stateful_source
//        :standard_stateful_source ('<' collect '>'|zero_or_more='*'|zero_or_one='?'|one_or_more='+')
//        ;

        StreamStateElement streamStateElement = (StreamStateElement) visit(ctx.standard_stateful_source());

        if (ctx.one_or_more != null) {
            CountStateElement countStateElement = new CountStateElement(streamStateElement, 1,
                    CountStateElement.ANY);
            populateQueryContext(countStateElement, ctx);
            return countStateElement;
        } else if (ctx.zero_or_more != null) {
            CountStateElement countStateElement = new CountStateElement(streamStateElement, 0,
                    CountStateElement.ANY);
            populateQueryContext(countStateElement, ctx);
            return countStateElement;
        } else if (ctx.zero_or_one != null) {
            CountStateElement countStateElement = new CountStateElement(streamStateElement, 0, 1);
            populateQueryContext(countStateElement, ctx);
            return countStateElement;
        } else if (ctx.collect() != null) {
            Object[] minMax = (Object[]) visit(ctx.collect());
            int min = CountStateElement.ANY;
            int max = CountStateElement.ANY;
            if (minMax[0] != null) {
                min = (Integer) minMax[0];
            }
            if (minMax[1] != null) {
                max = (Integer) minMax[1];
            }
            CountStateElement countStateElement = new CountStateElement(streamStateElement, min, max);
            populateQueryContext(countStateElement, ctx);
            return countStateElement;
        } else {
            throw newSiddhiParserException(ctx);
        }
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public AnonymousInputStream visitAnonymous_stream(@NotNull SiddhiQLParser.Anonymous_streamContext ctx) {

        if (ctx.anonymous_stream() != null) {
            return (AnonymousInputStream) visit(ctx.anonymous_stream());
        }

        Set<String> activeStreamsBackup = activeStreams;

        try {
            activeStreams = new HashSet<String>();

            Query query = Query.query().from((InputStream) visit(ctx.query_input()));

            if (ctx.query_section() != null) {
                query.select((Selector) visit(ctx.query_section()));
            }
            if (ctx.output_rate() != null) {
                query.output((OutputRate) visit(ctx.output_rate()));
            }

            if (ctx.output_event_type() != null) {
                query.outStream(new ReturnStream((OutputStream.OutputEventType) visit(ctx.output_event_type())));
            } else {
                query.outStream(new ReturnStream());
            }

            AnonymousInputStream anonymousInputStream = new AnonymousInputStream(query);
            populateQueryContext(anonymousInputStream, ctx);
            return anonymousInputStream;

        } finally {
            activeStreams.clear();
            activeStreams = activeStreamsBackup;
        }
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Filter visitFilter(@NotNull SiddhiQLParser.FilterContext ctx) {
        Filter filter = new Filter((Expression) visit(ctx.expression()));
        populateQueryContext(filter, ctx);
        return filter;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public StreamFunction visitStream_function(@NotNull SiddhiQLParser.Stream_functionContext ctx) {
        AttributeFunction attributeFunction = (AttributeFunction) visit(ctx.function_operation());
        StreamFunction streamFunction = new StreamFunction(attributeFunction.getNamespace(),
                attributeFunction.getName(), attributeFunction.getParameters());
        populateQueryContext(streamFunction, ctx);
        return streamFunction;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Window visitWindow(@NotNull SiddhiQLParser.WindowContext ctx) {
        AttributeFunction attributeFunction = (AttributeFunction) visit(ctx.function_operation());
        Window window = new Window(attributeFunction.getNamespace(), attributeFunction.getName(),
                attributeFunction.getParameters());
        populateQueryContext(window, ctx);
        return window;
    }

    @Override
    public BasicSelector visitGroup_by_query_selection(@NotNull SiddhiQLParser.Group_by_query_selectionContext ctx) {

        BasicSelector selector = new BasicSelector();

        List<OutputAttribute> attributeList = new ArrayList<OutputAttribute>(ctx.output_attribute().size());
        for (SiddhiQLParser.Output_attributeContext output_attributeContext : ctx.output_attribute()) {
            attributeList.add((OutputAttribute) visit(output_attributeContext));
        }
        selector.addSelectionList(attributeList);

        if (ctx.group_by() != null) {
            selector.addGroupByList((List<Variable>) visit(ctx.group_by()));
        }
        populateQueryContext(selector, ctx);
        return selector;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Selector visitQuery_section(@NotNull SiddhiQLParser.Query_sectionContext ctx) {

//        query_section
//        :(SELECT ('*'| (output_attribute (',' output_attribute)* ))) group_by? having?
//        ;

        Selector selector = new Selector();

        List<OutputAttribute> attributeList = new ArrayList<OutputAttribute>(ctx.output_attribute().size());
        for (SiddhiQLParser.Output_attributeContext output_attributeContext : ctx.output_attribute()) {
            attributeList.add((OutputAttribute) visit(output_attributeContext));
        }
        selector.addSelectionList(attributeList);

        if (ctx.group_by() != null) {
            selector.addGroupByList((List<Variable>) visit(ctx.group_by()));
        }
        if (ctx.having() != null) {
            selector.having((Expression) visit(ctx.having()));
        }
        if (ctx.order_by() != null) {
            selector.addOrderByList((List<OrderByAttribute>) visit(ctx.order_by()));
        }
        if (ctx.limit() != null) {
            selector.limit((Constant) visit(ctx.limit()));
        }
        populateQueryContext(selector, ctx);
        return selector;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public List<Variable> visitGroup_by(@NotNull SiddhiQLParser.Group_byContext ctx) {
        List<Variable> variableList = new ArrayList<Variable>(ctx.attribute_reference().size());
        for (SiddhiQLParser.Attribute_referenceContext attributeReferenceContext : ctx.attribute_reference()) {
            variableList.add((Variable) visit(attributeReferenceContext));
        }
        return variableList;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Expression visitHaving(@NotNull SiddhiQLParser.HavingContext ctx) {
        return (Expression) visit(ctx.expression());
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public List<OrderByAttribute> visitOrder_by(SiddhiQLParser.Order_byContext ctx) {
        List<OrderByAttribute> orderByAttributes = new ArrayList<OrderByAttribute>(ctx.order_by_reference().size());
        for (SiddhiQLParser.Order_by_referenceContext order_by_referenceContext : ctx.order_by_reference()) {
            OrderByAttribute orderByAttribute = (OrderByAttribute) visit(order_by_referenceContext);
            orderByAttributes.add(orderByAttribute);
        }
        return orderByAttributes;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public OrderByAttribute visitOrder_by_reference(SiddhiQLParser.Order_by_referenceContext ctx) {
        Variable variable = (Variable) visit(ctx.attribute_reference());
        if (ctx.order() != null && ctx.order().DESC() != null) {
            return new OrderByAttribute(variable, OrderByAttribute.Order.DESC);
        }
        return new OrderByAttribute(variable);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Constant visitLimit(SiddhiQLParser.LimitContext ctx) {
        Expression expression = (Expression) visit(ctx.expression());
        if (expression instanceof Constant) {
            populateQueryContext(expression, ctx);
            return (Constant) expression;
        } else {
            throw newSiddhiParserException(ctx,
                    "'limit' only accepts int and long constants but found '" + expression + "'");
        }
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public OutputStream visitQuery_output(@NotNull SiddhiQLParser.Query_outputContext ctx) {
//        query_output
//        :INSERT output_event_type? INTO target
//        |UPDATE OR INTO INSERT INTO output_event_type? INTO target
//        |DELETE target (FOR output_event_type)? (ON expression)?
//        |UPDATE target (FOR output_event_type)? (ON expression)?
//        |RETURN output_event_type?
//        ;

        if (ctx.INSERT() != null) {
            Source source = (Source) visit(ctx.target());
            if (ctx.UPDATE() != null && ctx.OR() != null) {
                if (source.isInnerStream) {
                    throw newSiddhiParserException(ctx, "UPDATE OR INTO INSERT be only used with Tables!");
                }
                if (ctx.output_event_type() != null) {
                    if (ctx.set_clause() != null) {
                        OutputStream outputStream = new UpdateOrInsertStream(source.streamId,
                                (OutputStream.OutputEventType) visit(ctx.output_event_type()),
                                (UpdateSet) visit(ctx.set_clause()),
                                (Expression) visit(ctx.expression()));
                        populateQueryContext(outputStream, ctx);
                        return outputStream;
                    } else {
                        OutputStream outputStream = new UpdateOrInsertStream(source.streamId,
                                (OutputStream.OutputEventType) visit(ctx.output_event_type()),
                                (Expression) visit(ctx.expression()));
                        populateQueryContext(outputStream, ctx);
                        return outputStream;
                    }
                } else {
                    if (ctx.set_clause() != null) {
                        OutputStream outputStream = new UpdateOrInsertStream(source.streamId,
                                (UpdateSet) visit(ctx.set_clause()), (Expression) visit(ctx.expression()));
                        populateQueryContext(outputStream, ctx);
                        return outputStream;
                    } else {
                        OutputStream outputStream = new UpdateOrInsertStream(source.streamId, (Expression)
                                visit(ctx.expression()));
                        populateQueryContext(outputStream, ctx);
                        return outputStream;
                    }
                }
            } else {
                if (ctx.output_event_type() != null) {
                    OutputStream outputStream = new InsertIntoStream(source.streamId, source.isInnerStream,
                            (OutputStream.OutputEventType) visit(ctx.output_event_type()));
                    populateQueryContext(outputStream, ctx);
                    return outputStream;
                } else {
                    OutputStream outputStream = new InsertIntoStream(source.streamId, source.isInnerStream);
                    populateQueryContext(outputStream, ctx);
                    return outputStream;
                }
            }
        } else if (ctx.DELETE() != null) {
            Source source = (Source) visit(ctx.target());
            if (source.isInnerStream) {
                throw newSiddhiParserException(ctx, "DELETE can be only used with Tables!");
            }
            if (ctx.output_event_type() != null) {
                OutputStream outputStream = new DeleteStream(source.streamId,
                        (OutputStream.OutputEventType) visit(ctx.output_event_type()),
                        (Expression) visit(ctx.expression()));
                populateQueryContext(outputStream, ctx);
                return outputStream;
            } else {
                OutputStream outputStream = new DeleteStream(source.streamId, (Expression) visit(ctx.expression()));
                populateQueryContext(outputStream, ctx);
                return outputStream;
            }
        } else if (ctx.UPDATE() != null) {
            Source source = (Source) visit(ctx.target());
            if (source.isInnerStream) {
                throw newSiddhiParserException(ctx, "DELETE can be only used with Tables!");
            }
            if (ctx.output_event_type() != null) {
                if (ctx.set_clause() != null) {
                    OutputStream outputStream = new UpdateStream(source.streamId,
                            (OutputStream.OutputEventType) visit(ctx.output_event_type()),
                            (UpdateSet) visit(ctx.set_clause()),
                            (Expression) visit(ctx.expression()));
                    populateQueryContext(outputStream, ctx);
                    return outputStream;
                } else {
                    OutputStream outputStream = new UpdateStream(source.streamId,
                            (OutputStream.OutputEventType) visit(ctx.output_event_type()),
                            (Expression) visit(ctx.expression()));
                    populateQueryContext(outputStream, ctx);
                    return outputStream;
                }
            } else {
                if (ctx.set_clause() != null) {
                    OutputStream outputStream = new UpdateStream(source.streamId, (UpdateSet) visit(ctx.set_clause()),
                            (Expression) visit(ctx.expression()));
                    populateQueryContext(outputStream, ctx);
                    return outputStream;
                } else {
                    OutputStream outputStream = new UpdateStream(source.streamId,
                            (Expression) visit(ctx.expression()));
                    populateQueryContext(outputStream, ctx);
                    return outputStream;
                }
            }
        } else if (ctx.RETURN() != null) {
            if (ctx.output_event_type() != null) {
                OutputStream outputStream = new ReturnStream((OutputStream.OutputEventType)
                        visit(ctx.output_event_type()));
                populateQueryContext(outputStream, ctx);
                return outputStream;
            } else {
                OutputStream outputStream = new ReturnStream();
                populateQueryContext(outputStream, ctx);
                return outputStream;
            }
        } else {
            throw newSiddhiParserException(ctx);
        }
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public OutputStream.OutputEventType visitOutput_event_type(@NotNull SiddhiQLParser.Output_event_typeContext ctx) {
//        output_event_type
//        : ALL EVENTS | EXPIRED EVENTS | CURRENT? EVENTS
//        ;

        if (ctx.ALL() != null) {
            return OutputStream.OutputEventType.ALL_EVENTS;
        } else if (ctx.EXPIRED() != null) {
            return OutputStream.OutputEventType.EXPIRED_EVENTS;
        } else {
            return OutputStream.OutputEventType.CURRENT_EVENTS;
        }
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public UpdateSet visitSet_clause(@NotNull SiddhiQLParser.Set_clauseContext ctx) {
        UpdateSet updateSet = new UpdateSet();
        for (SiddhiQLParser.Set_assignmentContext setAssignmentContext : ctx.set_assignment()) {
            updateSet.set(((Variable) visit(setAssignmentContext.attribute_reference())),
                    (Expression) visit(setAssignmentContext.expression()));
        }
        populateQueryContext(updateSet, ctx);
        return updateSet;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public OutputRate visitOutput_rate(@NotNull SiddhiQLParser.Output_rateContext ctx) {
//        output_rate
//                : OUTPUT output_rate_type? EVERY ( time_value | INT_LITERAL EVENTS )
//                | OUTPUT SNAPSHOT EVERY time_value
//        ;

        if (ctx.SNAPSHOT() != null) {
            SnapshotOutputRate snapshotOutputRate = new SnapshotOutputRate(((TimeConstant)
                    visit(ctx.time_value())).value());
            populateQueryContext(snapshotOutputRate, ctx);
            return snapshotOutputRate;
        } else if (ctx.time_value() != null) {
            TimeOutputRate timeOutputRate = new TimeOutputRate(((TimeConstant) visit(ctx.time_value())).value());
            if (ctx.output_rate_type() != null) {
                timeOutputRate.output((OutputRate.Type) visit(ctx.output_rate_type()));
            }
            populateQueryContext(timeOutputRate, ctx);
            return timeOutputRate;
        } else if (ctx.EVENTS() != null) {
            EventOutputRate eventOutputRate = new EventOutputRate(Integer.parseInt(ctx.INT_LITERAL().getText()));
            if (ctx.output_rate_type() != null) {
                eventOutputRate.output((OutputRate.Type) visit(ctx.output_rate_type()));
            }
            populateQueryContext(eventOutputRate, ctx);
            return eventOutputRate;
        } else {
            throw newSiddhiParserException(ctx);
        }
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Object visitOutput_rate_type(@NotNull SiddhiQLParser.Output_rate_typeContext ctx) {

//        output_rate_type
//                : ALL
//                | LAST
//                | FIRST
//        ;
        if (ctx.ALL() != null) {
            return OutputRate.Type.ALL;
        } else if (ctx.LAST() != null) {
            return OutputRate.Type.LAST;
        } else if (ctx.FIRST() != null) {
            return OutputRate.Type.FIRST;
        } else {
            throw newSiddhiParserException(ctx);
        }
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Object visitOutput_attribute(@NotNull SiddhiQLParser.Output_attributeContext ctx) {

//        output_attribute
//                :attribute AS attribute_name
//                |attribute_reference
//        ;
        if (ctx.AS() != null) {
            OutputAttribute outputAttribute = new OutputAttribute((String) visit(ctx.attribute_name()),
                    (Expression) visit(ctx.attribute()));
            populateQueryContext(outputAttribute, ctx);
            return outputAttribute;
        } else {
            OutputAttribute outputAttribute = new OutputAttribute((Variable) visit(ctx.attribute_reference()));
            populateQueryContext(outputAttribute, ctx);
            return outputAttribute;
        }
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Object visitOr_math_operation(@NotNull SiddhiQLParser.Or_math_operationContext ctx) {
        if (ctx.OR() != null) {
            Expression expression = Expression.or((Expression) visit(ctx.math_operation(0)),
                    (Expression) visit(ctx.math_operation(1)));
            populateQueryContext(expression, ctx);
            return expression;
        } else {
            throw newSiddhiParserException(ctx);
        }
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Expression visitAnd_math_operation(@NotNull SiddhiQLParser.And_math_operationContext ctx) {
        if (ctx.AND() != null) {
            Expression expression = Expression.and((Expression) visit(ctx.math_operation(0)),
                    (Expression) visit(ctx.math_operation(1)));
            populateQueryContext(expression, ctx);
            return expression;
        } else {
            throw newSiddhiParserException(ctx);
        }
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Expression visitEquality_math_operation(@NotNull SiddhiQLParser.Equality_math_operationContext ctx) {
        if (ctx.eq != null) {
            Expression expression = Expression.compare((Expression) visit(ctx.math_operation(0)),
                    Compare.Operator.EQUAL, (Expression) visit(ctx.math_operation(1)));
            populateQueryContext(expression, ctx);
            return expression;
        } else if (ctx.not_eq != null) {
            Expression expression = Expression.compare((Expression) visit(ctx.math_operation(0)),
                    Compare.Operator.NOT_EQUAL, (Expression) visit(ctx.math_operation(1)));
            populateQueryContext(expression, ctx);
            return expression;
        } else {
            throw newSiddhiParserException(ctx);
        }
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Expression visitGreaterthan_lessthan_math_operation(
            @NotNull SiddhiQLParser.Greaterthan_lessthan_math_operationContext ctx) {
        Expression expression;
        if (ctx.gt != null) {
            expression = Expression.compare((Expression) visit(ctx.math_operation(0)), Compare.Operator.GREATER_THAN,
                    (Expression) visit(ctx.math_operation(1)));
        } else if (ctx.lt != null) {
            expression = Expression.compare((Expression) visit(ctx.math_operation(0)), Compare.Operator.LESS_THAN,
                    (Expression) visit(ctx.math_operation(1)));
        } else if (ctx.gt_eq != null) {
            expression = Expression.compare((Expression) visit(ctx.math_operation(0)),
                    Compare.Operator.GREATER_THAN_EQUAL, (Expression) visit(ctx.math_operation(1)));
        } else if (ctx.lt_eq != null) {
            expression = Expression.compare((Expression) visit(ctx.math_operation(0)),
                    Compare.Operator.LESS_THAN_EQUAL, (Expression) visit(ctx.math_operation(1)));
        } else {
            throw newSiddhiParserException(ctx);
        }
        populateQueryContext(expression, ctx);
        return expression;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Expression visitAddition_math_operation(@NotNull SiddhiQLParser.Addition_math_operationContext ctx) {
        Expression expression;
        if (ctx.add != null) {
            expression = Expression.add((Expression) visit(ctx.math_operation(0)),
                    (Expression) visit(ctx.math_operation(1)));
        } else if (ctx.substract != null) {
            expression = Expression.subtract((Expression) visit(ctx.math_operation(0)),
                    (Expression) visit(ctx.math_operation(1)));
        } else {
            throw newSiddhiParserException(ctx);
        }
        populateQueryContext(expression, ctx);
        return expression;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Expression visitMultiplication_math_operation(
            @NotNull SiddhiQLParser.Multiplication_math_operationContext ctx) {
        Expression expression;
        if (ctx.multiply != null) {
            expression = Expression.multiply((Expression) visit(ctx.math_operation(0)),
                    (Expression) visit(ctx.math_operation(1)));
        } else if (ctx.devide != null) {
            expression = Expression.divide((Expression) visit(ctx.math_operation(0)),
                    (Expression) visit(ctx.math_operation(1)));
        } else if (ctx.mod != null) {
            expression = Expression.mod((Expression) visit(ctx.math_operation(0)),
                    (Expression) visit(ctx.math_operation(1)));
        } else {
            throw newSiddhiParserException(ctx);
        }
        populateQueryContext(expression, ctx);
        return expression;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Expression visitNot_math_operation(@NotNull SiddhiQLParser.Not_math_operationContext ctx) {
        Expression expression = Expression.not((Expression) visit(ctx.math_operation()));
        populateQueryContext(expression, ctx);
        return expression;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Object visitIn_math_operation(@NotNull SiddhiQLParser.In_math_operationContext ctx) {
        Expression expression = Expression.in((Expression) visit(ctx.math_operation()), (String) visit(ctx.name()));
        populateQueryContext(expression, ctx);
        return expression;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Object visitBasic_math_operation(@NotNull SiddhiQLParser.Basic_math_operationContext ctx) {
        if (ctx.math_operation() != null) {
            return visit(ctx.math_operation());
        } else if (ctx.attribute_reference() != null) {
            return visit(ctx.attribute_reference());
        } else if (ctx.constant_value() != null) {
            return visit(ctx.constant_value());
        } else if (ctx.null_check() != null) {
            return visit(ctx.null_check());
        } else if (ctx.function_operation() != null) {
            return visit(ctx.function_operation());
        } else {
            throw newSiddhiParserException(ctx);
        }
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Object visitFunction_operation(@NotNull SiddhiQLParser.Function_operationContext ctx) {
        Expression expression;
        if (ctx.function_namespace() != null) {
            if (ctx.attribute_list() != null) {
                expression = Expression.function((String) visit(ctx.function_namespace()),
                        (String) visit(ctx.function_id()), (Expression[]) visit(ctx.attribute_list()));
            } else {
                expression = Expression.function((String) visit(ctx.function_namespace()),
                        (String) visit(ctx.function_id()), null);
            }

        } else {
            if (ctx.attribute_list() != null) {
                expression = Expression.function((String) visit(ctx.function_id()),
                        (Expression[]) visit(ctx.attribute_list()));
            } else {
                expression = Expression.function((String) visit(ctx.function_id()), null);
            }
        }
        populateQueryContext(expression, ctx);
        return expression;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Object visitAttribute_list(@NotNull SiddhiQLParser.Attribute_listContext ctx) {
        Expression[] expressionArray = new Expression[ctx.attribute().size()];
        List<SiddhiQLParser.AttributeContext> attribute = ctx.attribute();
        for (int i = 0; i < attribute.size(); i++) {
            SiddhiQLParser.AttributeContext attributeContext = attribute.get(i);
            expressionArray[i] = (Expression) visit(attributeContext);
        }
        return expressionArray;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Object visitNull_check(@NotNull SiddhiQLParser.Null_checkContext ctx) {
        Expression expression;

        if (ctx.stream_reference() != null) {
            StreamReference streamReference = (StreamReference) visit(ctx.stream_reference());
            if (streamReference.isInnerStream) {   //InnerStream
                if (streamReference.streamIndex != null) {
                    expression = Expression.isNullInnerStream(streamReference.streamId, streamReference.streamIndex);
                } else {
                    expression = Expression.isNullInnerStream(streamReference.streamId);
                }
            } else {
                if (activeStreams.contains(streamReference.streamId)) { //Stream
                    if (streamReference.streamIndex != null) {
                        expression = Expression.isNullStream(streamReference.streamId, streamReference.streamIndex);
                    } else {
                        expression = Expression.isNullStream(streamReference.streamId);
                    }
                } else { //Attribute
                    expression = Expression.isNull(Expression.variable(streamReference.streamId));
                }
            }
        } else if (ctx.function_operation() != null) {
            expression = Expression.isNull((Expression) visit(ctx.function_operation()));
        } else { //attribute_reference
            expression = Expression.isNull((Expression) visit(ctx.attribute_reference()));
        }
        populateQueryContext(expression, ctx);
        return expression;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Object visitStream_reference(@NotNull SiddhiQLParser.Stream_referenceContext ctx) {
        StreamReference streamReference = new StreamReference();
        if (ctx.hash != null) {
            streamReference.isInnerStream = true;
        }
        streamReference.streamId = (String) visit(ctx.name());
        if (ctx.attribute_index() != null) {
            streamReference.streamIndex = (Integer) visit(ctx.attribute_index());
        }
        return streamReference;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Variable visitAttribute_reference(@NotNull SiddhiQLParser.Attribute_referenceContext ctx) {

//        attribute_reference
//        : hash1='#'? name1=name ('['attribute_index1=attribute_index']')? (hash2='#' name2=name
// ('['attribute_index2=attribute_index']')?)? '.'  attribute_name
//        | attribute_name
//        ;

        Variable variable = Expression.variable((String) visit(ctx.attribute_name()));

        if (ctx.name1 != null && ctx.name2 != null) { //Stream and Function
            variable.setStreamId(ctx.hash1 != null, (String) visit(ctx.name1));
            if (ctx.attribute_index1 != null) {
                variable.setStreamIndex((Integer) visit(ctx.attribute_index1));
            }

            variable.setFunctionId((String) visit(ctx.name2));
            if (ctx.attribute_index2 != null) {
                variable.setFunctionIndex((Integer) visit(ctx.attribute_index2));
            }
        } else if (ctx.name1 != null) {   //name2 == null
            if (ctx.hash1 == null) {   //Stream
                variable.setStreamId((String) visit(ctx.name1));
                if (ctx.attribute_index1 != null) {
                    variable.setStreamIndex((Integer) visit(ctx.attribute_index1));
                }
            } else {  //InnerStream or Function
                String name = (String) visit(ctx.name1);

                if (activeStreams.contains("#" + name)) { //InnerStream
                    variable.setStreamId(true, name);
                    if (ctx.attribute_index1 != null) {
                        variable.setStreamIndex((Integer) visit(ctx.attribute_index1));
                    }

                } else { //Function
                    variable.setFunctionId(name);
                    if (ctx.attribute_index1 != null) {
                        variable.setFunctionIndex((Integer) visit(ctx.attribute_index1));
                    }
                }
            }
        }
        populateQueryContext(variable, ctx);
        return variable;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Integer visitAttribute_index(@NotNull SiddhiQLParser.Attribute_indexContext ctx) {
        int index;
        if (ctx.LAST() != null) {
            index = SiddhiConstants.LAST;
            if (ctx.INT_LITERAL() != null) {
                index -= Integer.parseInt(ctx.INT_LITERAL().getText());
            }
        } else {
            index = Integer.parseInt(ctx.INT_LITERAL().getText());
        }
        return index;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Object visitProperty_name(@NotNull SiddhiQLParser.Property_nameContext ctx) {

        StringBuilder stringBuilder = new StringBuilder();
        List<SiddhiQLParser.NameContext> propertyNameList = ctx.name();
        List<SiddhiQLParser.Property_separatorContext> propertySeparator = ctx.property_separator();
        for (int i = 0; i < propertyNameList.size(); i++) {
            stringBuilder.append(visit(propertyNameList.get(i)));
            if (i < propertySeparator.size()) {
                stringBuilder.append(propertySeparator.get(i).getText());
            }
        }
        return stringBuilder.toString();
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Source visitSource(@NotNull SiddhiQLParser.SourceContext ctx) {
        Source source = new Source();
        source.streamId = (String) visit(ctx.stream_id());
        source.isInnerStream = ctx.inner != null;

        activeStreams.add(ctx.getText());

        return source;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Object visitName(@NotNull SiddhiQLParser.NameContext ctx) {
        if (ctx.id() != null) {
            return visit(ctx.id());
        } else if (ctx.keyword() != null) {
            return visit(ctx.keyword());
        } else {
            throw newSiddhiParserException(ctx);
        }
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Object[] visitCollect(@NotNull SiddhiQLParser.CollectContext ctx) {
        Object[] minMax = new Object[2];

        if (ctx.start == null && ctx.end == null) {
            Integer value = Integer.parseInt(ctx.INT_LITERAL(0).getText());
            minMax[0] = value;
            minMax[1] = value;
        } else {
            if (ctx.start != null) {
                minMax[0] = Integer.parseInt(ctx.start.getText());
            }
            if (ctx.end != null) {
                minMax[1] = Integer.parseInt(ctx.end.getText());
            }
        }
        return minMax;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Object visitAttribute_type(@NotNull SiddhiQLParser.Attribute_typeContext ctx) {

//        attribute_type
//                :STRING
//                |INT
//                |LONG
//                |FLOAT
//                |DOUBLE
//                |BOOL
//                |OBJECT
//        ;

        if (ctx.STRING() != null) {
            return Attribute.Type.STRING;
        } else if (ctx.INT() != null) {
            return Attribute.Type.INT;
        } else if (ctx.LONG() != null) {
            return Attribute.Type.LONG;
        } else if (ctx.FLOAT() != null) {
            return Attribute.Type.FLOAT;
        } else if (ctx.DOUBLE() != null) {
            return Attribute.Type.DOUBLE;
        } else if (ctx.BOOL() != null) {
            return Attribute.Type.BOOL;
        } else if (ctx.OBJECT() != null) {
            return Attribute.Type.OBJECT;
        } else {
            throw newSiddhiParserException(ctx);
        }
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public JoinInputStream.Type visitJoin(@NotNull SiddhiQLParser.JoinContext ctx) {
        if (ctx.OUTER() != null) {
            if (ctx.FULL() != null) {
                return JoinInputStream.Type.FULL_OUTER_JOIN;
            } else if (ctx.RIGHT() != null) {
                return JoinInputStream.Type.RIGHT_OUTER_JOIN;
            } else if (ctx.LEFT() != null) {
                return JoinInputStream.Type.LEFT_OUTER_JOIN;
            } else {
                throw newSiddhiParserException(ctx, "Found " + ctx.getText() + " but only FULL OUTER JOIN, RIGHT " +
                        "OUTER JOIN, LEFT OUTER JOIN are supported!");
            }
        }
        return JoinInputStream.Type.JOIN;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Constant visitConstant_value(@NotNull SiddhiQLParser.Constant_valueContext ctx) {

//        constant_value
//                :bool_value
//                |signed_double_value
//                |signed_float_value
//                |signed_long_value
//                |signed_int_value
//                |time_value
//                |string_value
//        ;

        Constant constant;

        if (ctx.bool_value() != null) {
            constant = Expression.value(((BoolConstant) visit(ctx.bool_value())).getValue());
        } else if (ctx.signed_double_value() != null) {
            constant = Expression.value(((DoubleConstant) visit(ctx.signed_double_value())).getValue());
        } else if (ctx.signed_float_value() != null) {
            constant = Expression.value(((FloatConstant) visit(ctx.signed_float_value())).getValue());
        } else if (ctx.signed_long_value() != null) {
            constant = Expression.value(((LongConstant) visit(ctx.signed_long_value())).getValue());
        } else if (ctx.signed_int_value() != null) {
            constant = Expression.value(((IntConstant) visit(ctx.signed_int_value())).getValue());
        } else if (ctx.time_value() != null) {
            constant = (TimeConstant) visit(ctx.time_value());
        } else if (ctx.string_value() != null) {
            constant = Expression.value(((StringConstant) visit(ctx.string_value())).getValue());
        } else {
            throw newSiddhiParserException(ctx);
        }
        populateQueryContext(constant, ctx);
        return constant;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public String visitId(@NotNull SiddhiQLParser.IdContext ctx) {
        return ctx.getText();
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public Object visitKeyword(@NotNull SiddhiQLParser.KeywordContext ctx) {
        return ctx.getText();
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public TimeConstant visitTime_value(@NotNull SiddhiQLParser.Time_valueContext ctx) {
        TimeConstant timeValueInMillis = Expression.Time.milliSec(0);
        if (ctx.millisecond_value() != null) {
            timeValueInMillis.milliSec(((TimeConstant) visit(ctx.millisecond_value())).value());
        }
        if (ctx.second_value() != null) {
            timeValueInMillis.milliSec(((TimeConstant) visit(ctx.second_value())).value());
        }
        if (ctx.minute_value() != null) {
            timeValueInMillis.milliSec(((TimeConstant) visit(ctx.minute_value())).value());
        }
        if (ctx.hour_value() != null) {
            timeValueInMillis.milliSec(((TimeConstant) visit(ctx.hour_value())).value());
        }
        if (ctx.day_value() != null) {
            timeValueInMillis.milliSec(((TimeConstant) visit(ctx.day_value())).value());
        }
        if (ctx.week_value() != null) {
            timeValueInMillis.milliSec(((TimeConstant) visit(ctx.week_value())).value());
        }
        if (ctx.month_value() != null) {
            timeValueInMillis.milliSec(((TimeConstant) visit(ctx.month_value())).value());
        }
        if (ctx.year_value() != null) {
            timeValueInMillis.milliSec(((TimeConstant) visit(ctx.year_value())).value());
        }
        populateQueryContext(timeValueInMillis, ctx);
        return timeValueInMillis;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public TimeConstant visitYear_value(@NotNull SiddhiQLParser.Year_valueContext ctx) {
        TimeConstant timeConstant = Expression.Time.year(Long.parseLong(ctx.INT_LITERAL().getText().
                replaceFirst("[lL]", "")));
        populateQueryContext(timeConstant, ctx);
        return timeConstant;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public TimeConstant visitMonth_value(@NotNull SiddhiQLParser.Month_valueContext ctx) {
        TimeConstant timeConstant = Expression.Time.month(Long.parseLong(ctx.INT_LITERAL().getText().
                replaceFirst("[lL]", "")));
        populateQueryContext(timeConstant, ctx);
        return timeConstant;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public TimeConstant visitWeek_value(@NotNull SiddhiQLParser.Week_valueContext ctx) {
        TimeConstant timeConstant = Expression.Time.week(Long.parseLong(ctx.INT_LITERAL().getText().
                replaceFirst("[lL]", "")));
        populateQueryContext(timeConstant, ctx);
        return timeConstant;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public TimeConstant visitDay_value(@NotNull SiddhiQLParser.Day_valueContext ctx) {
        TimeConstant timeConstant = Expression.Time.day(Long.parseLong(ctx.INT_LITERAL().getText().
                replaceFirst("[lL]", "")));
        populateQueryContext(timeConstant, ctx);
        return timeConstant;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public TimeConstant visitHour_value(@NotNull SiddhiQLParser.Hour_valueContext ctx) {
        TimeConstant timeConstant = Expression.Time.hour(Long.parseLong(ctx.INT_LITERAL().getText().
                replaceFirst("[lL]", "")));
        populateQueryContext(timeConstant, ctx);
        return timeConstant;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public TimeConstant visitMinute_value(@NotNull SiddhiQLParser.Minute_valueContext ctx) {
        TimeConstant timeConstant = Expression.Time.minute(Long.parseLong(ctx.INT_LITERAL().getText().
                replaceFirst("[lL]", "")));
        populateQueryContext(timeConstant, ctx);
        return timeConstant;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public TimeConstant visitSecond_value(@NotNull SiddhiQLParser.Second_valueContext ctx) {
        TimeConstant timeConstant = Expression.Time.sec(Long.parseLong(ctx.INT_LITERAL().getText().
                replaceFirst("[lL]", "")));
        populateQueryContext(timeConstant, ctx);
        return timeConstant;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public TimeConstant visitMillisecond_value(@NotNull SiddhiQLParser.Millisecond_valueContext ctx) {
        TimeConstant timeConstant = Expression.Time.milliSec(Long.parseLong(ctx.INT_LITERAL().getText().
                replaceFirst("[lL]", "")));
        populateQueryContext(timeConstant, ctx);
        return timeConstant;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public DoubleConstant visitSigned_double_value(@NotNull SiddhiQLParser.Signed_double_valueContext ctx) {
        DoubleConstant doubleConstant = Expression.value(Double.parseDouble(ctx.getText()));
        populateQueryContext(doubleConstant, ctx);
        return doubleConstant;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public LongConstant visitSigned_long_value(@NotNull SiddhiQLParser.Signed_long_valueContext ctx) {
        LongConstant longConstant = Expression.value(Long.parseLong(ctx.getText().replaceFirst("[lL]", "")));
        populateQueryContext(longConstant, ctx);
        return longConstant;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public FloatConstant visitSigned_float_value(@NotNull SiddhiQLParser.Signed_float_valueContext ctx) {
        FloatConstant floatConstant = Expression.value(Float.parseFloat(ctx.getText()));
        populateQueryContext(floatConstant, ctx);
        return floatConstant;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public IntConstant visitSigned_int_value(@NotNull SiddhiQLParser.Signed_int_valueContext ctx) {
        IntConstant intConstant = Expression.value(Integer.parseInt(ctx.getText()));
        populateQueryContext(intConstant, ctx);
        return intConstant;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public BoolConstant visitBool_value(@NotNull SiddhiQLParser.Bool_valueContext ctx) {
        BoolConstant boolConstant = Expression.value("true".equalsIgnoreCase(ctx.getText()));
        populateQueryContext(boolConstant, ctx);
        return boolConstant;
    }

    /**
     * {@inheritDoc}
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx Siddhi query parser context
     */
    @Override
    public StringConstant visitString_value(@NotNull SiddhiQLParser.String_valueContext ctx) {
        StringConstant stringConstant = Expression.value(ctx.STRING_LITERAL().getText());
        populateQueryContext(stringConstant, ctx);
        return stringConstant;
    }

    public SiddhiParserException newSiddhiParserException(ParserRuleContext context) {
        return new SiddhiParserException("Syntax error in SiddhiQL, near '" + context.getText() + "'.",
                new int[]{context.getStart().getLine(), context.getStart().getCharPositionInLine()},
                new int[]{context.getStop().getLine(), context.getStop().getCharPositionInLine()});
    }

    public SiddhiParserException newSiddhiParserException(ParserRuleContext context, String message) {
        return new SiddhiParserException("Syntax error in SiddhiQL, near '" + context.getText() + "', " + message + ".",
                new int[]{context.getStart().getLine(), context.getStart().getCharPositionInLine()},
                new int[]{context.getStop().getLine(), context.getStop().getCharPositionInLine()});
    }

    public SiddhiParserException newSiddhiParserException(ParserRuleContext context, String message, Throwable t) {
        return new SiddhiParserException("Syntax error in SiddhiQL, near '" + context.getText() + "', " + message + ".",
                t, new int[]{context.getStart().getLine(), context.getStart().getCharPositionInLine()},
                new int[]{context.getStop().getLine(), context.getStop().getCharPositionInLine()});
    }


    @Override
    public TimePeriod.Duration visitAggregation_time_duration(
            @NotNull SiddhiQLParser.Aggregation_time_durationContext ctx) {
        if (ctx.SECONDS() != null) {
            return TimePeriod.Duration.SECONDS;
        }

        if (ctx.MINUTES() != null) {
            return TimePeriod.Duration.MINUTES;
        }

        if (ctx.HOURS() != null) {
            return TimePeriod.Duration.HOURS;
        }

        if (ctx.DAYS() != null) {
            return TimePeriod.Duration.DAYS;
        }

        if (ctx.MONTHS() != null) {
            return TimePeriod.Duration.MONTHS;
        }

        if (ctx.YEARS() != null) {
            return TimePeriod.Duration.YEARS;
        }

        throw newSiddhiParserException(ctx, "Found " + ctx.getText() + ", but only values SECONDS, MINUTES, HOURS,"
                + " DAYS, WEEKS, MONTHS or YEARS are supported");
    }

    @Override
    public TimePeriod visitAggregation_time_interval(@NotNull SiddhiQLParser.Aggregation_time_intervalContext ctx) {
        List<TimePeriod.Duration> durations = new ArrayList<TimePeriod.Duration>();

        for (SiddhiQLParser.Aggregation_time_durationContext context : ctx.aggregation_time_duration()) {
            durations.add((TimePeriod.Duration) visit(context));
        }

        TimePeriod.Duration[] durationVarArg = new TimePeriod.Duration[durations.size()];
        durationVarArg = durations.toArray(durationVarArg);
        TimePeriod timePeriod = TimePeriod.interval(durationVarArg);
        populateQueryContext(timePeriod, ctx);
        return timePeriod;
    }

    @Override
    public TimePeriod visitAggregation_time_range(@NotNull SiddhiQLParser.Aggregation_time_rangeContext ctx) {

        // read left and right contexts
        SiddhiQLParser.Aggregation_time_durationContext left = ctx.aggregation_time_duration().get(0);
        SiddhiQLParser.Aggregation_time_durationContext right = ctx.aggregation_time_duration().get(1);

        // Visit left and right expression using above contexts and create a new
        // RangeTimeSpecifier object
        TimePeriod.Duration leftTimeDuration = visitAggregation_time_duration(left);
        TimePeriod.Duration rightTimeDuration = visitAggregation_time_duration(right);
        TimePeriod timePeriod = TimePeriod.range(leftTimeDuration, rightTimeDuration);
        populateQueryContext(timePeriod, ctx);
        return timePeriod;
    }

    @Override
    public TimePeriod visitAggregation_time(@NotNull SiddhiQLParser.Aggregation_timeContext ctx) {

        if (ctx.aggregation_time_interval() != null) {
            TimePeriod timePeriod = visitAggregation_time_interval(ctx.aggregation_time_interval());
            populateQueryContext(timePeriod, ctx);
            return timePeriod;
        } else if (ctx.aggregation_time_range() != null) {
            TimePeriod timePeriod = visitAggregation_time_range(ctx.aggregation_time_range());
            populateQueryContext(timePeriod, ctx);
            return timePeriod;
        }
        throw newSiddhiParserException(ctx, "Found " + ctx.getText()
                + " but only comma separated time durations, or time duration ... time duration is supported!");
    }

    @Override
    public AggregationDefinition visitDefinition_aggregation_final(
            @NotNull SiddhiQLParser.Definition_aggregation_finalContext ctx) {
        return (AggregationDefinition) visit(ctx.definition_aggregation());
    }

    @Override
    public AggregationDefinition visitDefinition_aggregation(
            @NotNull SiddhiQLParser.Definition_aggregationContext ctx) {
        // Read the name of the aggregation
        String aggregationName = (String) visitAggregation_name(ctx.aggregation_name());

        // Create the aggregation using the extracted aggregation name
        AggregationDefinition aggregationDefinition = AggregationDefinition.id(aggregationName);

        // Get all annotation and populate the aggregation
        for (SiddhiQLParser.AnnotationContext annotationContext : ctx.annotation()) {
            aggregationDefinition.annotation((Annotation) visit(annotationContext));
        }

        // Attach the input stream
        BasicSingleInputStream basicSingleInputStream = (BasicSingleInputStream) visit(ctx.standard_stream());
        aggregationDefinition.from(basicSingleInputStream);

        // Extract the selector and attach it to the new aggregation
        BasicSelector selector = (BasicSelector) visit(ctx.group_by_query_selection());
        aggregationDefinition.select(selector);

        // Get the variable (if available) and aggregate on that variable
        if (ctx.attribute_reference() != null) {
            Variable aggregatedBy = (Variable) visit(ctx.attribute_reference());
            aggregationDefinition.aggregateBy(aggregatedBy);
        }

        // Extract the specified time-durations and attache it to the aggregation definition
        TimePeriod timePeriod = (TimePeriod) visit(ctx.aggregation_time());
        aggregationDefinition.every(timePeriod);
        populateQueryContext(aggregationDefinition, ctx);
        return aggregationDefinition;
    }

    @Override
    public Object visitWithin_time_range(SiddhiQLParser.Within_time_rangeContext ctx) {
        Within within;
        if (ctx.end_pattern == null) {
            within = Within.within((Expression) visit(ctx.start_pattern));
        } else {
            within = Within.within((Expression) visit(ctx.start_pattern), (Expression) visit(ctx.end_pattern));
        }
        populateQueryContext(within, ctx);
        return within;
    }

    @Override
    public Object visitStore_query_final(SiddhiQLParser.Store_query_finalContext ctx) {
        return visit(ctx.store_query());
    }

    @Override
    public Object visitStore_query(SiddhiQLParser.Store_queryContext ctx) {
        StoreQuery storeQuery = StoreQuery.query().from((InputStore) visit(ctx.store_input()));
        if (ctx.query_section() != null) {
            storeQuery = storeQuery.select((Selector) visit(ctx.query_section()));
        }
        populateQueryContext(storeQuery, ctx);
        return storeQuery;
    }

    @Override
    public Object visitStore_input(SiddhiQLParser.Store_inputContext ctx) {
        String sourceId = (String) visit(ctx.source_id());
        String alias = null;
        if (ctx.alias() != null) {
            alias = (String) visit(ctx.source_id());
        }
        Store store = InputStore.store(alias, sourceId);
        Expression expression = null;
        if (ctx.expression() != null) {
            expression = (Expression) visit(ctx.expression());
        }
        populateQueryContext(store, ctx);
        if (ctx.per() != null) {
            return store.on(expression, (Within) visit(ctx.within_time_range()), (Expression) visit(ctx.per()));
        } else if (expression != null) {
            return store.on(expression);
        } else {
            return store;
        }
    }

    private void populateQueryContext(SiddhiElement siddhiElement, @NotNull ParserRuleContext ctx) {
        if (siddhiElement != null && siddhiElement.getQueryContextStartIndex() == null &&
                siddhiElement.getQueryContextEndIndex() == null) {
            siddhiElement.setQueryContextStartIndex(new int[]{ctx.getStart().getLine(),
                    ctx.getStart().getCharPositionInLine()});
            siddhiElement.setQueryContextEndIndex(new int[]{ctx.getStop().getLine(),
                    ctx.getStop().getCharPositionInLine() + 1 + ctx.getStop().getStopIndex()
                            - ctx.getStop().getStartIndex()});
        }
    }

    private static class Source {
        private String streamId;
        private boolean isInnerStream;
    }

    private static class StreamReference {
        private String streamId;
        private boolean isInnerStream;
        private Integer streamIndex;
    }
}
