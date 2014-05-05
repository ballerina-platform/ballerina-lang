package org.wso2.siddhi.core.executor.function;

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.exception.QueryCreationException;
import org.wso2.siddhi.query.api.definition.Attribute;

/**
 * Created by seshika on 4/29/14.
 */
public class SinFunctionExecutor extends FunctionExecutor {

    @Override
    public void init(Attribute.Type[] attributeTypes, SiddhiContext siddhiContext) {
        if (attributeSize != 1) {
            throw new QueryCreationException("Sin function has to have exactly 1 parameter, currently " + attributeSize + " parameters provided");
        }
     }

    @Override
    protected Object process(Object data) {
        return Math.sin(Double.parseDouble(data.toString()));
    }

    @Override
    public void destroy() {
    }

    @Override
    public Attribute.Type getReturnType() {
        return Attribute.Type.DOUBLE;
    }
}
