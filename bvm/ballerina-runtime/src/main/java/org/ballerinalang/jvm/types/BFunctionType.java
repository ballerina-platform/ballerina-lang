/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.jvm.types;

import java.util.Arrays;

/**
 * {@code {@link BFunctionType }} represents a function type in ballerina.
 *
 * @since 0.995.0
 */
public class BFunctionType extends AnnotatableType {

    public BType[] paramTypes;
    public BType restType;
    public BType retType;

    public BFunctionType() {
        super("function ()", null, Object.class);
        this.paramTypes = new BType[0];
        this.retType = BTypes.typeNull;
    }

    public BFunctionType(BType[] paramTypes, BType restType, BType retType) {
        super("function ()", null, Object.class);
        this.paramTypes = paramTypes;
        this.restType = restType;
        this.retType = retType;
    }

    public BType[] getParameterType() {
        return paramTypes;
    }

    public BType getReturnParameterType() {
        return retType;
    }

    @Override
    public <V extends Object> V getZeroValue() {
        return null;
    }

    @Override
    public <V extends Object> V getEmptyValue() {
        return null;
    }

    @Override
    public int getTag() {
        return TypeTags.FUNCTION_POINTER_TAG;
    }

    public static String getTypeName(BType[] parameterType, BType retType) {
        return "function (" + (parameterType != null ? getBTypeListAsString(parameterType) : "") + ")" +
                (retType != null ? " returns (" + retType + ")" : "");
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
        return retType.equals(that.retType);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(paramTypes);
        result = 31 * result + retType.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return getTypeName(paramTypes, retType);
    }

    @Override
    public String getAnnotationKey() {
        return this.typeName;
    }
}
