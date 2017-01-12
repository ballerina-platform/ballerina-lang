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
import org.wso2.ballerina.core.model.Const;
import org.wso2.ballerina.core.model.Parameter;
import org.wso2.ballerina.core.model.Position;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.VariableDcl;
import org.wso2.ballerina.core.model.types.BTypes;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.NativeConstruct;
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
public abstract class AbstractNativeAction implements Action, NativeConstruct {

    public static final BValue[] VOID_RETURN = new BValue[0];
    private static final Logger log = LoggerFactory.getLogger(AbstractNativeAction.class);
    private String packageName, actionName;
    private SymbolName symbolName;
    private List<Annotation> annotations;
    private List<Parameter> parameters;
    private List<Parameter> returnParams;
    private List<Const> constants;
    private int stackFrameSize;
    private Position actionLocation;

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
        packageName = action.packageName();
        actionName = action.actionName();
        String connectorName = action.connectorName();
        String symName = packageName + ":" + connectorName + "." + actionName;
        symbolName = new SymbolName(symName);
        stackFrameSize = action.args().length;
        Arrays.stream(action.args()).
                forEach(argument -> {
                    try {
                        parameters.add(new Parameter(BTypes.getType(argument.type().getName()),
                                new SymbolName(argument.name())));
                    } catch (BallerinaException e) {
                        // TODO: Fix this when TypeC.getType method is improved.
                        log.error("Internal Error..! Error while processing Parameters for Native ballerina" +
                                " action {}:{}.", packageName, actionName, e);
                    }
                });
        Arrays.stream(action.returnType()).forEach(returnType -> {
            try {
                returnParams.add(new Parameter(BTypes.getType(returnType.getName()), null));
            } catch (BallerinaException e) {
                // TODO: Fix this when TypeC.getType method is improved.
                log.error("Internal Error..! Error while processing ReturnTypes for Native ballerina" +
                        " action {}:{}.", packageName, actionName, e);
            }
        });
        Arrays.stream(action.consts()).forEach(constant -> {
            try {
                constants.add(Utils.getConst(constant));
            } catch (MalformedEntryException e) {
                log.error("Internal Error..! Error while processing pre defined const {} for Native ballerina" +
                        " action {}:{}.", constant.identifier(), packageName, actionName, e);
            }
        });
        // TODO: Handle Ballerina Annotations.
    }

    public String getPackageName() {
        return packageName;
    }

    @Override
    public String getName() {
        return symbolName.getName();
    }

    public SymbolName getSymbolName() {
        return symbolName;
    }

    @Override
    public void setSymbolName(SymbolName symbolName) {
        this.symbolName = symbolName;
    }

    @Override
    public Annotation[] getAnnotations() {
        return annotations.toArray(new Annotation[annotations.size()]);
    }

    @Override
    public Parameter[] getParameters() {
        return parameters.toArray(new Parameter[parameters.size()]);
    }

    public VariableDcl[] getVariableDcls() {
        return new VariableDcl[0];
    }

    @Override
    public Parameter[] getReturnParameters() {
        return returnParams.toArray(new Parameter[returnParams.size()]);
    }

    public int getStackFrameSize() {
        return stackFrameSize;
    }

    public void setStackFrameSize(int stackFrameSize) {
        this.stackFrameSize = stackFrameSize;
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

//    @Override
//    public void interpret(Context ctx) {
//        execute(ctx);

    // TODO : Support for multiple return values and to be change after  support statement callback chaning

//    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Position getLocation() {
        return actionLocation;
    }
}
