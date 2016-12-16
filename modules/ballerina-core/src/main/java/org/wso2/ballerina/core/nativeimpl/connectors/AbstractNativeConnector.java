package org.wso2.ballerina.core.nativeimpl.connectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.model.Connector;
import org.wso2.ballerina.core.model.Parameter;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.types.TypeC;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.nativeimpl.NativeConstruct;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaConnector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents Native Ballerina Connector.
 */
public abstract class AbstractNativeConnector implements Connector, NativeConstruct {

    private static final Logger log = LoggerFactory.getLogger(AbstractNativeConnector.class);

    private SymbolName symbolName;

    private String packageName;

    private String connectorName;

    private List<Parameter> parameters;

    public AbstractNativeConnector() {
        parameters = new ArrayList<>();
        buildModel();

    }

    /*
     * Build Native Action Model using Java annotation.
     */
    private void buildModel() {
        BallerinaConnector connector = this.getClass().getAnnotation(BallerinaConnector.class);
        packageName = connector.packageName();
        connectorName = connector.connectorName();
        String symName = packageName + ":" + connectorName;
        symbolName = new SymbolName(symName);
        Arrays.stream(connector.args()).
                forEach(argument -> {
                    try {
                        parameters.add(new Parameter(TypeC.getTypeC(argument.type().getName()),
                                new SymbolName(argument.name())));
                    } catch (RuntimeException e) {
                        // TODO: Fix this when TypeC.getType method is improved.
                        log.warn("Error while processing Parameters for Native ballerina Connector {}:{}.", packageName,
                                connectorName, e);
                    }
                });
    }

    public abstract boolean init(BValueRef[] bValueRefs);

    public SymbolName getSymbolName() {
        return symbolName;
    }

    @Override
    public Parameter[] getParameters() {
        return parameters.toArray(new Parameter[parameters.size()]);
    }
}
