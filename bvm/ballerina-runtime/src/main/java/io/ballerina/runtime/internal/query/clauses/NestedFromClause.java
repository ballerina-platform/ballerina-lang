package io.ballerina.runtime.internal.query.clauses;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.query.pipeline.Frame;
import io.ballerina.runtime.internal.query.utils.BallerinaIteratorUtils;

import java.util.*;
import java.util.stream.Stream;

/**
 * Represents a nested `from` clause in the query pipeline that processes a stream of frames.
 */
public class NestedFromClause implements PipelineStage {
    private final BFunctionPointer collectionFunc;
    private final Environment env;

    /**
     * Constructor for the NestedFromClause.
     *
     * @param env The runtime environment.
     * @param collectionFunc The function to extract the collection from each frame.
     */
    public NestedFromClause(Environment env, BFunctionPointer collectionFunc) {
        this.collectionFunc = collectionFunc;
        this.env = env;
    }

    public static NestedFromClause initNestedFromClause(Environment env, BFunctionPointer collectionFunc) {
        return new NestedFromClause(env, collectionFunc);
    }

    /**
     * Processes a stream of frames by applying the collection function and iterating over the resulting collection.
     *
     * @param inputStream The input stream of frames.
     * @return A stream of frames from the nested collections.
     */
    @Override
    public Stream<Frame> process(Stream<Frame> inputStream) {
        return inputStream.flatMap(frame -> {
            try {
                Object collection = collectionFunc.call(env.getRuntime(), new Object[]{frame.getRecord()});

                if (collection == null) {
                    return Stream.empty();
                }

                Iterator<Object> itr = BallerinaIteratorUtils.getIterator(env, collection);
                List<Frame> results = new ArrayList<>();

                while (itr.hasNext()) {
                    Object item = itr.next();

                    Map<Object, Object> refs = new HashMap<>();
                    BMap<BString, Object> newRecord = (BMap<BString, Object>) frame.getRecord().copy(refs);

                    if (item instanceof BMap) {
                        newRecord.put(StringUtils.fromString("value"), item);
                        results.add(new Frame(newRecord));
                    } else {
                        throw new RuntimeException("Unsupported item type: " + item.getClass());
                    }
                }

                return results.stream();
            } catch (Exception e) {
                throw new RuntimeException("Error applying collection function in nested from clause", e);
            }
        });
    }
}
