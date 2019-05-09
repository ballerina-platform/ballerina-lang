/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.util;

import org.ballerinalang.BLangProgramRunner;
import org.ballerinalang.bre.bvm.BVMExecutor;
import org.ballerinalang.bre.old.WorkerExecutionContext;
import org.ballerinalang.jvm.Scheduler;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.XMLNodeType;
import org.ballerinalang.jvm.types.BTypedescType;
import org.ballerinalang.jvm.util.exceptions.BLangRuntimeException;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.FutureValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.TableValue;
import org.ballerinalang.jvm.values.TypedescValue;
import org.ballerinalang.jvm.values.XMLItem;
import org.ballerinalang.jvm.values.XMLSequence;
import org.ballerinalang.jvm.values.XMLValue;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BField;
import org.ballerinalang.model.types.BMapType;
import org.ballerinalang.model.types.BObjectType;
import org.ballerinalang.model.types.BRecordType;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.types.BTableType;
import org.ballerinalang.model.types.BTupleType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypeDesc;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BTable;
import org.ballerinalang.model.values.BTypeDescValue;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.model.values.BXMLSequence;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.debugger.Debugger;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility methods for run Ballerina functions.
 *
 * @since 0.94
 */
public class BRunUtil {

    /**
     * Invoke a ballerina function with state. Need to use compileAndSetup method in BCompileUtil to use this.
     *
     * @param compileResult CompileResult instance
     * @param functionName  Name of the function to invoke
     * @return return values of the function
     */
    public static BValue[] invokeStateful(CompileResult compileResult, String functionName) {
        BValue[] args = {};
        return invokeStateful(compileResult, functionName, args);
    }

    /**
     * Invoke a ballerina function with state. Need to use compileAndSetup method in BCompileUtil to use this.
     *
     * @param compileResult CompileResult instance
     * @param functionName  Name of the function to invoke
     * @param args          Input parameters for the function
     * @return return values of the function
     */
    public static BValue[] invokeStateful(CompileResult compileResult, String functionName, BValue[] args) {
        if (compileResult.getErrorCount() > 0) {
            throw new IllegalStateException(compileResult.toString());
        }
        return invokeStateful(compileResult, compileResult.getProgFile().getEntryPkgName(), functionName, args);
    }

    /**
     * Invoke a ballerina function with state. Need to use compileAndSetup method in BCompileUtil to use this.
     *
     * @param compileResult CompileResult instance
     * @param packageName   Name of the package to invoke
     * @param functionName  Name of the function to invoke
     * @return return values of the function
     */
    public static BValue[] invokeStateful(CompileResult compileResult, String packageName, String functionName) {
        BValue[] args = {};
        return invokeStateful(compileResult, packageName, functionName, args);
    }

    /**
     * Invoke a ballerina function with state. Need to use compileAndSetup method in BCompileUtil to use this.
     *
     * @param compileResult CompileResult instance
     * @param packageName   Name of the package to invoke
     * @param functionName  Name of the function to invoke
     * @param args          Input parameters for the function
     * @return return values of the function
     */
    public static BValue[] invokeStateful(CompileResult compileResult, String packageName,
                                          String functionName, BValue[] args) {
        if (compileResult.getErrorCount() > 0) {
            throw new IllegalStateException(compileResult.toString());
        }

        ProgramFile programFile = compileResult.getProgFile();
        PackageInfo packageInfo = programFile.getPackageInfo(packageName);
        FunctionInfo functionInfo = packageInfo.getFunctionInfo(functionName);
        if (functionInfo == null) {
            throw new RuntimeException("Function '" + functionName + "' is not defined");
        }

        int requiredArgNo = functionInfo.getParamTypes().length;
        int providedArgNo = args.length;
        if (requiredArgNo != providedArgNo) {
            throw new RuntimeException("Wrong number of arguments. Required: " + requiredArgNo + " , found: " +
                    providedArgNo + ".");
        }

        BValue[] response = BVMExecutor.executeFunction(programFile, functionInfo, args);

        return spreadToBValueArray(response);
    }

//    Package init helpers
    /**
     * Invoke package init function.
     *
     * @param compileResult CompileResult instance
     */
    public static void invokePackageInit(CompileResult compileResult) {
        if (compileResult.getErrorCount() > 0) {
            throw new IllegalStateException(compileResult.toString());
        }
        invokePackageInit(compileResult, compileResult.getProgFile().getEntryPkgName());
    }

