/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.bindgen.model;

import org.ballerinalang.bindgen.utils.BindgenEnv;
import org.ballerinalang.bindgen.utils.BindgenUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.ballerinalang.bindgen.utils.BindgenConstants.ARRAY_BRACKETS;
import static org.ballerinalang.bindgen.utils.BindgenConstants.BALLERINA_RESERVED_WORDS;
import static org.ballerinalang.bindgen.utils.BindgenConstants.EXCEPTION_CLASS_PREFIX;
import static org.ballerinalang.bindgen.utils.BindgenConstants.JAVA_STRING;
import static org.ballerinalang.bindgen.utils.BindgenConstants.JAVA_STRING_ARRAY;
import static org.ballerinalang.bindgen.utils.BindgenUtils.getAlias;
import static org.ballerinalang.bindgen.utils.BindgenUtils.getBallerinaHandleType;
import static org.ballerinalang.bindgen.utils.BindgenUtils.getBallerinaParamType;
import static org.ballerinalang.bindgen.utils.BindgenUtils.getJavaType;
import static org.ballerinalang.bindgen.utils.BindgenUtils.isStaticMethod;

/**
 * Class for storing details pertaining to a specific Java method used for Ballerina bridge code generation.
 *
 * @since 1.2.0
 */
public class JMethod extends BFunction {

    private BindgenEnv env;
    private boolean isStatic;
    private boolean hasReturn = false;
    private boolean returnError = false;
    private boolean objectReturn = false;
    private boolean isArrayReturn = false;
    private boolean hasException = false;
    private boolean handleException = false;
    private boolean isStringReturn = false;
    private boolean isStringArrayReturn = false;
    private boolean javaArraysModule = false;
    private String parentPrefix;

    private Class parentClass;
    private Method method;
    private String methodName;
    private String unescapedMethodName;
    private String returnType;
    private String exceptionName;
    private String returnTypeJava;
    private String javaMethodName;
    private String exceptionConstName;
    private String returnComponentType;

    private List<JParameter> parameters = new ArrayList<>();
    private StringBuilder paramTypes = new StringBuilder();
    private Set<String> importedPackages = new HashSet<>();

    JMethod(Method m, BindgenEnv env, String parentPrefix, Class jClass, int overloaded) {
        super(BFunctionKind.METHOD, env);
        this.env = env;
        this.parentPrefix = parentPrefix;
        method = m;
        javaMethodName = m.getName();
        if (overloaded > 1) {
            methodName = m.getName() + overloaded;
        } else {
            methodName = m.getName();
        }
        isStatic = isStaticMethod(m);
        super.setStatic(isStatic);
        this.parentClass = jClass;
        setDeclaringClass(parentClass);

        // Set the attributes required to identify different return types.
        Class returnTypeClass = m.getReturnType();
        if (!returnTypeClass.equals(Void.TYPE)) {
            setReturnTypeAttributes(returnTypeClass);
        }
        setExternalFunctionName(jClass.getName().replace(".", "_").replace("$", "_") + "_" + methodName);
        // Set the attributes relevant to error returns.
        for (Class<?> exceptionType : m.getExceptionTypes()) {
            try {
                if (!this.getClass().getClassLoader().loadClass(RuntimeException.class.getCanonicalName())
                        .isAssignableFrom(exceptionType)) {
                    JError jError = new JError(exceptionType);
                    setThrowable(jError);
                    BindgenUtils.addImportedPackage(exceptionType, importedPackages);
                    exceptionName = jError.getShortExceptionName();
                    exceptionConstName = jError.getExceptionConstName();
                    if (env.getModulesFlag()) {
                        exceptionName = getPackageAlias(exceptionName, exceptionType);
                        exceptionConstName = getPackageAlias(exceptionConstName, exceptionType);
                    }
                    env.setExceptionList(jError);
                    hasException = true;
                    handleException = true;
                    break;
                }
            } catch (ClassNotFoundException ignore) {
            }
        }
        setErrorType(exceptionName);

        // Set the attributes required to identify different parameters.
        setParameters(m.getParameters());

        if (objectReturn && !env.getAllJavaClasses().contains(returnTypeClass.getName())) {
            if (isArrayReturn) {
                env.setClassListForLooping(returnTypeClass.getComponentType().getName());
            } else {
                env.setClassListForLooping(returnTypeClass.getName());
            }
        }

        unescapedMethodName = methodName;
        if (isStatic) {
            super.setFunctionName(getAlias(jClass, env.getAliases()) + "_" + methodName);
        } else {
            List<String> reservedWords = Arrays.asList(BALLERINA_RESERVED_WORDS);
            if (reservedWords.contains(methodName)) {
                methodName = "'" + methodName;
            }
            super.setFunctionName(methodName);
        }

        if (exceptionName == null && returnType == null && javaArraysModule) {
            setErrorType("error?");
        }
    }

