package io.ballerina.runtime.internal.query.clauses;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.query.pipeline.Frame;
import io.ballerina.runtime.internal.query.utils.BallerinaIteratorUtils;
import io.ballerina.runtime.internal.query.utils.QueryException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_QUERY_PKG_ID;
import static io.ballerina.runtime.internal.query.utils.QueryConstants.VALUE_FIELD;

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
                Object collection = collectionFunc.call(env.getRuntime(), frame.getRecord());

                if (collection == null) {
                    return Stream.empty();
                }

                Iterator<Object> itr = BallerinaIteratorUtils.getIterator(env, collection);
                List<Frame> results = new ArrayList<>();

                while (itr.hasNext()) {
                    Object item = itr.next();

                    BMap<BString, Object> originalRecord = frame.getRecord();
                    BMap<BString, Object> newRecord = ValueCreator.createRecordValue(BALLERINA_QUERY_PKG_ID, "_Frame");

                    originalRecord.entrySet().forEach(entry -> {
                        BString key = entry.getKey();
                        Object value = entry.getValue();
                        newRecord.put(key, value);
                    });
                    newRecord.put(VALUE_FIELD, item);
                    results.add(new Frame(newRecord));
                }

                return results.stream();
            } catch (BError e) {
                throw new QueryException(e);
            }
        });
    }
}