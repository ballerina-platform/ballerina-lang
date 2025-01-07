package io.ballerina.runtime.internal.query.utils;

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.query.clauses.FromClause;
import io.ballerina.runtime.internal.query.clauses.SelectClause;
import io.ballerina.runtime.internal.query.clauses.WhereClause;
import io.ballerina.runtime.internal.query.pipeline.FrameContext;
import io.ballerina.runtime.internal.query.pipeline.StreamPipeline;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class StreamPipelineUtils {
    public static <T> StreamPipeline<T> initializePipeline(Class<T> completionType, Object collection) {
        StreamPipeline<T> pipeline = new StreamPipeline<>(completionType, collection);
        return pipeline;
    }

    public static <T> void addFromClause(StreamPipeline<T> pipeline, Map<String, String> variableMappings) {
        FromClause<T> fromClause = new FromClause<>(pipeline.getCollection(), variableMappings);
        pipeline.addStage(fromClause);
    }

    public static <T> void addSelectClause(StreamPipeline<T> pipeline, Map<String, String> selectMappings) {
        SelectClause<T> selectClause = new SelectClause<>(selectMappings);
        pipeline.addStage(selectClause);
    }

    public static <T> void addWhereClause(StreamPipeline<T> pipeline, Predicate<FrameContext<T>> condition) {
        WhereClause<T> whereClause = new WhereClause<>(condition);
        pipeline.addStage(whereClause);
    }

//    public static <T> BArray toBArray(StreamPipeline<T> pipeline) {
//        Stream<FrameContext<T>> stream = pipeline.getStream();
//        System.out.println(stream);
//        BArray result = ValueCreator.createArrayValue(stream.toArray(), pipeline.getCompletionType().)
//        return ValueCreator.createArrayValue(stream.toArray());
//    }
}
