/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*/
package org.ballerinalang.model.nodes.fragments.expressions;

import org.ballerinalang.model.LinkedNodeVisitor;
import org.ballerinalang.model.nodes.AbstractLinkedNode;
import org.ballerinalang.natives.AbstractNativeTypeMapper;

/**
 * Represents a node for invoking a native type mappers.
 */
public class InvokeNativeTypeMapperNode extends AbstractLinkedNode {

    private AbstractNativeTypeMapper callableUnit;

    public InvokeNativeTypeMapperNode(AbstractNativeTypeMapper callableUnit) {
        this.callableUnit = callableUnit;
    }

    @Override
    public void accept(LinkedNodeVisitor nodeVisitor) {
        nodeVisitor.visit(this);
    }

    public AbstractNativeTypeMapper getCallableUnit() {
        return callableUnit;
    }
}
