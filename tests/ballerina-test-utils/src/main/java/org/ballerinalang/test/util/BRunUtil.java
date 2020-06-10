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

import org.apache.axiom.om.OMNode;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.DecimalValueKind;
import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.XMLFactory;
import org.ballerinalang.jvm.XMLNodeType;
import org.ballerinalang.jvm.commons.ArrayState;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BIntersectionType;
import org.ballerinalang.jvm.types.BPackage;
import org.ballerinalang.jvm.types.BTypedescType;
import org.ballerinalang.jvm.types.TypeFlags;
import org.ballerinalang.jvm.util.exceptions.BLangRuntimeException;
import org.ballerinalang.jvm.values.AbstractObjectValue;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.BmpStringValue;
import org.ballerinalang.jvm.values.DecimalValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.FPValue;
import org.ballerinalang.jvm.values.FutureValue;
import org.ballerinalang.jvm.values.HandleValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.NonBmpStringValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.StreamValue;
import org.ballerinalang.jvm.values.TypedescValue;
import org.ballerinalang.jvm.values.XMLSequence;
import org.ballerinalang.jvm.values.XMLValue;
import org.ballerinalang.jvm.values.api.BXML;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BErrorType;
import org.ballerinalang.model.types.BField;
import org.ballerinalang.model.types.BFiniteType;
import org.ballerinalang.model.types.BFunctionType;
import org.ballerinalang.model.types.BMapType;
import org.ballerinalang.model.types.BObjectType;
import org.ballerinalang.model.types.BRecordType;
import org.ballerinalang.model.types.BServiceType;
import org.ballerinalang.model.types.BStreamType;
import org.ballerinalang.model.types.BTupleType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypeDesc;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.BUnionType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BDecimal;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BFunctionPointer;
import org.ballerinalang.model.values.BHandleValue;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BStream;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BTypeDescValue;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.model.values.BValueType;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.model.values.BXMLSequence;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.util.BArrayState;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility methods for run Ballerina functions.
 *
 * @since 0.94
 */
public class BRunUtil {

    public static final String IS_STRING_VALUE_PROP = "ballerina.bstring";

    /**
     * Invoke a ballerina function.
     *
     * @param compileResult CompileResult instance
     * @param functionName  Name of the function to invoke
     * @param args          Input parameters for the function
     * @return return values of the function
     */
    public static BValue[] invoke(CompileResult compileResult, String functionName, Object[] args) {
        return invokeOnJBallerina(compileResult, functionName, args, getJvmParamTypes(args));
    }

    private static Class<?>[] getJvmParamTypes(Object[] args) {
        Class<?>[] paramTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];

