/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.ballerina.core.nativeimpl.connectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.Action;
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.ConstDef;
import org.wso2.ballerina.core.model.NodeLocation;
import org.wso2.ballerina.core.model.NodeVisitor;
import org.wso2.ballerina.core.model.Parameter;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.VariableDef;
import org.wso2.ballerina.core.model.statements.BlockStmt;
import org.wso2.ballerina.core.model.symbols.SymbolScope;
import org.wso2.ballerina.core.model.types.BType;
import org.wso2.ballerina.core.model.types.BTypes;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaAction;
import org.wso2.ballerina.core.nativeimpl.annotations.Utils;
import org.wso2.ballerina.core.nativeimpl.exceptions.ArgumentOutOfRangeException;
import org.wso2.ballerina.core.nativeimpl.exceptions.MalformedEntryException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents Native Ballerina Action.
 */
public abstract class AbstractNativeAction implements Action {
    public static final BValue[] VOID_RETURN = new BValue[0];
    private static final Logger log = LoggerFactory.getLogger(AbstractNativeAction.class);

    private NodeLocation location;

    // BLangSymbol related attributes
    protected String name;
    protected String pkgPath;
    protected boolean isPublic = true;
    protected SymbolName symbolName;

    private List<Annotation> annotations;
    private List<Parameter> parameters;
    private List<Parameter> returnParams;
    private List<ConstDef> constants;
    private int stackFrameSize;

    public AbstractNativeAction() {
        parameters = new ArrayList<>();
        returnParams = new ArrayList<>();
        annotations = new ArrayList<>();
        constants = new ArrayList<>();
        buildModel();

    }

    /*
     * Build Native Action Model using Java annotation.
     */
    private void buildModel() {
        BallerinaAction action = this.getClass().getAnnotation(BallerinaAction.class);
        pkgPath = action.packageName();
        name = action.actionName();
        String connectorName = action.connectorName();
        String symName = pkgPath + ":" + connectorName + "." + name;
        symbolName = new SymbolName(symName);
        stackFrameSize = action.args().length;
        Arrays.stream(action.args()).
                forEach(argument -> {
                    try {
                        BType bType;
                        if (!argument.type().equals(TypeEnum.ARRAY)) {
                            bType = BTypes.getType(argument.type().getName());
                        } else {
                            bType = BTypes.getArrayType(argument.elementType().getName());
                        }
                        parameters.add(new Parameter(bType, new SymbolName(argument.name())));
                    } catch (BallerinaException e) {
                        // TODO: Fix this when TypeC.getType method is improved.
                        log.error("Internal Error..! Error while processing Parameters for Native ballerina" +
                                " action {}:{}.", pkgPath, name, e);
                    }
                });
        Arrays.stream(action.returnType()).forEach(returnType -> {
            try {
                returnParams.add(new Parameter(BTypes.getType(returnType.getName()), null));
            } catch (BallerinaException e) {
                // TODO: Fix this when TypeC.getType method is improved.
                log.error("Internal Error..! Error while processing ReturnTypes for Native ballerina" +
                        " action {}:{}.", pkgPath, name, e);
            }
        });
        Arrays.stream(action.consts()).forEach(constant -> {
            try {
                constants.add(Utils.getConst(constant));
            } catch (MalformedEntryException e) {
                log.error("Internal Error..! Error while processing pre defined const {} for Native ballerina" +
                        " action {}:{}.", constant.identifier(), pkgPath, name, e);
            }
        });
        // TODO: Handle Ballerina Annotations.
    }

    /**
     * Get Argument by index.
     *
     * @param context current {@code {@link Context}} instance.
     * @param index   index of the parameter.
     * @return BValue;
     */
    public BValue getArgument(Context context, int index) {
        if (index > -1 && index < parameters.size()) {
            return context.getControlStack().getCurrentFrame().values[index];
        }
        throw new ArgumentOutOfRangeException(index);
    }

    public abstract BValue execute(Context context);


    // Methods in CallableUnit interface

    @Override
    public void setSymbolName(SymbolName symbolName) {
        this.symbolName = symbolName;
    }

    /**
     * Get all the Annotations associated with a BallerinaFunction.
     *
     * @return list of Annotations
     */
    @Override
    public Annotation[] getAnnotations() {
        return annotations.toArray(new Annotation[annotations.size()]);
    }

    /**
     * Get list of Arguments associated with the function definition.
     *
     * @return list of Arguments
     */
    @Override
    public Parameter[] getParameters() {
        return parameters.toArray(new Parameter[parameters.size()]);
    }

    /**
     * Get all the variableDcls declared in the scope of BallerinaFunction.
     *
     * @return list of all BallerinaFunction scoped variableDcls
     */
    @Override
    public VariableDef[] getVariableDefs() {
        return new VariableDef[0];
    }

    @Override
    public BlockStmt getCallableUnitBody() {
        return null;
    }

    @Override
    public Parameter[] getReturnParameters() {
        return returnParams.toArray(new Parameter[returnParams.size()]);
    }

    @Override
    public int getStackFrameSize() {
        return stackFrameSize;
    }

    @Override
    public void setStackFrameSize(int stackFrameSize) {
        this.stackFrameSize = stackFrameSize;
    }


    // Methods in Node interface

    @Override
    public void accept(NodeVisitor visitor) {
    }

    @Override
    public NodeLocation getNodeLocation() {
        return location;
    }

    // Methods in BLangSymbol interface

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPackagePath() {
        return pkgPath;
    }

    @Override
    public boolean isPublic() {
        return true;
    }

    @Override
    public boolean isNative() {
        return true;
    }

    @Override
    public SymbolName getSymbolName() {
        return symbolName;
    }

    @Override
    public SymbolScope getSymbolScope() {
        return null;
    }
}
