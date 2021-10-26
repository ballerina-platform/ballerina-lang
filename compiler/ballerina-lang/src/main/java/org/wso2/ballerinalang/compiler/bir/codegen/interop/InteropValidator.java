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
package org.wso2.ballerinalang.compiler.bir.codegen.interop;

import io.ballerina.projects.CompilerBackend;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.PlatformLibrary;
import io.ballerina.projects.PlatformLibraryScope;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.isExternFunc;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.AnnotationProc.getInteropAnnotValue;

/**
 * Java interop validation class for both field access and method invocations.
 *
 * @since 1.2.0
 */
public class InteropValidator {

    private static final CompilerContext.Key<InteropValidator> INTEROP_VALIDATE = new CompilerContext.Key<>();
    private final SymbolTable symbolTable;
    private final BLangDiagnosticLog dlog;

    private InteropValidator(CompilerContext compilerContext) {
        compilerContext.put(INTEROP_VALIDATE, this);
        this.symbolTable = SymbolTable.getInstance(compilerContext);
        this.dlog = BLangDiagnosticLog.getInstance(compilerContext);
    }

    public static InteropValidator getInstance(CompilerContext context) {

        InteropValidator interopValidator = context.get(INTEROP_VALIDATE);
        if (interopValidator == null) {
            interopValidator = new InteropValidator(context);
        }
        return interopValidator;
    }

    public void validate(ModuleId moduleId, CompilerBackend compilerBackend, BLangPackage bLangPackage) {
        dlog.setCurrentPackageId(bLangPackage.packageID);
        validateModulePackages(moduleId, compilerBackend, bLangPackage);
        validateTestPackages(moduleId, compilerBackend, bLangPackage);
    }

    private void validateModulePackages(ModuleId moduleId, CompilerBackend compilerBackend,
                                        BLangPackage bLangPackage) {
        // find module dependencies path
        Set<Path> moduleDependencyPaths = getPlatformDependencyPaths(
                moduleId, compilerBackend, PlatformLibraryScope.DEFAULT);

        // Add runtime library
        Path runtimeJar = compilerBackend.runtimeLibrary().path();
        // We check if the runtime jar exist to support bootstrap
        if (Files.exists(runtimeJar)) {
            moduleDependencyPaths.add(runtimeJar);
        }

        ClassLoader classLoader = makeClassLoader(moduleDependencyPaths);
        BIRNode.BIRPackage birPackage = bLangPackage.symbol.bir;
        // validate module functions with class names
        validateFunctions(classLoader, birPackage);
    }

    private void validateTestPackages(ModuleId moduleId, CompilerBackend compilerBackend,
                                      BLangPackage bLangPackage) {
        if (bLangPackage.moduleContextDataHolder.skipTests() || !bLangPackage.hasTestablePackage()) {
            return;
        }
        Set<Path> testDependencies = getPlatformDependencyPaths(moduleId, compilerBackend,
                                                                PlatformLibraryScope.DEFAULT);
        testDependencies.addAll(getPlatformDependencyPaths(moduleId, compilerBackend,
                                                           PlatformLibraryScope.TEST_ONLY));

        // Add runtime library
        Path runtimeJar = compilerBackend.runtimeLibrary().path();
        // We check if the runtime jar exist to support bootstrap
        if (Files.exists(runtimeJar)) {
            testDependencies.add(runtimeJar);
        }
        ClassLoader classLoader = makeClassLoader(testDependencies);
        bLangPackage.getTestablePkgs().forEach(testablePackage -> {
            BIRNode.BIRPackage testBirPackage = testablePackage.symbol.bir;
            validateFunctions(classLoader, testBirPackage);
        });
    }

    private void validateFunctions(ClassLoader classLoader, BIRNode.BIRPackage testBirPackage) {
        // validate test module functions with class names
        validateModuleFunctions(testBirPackage, classLoader);
        // validate test module type functions with class names
        validateTypeAttachedFunctions(testBirPackage, classLoader);
    }

