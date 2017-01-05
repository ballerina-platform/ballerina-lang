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

package org.wso2.ballerina.core.nativeimpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.Const;
import org.wso2.ballerina.core.model.Function;
import org.wso2.ballerina.core.model.Parameter;
import org.wso2.ballerina.core.model.Position;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.VariableDcl;
import org.wso2.ballerina.core.model.types.BType;
import org.wso2.ballerina.core.model.types.BTypes;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Utils;
import org.wso2.ballerina.core.nativeimpl.exceptions.ArgumentOutOfRangeException;
import org.wso2.ballerina.core.nativeimpl.exceptions.MalformedEntryException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * {@code {@link AbstractNativeFunction}} represents a Abstract implementation of Native Ballerina Function.
 */
public abstract class AbstractNativeFunction implements NativeConstruct, Function {

    /* Void RETURN */
    public static final BValue[] VOID_RETURN = new BValue[0];
    private static final Logger log = LoggerFactory.getLogger(AbstractNativeFunction.class);
    private String packageName, functionName;
    private SymbolName symbolName;
    private List<Annotation> annotations;
    private List<Parameter> parameters;
    private List<BType> returnTypes;
    private BType[] returnTypesC;
    private boolean isPublicFunction;
    private List<Const> constants;
    private int stackFrameSize;
    private Position functionLocation;

    public AbstractNativeFunction() {
        parameters = new ArrayList<>();
        returnTypes = new ArrayList<>();
        annotations = new ArrayList<>();
        constants = new ArrayList<>();
        buildModel();
    }

    /**
     * Build Native function Model using Java annotation.
     */
    private void buildModel() {
        BallerinaFunction function = this.getClass().getAnnotation(BallerinaFunction.class);
        packageName = function.packageName();
        functionName = function.functionName();

        Argument[] methodParams = function.args();

        stackFrameSize = methodParams.length;

        symbolName = new SymbolName(packageName + ":" + functionName);
        isPublicFunction = function.isPublic();
        Arrays.stream(methodParams).
                forEach(argument -> {
                    try {
                        BType bType;
                        // For non-array types.
                        if (!argument.type().equals(TypeEnum.ARRAY)) {
                            bType = BTypes.getType(argument.type().getName());
                        } else {
                            bType = BTypes.getArrayType(argument.elementType().getName());
                        }
                        parameters.add(new Parameter(bType, new SymbolName(argument.name())));
                    } catch (BallerinaException e) {
                        // TODO: Fix this when TypeC.getType method is improved.
                        log.error("Internal Error..! Error while processing Parameters for Native ballerina" +
                                " function {}:{}.", packageName, functionName, e);
                    }
                });
        Arrays.stream(function.returnType()).forEach(
                returnType -> {
                    try {
                        BType type;
                        if (!returnType.type().equals(TypeEnum.ARRAY)) {
                            type = BTypes.getType(returnType.type().getName());
                        } else {
                            type = BTypes.getArrayType(returnType.elementType().getName());
                        }
                        returnTypes.add(type);
                    } catch (BallerinaException e) {
                        // TODO: Fix this when TypeC.getType method is improved.
                        log.error("Internal Error..! Error while processing ReturnTypes for Native ballerina " +
                                "function {}:{}.", packageName, functionName, e);
                    }
                });

        Arrays.stream(function.consts()).forEach(
                constant -> {
                    try {
                        constants.add(Utils.getConst(constant));
                    } catch (MalformedEntryException e) {
                        log.error("Internal Error..! Error while processing pre defined const {} for Native " +
                                "ballerina function {}:{}.", constant.identifier(), packageName, functionName, e);
                    }
                }
        );
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

    /**
     * Get all the variableDcls declared in the scope of BallerinaFunction
     *
     * @return list of all BallerinaFunction scoped variableDcls
     */
    public VariableDcl[] getVariableDcls() {
        return new VariableDcl[0];
    }


    @SuppressWarnings("unchecked")
    public BType[] getReturnTypes() {
        return returnTypes.toArray(new BType[returnTypes.size()]);
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

    @Override
    public boolean isPublic() {
        return isPublicFunction;
    }

    /**
     * Where Native Function logic is implemented.
     *
     * @param context Current Context instance
     * @return Native function return BValue array
     */
    public abstract BValue[] execute(Context context);

    public void executeNative(Context context) {
        BValue[] retVals = execute(context);
        BValue[] returnRefs = context.getControlStack().getCurrentFrame().returnValues;
        if (returnRefs.length != 0) {
            returnRefs[0] = retVals[0];
        }
    }

    /**
     * Util method to construct BValue array.
     *
     * @param values
     * @return BValue
     */
    public BValue[] getBValues(BValue... values) {
        return values;
    }


    public Const[] getFunctionConstats() {
        return constants.toArray(new Const[constants.size()]);
    }

    @Override
    public int getStackFrameSize() {
        return stackFrameSize;
    }

    @Override
    public void setStackFrameSize(int stackFrameSize) {
        this.stackFrameSize = stackFrameSize;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Position getFunctionLocation() {
        return functionLocation;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setFunctionLocation(Position location) {
        this.functionLocation = location;
    }
}