    /**
     * Invoke package init function.
     *
     * @param compileResult CompileResult instance
     * @param packageName   Name of the package to invoke
     */
    protected static void invokePackageInit(CompileResult compileResult, String packageName) {
        if (compileResult.getErrorCount() > 0) {
            throw new IllegalStateException(compileResult.toString());
        }
        ProgramFile programFile = compileResult.getProgFile();
        WorkerExecutionContext context = new WorkerExecutionContext(programFile);
        Debugger debugger = new Debugger(programFile);
        programFile.setDebugger(debugger);
        compileResult.setContext(context);

        BVMExecutor.invokePackageInitFunctions(programFile);
    }

    /**
     * Invoke a ballerina function.
     *
     * @param compileResult CompileResult instance
     * @param packageName   Name of the package to invoke
     * @param functionName  Name of the function to invoke
     * @param args          Input parameters for the function
     * @return return values of the function
     */
    public static BValue[] invoke(CompileResult compileResult, String packageName, String functionName, BValue[] args) {
        if (compileResult.getErrorCount() > 0) {
            throw new IllegalStateException(compileResult.toString());
        }

        ProgramFile programFile = compileResult.getProgFile();
        PackageInfo packageInfo = programFile.getPackageInfo(packageName);
        FunctionInfo functionInfo = packageInfo.getFunctionInfo(functionName);
        if (functionInfo == null) {
            throw new RuntimeException("Function '" + functionName + "' is not defined");
        }

        BValue[] response = new BValue[]{BLangProgramRunner.runProgram(programFile, functionInfo, args)};

        return spreadToBValueArray(response);
    }

    /**
     * Invoke a ballerina function.
     *
     * @param compileResult CompileResult instance
     * @param packageName   Name of the package to invoke
     * @param functionName  Name of the function to invoke
     * @return return values of the function
     */
    public static BValue[] invoke(CompileResult compileResult, String packageName, String functionName) {
        BValue[] args = {};
        return invoke(compileResult, packageName, functionName, args);
    }

    /**
     * Invoke a ballerina function.
     *
     * @param compileResult CompileResult instance
     * @param functionName  Name of the function to invoke
     * @param args          Input parameters for the function
     * @return return values of the function
     */
    public static BValue[] invoke(CompileResult compileResult, String functionName, BValue[] args) {
        BValue[] response = invokeFunction(compileResult, functionName, args);
        return spreadToBValueArray(response);
    }

    /**
     * This method takes care of invocation on JBallerina and the mapping of input and output values. It will use the
     * given BVM based argument and function details to invoke on JBallerina and return results as BValues to maintain
     * backward compatibility with existing invoke methods in BRunUtil.
     *
     * @param compileResult CompileResult instance
     * @param functionName  Name of the function to invoke
     * @param args          Input parameters for the function
     * @return return values of the function
     */
    private static BValue[] invokeOnJBallerina(CompileResult compileResult, String functionName, BValue[] args) {
        BIRNode.BIRFunction function = getInvokedFunction(compileResult, functionName);
        return invoke(compileResult, function, functionName, args);
    }

