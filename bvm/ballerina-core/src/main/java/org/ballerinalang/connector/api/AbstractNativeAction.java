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
package org.ballerinalang.connector.api;


import org.ballerinalang.bre.Context;
import org.ballerinalang.model.Action;
import org.ballerinalang.model.AnnotationAttachment;
import org.ballerinalang.model.Identifier;
import org.ballerinalang.model.NativeUnit;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.ParameterDef;
import org.ballerinalang.model.VariableDef;
import org.ballerinalang.model.WhiteSpaceDescriptor;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.exceptions.ArgumentOutOfRangeException;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.exceptions.FlowBuilderException;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents Native Ballerina Action.
 */
public abstract class AbstractNativeAction implements NativeUnit, Action {
    public static final BValue[] VOID_RETURN = new BValue[0];

    // BLangSymbol related attributes
    protected Identifier identifier;
    protected String pkgPath;
    protected boolean isPublic = true;

    private List<AnnotationAttachment> annotations;
    private List<ParameterDef> parameterDefs;
    private List<ParameterDef> returnParams;
    private int stackFrameSize;

    private BType[] returnParamTypes;
    private BType[] parameterTypes;

    private int tempStackFrameSize;

    public AbstractNativeAction() {
        parameterDefs = new ArrayList<>();
        returnParams = new ArrayList<>();
        annotations = new ArrayList<>();
    }

    public BValue getRefArgument(Context context, int index) {
        if (index > -1) {
            BValue result = context.getControlStack().getCurrentFrame().getRefRegs()[index];
            if (result == null) {
                throw new BallerinaException("argument " + index + " is null");
            }

            return result;
        }
        throw new ArgumentOutOfRangeException(index);
    }

    public long getIntArgument(Context context, int index) {
        if (index > -1) {
            return context.getControlStack().getCurrentFrame().getLongRegs()[index];
        }
        throw new ArgumentOutOfRangeException(index);
    }

    public String getStringArgument(Context context, int index) {
        if (index > -1) {
            return context.getControlStack().getCurrentFrame().getStringRegs()[index];
        }
        throw new ArgumentOutOfRangeException(index);
    }

    public double getFloatArgument(Context context, int index) {
        if (index > -1) {
            return context.getControlStack().getCurrentFrame().getDoubleRegs()[index];
        } else {
            throw new ArgumentOutOfRangeException(index);
        }
    }

    public boolean getBooleanArgument(Context context, int index) {
        if (index > -1) {
            return (context.getControlStack().getCurrentFrame().getIntRegs()[index] == 1);
        }
        throw new ArgumentOutOfRangeException(index);
    }

    public byte[] getBlobArgument(Context context, int index) {
        if (index > -1) {
            return context.getControlStack().getCurrentFrame().getByteRegs()[index];
        }
        throw new ArgumentOutOfRangeException(index);
    }

    public abstract ConnectorFuture execute(Context context);

    /**
     * Declare implementation of Native action is support non-blocking behaviour.
     * <p>
     * Default is false, Override to support non-blocking behaviour.
     *
     * @return true, if current is implementation supports non-blocking.
     */
    public boolean isNonBlockingAction() {
        return false;
    }

    // Methods in CallableUnit interface

    /**
     * Get all the Annotations associated with a BallerinaFunction.
     *
     * @return list of Annotations
     */
    @Override
    public AnnotationAttachment[] getAnnotations() {
        return annotations.toArray(new AnnotationAttachment[annotations.size()]);
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

    // Methods in Node interface

    @Override
    public NodeLocation getNodeLocation() {
        return null;
    }

    @Override
    public WhiteSpaceDescriptor getWhiteSpaceDescriptor() {
        return null;
    }

    @Override
    public String getName() {
        return identifier != null ? identifier.getName() : null;
    }

    // Methods in BLangSymbol interface

    @Override
    public void setName(String name) {
        this.identifier = new Identifier(name);
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
}
