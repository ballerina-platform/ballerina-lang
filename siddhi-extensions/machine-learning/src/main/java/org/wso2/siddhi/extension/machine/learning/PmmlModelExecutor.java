/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.extension.machine.learning;

import org.apache.log4j.Logger;
import org.dmg.pmml.DataType;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.IOUtil;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.EvaluatorUtil;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.evaluator.ModelEvaluatorFactory;
import org.jpmml.manager.PMMLManager;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.in.InEvent;
import org.wso2.siddhi.core.event.in.InListEvent;
import org.wso2.siddhi.core.event.in.InStream;
import org.wso2.siddhi.core.exception.QueryCreationException;
import org.wso2.siddhi.core.executor.expression.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.transform.TransformProcessor;
import org.wso2.siddhi.core.util.parser.ExecutorParser;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.expression.constant.StringConstant;
import org.wso2.siddhi.query.api.extension.annotation.SiddhiExtension;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  This extension can be used to predict the output for a given input event using a predefined 
 *  machine learning model written in pmml format. It supports the following expression.
 *  mlearn:getModelPrediction (pmmlDefinition, feature1, feature2, ... , featureN)
 *  Where,  pmmlDefinition      :   pmml Definition or the file path to the pmml definition
 *          feature1 - featureN :   Input values for the features, to be used to predict the output
 */
@SiddhiExtension(namespace = "mlearn", function = "getModelPrediction")
public class PmmlModelExecutor extends TransformProcessor {

    private static final Logger logger = Logger.getLogger(PmmlModelExecutor.class);
    private Map<String, Integer> parameterPositions = new HashMap<String, Integer>();
    private Map<FieldName, FieldValue> inData = new HashMap<FieldName, FieldValue>();
    private List<FieldName> allFields;          // All the fields defined in the pmml definition
    private List<FieldName> predictedFields;    // Predicted fields of the pmml definition
    private List<FieldName> outputFields;       // Output fields of the pmml definition
    private Evaluator evaluator;
    private List<FieldName> inputs = new ArrayList<FieldName>();
    private Map<FieldName, ?> result;
    private FieldName featureName;
    private Object featureValue;

    public PmmlModelExecutor() {
    }

    /**
     * Parse the pmml model and initialize event streams.
     * 
     * @param parameters            Array of parameters in the siddhi query
     * @param expressionExecutors
     * @param inStreamDefinition    Definition of the input stream
     * @param outStreamDefinition   Definition of the output stream
     * @param elementId
     * @param siddhiContext         Siddhi Context
     */
    @Override
    protected void init(Expression[] parameters, List<ExpressionExecutor> expressionExecutors,
            StreamDefinition inStreamDefinition, StreamDefinition outStreamDefinition,
            String elementId, SiddhiContext siddhiContext) {
        // Identify the input parameters and their positions
        for (Expression parameter : parameters) {
            if (parameter instanceof Variable) {
                Variable var = (Variable) parameter;
                String attributeName = var.getAttributeName();
                parameterPositions.put(attributeName, inStreamDefinition.getAttributePosition(
                    attributeName));
            }
        }
        // Extract the pmml definitions from the siddhi query
        String pmmlDefinition = getPmmlDefinition(parameters);
        // Unmarshal the definition and get an executable pmml model
        PMML pmmlModel = unmarshal(pmmlDefinition);
        // Get the different types of fields defined in the pmml model
        PMMLManager pmmlManager = new PMMLManager(pmmlModel);
        evaluator =(Evaluator) pmmlManager.getModelManager(null,ModelEvaluatorFactory.getInstance());
        allFields = evaluator.getActiveFields();
        predictedFields = evaluator.getPredictedFields();
        outputFields = evaluator.getOutputFields();

        /*
        * Define input fields. Filter outs auto-generated input fields in pmmlDefinition, by 
        * selecting only the fields that exist in the given siddhi query.
        */
        for (FieldName field : allFields) {
            if (parameterPositions.containsKey(field.getValue())) {
                inputs.add(field);
            }
        }
        this.outStreamDefinition = new StreamDefinition().name("pmmlPredictedStream");
        initializeOutputStream(pmmlModel);
    }
    
    /**
     * Extract the pmml definitions from the Expression arrays.
     * 
     * @param parameters    Parameters array of the expression
     * @return  pmml definition
     */
    private String getPmmlDefinition(Expression[] parameters) {
        if (parameters[0] instanceof StringConstant) {
            Expression expression = parameters[0];
            ExpressionExecutor executor = ExecutorParser.parseExpression(expression, null,
                elementId, false,siddhiContext);
            return (String) executor.execute(null);
        } else {
            throw new QueryCreationException("Cannot find a pmml definition as the first " +
                "attribute in the query.");
        }
    }
    
