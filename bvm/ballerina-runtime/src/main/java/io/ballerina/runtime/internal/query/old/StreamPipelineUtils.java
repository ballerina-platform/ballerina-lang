//package io.ballerina.runtime.internal.query.utils;
//
//
//import io.ballerina.runtime.internal.query.clauses.FromClause;
//import io.ballerina.runtime.internal.query.clauses.LimitClause;
//import io.ballerina.runtime.internal.query.clauses.SelectClause;
//import io.ballerina.runtime.internal.query.clauses.WhereClause;
//import io.ballerina.runtime.internal.query.old.FrameContext;
//import io.ballerina.runtime.internal.query.pipeline.StreamPipeline;
//
//import java.util.Map;
//import java.util.function.Function;
//import java.util.function.Predicate;
//
//
//public class StreamPipelineUtils {
//    public static <T> StreamPipeline<T> initializePipeline(Class<T> completionType, Object collection) {
//        StreamPipeline<T> pipeline = new StreamPipeline<>(completionType, collection);
//        return pipeline;
//    }
//
//    public static <T> void addFromClause(StreamPipeline<T> pipeline, Map<String, String> variableMappings) {
//        FromClause<T> fromClause = new FromClause<>(pipeline.getCollection(), variableMappings);
//        pipeline.addStage(fromClause);
//    }
//

//
//    public static <T> void addWhereClause(StreamPipeline<T> pipeline, Predicate<FrameContext<T>> condition) {
//        WhereClause<T> whereClause = new WhereClause<>(condition);
//        pipeline.addStage(whereClause);
//    }
//
//    public static <T> void addLimitClause(StreamPipeline<T> pipeline, long limit) {
//        LimitClause<T> limitClause = new LimitClause<>(limit);
//        pipeline.addStage(limitClause);
//    }
//}
