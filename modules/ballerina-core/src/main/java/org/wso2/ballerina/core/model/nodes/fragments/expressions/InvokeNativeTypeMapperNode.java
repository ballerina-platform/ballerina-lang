/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*/
package org.wso2.ballerina.core.model.nodes.fragments.expressions;

import org.wso2.ballerina.core.model.LinkedNodeVisitor;
import org.wso2.ballerina.core.model.nodes.AbstractLinkedNode;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeTypeMapper;

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