    /**
     * Unmarshal the definition and get an executable pmml model.
     * 
     * @return  pmml model
     */
    private PMML unmarshal(String pmmlDefinition) {
        try {
            // if the given is a file path, read the pmml definition from the file
            if (isFilePath(pmmlDefinition)) {
                String path = pmmlDefinition;
                File pmmlFile = new File(path);
                return IOUtil.unmarshal(pmmlFile);
            } else {
                // else, read from the given definition
                InputSource pmmlSource = new InputSource(new StringReader(pmmlDefinition));
                return IOUtil.unmarshal(pmmlSource);
            }
        } catch (Exception e) {
            logger.error("Failed to unmarshal the pmml definition: " + e.getMessage());
            throw new QueryCreationException("Failed to unmarshal the pmml definition: " 
                    + e.getMessage(), e);
        }
    }
    
    /**
     * Extract the name and the data type of the output fields and predicted fields from the
     * pmml definition and initialize an output stream having attributes with same name and data
     * type.
     * 
     * @param pmmlModel     Pmml model to which the output stream is define
     */
    protected void initializeOutputStream(PMML pmmlModel) {
        for (FieldName predictedField : predictedFields) {
            String dataType = evaluator.getDataField(predictedField).getDataType().toString();
            Attribute.Type type = null;
            if (dataType.equalsIgnoreCase("double")) {
                type = Attribute.Type.DOUBLE;
            } else if (dataType.equalsIgnoreCase("float")) {
                type = Attribute.Type.FLOAT;
            } else if (dataType.equalsIgnoreCase("integer")) {
                type = Attribute.Type.INT;
            } else if (dataType.equalsIgnoreCase("long")) {
                type = Attribute.Type.LONG;
            } else if (dataType.equalsIgnoreCase("string")) {
                type = Attribute.Type.STRING;
            } else if (dataType.equalsIgnoreCase("boolean")) {
                type = Attribute.Type.BOOL;
            }
            this.outStreamDefinition.attribute(predictedField.toString(), type);
        }
        for (FieldName outputField : outputFields) {
            DataType dataType = evaluator.getOutputField(outputField).getDataType();
            if (dataType == null) {
                dataType = evaluator.getDataField(predictedFields.get(0)).getDataType();
            }
            Attribute.Type type = null;
            if (dataType.toString().equalsIgnoreCase("double")) {
                type = Attribute.Type.DOUBLE;
            } else if (dataType.toString().equalsIgnoreCase("float")) {
                type = Attribute.Type.FLOAT;
            } else if (dataType.toString().equalsIgnoreCase("integer")) {
                type = Attribute.Type.INT;
            } else if (dataType.toString().equalsIgnoreCase("long")) {
                type = Attribute.Type.LONG;
            } else if (dataType.toString().equalsIgnoreCase("string")) {
                type = Attribute.Type.STRING;
            } else if (dataType.toString().equalsIgnoreCase("boolean")) {
                type = Attribute.Type.BOOL;
            }
            this.outStreamDefinition.attribute(outputField.toString(), type);
        }
    }
    
    /**
     * Predicts the output for a incoming event.
     * 
     * @param inEvent   Input event
     * @return          Event having the prediction
     */
    @Override
    protected InStream processEvent(InEvent inEvent) {
        //read the value of each input attribute from input stream and populate a single row
        for (FieldName inputfield : inputs) {
            featureName = new FieldName(inputfield.getValue());
            featureValue = inEvent.getData(parameterPositions.get(inputfield.toString()));
            inData.put(featureName, EvaluatorUtil.prepare(evaluator, featureName, featureValue));
        }
        
        //evaluate the pmmlModel using the above values and get the pmmlModel's output
        result = evaluator.evaluate(inData);
        Object[] resltObjct = new Object[result.size()];
        int i = 0;
        for (FieldName fieldName : result.keySet()) {
            resltObjct[i] = EvaluatorUtil.decode(result.get(fieldName));
            i++;
        }
        return new InEvent("pmmlPredictedStream", System.currentTimeMillis(), resltObjct);
    }

    /**
     * Predicts the outputs for a incoming event list.
     */
    @Override
    protected InStream processEvent(InListEvent inListEvent) {
        InListEvent transformedListEvent = new InListEvent();
        for (Event event : inListEvent.getEvents()) {
            if (event instanceof InEvent) {
                transformedListEvent.addEvent((Event) processEvent((InEvent) event));
            }
        }
        return transformedListEvent;
    }

    @Override
    protected Object[] currentState() {
        return new Object[] { parameterPositions };
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void restoreState(Object[] objects) {
        if (objects.length > 0 && objects[0] instanceof Map) {
            parameterPositions = (Map<String, Integer>) objects[0];
        }
    }

    /**
     * Check whether a given file exists.
     * 
     * @param path  Path of the file to be validated
     * @return      Boolean value indicating the validity
     */
    private boolean isFilePath(String path) {
        File file = new File(path);
        if (file.exists() && !file.isDirectory() && file.canRead()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void destroy() {
    }
}
