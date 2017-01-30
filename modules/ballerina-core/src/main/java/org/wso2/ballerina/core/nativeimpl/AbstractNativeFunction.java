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
import org.wso2.ballerina.core.model.ConstDef;
import org.wso2.ballerina.core.model.Function;
import org.wso2.ballerina.core.model.NodeLocation;
import org.wso2.ballerina.core.model.NodeVisitor;
import org.wso2.ballerina.core.model.ParameterDef;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.VariableDef;
import org.wso2.ballerina.core.model.statements.BlockStmt;
import org.wso2.ballerina.core.model.symbols.SymbolScope;
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
 *
 * @since 0.8.0
 */
public abstract class AbstractNativeFunction implements Function {

    /* Void RETURN */
    public static final BValue[] VOID_RETURN = new BValue[0];
    private static final Logger log = LoggerFactory.getLogger(AbstractNativeFunction.class);

    // BLangSymbol related attributes
    protected String name;
    protected String pkgPath;
    protected boolean isPublic = true;
    protected SymbolName symbolName;

    private List<Annotation> annotations;
    private List<ParameterDef> parameterDefs;
    private List<ParameterDef> returnParams;
    private List<ConstDef> constants;
    private int stackFrameSize;

    public AbstractNativeFunction() {
        parameterDefs = new ArrayList<>();
        returnParams = new ArrayList<>();
        annotations = new ArrayList<>();
        constants = new ArrayList<>();
        buildModel();
    }

    /**
     * Build Native function Model using Java annotation.
     */
    private void buildModel() {
        BallerinaFunction function = this.getClass().getAnnotation(BallerinaFunction.class);
        pkgPath = function.packageName();
        name = function.functionName();

        Argument[] methodParams = function.args();

        stackFrameSize = methodParams.length;

        symbolName = new SymbolName(pkgPath + ":" + name);
        isPublic = function.isPublic();
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
                        parameterDefs.add(new ParameterDef(bType, new SymbolName(argument.name())));
                    } catch (BallerinaException e) {
                        // TODO: Fix this when TypeC.getType method is improved.
                        log.error("Internal Error..! Error while processing Parameters for Native ballerina" +
                                " function {}:{}.", pkgPath, name, e);
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
                        returnParams.add(new ParameterDef(type, null));
                    } catch (BallerinaException e) {
                        // TODO: Fix this when TypeC.getType method is improved.
                        log.error("Internal Error..! Error while processing ReturnTypes for Native ballerina " +
                                "function {}:{}.", pkgPath, name, e);
                    }
                });

        Arrays.stream(function.consts()).forEach(
                constant -> {
                    try {
                        constants.add(Utils.getConst(constant));
                    } catch (MalformedEntryException e) {
                        log.error("Internal Error..! Error while processing pre defined const {} for Native " +
                                "ballerina function {}:{}.", constant.identifier(), pkgPath, name, e);
                    }
                }
        );
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
        if (index > -1 && index < parameterDefs.size()) {
            return context.getControlStack().getCurrentFrame().values[index];
        }
        throw new ArgumentOutOfRangeException(index);
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


    // Methods in Node interface

    @Override
    public void accept(NodeVisitor visitor){
    }

    @Override
    public NodeLocation getNodeLocation() {
        return null;
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

    @Override
    public SymbolScope getSymbolScope() {
        return null;
    }
}