    private Set<Path> getPlatformDependencyPaths(ModuleId moduleId, CompilerBackend compilerBackend,
                                                 PlatformLibraryScope scope) {
        return getPlatformDependencyPaths(compilerBackend.platformLibraryDependencies(moduleId.packageId(), scope));
    }

    public Set<Path> getPlatformDependencyPaths(Collection<PlatformLibrary> platformLibraries) {
        Set<Path> set = new HashSet<>();
        for (PlatformLibrary platformLibrary : platformLibraries) {
            Path path = platformLibrary.path();
            set.add(path);
        }
        return set;
    }

    private void validateModuleFunctions(BIRNode.BIRPackage module, ClassLoader classLoader) {
        // filter out functions.
        List<BIRNode.BIRFunction> functions = module.functions;
        List<BIRNode.BIRFunction> jBirFunctions = new ArrayList<>(functions.size());
        for (BIRNode.BIRFunction func : functions) {
            try {
                jBirFunctions.add(getBirFunction(func, classLoader));
            } catch (JInteropException e) {
                dlog.error(func.pos, e.getCode(), e.getMessage());
            }
        }
        module.functions.clear();
        module.functions.addAll(jBirFunctions);
    }

    private void validateTypeAttachedFunctions(BIRNode.BIRPackage module, ClassLoader classLoader) {
        List<BIRNode.BIRTypeDefinition> typeDefs = module.typeDefs;
        for (BIRNode.BIRTypeDefinition optionalTypeDef : typeDefs) {
            List<BIRNode.BIRFunction> attachedFuncs = optionalTypeDef.attachedFuncs;
            List<BIRNode.BIRFunction> jAttachedFuncs = new ArrayList<>(attachedFuncs.size());
            for (BIRNode.BIRFunction func : attachedFuncs) {
                try {
                    jAttachedFuncs.add(getBirFunction(func, classLoader));
                } catch (JInteropException e) {
                    dlog.error(func.pos, e.getCode(), e.getMessage());
                }
                optionalTypeDef.attachedFuncs = jAttachedFuncs;
            }
        }
    }

    private ClassLoader makeClassLoader(Set<Path> moduleDependencies) {
        if (moduleDependencies == null || moduleDependencies.size() == 0) {
            return Thread.currentThread().getContextClassLoader();
        }
        List<URL> dependentJars = new ArrayList<>();
        for (Path dependency : moduleDependencies) {
            try {
                dependentJars.add(dependency.toUri().toURL());
            } catch (MalformedURLException e) {
                // ignore
            }
        }
        return new URLClassLoader(dependentJars.toArray(new URL[]{}), ClassLoader.getPlatformClassLoader());
    }

    /**
     * Method that validates Java interop functions and link them with Java methods.
     *
     * @param methodValidationRequest the methodValidationRequest
     * @return validated and linked java method representation
     */
    JMethod validateAndGetJMethod(InteropValidationRequest.MethodValidationRequest methodValidationRequest,
                                  ClassLoader classLoader) {
        // Populate JMethodRequest from the BValue
        JMethodRequest jMethodRequest = JMethodRequest.build(methodValidationRequest, classLoader);

        // Find the most specific Java method or constructor for the given request
        JMethodResolver methodResolver = new JMethodResolver(classLoader, symbolTable);

        return methodResolver.resolve(jMethodRequest);
    }

    private BIRNode.BIRFunction getBirFunction(BIRNode.BIRFunction birFunc, ClassLoader classLoader) {
        if (isExternFunc(birFunc)) {
            InteropValidationRequest jInteropValidationReq = getInteropAnnotValue(birFunc);
            if (jInteropValidationReq != null) {
                return createJInteropFunction(jInteropValidationReq, birFunc, classLoader);
            }
        }
        return birFunc;
    }

