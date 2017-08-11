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
package org.ballerinalang.runtime.model;

import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BLangRuntimeException;

/**
 * Represents a Ballerina Service Interceptor of a Service Connector.
 *
 * @since 0.89
 */
public class ServiceInterceptor {

    private ProgramFile programFile;
    private PackageInfo packageInfo;
    private FunctionInfo requestFunction, responseFunction;
    public static final String REQUEST_INTERCEPTOR_NAME = "requestInterceptor";
    public static final String RESPONSE_INTERCEPTOR_NAME = "responseInterceptor";

    protected ServiceInterceptor(ProgramFile bLangProgram, PackageInfo bLangPackage, FunctionInfo requestFunction,
                                 FunctionInfo responseFunction) {
        this.programFile = bLangProgram;
        this.packageInfo = bLangPackage;
        this.requestFunction = requestFunction;
        this.responseFunction = responseFunction;
    }

    public ProgramFile getProgramFile() {
        return programFile;
    }

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public FunctionInfo getRequestFunction() {
        return requestFunction;
    }

    public FunctionInfo getResponseFunction() {
        return responseFunction;
    }

    /**
     * Represent Result of an interception.
     */
    public static class Result {

        boolean invokeNext;
        BMessage messageIntercepted;

        public Result(boolean invokeNext, BMessage messageIntercepted) {
            this.invokeNext = invokeNext;
            this.messageIntercepted = messageIntercepted;
        }

        public boolean isInvokeNext() {
            return invokeNext;
        }

        public BMessage getMessageIntercepted() {
            return messageIntercepted;
        }
    }

    /**
     * Builder for building ServiceInterceptor.
     *
     * @since 0.89
     */
    public static class ServiceInterceptorBuilder {

        private ProgramFile programFile;
        private String packageName;

        public ServiceInterceptorBuilder(ProgramFile programFile, String packageName) {
            this.programFile = programFile;
            this.packageName = packageName;
        }

        public ServiceInterceptor build() {
            FunctionInfo request, response;
            PackageInfo packageInfo = programFile.getPackageInfo(packageName);
            if (packageInfo == null) {
                throw new BLangRuntimeException("no exported package found called " + packageName);
            }
            request = getInterceptorFunction(packageInfo, REQUEST_INTERCEPTOR_NAME);
            response = getInterceptorFunction(packageInfo, RESPONSE_INTERCEPTOR_NAME);
            return new ServiceInterceptor(programFile, packageInfo, request, response);
        }

        private FunctionInfo getInterceptorFunction(PackageInfo packageInfo, String funcName) {
            BType[] interceptorInTypes = {BTypes.typeMessage};
            BType[] interceptorOutTypes = {BTypes.typeBoolean, BTypes.typeMessage};

            FunctionInfo function = packageInfo.getFunctionInfo(funcName);
            if (function != null && matchArgTypes(function.getParamTypes(), interceptorInTypes)
                    && matchArgTypes(function.getRetParamTypes(), interceptorOutTypes)) {
                return function;
            }
            return null;
        }

        private boolean matchArgTypes(BType[] actualArgTypes, BType[] expectedArgTypes) {
            boolean matching = false;
            if (actualArgTypes.length == expectedArgTypes.length) {
                matching = true;
                for (int i = 0; i < actualArgTypes.length; i++) {
                    // Match only primitive types.
                    if (!actualArgTypes[i].equals(expectedArgTypes[i])) {
                        matching = false;
                        break;
                    }
                }
            }
            return matching;
        }
    }
}