    /**
     * This method handles the input arguments and output result mapping between BVM types, values to JVM types, values.
     *
     * @param compileResult CompileResult instance
     * @param function function model instance from BIR model
     * @param functionName name of the function to be invoked
     * @param bvmArgs input arguments to be used with function invocation
     * @return return the result from function invocation
     */
    private static BValue[] invoke(CompileResult compileResult, BIRNode.BIRFunction function, String functionName,
                                   BValue[] bvmArgs) {
        List<org.wso2.ballerinalang.compiler.semantics.model.types.BType> bvmParamTypes = function.type.paramTypes;
        Class<?>[] jvmParamTypes = new Class[bvmParamTypes.size() + 1];
        Object[] jvmArgs = new Object[bvmParamTypes.size() + 1];
        jvmParamTypes[0] = Strand.class;

        for (int i = 0; i < bvmParamTypes.size(); i++) {
            org.wso2.ballerinalang.compiler.semantics.model.types.BType type = bvmParamTypes.get(i);
            Class<?> typeClazz;
            Object argument = getJVMValue(type, bvmArgs[i]);
            switch (type.tag) {
                case TypeTags.INT_TAG:
                case TypeTags.BYTE_TAG:
                    typeClazz = long.class;
                    break;
                case TypeTags.BOOLEAN_TAG:
                    typeClazz = boolean.class;
                    break;
                case TypeTags.STRING_TAG:
                    typeClazz = String.class;
                    break;
                case TypeTags.FLOAT_TAG:
                    typeClazz = double.class;
                    break;
                case TypeTags.ARRAY_TAG:
                    typeClazz = ArrayValue.class;
                    break;
                default:
                    throw new RuntimeException("Function signature type '" + type + "' is not supported");
            }

            jvmParamTypes[i + 1] = typeClazz;
            jvmArgs[i + 1] = argument;
        }

        Object jvmResult;
        BIRNode.BIRPackage birPackage = ((BLangPackage) compileResult.getAST()).symbol.bir;
        String funcClassName = BFileUtil.getQualifiedClassName(birPackage.org.value, birPackage.name.value,
                                                               function.pos.src.cUnitName.replaceAll(".bal", ""));
        Class<?> funcClass = compileResult.getClassLoader().loadClass(funcClassName);
        try {
            Method method = funcClass.getDeclaredMethod(functionName, jvmParamTypes);
            Function<Object[], Object> func = a -> {
                try {
                    return method.invoke(null, a);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Error while invoking function '" + functionName + "'", e);
                } catch (InvocationTargetException e) {
                    Throwable t = e.getTargetException();
                    if (t instanceof BLangRuntimeException) {
                        throw (BLangRuntimeException) t;
                    }
                    throw new RuntimeException("Error while invoking function '" + functionName + "'", e);
                }
            };

            Scheduler scheduler = new Scheduler();
            FutureValue futureValue = scheduler.schedule(jvmArgs, func);
            scheduler.execute();
            jvmResult = futureValue.result;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Error while invoking function '" + functionName + "'", e);
        }

        BValue result = getBVMValue(jvmResult);
        return new BValue[] { result };
    }

    private static Object getJVMValue(org.wso2.ballerinalang.compiler.semantics.model.types.BType type, BValue value) {
        switch (type.tag) {
            case TypeTags.INT_TAG:
            case TypeTags.BYTE_TAG:
                return ((BInteger) value).intValue();
            case TypeTags.BOOLEAN_TAG:
                return ((BBoolean) value).booleanValue();
            case TypeTags.STRING_TAG:
                return value.stringValue();
            case TypeTags.FLOAT_TAG:
                return ((BFloat) value).floatValue();
            case TypeTags.ARRAY_TAG:
                org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType arrayType =
                        (org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType) type;
                BValueArray array = (BValueArray) value;
                ArrayValue jvmArray = new ArrayValue(getJVMType(arrayType));
                for (int i = 0; i < array.size(); i++) {
                    switch (arrayType.eType.tag) {
                        case TypeTags.INT_TAG:
                            jvmArray.add(i, array.getInt(i));
                            break;
                        case TypeTags.BYTE_TAG:
                            jvmArray.add(i, array.getByte(i));
                            break;
                        case TypeTags.BOOLEAN_TAG:
                            jvmArray.add(i, array.getBoolean(i));
                            break;
                        case TypeTags.STRING_TAG:
                            jvmArray.add(i, array.getString(i));
                            break;
                        case TypeTags.FLOAT_TAG:
                            jvmArray.add(i, array.getFloat(i));
                            break;
                        default:
                            throw new RuntimeException("Function signature type '" + type + "' is not supported");
                    }
                }
                return jvmArray;
            default:
                throw new RuntimeException("Function signature type '" + type + "' is not supported");
        }
    }

    private static org.ballerinalang.jvm.types.BType
            getJVMType(org.wso2.ballerinalang.compiler.semantics.model.types.BType type) {
        switch (type.tag) {
            case TypeTags.INT_TAG:
                return org.ballerinalang.jvm.types.BTypes.typeInt;
            case TypeTags.BYTE_TAG:
                return org.ballerinalang.jvm.types.BTypes.typeByte;
            case TypeTags.BOOLEAN_TAG:
                return org.ballerinalang.jvm.types.BTypes.typeBoolean;
            case TypeTags.STRING_TAG:
                return org.ballerinalang.jvm.types.BTypes.typeString;
            case TypeTags.FLOAT_TAG:
                return org.ballerinalang.jvm.types.BTypes.typeFloat;
            case TypeTags.ARRAY_TAG:
                org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType arrayType =
                        (org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType) type;
                org.ballerinalang.jvm.types.BType elementType = getJVMType(arrayType.getElementType());
                return new org.ballerinalang.jvm.types.BArrayType(elementType);
            default:
                throw new RuntimeException("Function argument for type '" + type + "' is not supported");
        }
    }

