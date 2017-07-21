/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.model.types;

/**
 * {@code FunctionTypeName} represents a function pointer type name with its constraints at
 * {@link org.ballerinalang.model.builder.BLangModelBuilder}.
 *
 * @since 0.90
 */
public class FunctionTypeName extends SimpleTypeName {
    private SimpleTypeName[] paramTypes;
    private SimpleTypeName[] returnParamsTypes;

    private boolean isReturnWordAvailable;
    private String[] paramFieldNames;
    private String[] returnParamFieldNames;

    public FunctionTypeName(SimpleTypeName[] paramTypes, SimpleTypeName[] returnParamsTypes) {
        super(getTypeName(paramTypes, returnParamsTypes), null, null);
        this.paramTypes = paramTypes;
        this.returnParamsTypes = returnParamsTypes;
    }

    public SimpleTypeName[] getParamTypes() {
        return paramTypes;
    }

    public SimpleTypeName[] getReturnParamsTypes() {
        return returnParamsTypes;
    }

    @Override
    public String toString() {
        return getTypeName(paramTypes, returnParamsTypes);
    }

    public static String getTypeName(SimpleTypeName[] paramTypes, SimpleTypeName[] returnParamsTypes) {
        return "function (" + (paramTypes != null ?
                getSimpleTypeNameListAsString(paramTypes) : "") + ")" + (returnParamsTypes != null ? " returns (" +
                getSimpleTypeNameListAsString(returnParamsTypes) + ")" : "");
    }

    private static String getSimpleTypeNameListAsString(SimpleTypeName[] typeNames) {
        StringBuffer br = new StringBuffer();
        int i = 0;
        for (SimpleTypeName simpleTypeName : typeNames) {
            br.append(simpleTypeName.getName());
            if (++i < typeNames.length) {
                br.append(",");
            }
        }
        return br.toString();
    }

    /* Util methods for retrieving information about function type syntex */

    public boolean isReturnWordAvailable() {
        return isReturnWordAvailable;
    }

    public void setReturnWordAvailable(boolean returnWordAvailable) {
        isReturnWordAvailable = returnWordAvailable;
    }

    public String[] getParamFieldNames() {
        return paramFieldNames;
    }

    public void setParamFieldNames(String[] paramFieldNames) {
        this.paramFieldNames = paramFieldNames;
    }

    public String[] getReturnParamFieldNames() {
        return returnParamFieldNames;
    }

    public void setReturnParamFieldNames(String[] returnParamFieldNames) {
        this.returnParamFieldNames = returnParamFieldNames;
    }
}
