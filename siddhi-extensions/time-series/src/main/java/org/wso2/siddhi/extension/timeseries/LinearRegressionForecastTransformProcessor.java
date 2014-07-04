package org.wso2.siddhi.extension.timeseries;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.in.InEvent;
import org.wso2.siddhi.core.event.in.InListEvent;
import org.wso2.siddhi.core.event.in.InStream;
import org.wso2.siddhi.core.exception.QueryCreationException;
import org.wso2.siddhi.core.executor.expression.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.transform.TransformProcessor;
import org.wso2.siddhi.extension.timeseries.linreg.RegressionCalculator;
import org.wso2.siddhi.extension.timeseries.linreg.SimpleLinearRegressionCalculator;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.expression.constant.DoubleConstant;
import org.wso2.siddhi.query.api.expression.constant.IntConstant;
import org.wso2.siddhi.query.api.extension.annotation.SiddhiExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by seshika on 4/9/14.
 */
@SiddhiExtension(namespace = "timeseries", function = "forecast")

/**
 * The methods supported by this function are
 * timeseries:forecast(expression/int/long/float/double nextX, int/long/float/double y, int/long/float/double x)
 * and
 * timeseries:forecast(int calcInterval, int batchSize, double confidenceInterval, expression/int/long/float/double nextX, int/long/float/double y, int/long/float/double x)
 *
 * where
 * @param calcInterval      Frequency of regression calculation
 * @param batchSize         Maximum number of events, used for regression calculation
 * @param confidenceInterval Confidence interval to be used for regression calculation
 * @param nextX             Independant variable to be used to forecast next Y
 * @param y                 Dependant variable
 * @param x                 Independant variable
 */

public class LinearRegressionForecastTransformProcessor extends TransformProcessor
{

    static final Logger log = Logger.getLogger(LinearRegressionForecastTransformProcessor.class);

    private int paramCount = 0;         // Number of x variables +1
    private int calcInterval = 1;       // how frequently regression is calculated
    private int batchSize = 1000000000; // maximum number of events for a regression calculation
    private ExpressionExecutor exp;     // input variable for forecasting
    private double ci = 0.95;           // confidence interval
    private final int SIMPLE_LINREG_INPUT_PARAM_COUNT = 2;
    private Map<Integer, String> paramPositions = new HashMap<Integer, String>();
    private RegressionCalculator regressionCalculator = null;
    Object [] regResult;

    public LinearRegressionForecastTransformProcessor() {
    }

    @Override
    protected void init(Expression[] parameters, List<ExpressionExecutor> expressionExecutors, StreamDefinition inStreamDefinition, StreamDefinition outStreamDefinition, String elementId, SiddhiContext siddhiContext) {

        if (log.isDebugEnabled()) {
            log.debug("Query Initialized. Stream Parameters: " + inStreamDefinition.toString());
        }

        // Capture constant inputs
        if(parameters[1] instanceof IntConstant) {
            try {
                calcInterval = ((IntConstant) parameters[0]).getValue();
                batchSize = ((IntConstant) parameters[1]).getValue();
            } catch(ClassCastException c) {
                throw new QueryCreationException("Calculation interval, batch size and range should be of type int");
            }
            try {
                ci = ((DoubleConstant) parameters[2]).getValue();
            } catch(ClassCastException c) {
                throw new QueryCreationException("Confidence interval should be of type double and a value between 0 and 1");
            }
            // Capture input used for forecasting
            exp = expressionExecutors.get(3);

            // Capture variable inputs
            for (int i=4; i<parameters.length; i++) {
                if (parameters[i] instanceof Variable) {
                    Variable var = (Variable) parameters[i];
                    String attributeName = var.getAttributeName();
                    paramPositions.put(inStreamDefinition.getAttributePosition(attributeName), attributeName );
                    paramCount++;
                }
            }
        } else {
            // Capture input used for forecasting
            exp = expressionExecutors.get(0);

            // Capture variable inputs
            for (int i=1; i<parameters.length; i++) {
                if (parameters[i] instanceof Variable) {
                    Variable var = (Variable) parameters[i];
                    String attributeName = var.getAttributeName();
                    paramPositions.put(inStreamDefinition.getAttributePosition(attributeName), attributeName );
                    paramCount++;
                }
            }
        }

        // pick the appropriate regression calculator
        if(paramCount > SIMPLE_LINREG_INPUT_PARAM_COUNT) {
            throw new QueryCreationException("Outlier Function is available only for simple linear regression");
        } else {
            regressionCalculator = new SimpleLinearRegressionCalculator(paramCount, calcInterval, batchSize, ci);
        }

        // Creating outstream
        if (outStreamDefinition == null) { //WHY DO WE HAVE TO CHECK WHETHER ITS NULL?
            this.outStreamDefinition = new StreamDefinition().name("linregStream");
            this.outStreamDefinition.attribute("forecastY", Attribute.Type.DOUBLE);
            this.outStreamDefinition.attribute("stdError", Attribute.Type.DOUBLE);
            this.outStreamDefinition.attribute("beta0", Attribute.Type.DOUBLE);
            this.outStreamDefinition.attribute("beta1", Attribute.Type.DOUBLE);

            // Creating outstream attributes for all the attributes in the input stream
            for(Attribute strDef : inStreamDefinition.getAttributeList()) {
                this.outStreamDefinition.attribute(strDef.getName(), strDef.getType());
            }
        }
    }



    @Override
    protected InStream processEvent(InEvent inEvent) {

        if (log.isDebugEnabled()) {
            log.debug("processEvent");
        }
        Object [] inStreamData = inEvent.getData();
        Object[] outStreamData;
        Object[] temp = regressionCalculator.calculateLinearRegression(inEvent, paramPositions);

        if(regResult==null && temp==null){  // When calculation interval > 1, send null until the first regression calculation
            outStreamData = null;
        } else {
            double xDash = ((Number) exp.execute(inEvent)).doubleValue();

            // For each calculation, get new regression coefficients, otherwise use previous coefficients
            if(temp!=null) {
                // Calculating forecast Y based on regression equation and given X
                outStreamData = new Object[temp.length + inStreamData.length+1];
                outStreamData[0] = ((Number) temp[1]).doubleValue()+ xDash * ((Number) temp[2]).doubleValue();
                regResult = temp;
            } else {
                outStreamData = new Object[regResult.length + inStreamData.length+1];
                outStreamData[0] = ((Number) regResult[1]).doubleValue()+ xDash * ((Number) regResult[2]).doubleValue();
            }

            // Combining Regression Results and the Input Stream Data
            System.arraycopy(regResult, 0, outStreamData, 1, regResult.length);
            System.arraycopy(inStreamData, 0, outStreamData, regResult.length+1, inStreamData.length);
        }

        return new InEvent(inEvent.getStreamId(), System.currentTimeMillis(),  outStreamData);

    }
    @Override
    protected InStream processEvent(InListEvent inListEvent) {
        InStream inStream = null;

        for (Event event : inListEvent.getEvents()) {
            if (event instanceof InEvent) {
                inStream = processEvent((InEvent) event);
            }
        }
        return inStream;
    }
    @Override
    protected Object[] currentState() {
        return null;
    }
    @Override
    protected void restoreState(Object[] objects) {
    }

    @Override
    public void destroy() {
    }

}
