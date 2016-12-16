package org.wso2.ballerina.core.nativeimpl.connectors;

import org.wso2.ballerina.core.model.Connector;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.nativeimpl.NativeConstruct;

/**
 * Represents Native Ballerina Connector.
 */
public abstract class NativeConnector implements Connector, NativeConstruct {
    public abstract boolean init(BValueRef[] bValueRefs);

}
