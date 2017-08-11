/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 **/

package org.ballerinalang.natives;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.AnnotationAttachment;
import org.ballerinalang.model.Function;
import org.ballerinalang.model.Identifier;
import org.ballerinalang.model.NativeUnit;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.ParameterDef;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.SymbolScope;
import org.ballerinalang.model.VariableDef;
import org.ballerinalang.model.WhiteSpaceDescriptor;
import org.ballerinalang.model.Worker;
import org.ballerinalang.model.statements.BlockStmt;
import org.ballerinalang.model.statements.Statement;
import org.ballerinalang.model.types.BFunctionType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.SimpleTypeName;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.exceptions.ArgumentOutOfRangeException;
import org.ballerinalang.runtime.worker.WorkerDataChannel;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.exceptions.FlowBuilderException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * {@code {@link AbstractNativeFunction}} represents a Abstract implementation of Native Ballerina Function.
 *
 * @since 0.8.0
 */
public abstract class AbstractNativeFunction implements NativeUnit, Function {

    /**
     * Value to be returned for functions having a void return.
     */
    public static final BValue[] VOID_RETURN = new BValue[0];

    // BLangSymbol related attributes
    protected Identifier identifier;
    protected String pkgPath;
    protected boolean isPublic = true;
    protected SymbolName symbolName;

    private List<AnnotationAttachment> annotations;
    private List<ParameterDef> parameterDefs;
    private List<ParameterDef> returnParams;
    private int stackFrameSize;

    private BType[] returnParamTypes;
    private BType[] parameterTypes;
    private SimpleTypeName[] returnParamTypeNames;
    private SimpleTypeName[] argTypeNames;
    private String[] argNames;
    private int tempStackFrameSize;
    private BType bType;

    /**
     * Initialize a native function.
     */
    public AbstractNativeFunction() {
        parameterDefs = new ArrayList<>();
        returnParams = new ArrayList<>();
        annotations = new ArrayList<>();
    }

    public BValue getRefArgument(Context context, int index) {
        if (index > -1 && index < argTypeNames.length) {
            BValue result = context.getControlStackNew().getCurrentFrame().getRefLocalVars()[index];
            if (result == null) {
                throw new BallerinaException("argument " + index + " is null");
            }

            return result;
        }
        throw new ArgumentOutOfRangeException(index);
    }

    public byte[] getBlobArgument(Context context, int index) {
        if (index > -1 && index < argTypeNames.length) {
            byte[] result = context.getControlStackNew().getCurrentFrame().getByteLocalVars()[index];
            if (result == null) {
                throw new BallerinaException("argument " + index + " is null");
            }

            return result;
        }
        throw new ArgumentOutOfRangeException(index);
    }

    /**
     * This will return a int variable defined in ballerina level.
     * In java level it would be a long value.
     *
     * @param context In which the variable reside.
     * @param index   Index of the variable location.
     * @return Long value.
     */
    public long getIntArgument(Context context, int index) {
        if (index > -1 && index < argTypeNames.length) {
            return context.getControlStackNew().getCurrentFrame().getLongLocalVars()[index];
        }
        throw new ArgumentOutOfRangeException(index);
    }

    public String getStringArgument(Context context, int index) {
        if (index > -1 && index < argTypeNames.length) {
            return context.getControlStackNew().getCurrentFrame().getStringLocalVars()[index];
        }
        throw new ArgumentOutOfRangeException(index);
    }

    /**
     * This will return a float variable defined in ballerina level.
     * In java level that would be a double value.
     *
     * @param context In which the variable reside.
     * @param index   Index of the variable location.
     * @return Double value.
     */
    public double getFloatArgument(Context context, int index) {
        if (index > -1 && index < this.argTypeNames.length) {
            return context.getControlStackNew().getCurrentFrame().getDoubleLocalVars()[index];
        } else {
            throw new ArgumentOutOfRangeException(index);
        }
    }

    public boolean getBooleanArgument(Context context, int index) {
        if (index > -1 && index < argTypeNames.length) {
            return (context.getControlStackNew().getCurrentFrame().getIntLocalVars()[index] == 1);
        }
        throw new ArgumentOutOfRangeException(index);
    }

    @Override
    public BType getType() {
        if (bType == null) {
            BFunctionType functionType = new BFunctionType(this.getSymbolScope().getEnclosingScope(), parameterTypes,
                    returnParamTypes);
            bType = functionType;
        }
        return bType;
    }

    @Override
    public void setType(BType type) {
    }

    @Override
    public Kind getKind() {
        return null;
    }

    @Override
    public void setKind(Kind kind) {
    }

    @Override
    public int getVarIndex() {
        return 0;
    }

    @Override
    public void setVarIndex(int index) {
    }

    @Override
    public SimpleTypeName getTypeName() {
        return null;
    }

    /**
     * Where Native Function logic is implemented.
     *
     * @param context Current Context instance
     * @return Native function return BValue arrays
     */
    public abstract BValue[] execute(Context context);

    /**
     * Execute this native function and set the values for return parameters.
     *
     * @param context Ballerina Context
     */
    public void executeNative(Context context) {
        try {
            BValue[] retVals = execute(context);
            BValue[] returnRefs = context.getControlStackNew().getCurrentFrame().returnValues;

            if (returnRefs.length != 0) {
                for (int i = 0; i < returnRefs.length; i++) {
                    if (i < retVals.length) {
                        returnRefs[i] = retVals[i];
                    } else {
                        break;
                    }
                }
            }
        } catch (RuntimeException e) {
            throw e;
        }
    }

    /**
     * Util method to construct BValue arrays.
     *
     * @param values BValues to construct the array
     * @return Array of BValues
     */
    public BValue[] getBValues(BValue... values) {
        return values;
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
    public WhiteSpaceDescriptor getWhiteSpaceDescriptor() {
        return null;
    }

    @Override
    public String getName() {
        return identifier.getName();
    }

    @Override
    public Identifier getIdentifier() {
        return identifier;
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
        return isPublic;
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
    public void setArgNames(String[] argNames) {
        this.argNames = argNames;
    }

    @Override
    public SimpleTypeName[] getArgumentTypeNames() {
        return argTypeNames;
    }

    @Override
    public String[] getArgumentNames() {
        return argNames;
    }

    @Override
    public SimpleTypeName[] getReturnParamTypeNames() {
        return returnParamTypeNames;
    }

    @Override
    public void setReturnParamTypeNames(SimpleTypeName[] returnParamTypes) {
        this.returnParamTypeNames = returnParamTypes;
    }

    /**
     * Get worker interaction statements related to a callable unit.
     *
     * @return Queue of worker interactions
     */
    @Override
    public Queue<Statement> getWorkerInteractionStatements() {
        return null;
    }

    /**
     * Get the workers defined within a callable unit.
     *
     * @return Array of workers
     */
    @Override
    public Worker[] getWorkers() {
        return new Worker[0];
    }

    @Override
    public void addWorkerDataChannel(WorkerDataChannel workerDataChannel) {

    }

    @Override
    public Map<String, WorkerDataChannel> getWorkerDataChannelMap() {
        return null;
    }
}
