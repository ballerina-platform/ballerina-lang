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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by seshika on 4/9/14.
 */
@SiddhiExtension(namespace = "timeseries", function = "outlier")

/**
 * The methods supported by this function are
 * timeseries:outlier(int range, int/long/float/double y, int/long/float/double x)
 * and
 * timeseries:outlier(int calcInterval, int batchSize, double confidenceInterval, int range, int/long/float/double y, int/long/float/double x)
 *
 * where
 * @param calcInterval       Frequency of regression calculation
 * @param batchSize          Maximum number of events, used for regression calculation
 * @param confidenceInterval Confidence interval to be used for regression calculation
 * @param range              Number of standard deviations to be used as 'normal' range
 * @param y                  Dependant variable
 * @param x                  Independant variable
 */

public class LinearRegressionOutlierTransformProcessor extends TransformProcessor
{

    static final Logger log = Logger.getLogger(LinearRegressionOutlierTransformProcessor.class);

    private int eventCount = 0;         // Number of events added
    private int paramCount = 0;         // Number of x variables +1
    private int calcInterval = 1;       // The frequency of regression calculation
    private int batchSize = 1000000000; // Maximum # of events, used for regression calculation
    private double ci = 0.95;           // Confidence Interval
    private int range = 1;              // Number of standard deviations for outlier calc
    private Object[] regResult;         // Calculated regression coefficients
    private Map<Integer, String> paramPositions = new HashMap<Integer, String>(); // Input parameters
    private final int SIMPLE_LINREG_INPUT_PARAM_COUNT = 2;
    private RegressionCalculator regressionCalculator = null;

    public LinearRegressionOutlierTransformProcessor() {
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
                range = ((IntConstant) parameters[3]).getValue();
            } catch(ClassCastException c) {
                throw new QueryCreationException("Calculation interval, batch size and range should be of type int");
            }
            try {
                ci = ((DoubleConstant) parameters[2]).getValue();
            } catch(ClassCastException c) {
                throw new QueryCreationException("Confidence interval should be of type double and a value between 0 and 1");
            }

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
            try {
                range = ((IntConstant) parameters[0]).getValue();
            } catch(ClassCastException c) {
                throw new QueryCreationException("Range should be of type int");
            }

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
            this.outStreamDefinition.attribute("outlier", Attribute.Type.BOOL);
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

        Object [] outStreamData;
        Object [] temp;
        Object [] inStreamData = inEvent.getData();
        Boolean result = false; // Becomes true if its an outlier
        eventCount++;

        if(eventCount <= 3 || eventCount <= calcInterval) { // Need atleast 3 events to build the regression equation. Forecasting can be done after that.
            regResult = regressionCalculator.calculateLinearRegression(inEvent, paramPositions);
            outStreamData = null;
        } else {
            // Get the current X value
            Iterator<Map.Entry<Integer, String>> it = paramPositions.entrySet().iterator();
            it.next();
            double nextX = ((Number) inEvent.getData(it.next().getKey())).doubleValue();

            // Calculate the upper limit and the lower limit based on standard error and regression equation
            double forecastY = (Double) regResult[1] + nextX * (Double) regResult[2];
            double upLimit = (Double) regResult[0] * range + forecastY;
            double downLimit = - (Double) regResult[0] * range + forecastY;

            // Check whether next Y value is an outlier based on the next X value and the current regression equation
            double nextY = ((Number)inEvent.getData0()).doubleValue();
            if(nextY < downLimit || nextY > upLimit) {
                result = true;
            }
            temp = regressionCalculator.calculateLinearRegression(inEvent, paramPositions);
            if (temp!=null) {
                regResult = temp;
            }
            outStreamData = new Object[inStreamData.length + 4];
            outStreamData[0] = result;
            System.arraycopy(regResult,0,outStreamData, 1, regResult.length);
            System.arraycopy(inStreamData, 0, outStreamData, regResult.length+1, inStreamData.length);
        }

        return new InEvent(inEvent.getStreamId(), System.currentTimeMillis(), outStreamData);
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
