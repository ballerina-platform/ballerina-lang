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
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.nativeimpl.jvm.interop;

import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.ballerinalang.nativeimpl.jvm.interop.JInteropException.OVERLOADED_METHODS_REASON;

/**
 * Responsible for resolving a Java method for a given {@code JMethodResolverRequest}.
 *
 * @since 1.0.0
 */
class JMethodResolver {

    JMethod resolve(JMethodRequest jMethodRequest) {
        // 1) Get java methods (that matches with the method name) or constructor list
        // 2) If the above list is zero then throw an error
        // 3) Filter out the constructors or methods that have the same number of
        //      parameters as the number of constraints
        // 4) If the above list is zero then throw an error
        ParamTypeConstraint[] paramTypeConstraints = jMethodRequest.paramTypeConstraints;
        List<JMethod> jMethods = findMethodsWithSameParamCount(paramTypeConstraints.length,
                jMethodRequest.declaringClass, jMethodRequest.methodName, jMethodRequest.kind);
        if (jMethods.isEmpty()) {
            throw new JInteropException(JInteropException.METHOD_NOT_FOUND_REASON,
                    "No such Java method '" + jMethodRequest.methodName +
                            "' found in class '" + jMethodRequest.declaringClass + "'");
        }
        // 5) Now resolve the most specific method using the constraints.
        return resolve(jMethodRequest, jMethods);
    }

    private JMethod resolve(JMethodRequest jMethodRequest, List<JMethod> jMethods) {
        boolean noConstraints = noConstraintsSpecified(jMethodRequest.paramTypeConstraints);
        if (jMethods.size() == 1 && noConstraints) {
            return jMethods.get(0);
        } else if (noConstraints) {
            throw new JInteropException(OVERLOADED_METHODS_REASON,
                    "Overloaded methods with the name '" + jMethodRequest.methodName +
                            "', please specify Java class types for each parameter " +
                            "with 'paramTypes' field in the annotation");
        }

        JMethod jMethod = resolveExactMethod(jMethodRequest.declaringClass, jMethodRequest.methodName,
                jMethodRequest.kind, jMethodRequest.paramTypeConstraints);

        if (jMethod == JMethod.NO_SUCH_METHOD) {
            return resolveMatchingMethod(jMethods, jMethodRequest.paramTypeConstraints);
        }
        return jMethod;
    }

    private JMethod resolveExactMethod(Class clazz, String name, JMethodKind kind, ParamTypeConstraint[] constraints) {
        Class<?>[] paramTypes = new Class<?>[constraints.length];
        for (int constraintIndex = 0; constraintIndex < constraints.length; constraintIndex++) {
            paramTypes[constraintIndex] = constraints[constraintIndex].get();
        }

        Executable executable = (kind == JMethodKind.CONSTRUCTOR) ? resolveConstructor(clazz, paramTypes) :
                resolveMethod(clazz, name, paramTypes);
        if (executable != null) {
            return JMethod.build(kind, (kind == JMethodKind.CONSTRUCTOR) ? resolveConstructor(clazz, paramTypes) :
                    resolveMethod(clazz, name, paramTypes));
        } else {
            return JMethod.NO_SUCH_METHOD;
        }
    }

    private JMethod resolveMatchingMethod(List<JMethod> jMethods, ParamTypeConstraint[] constraints) {
        List<JMethod> resolvedJMethods = new ArrayList<>();
        for (JMethod jMethod : jMethods) {
            boolean resolved = true;
            Class<?>[] formalParamTypes = jMethod.getParamTypes();
            for (int paramIndex = 0; paramIndex < formalParamTypes.length; paramIndex++) {
                Class<?> formalParamType = formalParamTypes[paramIndex];
                if (formalParamType.isAssignableFrom(constraints[paramIndex].get())) {
                    continue;
                }
                resolved = false;
                break;
            }
            if (resolved) {
                resolvedJMethods.add(jMethod);
            }
        }

        if (resolvedJMethods.isEmpty()) {
            throw new JInteropException(JInteropException.METHOD_NOT_FOUND_REASON,
                    "no such Java method '" + jMethods.get(0).getName() +
                            "' that matches with the given parameter types" +
                            " found in class '" + jMethods.get(0).getClassName() + "'");
        } else if (resolvedJMethods.size() > 1) {
            throw new JInteropException(OVERLOADED_METHODS_REASON,
                    "Overloaded methods with the name '" + jMethods.get(0).getName() +
                            ", that matches with the specified parameter types constraints");
        } else {
            return resolvedJMethods.get(0);
        }
    }

    private List<JMethod> findMethodsWithSameParamCount(int paramCount,
                                                        Class<?> declaringClass,
                                                        String methodName,
                                                        JMethodKind kind) {
        return getExecutables(declaringClass, methodName, kind)
                .stream()
                .filter(executable -> executable.getParameterCount() == paramCount)
                .map(executable -> JMethod.build(kind, executable))
                .collect(Collectors.toList());
    }

    private List<Executable> getExecutables(Class clazz, String methodName, JMethodKind kind) {
        return kind == JMethodKind.CONSTRUCTOR ? Arrays.asList(clazz.getConstructors()) :
                Arrays.stream(clazz.getMethods())
                        .filter(method -> method.getName().equals(methodName))
                        .collect(Collectors.toList());
    }

    private Executable resolveConstructor(Class<?> clazz, Class<?>... paramTypes) {
        try {
            return clazz.getConstructor(paramTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private Executable resolveMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
        try {
            return clazz.getMethod(name, paramTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private boolean noConstraintsSpecified(ParamTypeConstraint[] constraints) {
        for (ParamTypeConstraint constraint : constraints) {
            if (constraint != ParamTypeConstraint.NO_CONSTRAINT) {
                return false;
            }
        }
        return true;
    }
}
