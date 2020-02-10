/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.wso2.ballerinalang.compiler.bir.codegen;

import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JVM byte code generator from BIR model.
 *
 * @since 1.2.0
 */
public class CodeGenerator {
    private static final CompilerContext.Key<CodeGenerator> CODE_GEN = new CompilerContext.Key<>();
    private final String JAVA_PACKAGE_SEPERATOR = "/";
    private final String FILE_NAME_PERIOD_SEPERATOR = "$$$";
    private final String WINDOWS_PATH_SEPERATOR = "\\";
    private final String BAL_EXTENSION = ".bal";
    private final String I_STRING_VALUE = "org/ballerinalang/jvm/values/StringValue";
    private final String BMP_STRING_VALUE = "org/ballerinalang/jvm/values/BmpStringValue";
    private final String NON_BMP_STRING_VALUE = "org/ballerinalang/jvm/values/NonBmpStringValue";

    private CodeGenerator(CompilerContext context) {

        context.put(CODE_GEN, this);
    }

    public static CodeGenerator getInstance(CompilerContext context) {

        CodeGenerator codeGenerator = context.get(CODE_GEN);
        if (codeGenerator == null) {
            codeGenerator = new CodeGenerator(context);
        }

        return codeGenerator;
    }

    private String getModuleLevelClassName(String orgName, String moduleName, String sourceFileName) {
        String className = cleanupSourceFileName(sourceFileName);
        // handle source file path start with '/'.
        if (className.startsWith(JAVA_PACKAGE_SEPERATOR)) {
            className = className.substring(1);
        }
        if (!moduleName.equals(".")) {
            className = cleanupName(moduleName) + "/" + className;
        }

        if (!orgName.equalsIgnoreCase("$anon")) {
            className = cleanupName(orgName) + "/" + className;
        }

        return className;
    }

    private String cleanupSourceFileName(String name) {
        return name.replace(".", FILE_NAME_PERIOD_SEPERATOR);
    }

    private String cleanupName(String name) {
        return name.replace(".", "_");
    }

    private String cleanupPathSeperators(String name) {
        //TODO: should use file_path:getPathSeparator();
        return name.replace(WINDOWS_PATH_SEPERATOR, JAVA_PACKAGE_SEPERATOR);
    }

    private boolean isExternFunc(BIRNode.BIRFunction func) {
        return (func.flags & Flags.NATIVE) == Flags.NATIVE:
    }

    private String cleanupBalExt(String name) {
        return name.replace(BAL_EXTENSION, "");
    }

    private boolean isBallerinaBuiltinModule(String orgName, String moduleName) {
        return orgName.equals("ballerina") && moduleName.equals("builtin");
    }

    public void generate(BIRNode.BIRPackage module) {

    }

