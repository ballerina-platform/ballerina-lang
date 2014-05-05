package org.wso2.siddhi.extension.timeseries;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.in.InEvent;
import org.wso2.siddhi.core.event.in.InListEvent;
import org.wso2.siddhi.core.event.in.InStream;
import org.wso2.siddhi.core.executor.expression.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.transform.TransformProcessor;
import org.wso2.siddhi.extension.timeseries.linreg.MultipleLinearRegressionCalculator;
import org.wso2.siddhi.extension.timeseries.linreg.RegressionCalculator;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.extension.annotation.SiddhiExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by waruna on 4/9/14.
 */
@SiddhiExtension(namespace = "timeseries", function = "regress")

public class LinearRegressionTransformProcessor extends TransformProcessor
{

    static final Logger log = Logger.getLogger(LinearRegressionTransformProcessor.class);

    private int paramCount = 0;
    private final int SIMPLE_LINREG_INPUT_PARAM_COUNT = 3;
    private Map<Integer, String> paramPositions = new HashMap<Integer, String>();
    private RegressionCalculator regressionCalculator = null;

    public LinearRegressionTransformProcessor() {
    }

    @Override
    protected InStream processEvent(InEvent inEvent) {
        log.debug("processEvent");

        return new InEvent(inEvent.getStreamId(), System.currentTimeMillis(),  regressionCalculator.linearRegressionCalculation( inEvent, paramPositions, paramCount));
    }
    @Override
    protected InStream processEvent(InListEvent inListEvent) {
        InEvent lastEvent = null;

        for (Event event : inListEvent.getEvents()) {
            if (event instanceof InEvent) {
                regressionCalculator.addEvent((InEvent) event, paramPositions, paramCount);
                lastEvent = (InEvent) event;
            }
        }

        return new InEvent(lastEvent.getStreamId(), System.currentTimeMillis(), regressionCalculator.processData());

    }
    @Override
    protected Object[] currentState() {
        return null;
    }
    @Override
    protected void restoreState(Object[] objects) {
        if (objects.length > 0 && objects[0] instanceof Map) {  //WHAT IS THIS IF CONDITION FOR?
        }
    }
    @Override
    protected void init(Expression[] parameters, List<ExpressionExecutor> expressionExecutors, StreamDefinition inStreamDefinition, StreamDefinition outStreamDefinition, String elementId, SiddhiContext siddhiContext) {

        if (outStreamDefinition == null) { //WHY DO WE HAVE TO CHECK WHETHER ITS NULL?
            this.outStreamDefinition = new StreamDefinition().name("linregStream");
            this.outStreamDefinition.attribute("stdError", Attribute.Type.DOUBLE);
        }

        // PROCESSING SIDDHI QUERY
        for (Expression parameter : parameters) {
            if (parameter instanceof Variable) {
                Variable var = (Variable) parameter;
                String attributeName = var.getAttributeName();
                paramPositions.put(inStreamDefinition.getAttributePosition(attributeName), attributeName );
                paramCount++;
            }
        }

        // DO A PERFORMANCE TEST TO SEE WHETHER IT MAKES SENSE TO USE SimpleLinearRegressionCalculator
        regressionCalculator = new MultipleLinearRegressionCalculator();

        // Creating the outstream based on the number of input parameters
        String betaVal, tStat;
        for (int itr = 0; itr <= paramCount - 2; itr++) {
            betaVal = "beta" + itr;
            tStat = "tStat" + itr;

            this.outStreamDefinition.attribute(betaVal, Attribute.Type.DOUBLE);
            this.outStreamDefinition.attribute(tStat, Attribute.Type.DOUBLE);
        }
    }
    @Override
    public void destroy() {
        regressionCalculator.close();
    }

}