            if (arg instanceof ObjectValue) {
                paramTypes[i] = ObjectValue.class;
            } else if (arg instanceof XMLValue) {
                paramTypes[i] = XMLValue.class;
            } else if (arg instanceof BmpStringValue) {
                paramTypes[i] = BmpStringValue.class;
            } else if (arg instanceof NonBmpStringValue) {
                paramTypes[i] = NonBmpStringValue.class;
            } else if (arg instanceof ArrayValue) {
                paramTypes[i] = ArrayValue.class;
            } else if (arg instanceof Integer) {
                paramTypes[i] = Long.class;
            } else if (arg instanceof Float) {
                paramTypes[i] = Double.class;
            } else if (arg instanceof Double) {
                paramTypes[i] = Double.class;
            } else if (arg instanceof Long) {
                paramTypes[i] = Long.class;
            } else if (arg instanceof Boolean) {
                paramTypes[i] = Boolean.class;
            } else if (arg instanceof MapValue) {
                paramTypes[i] = MapValue.class;
            } else if (arg instanceof ErrorValue) {
                paramTypes[i] = ErrorValue.class;
            } else {
                // This is done temporarily, until blocks are added here for all possible cases.
                throw new RuntimeException("unknown param type: " + arg.getClass());
            }
        }
        return paramTypes;
    }

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
     * @param paramTypes    Types of the parameters of the function
     * @return return values of the function
     */
    private static BValue[] invokeOnJBallerina(CompileResult compileResult, String functionName, Object[] args,
                                               Class<?>[] paramTypes) {
        BIRNode.BIRFunction function = getInvokedFunction(compileResult, functionName);
        args = addDefaultableBoolean(args);
        paramTypes = addDefaultableBooleanType(paramTypes);
        Object jvmResult = invoke(compileResult, function, functionName, args, paramTypes);
        BValue result = getBVMValue(jvmResult);
        return new BValue[] { result };
    }

    private static Object[] addDefaultableBoolean(Object[] args) {
        Object[] result = new Object[args.length * 2];
        for (int j = 0, i = 0; i < args.length; i++) {
            result[j] = args[i];
            result[j + 1] = true;
            j += 2;
        }
        return result;
    }

    private static Class<?>[] addDefaultableBooleanType(Class<?>[] paramTypes) {
        Class<?>[] result = new Class<?>[paramTypes.length * 2];
        for (int j = 0, i = 0; i < paramTypes.length; i++) {
            result[j] = paramTypes[i];
            result[j + 1] = boolean.class;
            j += 2;
        }
        return result;
    }

    private static BValue[] addDefaultableBoolean(BValue[] args) {
        BValue[] result = new BValue[args.length * 2];
        for (int j = 0, i = 0; i < args.length; i++) {
            result[j] = args[i];
            result[j + 1] = new BBoolean(true);
            j += 2;
        }
        return result;
    }

    /**
     * This method handles the input arguments.
     *
     * @param compileResult CompileResult instance
     * @param function function model instance from BIR model
     * @param functionName name of the function to be invoked
     * @param args input arguments to be used with function invocation
     * @param paramTypes types of the parameters of the function
     * @return return the result from function invocation
     */
    private static Object invoke(CompileResult compileResult, BIRNode.BIRFunction function, String functionName,
                                   Object[] args, Class<?>[] paramTypes) {
        assert args.length == paramTypes.length;
        Class<?>[] jvmParamTypes = new Class[paramTypes.length + 1];
        jvmParamTypes[0] = Strand.class;
        Object[] jvmArgs = new Object[args.length + 1];

        for (int i = 0; i < args.length; i++) {
            jvmArgs[i + 1] = args[i];
            jvmParamTypes[i + 1] = paramTypes[i];
        }

        Object jvmResult;
        BIRNode.BIRPackage birPackage = ((BLangPackage) compileResult.getAST()).symbol.bir;
        String funcClassName = BFileUtil.getQualifiedClassName(birPackage.org.value, birPackage.name.value,
                birPackage.version.value, getClassName(function.pos.src.cUnitName));
        try {
            Class<?> funcClass = compileResult.getClassLoader().loadClass(funcClassName);
            Method method = getMethod(functionName, funcClass);
            Function<Object[], Object> func = a -> {
                try {
                    return method.invoke(null, a);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Error while invoking function '" + functionName + "'", e);
                } catch (InvocationTargetException e) {
                    Throwable t = e.getTargetException();
                    if (t instanceof BLangRuntimeException) {
                        throw new org.ballerinalang.util.exceptions.BLangRuntimeException(t.getMessage());
                    }
                    if (t instanceof ErrorValue) {
                        throw new org.ballerinalang.util.exceptions
                                .BLangRuntimeException("error: " + ((ErrorValue) t).getPrintableStackTrace());
                    }
                    if (t instanceof StackOverflowError) {
                        throw new org.ballerinalang.util.exceptions.BLangRuntimeException("error: " +
                                "{ballerina}StackOverflow {\"message\":\"stack overflow\"}");
                    }
                    throw new RuntimeException("Error while invoking function '" + functionName + "'", e);
                }
            };

            Scheduler scheduler = new Scheduler(false);
            FutureValue futureValue = scheduler.schedule(jvmArgs, func, null, null, new HashMap<>(),
                    org.ballerinalang.jvm.types.BTypes.typeAny);
            scheduler.start();
            if (futureValue.panic instanceof RuntimeException) {
                throw new org.ballerinalang.util.exceptions.BLangRuntimeException(futureValue.panic.getMessage(),
                        futureValue.panic);
            }
            jvmResult = futureValue.result;
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException("Error while invoking function '" + functionName + "'", e);
        }

        return jvmResult;
    }

    private static Method getMethod(String functionName, Class<?> funcClass) throws NoSuchMethodException {
        Method declaredMethod = Arrays.stream(funcClass.getDeclaredMethods())
                .filter(method -> functionName.equals(method.getName()))
                .findAny()
                .orElse(null);

        if (declaredMethod != null) {
            return declaredMethod;
        } else {
            throw new NoSuchMethodException(functionName + " is not found");
        }
    }

    /**
     * This method handles the input arguments and output result mapping between BVM types, values to JVM types,
     * values.
     *
     * @param compileResult CompileResult instance
     * @param function      function model instance from BIR model
     * @param functionName  name of the function to be invoked
     * @param bvmArgs       input arguments to be used with function invocation
     * @return return the result from function invocation
     */
    private static BValue[] invoke(CompileResult compileResult, BIRNode.BIRFunction function, String functionName,
                                   BValue[] bvmArgs) {
        List<org.wso2.ballerinalang.compiler.semantics.model.types.BType> bvmParamTypes = function.type.paramTypes;
        Class<?>[] jvmParamTypes = new Class[bvmParamTypes.size() + 1];
        Object[] jvmArgs = new Object[bvmArgs.length + 1];
        jvmParamTypes[0] = Strand.class;

        for (int i = 0; i < bvmParamTypes.size(); i++) {
            org.wso2.ballerinalang.compiler.semantics.model.types.BType type = bvmParamTypes.get(i);
            Class<?> typeClazz;
            Object argument = getJVMValue(type, bvmArgs[i]);
            switch (type.tag) {
                case TypeTags.INT_TAG:
                    typeClazz = long.class;
                    break;
                case TypeTags.BYTE_TAG:
                    typeClazz = int.class;
                    break;
                case TypeTags.BOOLEAN_TAG:
                    typeClazz = boolean.class;
                    break;
                case TypeTags.STRING_TAG:
                    typeClazz = org.ballerinalang.jvm.values.api.BString.class;
                    break;
                case TypeTags.DECIMAL_TAG:
                    typeClazz = DecimalValue.class;
                    break;
                case TypeTags.FLOAT_TAG:
                    typeClazz = double.class;
                    break;
                case TypeTags.ARRAY_TAG:
                    typeClazz = ArrayValue.class;
                    break;
                case TypeTags.UNION_TAG:
                case TypeTags.ANY_TAG:
                case TypeTags.FINITE_TYPE_TAG:
                case TypeTags.JSON_TAG:
                    typeClazz = Object.class;
                    break;
                case TypeTags.RECORD_TYPE_TAG:
                case TypeTags.MAP_TAG:
                    typeClazz = MapValue.class;
                    break;
                case TypeTags.XML_TAG:
                    typeClazz = XMLValue.class;
                    break;
                case TypeTags.OBJECT_TYPE_TAG:
                    typeClazz = ObjectValue.class;
                    break;
                case TypeTags.NULL_TAG:
                    typeClazz = Object.class;
                    break;
                case TypeTags.HANDLE_TAG:
                    typeClazz = HandleValue.class;
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
                                                               birPackage.version.value,
                                                               getClassName(function.pos.src.cUnitName));
        try {
            Class<?> funcClass = compileResult.getClassLoader().loadClass(funcClassName);
            Method method = funcClass.getDeclaredMethod(functionName, jvmParamTypes);
            Function<Object[], Object> func = a -> {
                try {
                    return method.invoke(null, a);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Error while invoking function '" + functionName + "'", e);
                } catch (InvocationTargetException e) {
                    Throwable t = e.getTargetException();
                    if (t instanceof BLangRuntimeException) {
                        throw new org.ballerinalang.util.exceptions.BLangRuntimeException(t.getMessage());
                    }
                    if (t instanceof ErrorValue) {
                        throw new org.ballerinalang.util.exceptions
                                .BLangRuntimeException("error: " + ((ErrorValue) t).getPrintableStackTrace());
                    }
                    if (t instanceof StackOverflowError) {
                        throw new org.ballerinalang.util.exceptions.BLangRuntimeException("error: " +
                                "{ballerina}StackOverflow {\"message\":\"stack overflow\"}");
                    }
                    throw new RuntimeException("Error while invoking function '" + functionName + "'", e);
                }
            };

            Scheduler scheduler = new Scheduler(false);
            FutureValue futureValue = scheduler.schedule(jvmArgs, func, null, null, new HashMap<>(),
                    org.ballerinalang.jvm.types.BTypes.typeAny);
            scheduler.start();
            if (futureValue.panic instanceof RuntimeException) {
                throw new org.ballerinalang.util.exceptions.BLangRuntimeException(futureValue.panic.getMessage(),
                        futureValue.panic);
            }
            jvmResult = futureValue.result;
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException("Error while invoking function '" + functionName + "'", e);
        }

        BValue result = getBVMValue(jvmResult);
        return new BValue[]{result};
    }

    private static String getClassName(String balFileName) {
        if (!balFileName.endsWith(".bal")) {
            return balFileName;
        }

        return balFileName.substring(0, balFileName.length() - 4);
    }

    /**
     * This method converts the compile time BType and the value to to JVM runtime value.
     *
     * @param type compile time BType
     * @param value BVM value
     * @return JVM value
     */
    private static Object getJVMValue(org.wso2.ballerinalang.compiler.semantics.model.types.BType type,
                                      BValue value) {

        if (value == null) {
            return null;
        }

        switch (type.tag) {
            case TypeTags.NULL_TAG:
                return null;
            case TypeTags.BYTE_TAG:
                return (int) ((BValueType) value).byteValue();
            case TypeTags.INT_TAG:
                return ((BValueType) value).intValue();
            case TypeTags.BOOLEAN_TAG:
                return ((BBoolean) value).booleanValue();
            case TypeTags.STRING_TAG:
                return StringUtils.fromString(value.stringValue());
            case TypeTags.FLOAT_TAG:
                return ((BFloat) value).floatValue();
            case TypeTags.DECIMAL_TAG:
                BDecimal decimal = (BDecimal) value;
                return new DecimalValue(decimal.stringValue(), DecimalValueKind.valueOf(decimal.valueKind.name()));
            case TypeTags.ARRAY_TAG:
                org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType arrayType =
                        (org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType) type;
                BValueArray array = (BValueArray) value;
                org.ballerinalang.jvm.types.BType jvmType = getJVMType(array.getType());
                org.ballerinalang.jvm.types.BArrayType jvmArrayType;
                if (jvmType.getTag() == org.ballerinalang.jvm.types.TypeTags.ARRAY_TAG) {
                    jvmArrayType = (org.ballerinalang.jvm.types.BArrayType) jvmType;
                } else {
                    jvmArrayType = new org.ballerinalang.jvm.types.BArrayType(jvmType);
                }
                ArrayValue jvmArray = new ArrayValueImpl(jvmArrayType, array.size());
                for (int i = 0; i < array.size(); i++) {
                    switch (arrayType.eType.tag) {
                        case TypeTags.INT_TAG:
                            jvmArray.add(i, array.getInt(i));
                            break;
                        case TypeTags.BYTE_TAG:
                            jvmArray.add(i, array.getByte(i));
                            break;
                        case TypeTags.BOOLEAN_TAG:
                            jvmArray.add(i, array.getBoolean(i) == 1);
                            break;
                        case TypeTags.STRING_TAG:
                            jvmArray.add(i, array.getString(i));
                            break;
                        case TypeTags.FLOAT_TAG:
                            jvmArray.add(i, array.getFloat(i));
                            break;
                        default:
                            BRefType<?> refValue = array.getRefValue(i);
                            jvmArray.add(i, getJVMValue(refValue.getType(), refValue));
                            break;
                    }
                }
                return jvmArray;
            case TypeTags.UNION_TAG:
            case TypeTags.ANY_TAG:
            case TypeTags.ANYDATA_TAG:
            case TypeTags.FINITE_TYPE_TAG:
                return getJVMValue(value.getType(), value);
            case TypeTags.JSON_TAG:
                return getJVMValue(value.getType(), value);
            case TypeTags.RECORD_TYPE_TAG:
            case TypeTags.MAP_TAG:
                BMap<String, BValue> record = (BMap) value;
                MapValueImpl<org.ballerinalang.jvm.values.api.BString, Object> jvmRecord = new MapValueImpl<>(
                        getJVMType(type));
                for (Map.Entry<String, BValue> entry : record.getMap().entrySet()) {
                    BValue entryVal = entry.getValue();
                    Object jvmVal = entryVal == null ? null : getJVMValue(entryVal.getType(), entryVal);
                    jvmRecord.put(StringUtils.fromString(entry.getKey()), jvmVal);
                }
                return jvmRecord;
            case TypeTags.OBJECT_TYPE_TAG:
                PackageID packageID = type.tsymbol.pkgID;
                BPackage objPackage = new BPackage(packageID.orgName.getValue(), packageID.name.getValue(),
                        packageID.version.getValue());
                String objName = type.tsymbol.getName().getValue();

                ObjectValue jvmObject = BallerinaValues.createObjectValue(objPackage, objName);
                BMap<String, BValue> objVal = (BMap) value;
                for (Map.Entry<String, BValue> entry : objVal.getMap().entrySet()) {
                    BValue entryVal = entry.getValue();
                    Object jvmVal = entryVal == null ? null : getJVMValue(entryVal.getType(), entryVal);
                    jvmObject.set(StringUtils.fromString(entry.getKey()), jvmVal);
                }
                HashMap<String, Object> nativeData = ((BMap) value).getNativeData();
                if (nativeData == null) {
                    return jvmObject;
                }
                for (String key : nativeData.keySet()) {
                    jvmObject.addNativeData(key, nativeData.get(key));
                }
                return jvmObject;
            case TypeTags.XML_TAG:
                org.ballerinalang.model.values.BXML xml = (org.ballerinalang.model.values.BXML) value;
                if (xml.getNodeType() == org.ballerinalang.model.util.XMLNodeType.TEXT) {
                    return XMLFactory.createXMLText(xml.stringValue());
                }
                if (xml.getNodeType() != org.ballerinalang.model.util.XMLNodeType.SEQUENCE) {
                    return XMLFactory.parse(xml.stringValue());
                } else {
                    BValueArray elements = ((BXMLSequence) xml).value();
                    ArrayValue arrayValue = (ArrayValue) getJVMValue(elements.getType(), elements);

                    List<BXML> list = new ArrayList<>();
                    for (int i = 0; i < arrayValue.size(); i++) {
                        list.add((BXML) arrayValue.getRefValue(i));
                    }
                    return new XMLSequence(list);
                }
            case TypeTags.HANDLE_TAG:
                BHandleValue handleValue = (BHandleValue) value;
                return new HandleValue(handleValue.getValue());
            default:
                throw new RuntimeException("Function signature type '" + type + "' is not supported");
        }
    }

    /**
     * This method converts the runtime time BType and the value to JVM runtime value.
     *
     * @param type  runtime BType
     * @param value BVM value
     * @return JVM value
     */
    private static Object getJVMValue(BType type, BValue value) {

        if (value == null) {
            return null;
        }

        switch (type.getTag()) {
            case TypeTags.NULL_TAG:
                return null;
            case TypeTags.BYTE_TAG:
                return (int) ((BByte) value).byteValue();
            case TypeTags.INT_TAG:
                return ((BInteger) value).intValue();
            case TypeTags.BOOLEAN_TAG:
                return ((BBoolean) value).booleanValue();
            case TypeTags.STRING_TAG:
                return StringUtils.fromString(value.stringValue());
            case TypeTags.FLOAT_TAG:
                return ((BFloat) value).floatValue();
            case TypeTags.DECIMAL_TAG:
                return new DecimalValue(((BDecimal) value).value());
            case TypeTags.ARRAY_TAG:
                BArrayType arrayType = (BArrayType) type;
                BValueArray array = (BValueArray) value;
                ArrayValue jvmArray =
                        new ArrayValueImpl((org.ballerinalang.jvm.types.BArrayType) getJVMType(arrayType));
                for (int i = 0; i < array.size(); i++) {
                    switch (arrayType.getElementType().getTag()) {
                        case TypeTags.INT_TAG:
                            jvmArray.add(i, array.getInt(i));
                            break;
                        case TypeTags.BYTE_TAG:
                            jvmArray.add(i, array.getByte(i));
                            break;
                        case TypeTags.BOOLEAN_TAG:
                            jvmArray.add(i, array.getBoolean(i) == 1);
                            break;
                        case TypeTags.STRING_TAG:
                            jvmArray.add(i, array.getString(i));
                            break;
                        case TypeTags.FLOAT_TAG:
                            jvmArray.add(i, array.getFloat(i));
                            break;
                        case TypeTags.JSON_TAG:
                        case TypeTags.XML_TAG:
                            BRefType refValue = array.getRefValue(i);
                            jvmArray.add(i, getJVMValue(refValue.getType(), refValue));
                            break;
                        default:
                            throw new RuntimeException("Function signature type '" + type + "' is not supported");
                    }
                }
                return jvmArray;
            case TypeTags.MAP_TAG:
                BMapType mapType = (BMapType) type;
                BMap bMap = (BMap) value;
                MapValueImpl<Object, Object> jvmMap = new MapValueImpl<>(getJVMType(mapType));
                bMap.getMap().forEach((k, v) -> {
                    BValue bValue = bMap.get(k);
                    jvmMap.put(k, getJVMValue(bValue.getType(), bValue));
                });
                return jvmMap;
            case TypeTags.UNION_TAG:
            case TypeTags.ANY_TAG:
            case TypeTags.ANYDATA_TAG:
                return getJVMValue(value.getType(), value);
            case TypeTags.JSON_TAG:
                bMap = (BMap) value;
                jvmMap = new MapValueImpl<>(new org.ballerinalang.jvm.types.BMapType(getJVMType(type)));
                bMap.getMap().forEach((k, v) -> {
                    BValue bValue = bMap.get(k);
                    jvmMap.put(StringUtils.fromString(k.toString()),
                               bValue != null ? getJVMValue(bValue.getType(), bValue) : null);
                });
                return jvmMap;
            case TypeTags.OBJECT_TYPE_TAG:
                String objPackagePath = type.getPackagePath();
                BPackage objPkgPath = new BPackage(objPackagePath, "");
                String objName = type.getName();

                ObjectValue jvmObject = BallerinaValues.createObjectValue(objPkgPath, objName);
                HashMap<String, Object> nativeData = ((BMap) value).getNativeData();
                if (nativeData == null) {
                    return jvmObject;
                }
                for (String key : nativeData.keySet()) {
                    jvmObject.addNativeData(key, nativeData.get(key));
                }
                return jvmObject;
            case TypeTags.XML_TAG:
                org.ballerinalang.model.values.BXML xml = (org.ballerinalang.model.values.BXML) value;
                if (xml.getNodeType() != org.ballerinalang.model.util.XMLNodeType.SEQUENCE) {
                    return XMLFactory.parse(xml.stringValue());
                }
                BValueArray elements = ((BXMLSequence) xml).value();
                ArrayValue jvmValue = (ArrayValue) getJVMValue(elements.getType(), elements);
                List<BXML> list = new ArrayList<>();
                for (Object v : jvmValue.getValues()) {
                    list.add((BXML) v);
                }

                return new XMLSequence(list);
            case TypeTags.HANDLE_TAG:
                BHandleValue bHandleValue = (BHandleValue) value;
                return new HandleValue(bHandleValue.getValue());
            default:
                throw new RuntimeException("Function signature type '" + type + "' is not supported");
        }
    }

    /**
     * This methods returns the JVM type for the given compile time BType.
     *
     * @param type compile time BType
     * @return JVM type
     */
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
                return new org.ballerinalang.jvm.types.BArrayType(elementType, arrayType.size);
            case TypeTags.MAP_TAG:
                org.wso2.ballerinalang.compiler.semantics.model.types.BMapType mapType =
                        (org.wso2.ballerinalang.compiler.semantics.model.types.BMapType) type;
                org.ballerinalang.jvm.types.BType constrainType = getJVMType(mapType.getConstraint());
                return new org.ballerinalang.jvm.types.BMapType(constrainType);
            case TypeTags.UNION_TAG:
                ArrayList<org.ballerinalang.jvm.types.BType> members = new ArrayList<>();
                org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType unionType =
                        (org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType) type;
                for (org.wso2.ballerinalang.compiler.semantics.model.types.BType memberType :
                        unionType.getMemberTypes()) {
                    members.add(getJVMType(memberType));
                }
                return new org.ballerinalang.jvm.types.BUnionType(members);
            case TypeTags.ANY_TAG:
                return org.ballerinalang.jvm.types.BTypes.typeAny;
            case TypeTags.ANYDATA_TAG:
                return org.ballerinalang.jvm.types.BTypes.typeAnydata;
            case TypeTags.JSON_TAG:
                return org.ballerinalang.jvm.types.BTypes.typeJSON;
            case TypeTags.RECORD_TYPE_TAG:
                org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType recordType =
                        (org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType) type;
                Map<String, org.ballerinalang.jvm.types.BField> fields = new HashMap<>();
                for (org.wso2.ballerinalang.compiler.semantics.model.types.BField bvmField
                        : recordType.fields.values()) {
                    org.ballerinalang.jvm.types.BField jvmField =
                            new org.ballerinalang.jvm.types.BField(getJVMType(bvmField.type), bvmField.name.value, 0);
                    fields.put(bvmField.name.value, jvmField);
                }
                PackageID pkgID = recordType.tsymbol.pkgID;
                BPackage pkg = new BPackage(pkgID.orgName.value, pkgID.name.value, pkgID.version.value);
                org.ballerinalang.jvm.types.BType restFieldType =
                        recordType.sealed ? null : getJVMType(recordType.restFieldType);
                int typeFlags = getMask(recordType.isNullable(), recordType.isAnydata(), recordType.isPureType());
                org.ballerinalang.jvm.types.BRecordType jvmRecordType = new org.ballerinalang.jvm.types.BRecordType(
                        recordType.tsymbol.name.value, pkg, 0, fields, restFieldType, false, typeFlags);
                return jvmRecordType;
            case TypeTags.FINITE_TYPE_TAG:
                org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType finiteType =
                        (org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType) type;
                Set<Object> valueSpace = new HashSet<>();
                for (BLangExpression expr : finiteType.getValueSpace()) {
                    if (!(expr instanceof BLangLiteral)) {
                        continue;
                    }
                    Object value = ((BLangLiteral) expr).value;
                    valueSpace.add(value);
                }
                return new org.ballerinalang.jvm.types.BFiniteType(null, valueSpace,
                        getMask(finiteType.isNullable(), finiteType.isAnydata(), finiteType.isPureType()));
            default:
                throw new RuntimeException("Function argument for type '" + type + "' is not supported");
        }
    }

    private static int getMask(boolean nilable, boolean isAnydata, boolean isPureType) {
        int mask = nilable ? TypeFlags.NILABLE : 0;
        if (isAnydata) {
            mask = TypeFlags.addToMask(mask, TypeFlags.ANYDATA);
        }
        if (isPureType) {
            mask = TypeFlags.addToMask(mask, TypeFlags.PURETYPE);
        }
        return mask;
    }

    /**
     * This methods returns the JVM type for the given runtime time BType.
     *
     * @param type run time BType
     * @return JVM type
     */
    private static org.ballerinalang.jvm.types.BType getJVMType(BType type) {
        switch (type.getTag()) {
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
            case TypeTags.DECIMAL_TAG:
                return org.ballerinalang.jvm.types.BTypes.typeDecimal;
            case TypeTags.ARRAY_TAG:
                BArrayType arrayType = (BArrayType) type;
                org.ballerinalang.jvm.types.BType elementType = getJVMType(arrayType.getElementType());
                if (arrayType.getState() == BArrayState.UNSEALED) {
                    return new org.ballerinalang.jvm.types.BArrayType(elementType);
                }
                return new org.ballerinalang.jvm.types.BArrayType(elementType, arrayType.getSize());
            case TypeTags.MAP_TAG:
                BMapType mapType = (BMapType) type;
                org.ballerinalang.jvm.types.BType constrainType = getJVMType(mapType.getConstrainedType());
                return new org.ballerinalang.jvm.types.BMapType(constrainType);
            case TypeTags.UNION_TAG:
                ArrayList<org.ballerinalang.jvm.types.BType> members = new ArrayList<>();
                BUnionType unionType = (BUnionType) type;
                for (BType memberType : unionType.getMemberTypes()) {
                    members.add(getJVMType(memberType));
                }
                return new org.ballerinalang.jvm.types.BUnionType(members);
            case TypeTags.ANY_TAG:
                return org.ballerinalang.jvm.types.BTypes.typeAny;
            case TypeTags.ANYDATA_TAG:
                return org.ballerinalang.jvm.types.BTypes.typeAnydata;
            case TypeTags.JSON_TAG:
                return org.ballerinalang.jvm.types.BTypes.typeJSON;
            case TypeTags.HANDLE_TAG:
                return org.ballerinalang.jvm.types.BTypes.typeHandle;
            case TypeTags.XML_TAG:
                return org.ballerinalang.jvm.types.BTypes.typeXML;
            default:
                throw new RuntimeException("Function argument for type '" + type + "' is not supported");
        }
    }

    private static BRefType<?> getBVMValue(Object value) {
        return getBVMValue(value, new HashMap<>());
    }

    @SuppressWarnings("rawtypes")
    private static BRefType<?> getBVMValue(Object value, Map<String, BRefType> bvmValueMap) {
        String hashCode = String.valueOf(System.identityHashCode(value));
        if (value == null) {
            return null;
        }
        BRefType bvmValue = bvmValueMap.get(hashCode);
        if (bvmValue != null) {
            return bvmValue;
        }
        org.ballerinalang.jvm.types.BType type = TypeChecker.getType(value);
        switch (type.getTag()) {
            case org.ballerinalang.jvm.types.TypeTags.INT_TAG:
                bvmValue = new BInteger((long) value);
                break;
            case org.ballerinalang.jvm.types.TypeTags.BYTE_TAG:
                bvmValue = new BByte(((Number) value).longValue());
                break;
            case org.ballerinalang.jvm.types.TypeTags.FLOAT_TAG:
                bvmValue = new BFloat((double) value);
                break;
            case org.ballerinalang.jvm.types.TypeTags.BOOLEAN_TAG:
                bvmValue = new BBoolean((boolean) value);
                break;
            case org.ballerinalang.jvm.types.TypeTags.STRING_TAG:
                if (value instanceof org.ballerinalang.jvm.values.api.BString) {
                    bvmValue = new BString(((org.ballerinalang.jvm.values.api.BString) value).getValue());
                } else {
                    bvmValue = new BString((String) value);
                }
                break;
            case org.ballerinalang.jvm.types.TypeTags.DECIMAL_TAG:
                DecimalValue decimalValue = (DecimalValue) value;
                bvmValue = new BDecimal(decimalValue.value().toString(),
                                        org.ballerinalang.model.util.DecimalValueKind
                                                .valueOf(decimalValue.valueKind.name()));
                break;
            case org.ballerinalang.jvm.types.TypeTags.TUPLE_TAG:
                ArrayValue jvmTuple = ((ArrayValue) value);
                BRefType<?>[] tupleValues = new BRefType<?>[jvmTuple.size()];
                for (int i = 0; i < jvmTuple.size(); i++) {
                    tupleValues[i] = getBVMValue(jvmTuple.getRefValue(i), bvmValueMap);
                }
                bvmValue = new BValueArray(tupleValues, getBVMType(jvmTuple.getType(), new Stack<>()));
                break;
            case org.ballerinalang.jvm.types.TypeTags.ARRAY_TAG:
                org.ballerinalang.jvm.types.BArrayType arrayType = (org.ballerinalang.jvm.types.BArrayType) type;
                ArrayValue array = (ArrayValue) value;
                BValueArray bvmArray;
                if (arrayType.getElementType().getTag() == org.ballerinalang.jvm.types.TypeTags.ARRAY_TAG) {
                    bvmArray = new BValueArray(getBVMType(arrayType, new Stack<>()));
                } else if (arrayType.getState() == ArrayState.UNSEALED) {
                    bvmArray = new BValueArray(getBVMType(arrayType.getElementType(), new Stack<>()), -1);
                } else {
                    bvmArray = new BValueArray(getBVMType(arrayType.getElementType(), new Stack<>()), array.size());
                }

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
                            bvmArray.add(i, getBVMValue(array.getRefValue(i), bvmValueMap));
                            break;
                    }
                }
                bvmValue = bvmArray;
                break;
            case org.ballerinalang.jvm.types.TypeTags.RECORD_TYPE_TAG:
            case org.ballerinalang.jvm.types.TypeTags.JSON_TAG:
            case org.ballerinalang.jvm.types.TypeTags.MAP_TAG:
                MapValueImpl<?, ?> jvmMap = (MapValueImpl) value;
                BMap<Object, BRefType> bmap = new BMap<>(getBVMType(jvmMap.getType(), new Stack<>()));
                bvmValueMap.put(String.valueOf(value.hashCode()), bmap);
                for (Map.Entry entry : jvmMap.entrySet()) {
                    Object key = entry.getKey().toString();
                    bmap.put(key, getBVMValue(entry.getValue(), bvmValueMap));
                }
                bmap.getNativeData().putAll(jvmMap.getNativeDataMap());
                return bmap;
            case org.ballerinalang.jvm.types.TypeTags.ERROR_TAG:
                ErrorValue errorValue = (ErrorValue) value;
                BError cause = (BError) getBVMValue(errorValue.getCause(), bvmValueMap);
                BRefType<?> details = getBVMValue(errorValue.getDetails(), bvmValueMap);
                bvmValue = new BError(getBVMType(errorValue.getType(), new Stack<>()),
                                      errorValue.getErrorMessage().getValue(), cause, details);
                return bvmValue;
            case org.ballerinalang.jvm.types.TypeTags.NULL_TAG:
                bvmValue = null;
                break;
            case org.ballerinalang.jvm.types.TypeTags.OBJECT_TYPE_TAG:
                ObjectValue jvmObject = (ObjectValue) value;
                org.ballerinalang.jvm.types.BObjectType jvmObjectType = jvmObject.getType();
                BMap<String, BRefType<?>> bvmObject = new BMap<>(getBVMType(jvmObjectType, new Stack<>()));
                bvmValueMap.put(String.valueOf(value.hashCode()), bvmObject);
                for (String key : jvmObjectType.getFields().keySet()) {
                    Object val;
                    try {
                        val = jvmObject.get(StringUtils.fromString(key));
                    } catch (AbstractMethodError error) {
                        val = jvmObject.get(StringUtils.fromString(key));
                    }
                    bvmObject.put(key, getBVMValue(val, bvmValueMap));
                }

                HashMap<String, Object> nativeData = jvmObject.getNativeData();
                if (nativeData == null) {
                    return bvmObject;
                }
                for (String key : nativeData.keySet()) {
                    bvmObject.addNativeData(key, nativeData.get(key));
                }
                return bvmObject;
            case org.ballerinalang.jvm.types.TypeTags.XML_TAG:
            case org.ballerinalang.jvm.types.TypeTags.XML_ELEMENT_TAG:
            case org.ballerinalang.jvm.types.TypeTags.XML_COMMENT_TAG:
            case org.ballerinalang.jvm.types.TypeTags.XML_PI_TAG:
            case org.ballerinalang.jvm.types.TypeTags.XML_TEXT_TAG:
                if (value instanceof XMLValue) {
                    if (((XMLValue) value).getNodeType() != XMLNodeType.SEQUENCE) {
                        BXMLItem bxmlItem = new BXMLItem((OMNode) ((XMLValue) value).value());
                        bvmValue = bxmlItem;
                        break;
                    } else {
                        org.ballerinalang.jvm.types.BArrayType bArrayType =
                                new org.ballerinalang.jvm.types.BArrayType(org.ballerinalang.jvm.types.BTypes.typeXML);
                        ArrayValue arrayValue = new ArrayValueImpl(((XMLSequence) value).getChildrenList().toArray(),
                                bArrayType);

                        bvmValue = new BXMLSequence((BValueArray) getBVMValue(arrayValue));
                        break;
                    }
                }

                BValueArray ar = new BValueArray(BTypes.typeXML);
                int i = 0;
                for (Object obj : ((ArrayValue) value).getValues()) {
                    ar.add(i++, getBVMValue(obj, bvmValueMap));
                }
                bvmValue = new BXMLSequence(ar);
                break;
            case org.ballerinalang.jvm.types.TypeTags.TYPEDESC_TAG:
                TypedescValue typedescValue = (TypedescValue) value;
                bvmValue = new BTypeDescValue(getBVMType(typedescValue.getDescribingType(), new Stack<>()));
                break;
            case org.ballerinalang.jvm.types.TypeTags.STREAM_TAG:
                StreamValue streamValue = (StreamValue) value;
                bvmValue = new BStream(getBVMType(streamValue.getType(), new Stack<>()), streamValue.getStreamId());
                break;
            case org.ballerinalang.jvm.types.TypeTags.FUNCTION_POINTER_TAG:
                FPValue functionValue = (FPValue) value;
                bvmValue = new BFunctionPointer(getBVMType(functionValue.getType(), new Stack<>()));
                break;
            case org.ballerinalang.jvm.types.TypeTags.HANDLE_TAG:
                bvmValue = new BHandleValue(((HandleValue) value).getValue());
                break;
            case org.ballerinalang.jvm.types.TypeTags.SERVICE_TAG:
                org.ballerinalang.jvm.types.BObjectType bObjectType = ((AbstractObjectValue) value).getType();
                bvmValue = new BMap(getBVMType(bObjectType, new Stack<>()));
                break;
            default:
                throw new RuntimeException("Function invocation result for type '" + type + "' is not supported");
        }

        bvmValueMap.put(hashCode, bvmValue);
        return bvmValue;
    }

    private static BType getBVMType(org.ballerinalang.jvm.types.BType jvmType,
                                    Stack<org.ballerinalang.jvm.types.BField> selfTypeStack) {
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
            case org.ballerinalang.jvm.types.TypeTags.DECIMAL_TAG:
                return BTypes.typeDecimal;
            case org.ballerinalang.jvm.types.TypeTags.TUPLE_TAG:
                org.ballerinalang.jvm.types.BTupleType tupleType = (org.ballerinalang.jvm.types.BTupleType) jvmType;
                List<BType> memberTypes = new ArrayList<>();
                for (org.ballerinalang.jvm.types.BType type : tupleType.getTupleTypes()) {
                    memberTypes.add(getBVMType(type, selfTypeStack));
                }
                return new BTupleType(memberTypes);
            case org.ballerinalang.jvm.types.TypeTags.ARRAY_TAG:
                org.ballerinalang.jvm.types.BArrayType arrayType = (org.ballerinalang.jvm.types.BArrayType) jvmType;
                return new BArrayType(getBVMType(arrayType.getElementType(), selfTypeStack));
            case org.ballerinalang.jvm.types.TypeTags.ANY_TAG:
                return BTypes.typeAny;
            case org.ballerinalang.jvm.types.TypeTags.ANYDATA_TAG:
                return BTypes.typeAnydata;
            case org.ballerinalang.jvm.types.TypeTags.ERROR_TAG:
                org.ballerinalang.jvm.types.BErrorType errorType = (org.ballerinalang.jvm.types.BErrorType) jvmType;
                if (errorType == org.ballerinalang.jvm.types.BTypes.typeError) {
                    return BTypes.typeError;
                }

                BType detailType = getBVMType(errorType.detailType, selfTypeStack);
                BErrorType bvmErrorType =
                        // todo: using reason type as string is just a hack to get the code compile after removing error reason type.
                        new BErrorType(errorType.getName(), BTypes.typeString, detailType, errorType.getPackage().name);
                return bvmErrorType;
            case org.ballerinalang.jvm.types.TypeTags.RECORD_TYPE_TAG:
                org.ballerinalang.jvm.types.BRecordType recordType = (org.ballerinalang.jvm.types.BRecordType) jvmType;
                BRecordType bvmRecordType = new BRecordType(recordType.getName(),
                        recordType.getPackage().getName(), recordType.flags);
                Map<String, BField> recordFields =
                        recordType.getFields().entrySet().stream()
                                .filter(entry -> !selfTypeStack.contains(entry.getValue()))
                                .peek(entry -> selfTypeStack.push(entry.getValue()))
                                .collect(Collectors.toMap(Map.Entry::getKey,
                                                          entry -> new BField(
                                                                  getBVMType(entry.getValue().type, selfTypeStack),
                                                entry.getValue().getFieldName(), entry.getValue().flags),
                                        (a, b) -> b, LinkedHashMap::new));
                bvmRecordType.setFields(recordFields);
                return bvmRecordType;
            case org.ballerinalang.jvm.types.TypeTags.JSON_TAG:
                return BTypes.typeJSON;
            case org.ballerinalang.jvm.types.TypeTags.MAP_TAG:
                org.ballerinalang.jvm.types.BMapType mapType = (org.ballerinalang.jvm.types.BMapType) jvmType;
                return new BMapType(getBVMType(mapType.getConstrainedType(), selfTypeStack));
            case org.ballerinalang.jvm.types.TypeTags.STREAM_TAG:
                org.ballerinalang.jvm.types.BStreamType streamType = (org.ballerinalang.jvm.types.BStreamType) jvmType;
                return new BStreamType(getBVMType(streamType.getConstrainedType(), selfTypeStack));
            case org.ballerinalang.jvm.types.TypeTags.UNION_TAG:
                org.ballerinalang.jvm.types.BUnionType unionType = (org.ballerinalang.jvm.types.BUnionType) jvmType;
                memberTypes = new ArrayList<>();
                for (org.ballerinalang.jvm.types.BType type : unionType.getMemberTypes()) {
                    memberTypes.add(getBVMType(type, selfTypeStack));
                }
                return new BUnionType(memberTypes);
            case org.ballerinalang.jvm.types.TypeTags.INTERSECTION_TAG:
                return getBVMType(((BIntersectionType) jvmType).getEffectiveType(), selfTypeStack);
            case org.ballerinalang.jvm.types.TypeTags.OBJECT_TYPE_TAG:
                org.ballerinalang.jvm.types.BObjectType objectType = (org.ballerinalang.jvm.types.BObjectType) jvmType;
                BObjectType bvmObjectType =
                        new BObjectType(objectType.getName(), objectType.getPackage().getName(),
                                        objectType.flags);
                Map<String, BField> objectFields = new LinkedHashMap<>();
                for (org.ballerinalang.jvm.types.BField field : objectType.getFields().values()) {
                    if (selfTypeStack.contains(field)) {
                        continue;
                    }
                    selfTypeStack.push(field);
                    objectFields.put(field.name, new BField(getBVMType(field.type, selfTypeStack),
                                                            field.name, field.flags));
                }
                bvmObjectType.setFields(objectFields);
                return bvmObjectType;
            case org.ballerinalang.jvm.types.TypeTags.XML_TAG:
                return BTypes.typeXML;
            case org.ballerinalang.jvm.types.TypeTags.TYPEDESC_TAG:
                BTypedescType typedescType = (BTypedescType) jvmType;
                return new BTypeDesc(typedescType.getName(),
                        typedescType.getPackage() == null ? null : typedescType.getPackage().getName());
            case org.ballerinalang.jvm.types.TypeTags.NULL_TAG:
                return BTypes.typeNull;
            case org.ballerinalang.jvm.types.TypeTags.FINITE_TYPE_TAG:
                org.ballerinalang.jvm.types.BFiniteType jvmBFiniteType =
                        (org.ballerinalang.jvm.types.BFiniteType) jvmType;
                BFiniteType bFiniteType = new BFiniteType(jvmBFiniteType.getName(),
                        jvmBFiniteType.getPackage() == null ? null : jvmType.getPackage().name);
                jvmBFiniteType.valueSpace.forEach(jvmVal -> bFiniteType.valueSpace.add(getBVMValue(jvmVal)));
                return bFiniteType;

            case org.ballerinalang.jvm.types.TypeTags.FUNCTION_POINTER_TAG:
                org.ballerinalang.jvm.types.BFunctionType jvmBFunctionType =
                        (org.ballerinalang.jvm.types.BFunctionType) jvmType;
                BType[] bParamTypes = new BType[jvmBFunctionType.paramTypes.length];
                for (int i = 0; i < jvmBFunctionType.paramTypes.length; i++) {
                    bParamTypes[i] = getBVMType(jvmBFunctionType.paramTypes[i], selfTypeStack);
                }
                BType bRetType = getBVMType(jvmBFunctionType.retType, selfTypeStack);
                BType bRestType = null;
                if (jvmBFunctionType.restType != null) {
                    bRestType = getBVMType(jvmBFunctionType.restType, selfTypeStack);
                }
                return new BFunctionType(bParamTypes, bRestType, new BType[]{bRetType});
            case org.ballerinalang.jvm.types.TypeTags.HANDLE_TAG:
                return BTypes.typeHandle;
            case org.ballerinalang.jvm.types.TypeTags.SERVICE_TAG:
                return new BServiceType(jvmType.getName(), null, 0);
            case org.ballerinalang.jvm.types.TypeTags.READONLY_TAG:
                return BTypes.typeReadonly;
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
        BIRNode.BIRFunction function = getInvokedFunction(compileResult, functionName);
        args = addDefaultableBoolean(args);
        return invoke(compileResult, function, functionName, args);
    }

    /**
     * Invoke a ballerina function to get BReference Value Objects.
     *
     * @param compileResult CompileResult instance
     * @param functionName  Name of the function to invoke
     * @return return values of the function
     */
    public static BValue[] invokeFunction(CompileResult compileResult, String functionName) {
        return invokeFunction(compileResult, functionName, new BValue[]{});
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

    public static Object invokeAndGetJVMResult(CompileResult compileResult, String functionName) {
        BIRNode.BIRFunction function = getInvokedFunction(compileResult, functionName);
        return invoke(compileResult, function, functionName, new BValue[0], new Class<?>[0]);
    }
}