    public Map<String, JavaClass> generateClassNameMappings(BIRNode.BIRPackage module, String pkgName, String initClass,
                                                            Map<String, BIRLambdaCall> lambdaCalls,
                                                            InteropValidator interopValidator) {
        String orgName = module.org.value;
        String moduleName = module.name.value;
        String versionValue = module.version.value;

        Map<String, JavaClass> jvmClassMap = new HashMap<>();
//
//        foreach var globalVar in module.globalVars {
//            if (globalVar is bir:GlobalVariableDcl) {
//                globalVarClassNames[pkgName + globalVar.name.value] = initClass;
//            }
//        }
//        // filter out functions.
//        bir:Function?[] functions = module.functions;
//        if (functions.length() > 0) {
//            int funcSize = functions.length();
//            int count  = 0;
//            // Generate init klass. Init function should be the first function of the package, hence check first
//            // function.
//            bir:Function initFunc = <bir:Function>functions[0];
//            string functionName = initFunc.name.value;
        JavaClass klass;// = { sourceFileName:initFunc.pos.sourceFileName, moduleClass:initClass };
//            klass.functions[0] = initFunc;
//            addInitAndTypeInitInstructions(module, initFunc);
//            jvmClassMap[initClass] = klass;
//            birFunctionMap[pkgName + functionName] = getFunctionWrapper(initFunc, orgName, moduleName,
//                    versionValue, initClass);
//            count += 1;
//
//            // Add start function
//            bir:Function startFunc = <bir:Function>functions[1];
//            functionName = startFunc.name.value;
//            birFunctionMap[pkgName + functionName] = getFunctionWrapper(startFunc, orgName, moduleName,
//                    versionValue, initClass);
//            klass.functions[1] = startFunc;
//            count += 1;
//
//            // Add stop function
//            bir:Function stopFunc = <bir:Function>functions[2];
//            functionName = stopFunc.name.value;
//            birFunctionMap[pkgName + functionName] = getFunctionWrapper(stopFunc, orgName, moduleName,
//                    versionValue, initClass);
//            klass.functions[2] = stopFunc;
//            count += 1;
//


        List<BIRNode.BIRFunction> functions = module.functions;
        int numFunctions = functions.size();
        for (int i = 0; i < numFunctions; i++) {
            BIRNode.BIRFunction birFunc = functions.get(i);
            String birFuncName = birFunc.name.value;
            String balFileName = birFunc.pos.getSource().cUnitName;
            String birModuleClassName = getModuleLevelClassName(orgName, moduleName,
                    cleanupPathSeperators(cleanupBalExt(balFileName)));

            if (!isBallerinaBuiltinModule(orgName, moduleName)) {
                JavaClass javaClass = jvmClassMap.get(birModuleClassName);
                if (javaClass != null) {
                    javaClass.functions.add(birFunc);
                } else {
                    klass = new JavaClass(balFileName, birModuleClassName);
                    klass.functions.add(birFunc);
                    jvmClassMap.put(birModuleClassName, klass);
                }
            }

            BIRFunctionWrapper birFuncWrapperOrError;
            if (isExternFunc(getFunction(birFunc))) {
                throw new UnsupportedOperationException("natives are not migrated yet");
//                birFuncWrapperOrError = createExternalFunctionWrapper(interopValidator, birFunc, orgName, moduleName,
//                        versionValue, birModuleClassName);
            } else {
                //TODO: move to desugar
//                addDefaultableBooleanVarsToSignature(birFunc);
                birFuncWrapperOrError = getFunctionWrapper(birFunc, orgName, moduleName, versionValue, birModuleClassName);
            }

            if (birFuncWrapperOrError is error){
                dlogger.logError( < @untainted>birFuncWrapperOrError, <@untainted>birFunc.pos, <@untainted>module)
                continue;
            } else{
                birFunctionMap.put(pkgName + birFuncName, birFuncWrapperOrError);
            }
        }


//        // link typedef - object attached native functions
//        bir:TypeDef?[] typeDefs = module.typeDefs;
//
//        foreach var optionalTypeDef in typeDefs {
//            bir:TypeDef typeDef = getTypeDef(optionalTypeDef);
//            bir:BType bType = typeDef.typeValue;
//
//            if (bType is bir:BObjectType || bType is bir:BRecordType) {
//                string key = getModuleLevelClassName(orgName, moduleName, typeDef.name.value);
//                typeDefMap[key] = typeDef;
//            }
//
//            if ((bType is bir:BObjectType && !bType.isAbstract) || bType is bir:BServiceType) {
//                bir:Function?[] attachedFuncs = getFunctions(typeDef.attachedFuncs);
//                string typeName = "";
//                if (bType is bir:BObjectType) {
//                    typeName = bType.name.value;
//                } else {
//                    typeName = bType.oType.name.value;
//                }
//                foreach var func in attachedFuncs {
//
//                    // link the bir function for lookup
//                    bir:Function currentFunc = getFunction(func);
//                    string functionName = currentFunc.name.value;
//                    string lookupKey = typeName + "." + functionName;
//
//                    if (!isExternFunc(currentFunc)) {
//                        string className = getTypeValueClassName(module, typeName);
//                        birFunctionMap[pkgName + lookupKey] = getFunctionWrapper(currentFunc, orgName, moduleName,
//                                versionValue, className);
//                        continue;
//                    }
//
//                    var jClassName = lookupExternClassName(cleanupPackageName(pkgName), lookupKey);
//                    if (jClassName is string) {
//                        birFunctionMap[pkgName + lookupKey] = createOldStyleExternalFunctionWrapper(currentFunc, orgName,
//                                moduleName, versionValue, jClassName, jClassName);
//                    } else {
//                        error err = error("native function not available: " + pkgName + lookupKey);
//                        panic err;
//                    }
//                }
//            }
//        }
//        return jvmClassMap;

    }

    private BIRFunctionWrapper getFunctionWrapper(BIRNode.BIRFunction currentFunc, String orgName, String moduleName, String versionValue, String moduleClass) {

        BInvokableType functionTypeDesc = currentFunc.type;
        BIRNode.BIRVariableDcl receiver = currentFunc.receiver;
        BType attachedType = receiver != null ? receiver.type : null;
        String jvmMethodDescription = getMethodDesc(functionTypeDesc.paramTypes, functionTypeDesc ?.retType,
                attachedType = attachedType)
        String jvmMethodDescriptionBString = getMethodDesc(functionTypeDesc.paramTypes, functionTypeDesc ?.retType,
                attachedType = attachedType, useBString = true)
        return {
                orgName :orgName,
                moduleName :moduleName,
                versionValue :versionValue,
                func :currentFunc,
                fullQualifiedClassName :moduleClass,
                jvmMethodDescriptionBString :jvmMethodDescriptionBString,
                jvmMethodDescription :jvmMethodDescription
        }
    }