    private static BRefType<?> getBVMValue(Object value) {
        org.ballerinalang.jvm.types.BType type = TypeChecker.getType(value);
        switch (type.getTag()) {
            case org.ballerinalang.jvm.types.TypeTags.INT_TAG:
                return new BInteger((long) value);
            case org.ballerinalang.jvm.types.TypeTags.FLOAT_TAG:
                return new BFloat((double) value);
            case org.ballerinalang.jvm.types.TypeTags.BOOLEAN_TAG:
                return new BBoolean((boolean) value);
            case org.ballerinalang.jvm.types.TypeTags.STRING_TAG:
                return new BString((String) value);
            case org.ballerinalang.jvm.types.TypeTags.TUPLE_TAG:
                ArrayValue jvmTuple = ((ArrayValue) value);
                BRefType<?>[] tupleValues = new BRefType<?>[jvmTuple.size()];
                for (int i = 0; i < jvmTuple.size(); i++) {
                    tupleValues[i] = getBVMValue(jvmTuple.getRefValue(i));
                }
                return new BValueArray(tupleValues, getBVMType(jvmTuple.getType()));
            case org.ballerinalang.jvm.types.TypeTags.ARRAY_TAG:
                org.ballerinalang.jvm.types.BArrayType arrayType = (org.ballerinalang.jvm.types.BArrayType) type;
                ArrayValue array = (ArrayValue) value;
                BValueArray bvmArray = new BValueArray(getBVMType(arrayType.getElementType()));
                for (int i = 0; i < array.size(); i++) {
                    switch (arrayType.getElementType().getTag()) {
                        case TypeTags.INT_TAG:
                            bvmArray.add(i, array.getInt(i));
                            break;
                        case TypeTags.BYTE_TAG:
                            bvmArray.add(i, array.getByte(i));
                            break;
                        case TypeTags.BOOLEAN_TAG:
                            bvmArray.add(i, array.getBoolean(i) ? 1 : 0);
                            break;
                        case TypeTags.STRING_TAG:
                            bvmArray.add(i, array.getString(i));
                            break;
                        case TypeTags.FLOAT_TAG:
                            bvmArray.add(i, array.getFloat(i));
                            break;
                        default:
                            bvmArray.add(i, getBVMValue(array.getRefValue(i)));
                            break;
                    }
                }
                return bvmArray;
            case org.ballerinalang.jvm.types.TypeTags.RECORD_TYPE_TAG:
            case org.ballerinalang.jvm.types.TypeTags.JSON_TAG:
            case org.ballerinalang.jvm.types.TypeTags.MAP_TAG:
                MapValue jvmMap = (MapValue) value;
                BMap bmap = new BMap(getBVMType(jvmMap.getType()));
                for (Object key : jvmMap.keySet()) {
                    bmap.put(key, getBVMValue(jvmMap.get(key)));
                }
                return bmap;

            case org.ballerinalang.jvm.types.TypeTags.TABLE_TAG:
                TableValue jvmTable = (TableValue) value;
                org.ballerinalang.jvm.types.BTableType jvmTableType =
                        (org.ballerinalang.jvm.types.BTableType) type;
                BStructureType constraintType = (BStructureType) getBVMType(jvmTableType.getConstrainedType());
                BValueArray data = new BValueArray(BTypes.typeMap);

                while (jvmTable.hasNext()) {
                    BMap dataRow = new BMap(constraintType);
                    dataRow.getMap().putAll(((BMap<String, BValue>) getBVMValue(jvmTable.getNext())).getMap());
                    data.append(dataRow);
                }

                jvmTable.close();
                jvmTable.finalize();
                return new BTable(new BTableType(constraintType), null, null, data);

            case org.ballerinalang.jvm.types.TypeTags.ERROR_TAG:
                ErrorValue errorValue = (ErrorValue) value;
                BRefType<?> details = getBVMValue(errorValue.getDetails());
                return new BError(getBVMType(errorValue.getType()), errorValue.getReason(), details);
            case org.ballerinalang.jvm.types.TypeTags.NULL_TAG:
                return null;
            case org.ballerinalang.jvm.types.TypeTags.OBJECT_TYPE_TAG:
                ObjectValue jvmObject = (ObjectValue) value;
                org.ballerinalang.jvm.types.BObjectType jvmObjectType = jvmObject.getType();
                BMap<String, BRefType<?>> bvmObject = new BMap<>(getBVMType(jvmObjectType));

                for (String key : jvmObjectType.getFields().keySet()) {
                    bvmObject.put(key, getBVMValue(jvmObject.get(key)));
                }
                return bvmObject;
            case org.ballerinalang.jvm.types.TypeTags.XML_TAG:
                XMLValue<?> xml = (XMLValue<?>) value;
                if (xml.getNodeType() != XMLNodeType.SEQUENCE) {
                    return new BXMLItem(((XMLItem) xml).value());
                }
                ArrayValue elements = ((XMLSequence) xml).value();
                return new BXMLSequence((BValueArray) getBVMValue(elements));
            case org.ballerinalang.jvm.types.TypeTags.TYPEDESC_TAG:
                TypedescValue typedescValue = (TypedescValue) value;
                return new BTypeDescValue(getBVMType(typedescValue.getDescribingType()));
            default:
                throw new RuntimeException("Function invocation result for type '" + type + "' is not supported");
        }
    }

