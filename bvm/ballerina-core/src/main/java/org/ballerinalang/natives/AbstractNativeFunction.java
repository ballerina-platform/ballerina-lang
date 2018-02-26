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

import org.ballerinalang.model.Function;

/**
 * {@code {@link AbstractNativeFunction}} represents a Abstract implementation of Native Ballerina Function.
 *
 * @since 0.8.0
 */
public abstract class AbstractNativeFunction implements Function {
//
//    /**
//     * Value to be returned for functions having a void return.
//     */
//    public static final BValue[] VOID_RETURN = new BValue[0];
//
//    // BLangSymbol related attributes
//    protected Identifier identifier;
//    protected String pkgPath;
//    protected boolean isPublic = true;
//
//    private List<AnnotationAttachment> annotations;
//    private List<ParameterDef> parameterDefs;
//    private List<ParameterDef> returnParams;
//    private int stackFrameSize;
//
//    private BType[] returnParamTypes;
//    private BType[] parameterTypes;
//    private int tempStackFrameSize;
//
//    /**
//     * Initialize a native function.
//     */
//    public AbstractNativeFunction() {
//        annotations = new ArrayList<>();
//    }
//
//    public BValue getRefArgument(Context context, int index) {
//        if (index > -1) {
//            BValue result = context.getLocalWorkerData().refRegs[index];
//            if (result == null) {
//                throw new BallerinaException("argument " + index + " is null");
//            }
//
//            return result;
//        }
//        throw new ArgumentOutOfRangeException(index);
//    }
//
//    public byte[] getBlobArgument(Context context, int index) {
//        if (index > -1) {
//            byte[] result = context.getLocalWorkerData().byteRegs[index];
//            if (result == null) {
//                throw new BallerinaException("argument " + index + " is null");
//            }
//
//            return result;
//        }
//        throw new ArgumentOutOfRangeException(index);
//    }
//
//    /**
//     * This will return a int variable defined in ballerina level.
//     * In java level it would be a long value.
//     *
//     * @param context In which the variable reside.
//     * @param index   Index of the variable location.
//     * @return Long value.
//     */
//    public long getIntArgument(Context context, int index) {
//        if (index > -1) {
//            return context.getLocalWorkerData().longRegs[index];
//        }
//        throw new ArgumentOutOfRangeException(index);
//    }
//
//    public String getStringArgument(Context context, int index) {
//        if (index > -1) {
//            String str = context.getLocalWorkerData().stringRegs[index];
//            if (str == null) {
//                throw new BLangNullReferenceException();
//            }
//            return str;
//        }
//        throw new ArgumentOutOfRangeException(index);
//    }
//
//    /**
//     * This will return a float variable defined in ballerina level.
//     * In java level that would be a double value.
//     *
//     * @param context In which the variable reside.
//     * @param index   Index of the variable location.
//     * @return Double value.
//     */
//    public double getFloatArgument(Context context, int index) {
//        if (index > -1) {
//            return context.getLocalWorkerData().doubleRegs[index];
//        } else {
//            throw new ArgumentOutOfRangeException(index);
//        }
//    }
//
//    public boolean getBooleanArgument(Context context, int index) {
//        if (index > -1) {
//            return (context.getLocalWorkerData().intRegs[index] == 1);
//        }
//        throw new ArgumentOutOfRangeException(index);
//    }
//
//    @Override
//    public BType getType() {
//        return null;
//    }
//
//    @Override
//    public void setType(BType type) {
//    }
//
//    @Override
//    public Kind getKind() {
//        return null;
//    }
//
//    @Override
//    public void setKind(Kind kind) {
//    }
//
//    @Override
//    public int getVarIndex() {
//        return 0;
//    }
//
//    @Override
//    public void setVarIndex(int index) {
//    }
//
//    @Override
//    public SimpleTypeName getTypeName() {
//        return null;
//    }
//
//    /**
//     * Where Native Function logic is implemented.
//     *
//     * @param context Current Context instance
//     * @return Native function return BValue arrays
//     */
//    public abstract void execute(Context context);
//
//    /**
//     * Util method to construct BValue arrays.
//     *
//     * @param values BValues to construct the array
//     * @return Array of BValues
//     */
//    public BValue[] getBValues(BValue... values) {
//        return values;
//    }
//
//    // Methods in CallableUnit interface
//
//    /**
//     * Get all the Annotations associated with a BallerinaFunction.
//     *
//     * @return list of Annotations
//     */
//    @Override
//    public AnnotationAttachment[] getAnnotations() {
//        return annotations.toArray(new AnnotationAttachment[annotations.size()]);
//    }
//
//    /**
//     * Get list of Arguments associated with the function definition.
//     *
//     * @return list of Arguments
//     */
//    public ParameterDef[] getParameterDefs() {
//        return parameterDefs.toArray(new ParameterDef[parameterDefs.size()]);
//    }
//
//    /**
//     * Get all the variableDcls declared in the scope of BallerinaFunction.
//     *
//     * @return list of all BallerinaFunction scoped variableDcls
//     */
//    @Override
//    public VariableDef[] getVariableDefs() {
//        return new VariableDef[0];
//    }
//
//    @Override
//    public ParameterDef[] getReturnParameters() {
//        return returnParams.toArray(new ParameterDef[returnParams.size()]);
//    }
//
//    @Override
//    public int getStackFrameSize() {
//        return stackFrameSize;
//    }
//
//    @Override
//    public void setStackFrameSize(int stackFrameSize) {
//        this.stackFrameSize = stackFrameSize;
//    }
//
//    @Override
//    public int getTempStackFrameSize() {
//        return tempStackFrameSize;
//    }
//
//    @Override
//    public void setTempStackFrameSize(int stackFrameSize) {
//        if (this.tempStackFrameSize > 0 && stackFrameSize != this.tempStackFrameSize) {
//            throw new FlowBuilderException("Attempt to Overwrite tempValue Frame size. current :" +
//                    this.tempStackFrameSize + ", new :" + stackFrameSize);
//        }
//        this.tempStackFrameSize = stackFrameSize;
//    }
//
//    @Override
//    public BType[] getReturnParamTypes() {
//        return returnParamTypes;
//    }
//
//    @Override
//    public void setReturnParamTypes(BType[] returnParamTypes) {
//        this.returnParamTypes = returnParamTypes;
//    }
//
//    @Override
//    public BType[] getArgumentTypes() {
//        return parameterTypes;
//    }
//
//    @Override
//    public void setParameterTypes(BType[] parameterTypes) {
//        this.parameterTypes = parameterTypes;
//    }
//
//    // Methods in Node interface
//
//    @Override
//    public NodeLocation getNodeLocation() {
//        return null;
//    }
//
//    @Override
//    public WhiteSpaceDescriptor getWhiteSpaceDescriptor() {
//        return null;
//    }
//
//    @Override
//    public String getName() {
//        return identifier.getName();
//    }
//
//    // Methods in BLangSymbol interface
//
//    @Override
//    public void setName(String name) {
//        this.identifier = new Identifier(name);
//    }
//
//    @Override
//    public String getPackagePath() {
//        return pkgPath;
//    }
//
//    @Override
//    public void setPackagePath(String packagePath) {
//        this.pkgPath = packagePath;
//    }
//
//    @Override
//    public boolean isPublic() {
//        return isPublic;
//    }
//
//    @Override
//    public boolean isNative() {
//        return true;
//    }
}
