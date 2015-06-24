/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.siddhi.extension.ml;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.populater.ComplexEventPopulater;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.stream.StreamProcessor;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PredictStreamProcessor extends StreamProcessor {

    private static final String PREDICTION = "prediction";

    private ModelHandler modelHandler;
    private String modelStorageLocation;
    private boolean attributeSelectionAvailable;
    private Map<Integer, Integer> attributeIndexMap;           // <feature-index, attribute-index> pairs
    private int selectedAttributesSize;

    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor,
            StreamEventCloner streamEventCloner, ComplexEventPopulater complexEventPopulater) {

        while (streamEventChunk.hasNext()) {

            StreamEvent event = streamEventChunk.next();

            Object[] data;
            String[] featureValues;
            data = event.getOutputData();
            if (attributeSelectionAvailable) {
                featureValues = new String[selectedAttributesSize];
            } else {
                featureValues = new String[data.length - 1];
            }

            for (Map.Entry<Integer, Integer> entry : attributeIndexMap.entrySet()) {
                int featureIndex = entry.getKey();
                int attributeIndex = entry.getValue();
                featureValues[featureIndex] = String.valueOf(data[attributeIndex]);
            }

            if (featureValues != null) {
                try {
                    String predictionResult = modelHandler.predict(featureValues);
                    Object[] output = new Object[] { predictionResult };
                    complexEventPopulater.populateComplexEvent(event, output);
                } catch (Exception e) {
                    log.error("Error while predicting", e);
                    throw new ExecutionPlanRuntimeException("Error while predicting", e);
                }
            }
        }
        nextProcessor.process(streamEventChunk);
    }

    @Override
    protected List<Attribute> init(AbstractDefinition inputDefinition, ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {

        if(attributeExpressionExecutors.length == 0) {
            throw new ExecutionPlanValidationException("ML model storage location has not been defined as the first parameter");
        } else if(attributeExpressionExecutors.length == 1) {
            attributeSelectionAvailable = false;    // model-storage-location
        } else {
            attributeSelectionAvailable = true;  // model-storage-location, stream-attributes list
            selectedAttributesSize = attributeExpressionExecutors.length - 1;
        }

        if(attributeExpressionExecutors[0] instanceof ConstantExpressionExecutor)  {
            Object constantObj = ((ConstantExpressionExecutor) attributeExpressionExecutors[0]).getValue();
            modelStorageLocation = (String) constantObj;
        } else {
            throw new ExecutionPlanValidationException("ML model storage-location has not been defined as the first parameter");
        }

        return Arrays.asList(new Attribute(PREDICTION, Attribute.Type.DOUBLE));
    }

    @Override
    public void start() {
        try {
            modelHandler = new ModelHandler(modelStorageLocation);
            populateFeatureAttributeMapping();
        } catch (Exception e) {
            log.error("Error while retrieving ML-model : " + modelStorageLocation, e);
            throw new ExecutionPlanCreationException("Error while retrieving ML-model : " + modelStorageLocation + "\n" + e.getMessage());
        }
    }

    /**
     * Match the attribute index values of stream with feature index value of the model
     * @throws Exception
     */
    private void populateFeatureAttributeMapping() throws Exception {
        attributeIndexMap = new HashMap<Integer, Integer>();
        Map<String, Integer> featureIndexMap = modelHandler.getFeatures();
        List<Integer> newToOldIndicesList = modelHandler.getNewToOldIndicesList();

        if(attributeSelectionAvailable) {
            for (ExpressionExecutor expressionExecutor : attributeExpressionExecutors) {
                if(expressionExecutor instanceof VariableExpressionExecutor) {
                    VariableExpressionExecutor variable = (VariableExpressionExecutor) expressionExecutor;
                    String variableName = variable.getAttribute().getName();
                    if (featureIndexMap.get(variableName) != null) {
                        int featureIndex = featureIndexMap.get(variableName);
                        int newFeatureIndex = newToOldIndicesList.indexOf(featureIndex);
                        int attributeIndex = inputDefinition.getAttributePosition(variableName);
                        attributeIndexMap.put(newFeatureIndex, attributeIndex);
                    } else {
                        throw new ExecutionPlanCreationException("No matching feature name found in the model " +
                                "for the attribute : " + variableName);
                    }
                }
            }
        } else {
            String[] attributeNames = inputDefinition.getAttributeNameArray();
            for(String attributeName : attributeNames) {
                if (featureIndexMap.get(attributeName) != null) {
                    int featureIndex = featureIndexMap.get(attributeName);
                    int newFeatureIndex = newToOldIndicesList.indexOf(featureIndex);
                    int attributeIndex = inputDefinition.getAttributePosition(attributeName);
                    attributeIndexMap.put(newFeatureIndex, attributeIndex);
                } else {
                    throw new ExecutionPlanCreationException("No matching feature name found in the model " +
                            "for the attribute : " + attributeName);
                }
            }
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public Object[] currentState() {
        return new Object[0];
    }

    @Override
    public void restoreState(Object[] state) {

    }
}
