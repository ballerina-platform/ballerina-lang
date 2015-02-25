package org.wso2.siddhi.core.function;

import org.wso2.siddhi.query.api.definition.Attribute;

public interface EvalScript {
    void init(String name, String body);
    Object eval(String name, Object[] arg);
    void setReturnType(Attribute.Type returnType);
    Attribute.Type getReturnType();
}
