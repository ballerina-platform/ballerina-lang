/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.model.types;

import org.ballerinalang.model.SymbolScope;
import org.ballerinalang.model.values.BFunctionPointer;
import org.ballerinalang.model.values.BValue;

import java.util.Arrays;

/**
 * {@code {@link BFunctionType }} represents a function pointer type in ballerina.
 *
 * @since 0.90
 */
public class BFunctionType extends BType {

    private BType[] parameterType;
    private BType[] returnParameterType;

    private boolean isReturnWordAvailable;
    private String[] parametersFieldsNames = new String[0];
    private String[] returnsParametersFieldsNames = new String[0];

    public BFunctionType(SymbolScope symbolScope, BType[] parameterType, BType[] returnParameterType) {
        super(getTypeName(parameterType, returnParameterType), null, symbolScope, BFunctionPointer.class);
        this.parameterType = parameterType != null ? parameterType : new BType[0];
        this.returnParameterType = returnParameterType != null ? returnParameterType : new BType[0];
    }

    public BFunctionType(BType[] parameterType, BType[] returnParameterType) {
        super(getTypeName(parameterType, returnParameterType), null, null, BFunctionPointer.class);
        this.parameterType = parameterType != null ? parameterType : new BType[0];
        this.returnParameterType = returnParameterType != null ? returnParameterType : new BType[0];
    }

    public BFunctionType() {
        super("function ()", null, null, BFunctionPointer.class);
        this.parameterType = new BType[0];
        this.returnParameterType = new BType[0];
    }

    public BType[] getParameterType() {
        return parameterType;
    }

    public BType[] getReturnParameterType() {
        return returnParameterType;
    }

    @Override
    public <V extends BValue> V getZeroValue() {
        return null;
    }

    @Override
    public <V extends BValue> V getEmptyValue() {
        return null;
    }

    @Override
    public TypeSignature getSig() {
        return new TypeSignature(TypeSignature.SIG_FUNCTION);
        // TODO: Fix this for Runtime function types. Without this Type Casting doesn't work.
//        return new TypeSignature(TypeSignature.SIG_FUNCTION, getSigString());
    }

    @Override
    public int getTag() {
        return TypeTags.FUNCTION_POINTER_TAG;
    }


    private String getSigString() {
        return "(" + getBTypListAsString(parameterType, true) + ")("
                + getBTypListAsString(returnParameterType, true) + ")";
    }

    public static String getTypeName(BType[] parameterType, BType[] returnParameterType) {
        return "function (" + (parameterType != null ? getBTypListAsString(parameterType, false) : "") + ")"
                + (returnParameterType != null ? " returns (" + getBTypListAsString(returnParameterType, false) +
                ")" : "");
    }

    private static String getBTypListAsString(BType[] typeNames, boolean isSigNature) {
        StringBuffer br = new StringBuffer();
        int i = 0;
        for (BType type : typeNames) {
            br.append(isSigNature ? type.getSig() : type.getName());
            if (++i < typeNames.length) {
                br.append(isSigNature ? "" : ",");
            }
        }
        return br.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BFunctionType)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        BFunctionType that = (BFunctionType) o;
        if (!Arrays.equals(parameterType, that.parameterType)) {
            return false;
        }
        return Arrays.equals(returnParameterType, that.returnParameterType);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(parameterType);
        result = 31 * result + Arrays.hashCode(returnParameterType);
        return result;
    }

    /* Utility methods for Composer. */

    public boolean isReturnWordAvailable() {
        return isReturnWordAvailable;
    }

    public void setReturnWordAvailable(boolean returnWordAvailable) {
        isReturnWordAvailable = returnWordAvailable;
    }

    public String[] getParametersFieldsNames() {
        return parametersFieldsNames;
    }

    public void setParametersFieldsNames(String[] parametersFieldsNames) {
        this.parametersFieldsNames = parametersFieldsNames;
    }

    public String[] getReturnsParametersFieldsNames() {
        return returnsParametersFieldsNames;
    }

    public void setReturnsParametersFieldsNames(String[] returnsParametersFieldsNames) {
        this.returnsParametersFieldsNames = returnsParametersFieldsNames;
    }
}
