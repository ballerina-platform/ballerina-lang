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
package org.ballerinalang.core.model.types;

import org.ballerinalang.core.model.values.BFunctionPointer;
import org.ballerinalang.core.model.values.BValue;

import java.util.Arrays;

/**
 * {@code {@link BFunctionType }} represents a function type in ballerina.
 *
 * @since 0.90
 */
public class BFunctionType extends BType {

    public BType[] paramTypes;
    public BType restType;
    public BType[] retParamTypes;

    private boolean hasReturnsKeyword;
    private String[] parametersFieldsNames = new String[0];
    private String[] returnsParametersFieldsNames = new String[0];

    public BFunctionType() {
        super("function ()", null, BFunctionPointer.class);
        this.paramTypes = new BType[0];
        this.retParamTypes = new BType[0];
    }

    public BFunctionType(BType[] paramTypes, BType restType,  BType[] retParamType) {
        super("function ()", null, BFunctionPointer.class);
        this.paramTypes = paramTypes;
        this.restType = restType;
        this.retParamTypes = retParamType;
    }

    public BType[] getParameterType() {
        return paramTypes;
    }

    public BType[] getReturnParameterType() {
        return retParamTypes;
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
    public int getTag() {
        return TypeTags.FUNCTION_POINTER_TAG;
    }

    public static String getTypeName(BType[] parameterType, BType[] returnParameterType) {
        return "function (" + (parameterType != null ? getBTypeListAsString(parameterType) : "") + ")"
                + (returnParameterType != null ? " returns (" + getBTypeListAsString(returnParameterType) +
                ")" : "");
    }

    private static String getBTypeListAsString(BType[] typeNames) {
        StringBuffer br = new StringBuffer();
        int i = 0;
        for (BType type : typeNames) {
            br.append(type.getName());
            if (++i < typeNames.length) {
                br.append(",");
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
        if (!Arrays.equals(paramTypes, that.paramTypes)) {
            return false;
        }
        return Arrays.equals(retParamTypes, that.retParamTypes);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(paramTypes);
        result = 31 * result + Arrays.hashCode(retParamTypes);
        return result;
    }

    @Override
    public String toString() {
        return getTypeName(paramTypes, retParamTypes);
    }

    /* Utility methods for Composer. */

    public boolean hasReturnsKeyword() {
        return hasReturnsKeyword;
    }

    public void setHasReturnsKeyword(boolean hasReturnsKeyword) {
        this.hasReturnsKeyword = hasReturnsKeyword;
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
