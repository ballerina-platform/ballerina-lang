/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*/
package org.wso2.ballerina.core.model.nodes.fragments.expressions;

import org.wso2.ballerina.core.model.LinkedNodeVisitor;
import org.wso2.ballerina.core.model.nodes.AbstractLinkedNode;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeAction;

/**
 * Represents a node for invoking a native action.
 */
public class InvokeNativeActionNode extends AbstractLinkedNode {

    private AbstractNativeAction callableUnit;

    public InvokeNativeActionNode(AbstractNativeAction callableUnit) {
        this.callableUnit = callableUnit;
    }

    @Override
    public void accept(LinkedNodeVisitor nodeVisitor) {
        nodeVisitor.visit(this);
    }

    public AbstractNativeAction getCallableUnit() {
        return callableUnit;
    }
}
