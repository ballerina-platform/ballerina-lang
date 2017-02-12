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


import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.exception.FlowBuilderException;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.Action;
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.NativeUnit;
import org.wso2.ballerina.core.model.NodeLocation;
import org.wso2.ballerina.core.model.NodeVisitor;
import org.wso2.ballerina.core.model.ParameterDef;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.SymbolScope;
import org.wso2.ballerina.core.model.VariableDef;
import org.wso2.ballerina.core.model.statements.BlockStmt;
import org.wso2.ballerina.core.model.types.BType;
import org.wso2.ballerina.core.model.types.SimpleTypeName;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.exceptions.ArgumentOutOfRangeException;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents Native Ballerina Action.
 */
public abstract class AbstractNativeAction implements NativeUnit, Action {
    public static final BValue[] VOID_RETURN = new BValue[0];

    // BLangSymbol related attributes
    protected String name;
    protected String pkgPath;
    protected boolean isPublic = true;
    protected SymbolName symbolName;

    private List<Annotation> annotations;
    private List<ParameterDef> parameterDefs;
    private List<ParameterDef> returnParams;
    private int stackFrameSize;

    private BType[] returnParamTypes;
    private BType[] parameterTypes;
    private SimpleTypeName[] returnParamTypeNames;
    private SimpleTypeName[] argTypeNames;

    private int tempStackFrameSize;

    public AbstractNativeAction() {
        parameterDefs = new ArrayList<>();
        returnParams = new ArrayList<>();
        annotations = new ArrayList<>();
    }

    /**
     * Get Argument by index.
     *
     * @param context current {@code {@link Context}} instance.
     * @param index   index of the parameter.
     * @return BValue;
     */
    public BValue getArgument(Context context, int index) {
        if (index > -1 && index < argTypeNames.length) {
            BValue result = context.getControlStack().getCurrentFrame().values[index];
            if (result == null) {
                throw new BallerinaException("argument " + index + " is null");
            }
            return result;
        }
        throw new ArgumentOutOfRangeException(index);
    }

    public abstract BValue execute(Context context);

    /**
     * Invoke Non Blocking Native Action.
     *
     * @param context           Ballerina context.
     * @param connectorCallback Callback instance to notify completion of the action invocation.
     */
    public abstract void execute(Context context, BalConnectorCallback connectorCallback);

    /**
     * Validate Native Action invocation. This method will be invoked when callback.done().
     *
     * @param connectorCallback Connector Callback instance.
     */
    public abstract void validate(BalConnectorCallback connectorCallback);

    // Methods in CallableUnit interface

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
    public ParameterDef[] getParameterDefs() {
        return parameterDefs.toArray(new ParameterDef[parameterDefs.size()]);
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
    public ParameterDef[] getReturnParameters() {
        return returnParams.toArray(new ParameterDef[returnParams.size()]);
    }

    @Override
    public int getStackFrameSize() {
        return stackFrameSize;
    }

    @Override
    public void setStackFrameSize(int stackFrameSize) {
        this.stackFrameSize = stackFrameSize;
    }

    @Override
    public int getTempStackFrameSize() {
        return tempStackFrameSize;
    }

    @Override
    public void setTempStackFrameSize(int stackFrameSize) {
        if (this.tempStackFrameSize > 0 && stackFrameSize != this.tempStackFrameSize) {
            throw new FlowBuilderException("Attempt to Overwrite tempValue Frame size. current :" +
                    this.tempStackFrameSize + ", new :" + stackFrameSize);
        }
        this.tempStackFrameSize = stackFrameSize;
    }

    @Override
    public BType[] getReturnParamTypes() {
        return returnParamTypes;
    }

    @Override
    public void setReturnParamTypes(BType[] returnParamTypes) {
        this.returnParamTypes = returnParamTypes;
    }

    @Override
    public BType[] getArgumentTypes() {
        return parameterTypes;
    }

    @Override
    public void setParameterTypes(BType[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    @Override
    public void accept(NodeVisitor visitor) {
    }

    // Methods in Node interface

    @Override
    public NodeLocation getNodeLocation() {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    // Methods in BLangSymbol interface

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getPackagePath() {
        return pkgPath;
    }

    @Override
    public void setPackagePath(String packagePath) {
        this.pkgPath = packagePath;
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

    // Methods in NativeCallableUnit interface

    @Override
    public void setSymbolName(SymbolName symbolName) {
        this.symbolName = symbolName;
    }

    @Override
    public SymbolScope getSymbolScope() {
        return null;
    }

    @Override
    public void setArgTypeNames(SimpleTypeName[] argTypes) {
        this.argTypeNames = argTypes;
    }

    @Override
    public SimpleTypeName[] getArgumentTypeNames() {
        return argTypeNames;
    }

    @Override
    public SimpleTypeName[] getReturnParamTypeNames() {
        return returnParamTypeNames;
    }

    @Override
    public void setReturnParamTypeNames(SimpleTypeName[] returnParamTypes) {
        this.returnParamTypeNames = returnParamTypes;
    }
}
