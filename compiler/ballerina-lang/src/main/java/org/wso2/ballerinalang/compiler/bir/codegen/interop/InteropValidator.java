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

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.compiler.CompilerOptionName;
import org.ballerinalang.compiler.JarResolver;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.ballerinalang.compiler.JarResolver.JAR_RESOLVER_KEY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.isExternFunc;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.AnnotationProc.getInteropAnnotValue;
import static org.wso2.ballerinalang.compiler.util.CompilerUtils.getBooleanValueIfSet;

/**
 * Java interop validation class for both field access and method invocations.
 *
 * @since 1.2.0
 */
public class InteropValidator {

    private static final CompilerContext.Key<InteropValidator> INTEROP_VALIDATE = new CompilerContext.Key<>();
    private final SymbolTable symbolTable;
    private final BLangDiagnosticLog dlog;
    private boolean baloGen;
    private CompilerContext compilerContext;
    private boolean skipModuleDependencies;

    private InteropValidator(CompilerContext compilerContext) {
        compilerContext.put(INTEROP_VALIDATE, this);
        this.symbolTable = SymbolTable.getInstance(compilerContext);
        this.dlog = BLangDiagnosticLog.getInstance(compilerContext);
        this.compilerContext = compilerContext;
        CompilerOptions compilerOptions = CompilerOptions.getInstance(compilerContext);
        this.baloGen = getBooleanValueIfSet(compilerOptions, CompilerOptionName.BALO_GENERATION);
        this.skipModuleDependencies = getBooleanValueIfSet(compilerOptions,
                                                           CompilerOptionName.SKIP_MODULE_DEPENDENCIES);
    }

    public static InteropValidator getInstance(CompilerContext context) {

        InteropValidator interopValidator = context.get(INTEROP_VALIDATE);
        if (interopValidator == null) {
            interopValidator = new InteropValidator(context);
        }
        return interopValidator;
    }

    public BLangPackage validate(BLangPackage module) {
        dlog.setCurrentPackageId(module.symbol.pkgID);
        // find module dependencies path
        Set<Path> moduleDependencies = findDependencies(module.packageID);
        ClassLoader classLoader = makeClassLoader(moduleDependencies);
        BIRNode.BIRPackage birPackage = module.symbol.bir;
        // validate module functions with class names
        validateModuleFunctions(birPackage, classLoader);
        // validate type functions with class names
        validateTypeDefinitions(birPackage, classLoader);
        return module;
    }

    private Set<Path> findDependencies(PackageID packageID) {
        Set<Path> moduleDependencies = new HashSet<>();
        if (skipModuleDependencies) {
            return moduleDependencies;
        }
        if (baloGen) {
            moduleDependencies.addAll(readInteropDependencies());
        }
        JarResolver jarResolver = compilerContext.get(JAR_RESOLVER_KEY);
        if (jarResolver != null) {
            moduleDependencies.addAll(jarResolver.nativeDependencies(packageID));
        }
        return moduleDependencies;
    }

    private HashSet<Path> readInteropDependencies() {
        HashSet<Path> interopDependencies = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream("build/interopJars.txt"), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                interopDependencies.add(Paths.get(line));
            }
        } catch (IOException e) {
            throw new BLangCompilerException("error reading interop jar file names", e);
        }
        return interopDependencies;
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
        module.functions = jBirFunctions;
    }

    private void validateTypeDefinitions(BIRNode.BIRPackage module, ClassLoader classLoader) {

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
        Class clazz = JInterop.loadClass(className, classLoader);

        // 2) Load Java method details - use the method kind in the request - validate kind and the existence of the
        // method. Possible there may be more than one methods for the given kind and the name
        String fieldName = fieldValidationRequest.name;
        JavaField javaField;
        try {
            Field field = clazz.getField(fieldName);
            javaField = new JavaField(method, field);
        } catch (NoSuchFieldException e) {
            throw new JInteropException(DiagnosticCode.FIELD_NOT_FOUND, "No such field '" + fieldName +
                    "' found in class '" + className + "'");
        }

        return javaField;
    }

}
