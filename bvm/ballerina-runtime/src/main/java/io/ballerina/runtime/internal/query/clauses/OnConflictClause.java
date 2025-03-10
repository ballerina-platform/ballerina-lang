package io.ballerina.runtime.internal.query.clauses;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.query.pipeline.Frame;

import java.util.stream.Stream;

public class OnConflictClause implements PipelineStage {
    private final BFunctionPointer onConflictFunction;
    private final Environment env;

    public OnConflictClause(Environment env, BFunctionPointer onConflictFunction) {
        this.onConflictFunction = onConflictFunction;
        this.env = env;
    }

    public static OnConflictClause initOnConflictClause(Environment env, BFunctionPointer onConflictFunction) {
        return new OnConflictClause(env, onConflictFunction);
    }


    @Override
    public Stream<Frame> process(Stream<Frame> inputStream) {
        return inputStream.map(frame -> {
            Object result = onConflictFunction.call(env.getRuntime(), frame.getRecord());
            if (result instanceof BMap) {
                frame.updateRecord((BMap<BString, Object>) result);
                return frame;
            } else {
                throw (BError) result;
            }
        });
    }
}
