/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.annotation.util;

/**
 * Siddhi annotation constants class.
 */
public class AnnotationConstants {
    public static final String SINK_MAPPER_SUPER_CLASS = "org.wso2.siddhi.core.stream.output.sink.SinkMapper";
    public static final String SINK_SUPER_CLASS =
            "org.wso2.siddhi.core.stream.output.sink.Sink";
    public static final String SCRIPT_SUPER_CLASS =
            "org.wso2.siddhi.core.function.Script";
    public static final String FUNCTION_EXECUTOR_SUPER_CLASS =
            "org.wso2.siddhi.core.executor.function.FunctionExecutor";
    public static final String AGGREGATION_ATTRIBUTE_SUPER_CLASS =
            "org.wso2.siddhi.core.query.selector.attribute.aggregator.AttributeAggregator";
    public static final String DISTRIBUTION_STRATEGY_SUPER_CLASS =
            "org.wso2.siddhi.core.stream.output.sink.distributed.DistributionStrategy";
    public static final String STREAM_PROCESSOR_SUPER_CLASS =
            "org.wso2.siddhi.core.query.processor.stream.StreamProcessor";
    public static final String STREAM_FUNCTION_PROCESSOR_SUPER_CLASS =
            "org.wso2.siddhi.core.query.processor.stream.function.StreamFunctionProcessor";
    public static final String STORE_SUPER_CLASS = "org.wso2.siddhi.core.table.record.AbstractRecordTable";
    public static final String SOURCE_SUPER_CLASS = "org.wso2.siddhi.core.stream.input.source.Source";
    public static final String SOURCE_MAPPER_SUPER_CLASS = "org.wso2.siddhi.core.stream.input.source.SourceMapper";
    public static final String WINDOW_PROCESSOR_CLASS =
            "org.wso2.siddhi.core.query.processor.stream.window.WindowProcessor";
    public static final String INCREMENTAL_ATTRIBUTE_AGGREGATOR_SUPER_CLASS =
            "org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental.IncrementalAttributeAggregator";

    public static final String DISTRIBUTION_STRATEGY_NAMESPACE = "distributionStrategy";
    public static final String STORE_NAMESPACE = "store";
    public static final String SOURCE_NAMESPACE = "source";
    public static final String SOURCE_MAPPER_NAMESPACE = "sourceMapper";
    public static final String SINK_NAMESPACE = "sink";
    public static final String SINK_MAPPER_NAMESPACE = "sinkMapper";
}
