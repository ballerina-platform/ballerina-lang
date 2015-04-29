package org.wso2.siddhi.extension.regex;


import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * lookingAt(regex, inputSequence)
 * This method attempts to match the 'inputSequence', starting at the beginning, against the 'regex' pattern.
 * regex - regular expression. eg: "\d\d(.*)WSO2"
 * inputSequence - input sequence to be matched with the regular expression eg: "21 products are produced by WSO2 currently"
 * Accept Type(s) for lookingAt(regex, inputSequence);
 *         regex : STRING
 *         inputSequence : STRING
 * Return Type(s): BOOLEAN
 */
public class LookingAtFunctionExtension extends FunctionExecutor {
    Attribute.Type returnType = Attribute.Type.BOOL;

    //state-variables
    private boolean isRegexConstant = false;
    private String regexConstant;
    private Pattern patternConstant;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors.length != 2) {
            throw new ExecutionPlanValidationException("Invalid no of arguments passed to regex:lookingAt() function, required 2, " +
                    "but found " + attributeExpressionExecutors.length);
        }
        if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.STRING) {
            throw new ExecutionPlanValidationException("Invalid parameter type found for the first argument of regex:lookingAt() function, " +
                    "required "+Attribute.Type.STRING+", but found "+attributeExpressionExecutors[0].getReturnType().toString());
        }
        if (attributeExpressionExecutors[1].getReturnType() != Attribute.Type.STRING) {
            throw new ExecutionPlanValidationException("Invalid parameter type found for the second argument of regex:lookingAt() function, " +
                    "required "+Attribute.Type.STRING+", but found "+attributeExpressionExecutors[1].getReturnType().toString());
        }
        if(attributeExpressionExecutors[0] instanceof ConstantExpressionExecutor){
            isRegexConstant = true;
            regexConstant = (String) ((ConstantExpressionExecutor) attributeExpressionExecutors[0]).getValue();
            patternConstant = Pattern.compile(regexConstant);
        }
    }

    @Override
    protected Object execute(Object[] data) {
        String regex;
        Pattern pattern;
        Matcher matcher;

        if (data[0] == null) {
            throw new ExecutionPlanRuntimeException("Invalid input given to regex:lookingAt() function. First argument cannot be null");
        }
        if (data[1] == null) {
            throw new ExecutionPlanRuntimeException("Invalid input given to regex:lookingAt() function. Second argument cannot be null");
        }
        String source = (String) data[1];

        if(!isRegexConstant){
            regex = (String) data[0];
            pattern = Pattern.compile(regex);
            matcher = pattern.matcher(source);
            return matcher.lookingAt();

        } else {
            matcher = patternConstant.matcher(source);
            return matcher.lookingAt();
        }
    }

    @Override
    protected Object execute(Object data) {
        return null;  //Since the lookingAt function takes in 2 parameters, this method does not get called. Hence, not implemented.
    }

    @Override
    public void start() {
        //Nothing to start
    }

    @Override
    public void stop() {
        //Nothing to stop
    }

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }

    @Override
    public Object[] currentState() {
        return new Object[]{isRegexConstant, regexConstant, patternConstant};
    }

    @Override
    public void restoreState(Object[] state) {
        isRegexConstant = (Boolean) state[0];
        regexConstant = (String) state[1];
        patternConstant = (Pattern) state[2];
    }
}
