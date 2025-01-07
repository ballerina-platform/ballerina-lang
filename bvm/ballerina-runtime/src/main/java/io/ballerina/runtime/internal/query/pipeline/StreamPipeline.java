package io.ballerina.runtime.internal.query.pipeline;

import io.ballerina.runtime.internal.query.clauses.PipelineStage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class StreamPipeline<T> {
    private Stream<FrameContext<T>> stream;
    private final Class<T> completionType;
    private final List<PipelineStage<T>> stages = new ArrayList<>();
    private final Object collection;

    public StreamPipeline(Class<T> completionType, Object collection) {
        this.completionType = completionType;
        this.collection = collection;
    }

    public void addStage(PipelineStage<T> stage) {
        stages.add(stage);
    }

    public void execute(Object collection) {
        for (PipelineStage<T> stage : stages) {
            stream = stage.apply(stream);
        }
    }

    public Object getCollection(){
        return collection;
    }

    public Stream<FrameContext<T>> getStream() {
        return stream;
    }

    public Class<T> getCompletionType() {
        return completionType;
    }
}