    private static BType getBVMType(org.ballerinalang.jvm.types.BType jvmType) {
        switch (jvmType.getTag()) {
            case org.ballerinalang.jvm.types.TypeTags.INT_TAG:
                return BTypes.typeInt;
            case org.ballerinalang.jvm.types.TypeTags.FLOAT_TAG:
                return BTypes.typeFloat;
            case org.ballerinalang.jvm.types.TypeTags.STRING_TAG:
                return BTypes.typeString;
            case org.ballerinalang.jvm.types.TypeTags.BOOLEAN_TAG:
                return BTypes.typeBoolean;
            case org.ballerinalang.jvm.types.TypeTags.BYTE_TAG:
                return BTypes.typeByte;
            case org.ballerinalang.jvm.types.TypeTags.TUPLE_TAG:
                org.ballerinalang.jvm.types.BTupleType tupleType = (org.ballerinalang.jvm.types.BTupleType) jvmType;
                List<BType> memberTypes = new ArrayList<>();
                for (org.ballerinalang.jvm.types.BType type : tupleType.getTupleTypes()) {
                    memberTypes.add(getBVMType(type));
                }
                return new BTupleType(memberTypes);
            case org.ballerinalang.jvm.types.TypeTags.ARRAY_TAG:
                org.ballerinalang.jvm.types.BArrayType arrayType = (org.ballerinalang.jvm.types.BArrayType) jvmType;
                return new BArrayType(getBVMType(arrayType.getElementType()));
            case org.ballerinalang.jvm.types.TypeTags.ANY_TAG:
                return BTypes.typeAny;
            case org.ballerinalang.jvm.types.TypeTags.ANYDATA_TAG:
                return BTypes.typeAnydata;
            case org.ballerinalang.jvm.types.TypeTags.ERROR_TAG:
                return BTypes.typeError;
            case org.ballerinalang.jvm.types.TypeTags.RECORD_TYPE_TAG:
                org.ballerinalang.jvm.types.BRecordType recordType = (org.ballerinalang.jvm.types.BRecordType) jvmType;
                BRecordType bvmRecordType =
                        new BRecordType(null, recordType.getName(), recordType.getPackagePath(), recordType.flags);
                Map<String, BField> recordFields =
                        recordType.getFields().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> new BField(getBVMType(entry.getValue().type), entry.getValue().getFieldName(),
                        entry.getValue().flags), (a, b) -> b, LinkedHashMap::new));
                bvmRecordType.setFields(recordFields);
                return bvmRecordType;
            case org.ballerinalang.jvm.types.TypeTags.JSON_TAG:
                return BTypes.typeJSON;
            case org.ballerinalang.jvm.types.TypeTags.MAP_TAG:
                org.ballerinalang.jvm.types.BMapType mapType = (org.ballerinalang.jvm.types.BMapType) jvmType;
                return new BMapType(getBVMType(mapType.getConstrainedType()));
            case org.ballerinalang.jvm.types.TypeTags.TABLE_TAG:
                org.ballerinalang.jvm.types.BTableType tableType = (org.ballerinalang.jvm.types.BTableType) jvmType;
                return new BTableType(getBVMType(tableType.getConstrainedType()));
            case org.ballerinalang.jvm.types.TypeTags.UNION_TAG:
                return BTypes.typePureType;
            case org.ballerinalang.jvm.types.TypeTags.OBJECT_TYPE_TAG:
                org.ballerinalang.jvm.types.BObjectType objectType = (org.ballerinalang.jvm.types.BObjectType) jvmType;
                BObjectType bvmObjectType =
                        new BObjectType(null, objectType.getName(), objectType.getPackagePath(), objectType.flags);
                Map<String, BField> objectFields = new HashMap<>();
                for (org.ballerinalang.jvm.types.BField field : objectType.getFields().values()) {
                    objectFields.put(field.name, new BField(getBVMType(field.type), field.name, field.flags));
                }
                bvmObjectType.setFields(objectFields);
                return bvmObjectType;
            case org.ballerinalang.jvm.types.TypeTags.XML_TAG:
                return BTypes.typeXML;
            case org.ballerinalang.jvm.types.TypeTags.TYPEDESC_TAG:
                BTypedescType typedescType = (BTypedescType) jvmType;
                return new BTypeDesc(typedescType.getName(), typedescType.getPackagePath());
            case org.ballerinalang.jvm.types.TypeTags.NULL_TAG:
                return BTypes.typeNull;
            default:
                throw new RuntimeException("Unsupported jvm type: '" + jvmType + "' ");
        }
    }

    private static BIRNode.BIRFunction getInvokedFunction(CompileResult compileResult, String functionName) {

        if (compileResult.getErrorCount() > 0) {
            throw new IllegalStateException(compileResult.toString());
        }

        BIRNode.BIRPackage birPackage = ((BLangPackage) compileResult.getAST()).symbol.bir;
        return birPackage.functions.stream()
                .filter(function -> functionName.equals(function.name.value))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Function '" + functionName + "' is not defined"));
    }

    /**
     * Invoke a ballerina function to get BReference Value Objects.
     *
     * @param compileResult CompileResult instance
     * @param functionName  Name of the function to invoke
     * @param args          Input parameters for the function
     * @return return values of the function
     */
    public static BValue[] invokeFunction(CompileResult compileResult, String functionName, BValue[] args) {
        if (BCompileUtil.jBallerinaTestsEnabled()) {
            return invokeOnJBallerina(compileResult, functionName, args);
        }

        if (compileResult.getErrorCount() > 0) {
            throw new IllegalStateException(compileResult.toString());
        }

        ProgramFile programFile = compileResult.getProgFile();
        PackageInfo packageInfo = programFile.getPackageInfo(programFile.getEntryPkgName());
        FunctionInfo functionInfo = packageInfo.getFunctionInfo(functionName);
        if (functionInfo == null) {
            throw new RuntimeException("Function '" + functionName + "' is not defined");
        }

        return new BValue[]{BLangProgramRunner.runProgram(programFile, functionInfo, args)};
    }

    /**
     * Invoke a ballerina function to get BReference Value Objects.
     *
     * @param compileResult CompileResult instance
     * @param functionName Name of the function to invoke
     * @return return values of the function
     */
    public static BValue[] invokeFunction(CompileResult compileResult, String functionName) {
        return invokeFunction(compileResult, functionName, new BValue[] {});
    }

    private static BValue[] spreadToBValueArray(BValue[] response) {
        if (!(response != null && response.length > 0 && response[0] instanceof BValueArray)) {
            return response;
        }

        BValueArray refValueArray = (BValueArray) response[0];
        BType elementType = refValueArray.elementType;
        if (elementType == BTypes.typeString || elementType == BTypes.typeInt || elementType == BTypes.typeFloat
                || elementType == BTypes.typeBoolean || elementType == BTypes.typeByte) {
            return response;
        }

        int length = (int) refValueArray.size();
        BValue[] arr = new BValue[length];
        for (int i = 0; i < length; i++) {
            arr[i] = refValueArray.getRefValue(i);
        }
        return arr;
    }


    /**
     * Invoke a ballerina function.
     *
     * @param compileResult CompileResult instance
     * @param functionName  Name of the function to invoke
     * @return return values of the function
     */
    public static BValue[] invoke(CompileResult compileResult, String functionName) {
        BValue[] args = {};
        return invoke(compileResult, functionName, args);
    }

    /**
     * Invoke a ballerina function given context.
     *
     * @param compileResult CompileResult instance.
     * @param initFuncInfo Function to invoke.
     * @param context invocation context.
     */
    public static void invoke(CompileResult compileResult, FunctionInfo initFuncInfo,
            WorkerExecutionContext context) {
        Debugger debugger = new Debugger(compileResult.getProgFile());
        compileResult.getProgFile().setDebugger(debugger);

        BVMExecutor.executeFunction(compileResult.getProgFile(), initFuncInfo);
    }
}