    private BIRFunctionWrapper createExternalFunctionWrapper(InteropValidator interopValidator,
                                                             BIRNode.BIRFunction birFunc, String orgName,
                                                             String moduleName, String versionValue,
                                                             String birModuleClassName) {
        BIRFunctionWrapper birFuncWrapper;
//        jvm:InteropValidationRequest? jInteropValidationReq = getInteropAnnotValue(birFunc);
//        if (jInteropValidationReq is ()) {
//            // This is a old-style external Java interop function
//            String pkgName = getPackageName(orgName, moduleName);
//            var jClassName = lookupExternClassName(cleanupPackageName(pkgName), birFunc.name.value);
//            if (jClassName is String) {
//                if isBallerinaBuiltinModule(orgName, moduleName) {
//                    birFuncWrapper = getFunctionWrapper(birFunc, orgName, moduleName, versionValue, jClassName);
//                } else {
//                    birFuncWrapper = createOldStyleExternalFunctionWrapper(birFunc, orgName, moduleName, versionValue,
//                            birModuleClassName, jClassName);
//                }
//            } else {
//                error err = error("cannot find full qualified class name for extern function : " + pkgName +
//                        birFunc.name.value);
//                panic err;
//            }
//        } else {
//        birFuncWrapper = createJInteropFunctionWrapper(interopValidator, jInteropValidationReq, birFunc, orgName,
//                moduleName, versionValue, birModuleClassName);
//        }
        return null; // birFuncWrapper;
    }

    private BIRNode.BIRFunction getFunction(BIRNode.BIRFunction bfunction) {
        if (bfunction != null) {
            return bfunction;
        } else {
            throw nullPointerException("Invalid function");
        }
    }

    private String getMethodDesc(List<BType> paramTypes, BType retType, BType attachedType, boolean isExtern, boolean useBString) {
        String desc = "(Lorg/ballerinalang/jvm/scheduling/Strand;";

        if (attachedType != null) {
            throw new UnsupportedOperationException("attached type not migrated yet");
//            desc = desc + getArgTypeSignature(attachedType, useBString);
        }

        for (BType paramType : paramTypes) {
            desc = desc + getArgTypeSignature(paramType, useBString);
        }
        String returnType = generateReturnType(retType, isExtern, useBString);
        desc = desc + returnType;

        return desc;
    }

    private String getArgTypeSignature(BType bType, boolean useBString) {
        if (bType.tag == TypeTags.INT) {
            return "J";
        } else if (bType.tag == TypeTags.BYTE) {
            return "I";
        } else if (bType.tag == TypeTags.FLOAT) {
            return "D";
        } else if (bType.tag == TypeTags.STRING) {
            return String.format("L%s;", useBString ? I_STRING_VALUE : STRING_VALUE);
        } else if (bType.tag == TypeTags.DECIMAL) {
            return String.format("L%s;", DECIMAL_VALUE);
        } else if (bType.tag == TypeTags.BOOLEAN) {
            return "Z";
        } else if (bType.tag == TypeTags.NIL) {
            return String.format("L%s;", OBJECT);
        } else if (bType.tag == TypeTags.ARRAY || bType.tag == TypeTags.TUPLE) {
            return String.format("L%s;", ARRAY_VALUE);
        } else if (bType.tag == TypeTags.RTYPE) {
            return String.format("L%s;", ERROR_VALUE);
        } else if (bType.tag == TypeTags.ANYDATA ||
                bType is bir:BUnionType ||
                bType is bir:BJSONType ||
                bType is bir:BFiniteType ||
                bType is bir:BTypeAny){
            return String.format("L%s;", OBJECT);
        } else if (bType is bir:BMapType || bType is bir:BRecordType){
            return String.format("L%s;", MAP_VALUE);
        } else if (bType.tag is bir:BFutureType){
            return String.format("L%s;", FUTURE_VALUE);
        } else if (bType is bir:BTableType){
            return String.format("L%s;", TABLE_VALUE);
        } else if (bType is bir:BInvokableType){
            return String.format("L%s;", FUNCTION_POINTER);
        } else if (bType is bir:BTypeDesc){
            return String.format("L%s;", TYPEDESC_VALUE);
        } else if (bType is bir:BObjectType || bType is bir:BServiceType){
            return String.format("L%s;", OBJECT_VALUE);
        } else if (bType is bir:BXMLType){
            return String.format("L%s;", XML_VALUE);
        } else if (bType is bir:BTypeHandle){
            return String.format("L%s;", HANDLE_VALUE);
        } else{
            error err = error("JVM generation is not supported for type " + String.format("%s", bType));
            panic err;
        }
    }

//    private class JavaClass {
//        private final String sourceFileName;
//        private final String moduleClass;
//        public List<BIRNode.BIRFunction> functions = new ArrayList<>();
//
//        public JavaClass(String sourceFileName, String moduleClass) {
//            this.sourceFileName = sourceFileName;
//            this.moduleClass = moduleClass;
//        }
//    }
//
//    private class BIRFunctionWrapper {
//    }
//
//    private class BIRLambdaCall {
//    }
//
//    private class InteropValidator {
//    }

}