    BIRNode.BIRFunction createJInteropFunction(InteropValidationRequest jInteropValidationReq,
                                               BIRNode.BIRFunction birFunc, ClassLoader classLoader) {

        if (jInteropValidationReq instanceof InteropValidationRequest.MethodValidationRequest) {
            InteropValidationRequest.MethodValidationRequest methodValidationRequest =
                    ((InteropValidationRequest.MethodValidationRequest) jInteropValidationReq);
            methodValidationRequest.restParamExist = birFunc.restParam != null;
            JMethod jMethod = validateAndGetJMethod(methodValidationRequest, classLoader);
            return new JMethodBIRFunction(birFunc, jMethod);
        } else {
            InteropValidationRequest.FieldValidationRequest fieldValidationRequest =
                    (InteropValidationRequest.FieldValidationRequest) jInteropValidationReq;
            JavaField jField = validateAndGetJField(fieldValidationRequest, classLoader);
            return new JFieldBIRFunction(birFunc, jField);
        }
    }

    /**
     * Method that validates Java interop functions and link them with Java fields.
     *
     * @param fieldValidationRequest the fieldValidationRequest
     * @return validated and linked java field representation
     */
    JavaField validateAndGetJField(InteropValidationRequest.FieldValidationRequest fieldValidationRequest,
                                   ClassLoader classLoader) {
        // 1) Load Java class  - validate
        JFieldMethod method = fieldValidationRequest.fieldMethod;
        String className = fieldValidationRequest.klass;
        Class<?> clazz = JInterop.loadClass(className, classLoader);

        // 2) Load Java method details - use the method kind in the request - validate kind and the existence of the
        // method. Possible there may be more than one methods for the given kind and the name
        String fieldName = fieldValidationRequest.name;
        JavaField javaField;
        try {
            Field field = clazz.getField(fieldName);
            javaField = new JavaField(method, field);
        } catch (NoSuchFieldException e) {
            throw new JInteropException(DiagnosticErrorCode.FIELD_NOT_FOUND, "No such field '" + fieldName +
                    "' found in class '" + className + "'");
        }
        if (javaField.isStatic()) {
            validateJStaticField(method, fieldValidationRequest.bFuncType.paramTypes.size(), fieldName, className);
        } else {
            validateJInstanceField(method, fieldValidationRequest.bFuncType.paramTypes, fieldName, className);
        }
        return javaField;
    }

    void validateJStaticField(JFieldMethod method, int bFuncParamCount, String fieldName, String className) {
        if (method == JFieldMethod.MUTATE) {
            if (bFuncParamCount != 1) {
                throw new JInteropException(DiagnosticErrorCode.INVALID_NUMBER_OF_PARAMETERS,
                        "One parameter is required to set the value to the static field '" + fieldName +
                                "' in class '" + className + "'");
            }
        } else {
            if (bFuncParamCount != 0) {
                throw new JInteropException(DiagnosticErrorCode.INVALID_NUMBER_OF_PARAMETERS,
                        "No parameter is required to get the value of the static field '" + fieldName +
                                "' in class '" + className + "'");
            }
        }
    }

    void validateJInstanceField(JFieldMethod method, List<BType> bFuncParamTypes, String fieldName, String className) {
        int bFuncParamCount = bFuncParamTypes.size();
        if (method == JFieldMethod.MUTATE) {
            if (bFuncParamCount != 2) {
                throw new JInteropException(DiagnosticErrorCode.INVALID_NUMBER_OF_PARAMETERS,
                        "Two parameters are required to set the value to the instance field '" + fieldName +
                                "' in class '" + className + "'");
            } else if (bFuncParamTypes.get(0).tag != TypeTags.HANDLE) {
                throw new JInteropException(DiagnosticErrorCode.INVALID_PARAMETER_TYPE, "First parameter needs "
                        + "to be of the handle type to set the value to the instance field '" + fieldName +
                        "' in class '" + className + "'");
            }
        } else {
            if (bFuncParamCount != 1) {
                throw new JInteropException(DiagnosticErrorCode.INVALID_NUMBER_OF_PARAMETERS,
                        "One parameter is required to get the value of the instance field '" + fieldName +
                                "' in class '" + className + "'");
            } else if (bFuncParamTypes.get(0).tag != TypeTags.HANDLE) {
                throw new JInteropException(DiagnosticErrorCode.INVALID_PARAMETER_TYPE, "The parameter needs "
                        + "to be of the handle type to get the value of the instance field '" + fieldName +
                        "' in class '" + className + "'");
            }
        }
    }
}
