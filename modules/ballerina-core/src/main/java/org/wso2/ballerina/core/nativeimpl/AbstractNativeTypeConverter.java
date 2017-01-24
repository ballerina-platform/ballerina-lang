/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
 */
package org.wso2.ballerina.core.nativeimpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.Const;
import org.wso2.ballerina.core.model.Parameter;
import org.wso2.ballerina.core.model.Position;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.TypeConverter;
import org.wso2.ballerina.core.model.VariableDcl;
import org.wso2.ballerina.core.model.statements.BlockStmt;
import org.wso2.ballerina.core.model.types.BType;
import org.wso2.ballerina.core.model.types.BTypes;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaTypeConverter;
import org.wso2.ballerina.core.nativeimpl.annotations.Utils;
import org.wso2.ballerina.core.nativeimpl.exceptions.ArgumentOutOfRangeException;
import org.wso2.ballerina.core.nativeimpl.exceptions.MalformedEntryException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * {@code {@link AbstractNativeTypeConverter}} represents a Abstract implementation of Native Ballerina TypeConverter.
 */
public abstract class AbstractNativeTypeConverter implements NativeConstruct, TypeConverter {

    /* Void RETURN */
    public static final BValue[] VOID_RETURN = new BValue[0];
    private static final Logger log = LoggerFactory.getLogger(AbstractNativeTypeConverter.class);
    private String packageName, typeConverterName;
    private SymbolName symbolName;
    private List<Annotation> annotations;
    private List<Parameter> parameters;
    private List<Parameter> returnParams;
    private boolean isPublicTypeConverter;
    private List<Const> constants;
    private int stackFrameSize;

    public AbstractNativeTypeConverter() {
        parameters = new ArrayList<>();
        returnParams = new ArrayList<>();
        annotations = new ArrayList<>();
        constants = new ArrayList<>();
        buildModel();
    }

    /**
     * Build Native typeConverter Model using Java annotation.
     */
    private void buildModel() {
        BallerinaTypeConverter typeConverter = this.getClass().getAnnotation(BallerinaTypeConverter.class);
        packageName = typeConverter.packageName();
        typeConverterName = typeConverter.typeConverterName();

        Argument[] methodParams = typeConverter.args();
        stackFrameSize = methodParams.length;
        symbolName = new SymbolName(packageName + ":" + typeConverterName);
        isPublicTypeConverter = typeConverter.isPublic();
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
                                " typeConverter {}:{}.", packageName, typeConverterName, e);
                    }
                });
        Arrays.stream(typeConverter.returnType()).forEach(
                returnType -> {
                    try {
                        BType type;
                        if (!returnType.type().equals(TypeEnum.ARRAY)) {
                            type = BTypes.getType(returnType.type().getName());
                        } else {
                            type = BTypes.getArrayType(returnType.elementType().getName());
                        }
                        returnParams.add(new Parameter(type, null));
                    } catch (BallerinaException e) {
                        // TODO: Fix this when TypeC.getType method is improved.
                        log.error("Internal Error..! Error while processing ReturnTypes for Native ballerina " +
                                "typeConverter {}:{}.", packageName, typeConverterName, e);
                    }
                });

        Arrays.stream(typeConverter.consts()).forEach(
                constant -> {
                    try {
                        constants.add(Utils.getConst(constant));
                    } catch (MalformedEntryException e) {
                        log.error("Internal Error..! Error while processing pre defined const {} for Native " +
                                "ballerina typeConverter {}:{}.", constant.identifier(), packageName,
                                typeConverterName, e);
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

    @Override
    public String getTypeConverterName() {
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
     * Get all the variableDcls declared in the scope of BallerinaTypeConverter
     *
     * @return list of all BallerinaTypeConverter scoped variableDcls
     */
    public VariableDcl[] getVariableDcls() {
        return new VariableDcl[0];
    }

    public Parameter[] getReturnParameters() {
        return returnParams.toArray(new Parameter[returnParams.size()]);
    }

    /**
     * Get Argument by index.
     *
     * @param context current {@code {@link Context }} instance.
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
        return isPublicTypeConverter;
    }

    /**
     * Where Native TypeConverter logic is implemented.
     *
     * @param context Current Context instance
     * @return Native typeConverter return BValue array
     */
    public abstract BValue convert(Context context);

    public void convertNative(Context context) {
        BValue retVals = convert(context);
        BValue[] returnRefs = context.getControlStack().getCurrentFrame().returnValues;
        if (returnRefs.length != 0) {
            returnRefs[0] = retVals;
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


    public Const[] getTypeConverterConstats() {
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
    public Position getLocation() {
        return null;
    }

    @Override
    public BlockStmt getCallableUnitBody() {
        return null;
    }
}