    private void setReturnTypeAttributes(Class returnTypeClass) {
        hasReturn = true;
        BindgenUtils.addImportedPackage(returnTypeClass, importedPackages);

        returnTypeJava = getJavaType(returnTypeClass);
        setExternalReturnType(getBallerinaHandleType(returnTypeClass));
        returnType = getBallerinaParamType(returnTypeClass, env.getAliases());
        returnType = getExceptionName(returnTypeClass, returnType);
        if (returnTypeClass.isArray()) {
            javaArraysModule = true;
            hasException = true;
            returnError = true;
            isArrayReturn = true;
            if (returnTypeClass.getComponentType().isPrimitive()) {
                objectReturn = false;
            } else if (getAlias(returnTypeClass, env.getAliases()).equals(JAVA_STRING_ARRAY)) {
                objectReturn = false;
                isStringArrayReturn = true;
            } else {
                returnComponentType = getAlias(returnTypeClass.getComponentType(), env.getAliases());
                returnComponentType = getExceptionName(returnTypeClass.getComponentType(), returnComponentType);
                returnType = returnComponentType + ARRAY_BRACKETS;
                if (env.getModulesFlag()) {
                    returnType = getPackageAlias(returnType, returnTypeClass.getComponentType());
                    returnComponentType = getPackageAlias(returnComponentType, returnTypeClass.getComponentType());
                }
                objectReturn = true;
            }
        } else if (returnTypeClass.isPrimitive()) {
            objectReturn = false;
        } else if (getAlias(returnTypeClass, env.getAliases()).equals(JAVA_STRING)) {
            isStringReturn = true;
        } else {
            if (env.getModulesFlag()) {
                returnType = getPackageAlias(returnType, returnTypeClass);
            }
            objectReturn = true;
        }
        setReturnType(returnType);
    }

    private String getPackageAlias(String shortClassName, Class objectType) {
        if (objectType.getPackage() != parentClass.getPackage()) {
            return objectType.getPackageName().replace(".", "") + ":" + shortClassName;
        }
        return shortClassName;
    }

    private String getExceptionName(Class exception, String name) {
        try {
            // Append the exception class prefix in front of bindings generated for Java exceptions.
            if (this.getClass().getClassLoader().loadClass(Exception.class.getCanonicalName())
                    .isAssignableFrom(exception)) {
                return EXCEPTION_CLASS_PREFIX + name;
            }
        } catch (ClassNotFoundException ignore) {
            // Silently ignore if the exception class cannot be found.
        }
        return name;
    }

    private void setParameters(Parameter[] paramArr) {
        for (Parameter param : paramArr) {
            BindgenUtils.addImportedPackage(param.getType(), importedPackages);
            paramTypes.append(getAlias(param.getType(), env.getAliases()).toLowerCase(Locale.ENGLISH));
            JParameter parameter = new JParameter(param, parentClass, env);
            parameters.add(parameter);
            if (parameter.getIsPrimitiveArray()) {
                javaArraysModule = true;
                returnError = true;
                hasException = true;
            }
            if (parameter.isObjArrayParam() || parameter.getIsStringArray()) {
                javaArraysModule = true;
                returnError = true;
                hasException = true;
            }
        }
    }

    public String getJavaMethodName() {
        return javaMethodName;
    }

    String getParamTypes() {
        return paramTypes.toString();
    }

    public boolean getHasException() {
        return hasException;
    }

    public boolean getIsStringReturn() {
        return isStringReturn;
    }

    public String getReturnType() {
        return returnType;
    }

    public boolean getHasReturn() {
        return hasReturn;
    }

    public String getMethodName() {
        return unescapedMethodName;
    }

    public List<JParameter> getParameters() {
        return parameters;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public boolean isHandleException() {
        return handleException;
    }

    public String getExceptionName() {
        return exceptionName;
    }

    public boolean isReturnError() {
        return returnError;
    }

    public Method getMethod() {
        return method;
    }

    boolean requireJavaArrays() {
        return javaArraysModule;
    }

    Set<String> getImportedPackages() {
        return importedPackages;
    }

    public boolean isArrayReturn() {
        return isArrayReturn;
    }

    public String getBalFunctionName() {
        return parentPrefix + "_" + unescapedMethodName;
    }

    public String getFunctionReturnType() {
        StringBuilder returnString = new StringBuilder();
        if (getHasReturn()) {
            returnString.append(this.returnType);
            if (getIsStringReturn()) {
                returnString.append("?");
            }
            if (getHasException()) {
                if (isHandleException()) {
                    returnString.append("|").append(getExceptionName());
                    if (isReturnError()) {
                        returnString.append("|error");
                    }
                } else {
                    returnString.append("|error");
                }
            }
        } else if (getHasException()) {
            if (isHandleException()) {
                returnString.append(getExceptionName()).append("?");
                if (isReturnError()) {
                    returnString.append("|error?");
                }
            } else {
                returnString.append("error?");
            }
        }

        return returnString.toString();
    }

    public String getReturnComponentType() {
        return returnComponentType;
    }

    public String getReturnTypeJava() {
        return returnTypeJava;
    }

    public String getExceptionConstName() {
        return exceptionConstName;
    }

    public boolean isObjectReturn() {
        return objectReturn;
    }

    public boolean isStringReturn() {
        return isStringReturn;
    }

    public boolean isStringArrayReturn() {
        return isStringArrayReturn;
    }
}
