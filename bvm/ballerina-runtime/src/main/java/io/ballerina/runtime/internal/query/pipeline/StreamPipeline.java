/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com)
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.internal.query.pipeline;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.internal.query.clauses.PipelineStage;
import io.ballerina.runtime.internal.query.utils.BallerinaIteratorUtils;
import io.ballerina.runtime.internal.query.utils.QueryException;
import io.ballerina.runtime.internal.values.ErrorValue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A class that represents a stream pipeline for processing data.
 *
 * @since 2201.13.0
 */
public class StreamPipeline {

    private Stream<Frame> stream;
    private Supplier<Stream<Frame>> streamSupplier;
    private final List<PipelineStage> pipelineStages;
    private final BTypedesc constraintType;
    private final BTypedesc completionType;
    private final boolean isLazyLoading;
    private final Environment env;
    private final Iterator<?> itr;

    /**
     * Constructor for creating a StreamPipeline.
     *
     * @param collection      The collection to process.
     * @param constraintType  The type descriptor for the constraint type.
     * @param completionType  The type descriptor for the completion type.
     * @param isLazyLoading   Flag to indicate if lazy loading is enabled.
     */
    private StreamPipeline(Environment env,
                          Object collection,
                          BTypedesc constraintType,
                          BTypedesc completionType,
                          boolean isLazyLoading) {
        this.env = env;
        this.pipelineStages = new ArrayList<>();
        this.constraintType = constraintType;
        this.completionType = completionType;
        this.isLazyLoading = isLazyLoading;
        this.itr = BallerinaIteratorUtils.getIterator(env, collection);
        this.stream = initializeFrameStream(itr);
    }

    /**
     * Initializes a stream pipeline.
     *
     * @param env The environment.
     * @param collection The collection to process.
     * @param constraintType The type descriptor for the constraint type.
     * @param completionType The type descriptor for the completion type.
     * @param isLazyLoading Flag to indicate if lazy loading is enabled.
     * @return The stream pipeline.
     */
    public static Object initStreamPipeline(Environment env,
                                                Object collection,
                                                BTypedesc constraintType,
                                                BTypedesc completionType,
                                                boolean isLazyLoading) {
        try {
            return new StreamPipeline(env, collection, constraintType, completionType, isLazyLoading);
        } catch (ErrorValue e) {
            return e;
        }
    }

    /**
     * Adds a stage (clause) to the pipeline.
     *
     * @param streamPipeline The stream pipeline.
     * @param pipelineStage The pipeline stage (clause).
     */
    public static void addStreamFunction(StreamPipeline streamPipeline, PipelineStage pipelineStage) {
        streamPipeline.addStage(pipelineStage);
    }

    /**
     *  Add all pipeline process steps to the pipeline.
     *
     * @param pipeline The stream pipeline.
     * @return The processed stream.
     */
    public static Object getStreamFromPipeline(StreamPipeline pipeline) {
        try {
            pipeline.execute();
        } catch (QueryException e) {
            return e.getError();
        }

        return pipeline;
    }

    /**
     * Queue a stage (function) to the pipeline.
     *
     * @param stage A function to process each frame.
     */
    public void addStage(PipelineStage stage) {
        this.pipelineStages.add(stage);
    }

    /**
     * Processes the stream through all the pipeline stages.
     */
    public void execute() {
        for (PipelineStage stage : pipelineStages) {
            stream = stage.process(stream);
        }
    }

    /**
     * Initializes a stream of `Frame` objects from the provided Ballerina collection.
     *
     * @param itr The iterator for the Ballerina collection.
     * @return A Java stream of `Frame` objects.
     */
    private Stream<Frame> initializeFrameStream(Iterator<?> itr) throws ErrorValue {
        return BallerinaIteratorUtils.toStream(itr);
    }

    /**
     * Resets the pipeline for reuse.
     */
    public void reset() {

    }

    public BTypedesc getConstraintType() {
        return constraintType;
    }

    public BTypedesc getCompletionType() {
        return completionType;
    }

    public Stream<Frame> getStream() {
        return stream;
    }
}
