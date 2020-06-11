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
package org.wso2.ballerinalang.compiler.bir.emit;

import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BHandleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BServiceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.HashMap;
import java.util.Map;

import static org.wso2.ballerinalang.compiler.bir.emit.EmitterUtils.emitFlags;
import static org.wso2.ballerinalang.compiler.bir.emit.EmitterUtils.emitLBreaks;
import static org.wso2.ballerinalang.compiler.bir.emit.EmitterUtils.emitName;
import static org.wso2.ballerinalang.compiler.bir.emit.EmitterUtils.emitSpaces;
import static org.wso2.ballerinalang.compiler.bir.emit.EmitterUtils.emitTabs;
import static org.wso2.ballerinalang.compiler.bir.emit.EmitterUtils.getTypeName;

/**
 * BIR type emitter class to emit defined and builtin types.
 *
 * @since 1.2.0
 */
class TypeEmitter {

    static final Map<String, BType> B_TYPES = new HashMap<>();

    static String emitType(BType bType, int tabs) {

        switch (bType.tag) {
            case TypeTags.INT:
                return "int";
            case TypeTags.BOOLEAN:
                return "boolean";
            case TypeTags.ANY:
                return "any";
            case TypeTags.NIL:
                return "()";
            case TypeTags.NEVER:
                return "never";
            case TypeTags.BYTE:
                return "byte";
            case TypeTags.FLOAT:
                return "float";
            case TypeTags.STRING:
                return "string";
            case TypeTags.ANYDATA:
                return "anydata";
            case TypeTags.NONE:
                return "none";
            case TypeTags.JSON:
                return "json";
            case TypeTags.XML:
                return "xml";
            case TypeTags.DECIMAL:
                return "decimal";
            case TypeTags.UNION:
                return emitBUnionType((BUnionType) bType, tabs);
            case TypeTags.TUPLE:
                return emitBTupleType((BTupleType) bType, tabs);
            case TypeTags.INVOKABLE:
                return emitBInvokableType((BInvokableType) bType, tabs);
            case TypeTags.ARRAY:
                return emitBArrayType((BArrayType) bType, tabs);
            case TypeTags.RECORD:
                return emitBRecordType((BRecordType) bType, tabs);
            case TypeTags.OBJECT:
                if (bType instanceof BServiceType) {
                    return emitBServiceType((BServiceType) bType, tabs);
                }
                return emitBObjectType((BObjectType) bType, tabs);
            case TypeTags.MAP:
                return emitBMapType((BMapType) bType, tabs);
            case TypeTags.ERROR:
                return emitBErrorType((BErrorType) bType, tabs);
            case TypeTags.FUTURE:
                return emitBFutureType((BFutureType) bType, tabs);
            case TypeTags.TYPEDESC:
                return emitBTypeDesc((BTypedescType) bType, tabs);
            case TypeTags.FINITE:
                return emitBFiniteType((BFiniteType) bType, tabs);
            case TypeTags.HANDLE:
                return emitBTypeHandle((BHandleType) bType, tabs);
            default:
                throw new IllegalStateException("Invalid type");
        }
    }

    private static String emitBUnionType(BUnionType bType, int tabs) {

        StringBuilder unionStr = new StringBuilder();
        int length = bType.getMemberTypes().size();
        int i = 0;
        for (BType mType : bType.getMemberTypes()) {
            if (mType != null) {
                unionStr.append(emitTypeRef(mType, tabs));
                i += 1;
                if (i < length) {
                    unionStr.append(emitSpaces(1));
                    unionStr.append("|");
                    unionStr.append(emitSpaces(1));
                }
            }
        }
        return unionStr.toString();
    }

    private static String emitBTupleType(BTupleType bType, int tabs) {

        StringBuilder tupleStr = new StringBuilder("(");
        int length = bType.tupleTypes.size();
        int i = 0;
        for (BType mType : bType.tupleTypes) {
            if (mType != null) {
                tupleStr.append(emitTypeRef(mType, tabs));
                i += 1;
                if (i < length) {
                    tupleStr.append(",");
                    tupleStr.append(emitSpaces(1));
                }
            }
        }
        tupleStr.append(")");
        return tupleStr.toString();
    }

    private static String emitBInvokableType(BInvokableType bType, int tabs) {

        StringBuilder invString = new StringBuilder("function(");
        int pLength = bType.paramTypes.size();
        int i = 0;
        for (BType pType : bType.paramTypes) {
            if (pType != null) {
                invString.append(emitTypeRef(pType, tabs));
                i += 1;
                if (i < pLength) {
                    invString.append(",");
                    invString.append(emitSpaces(1));
                }
            }
        }
        invString.append(")");
        BType retType = bType.retType;
        if (retType != null) {
            invString.append(emitSpaces(1));
            invString.append("->");
            invString.append(emitSpaces(1));
            invString.append(emitTypeRef(retType, tabs));
        }
        return invString.toString();
    }

    private static String emitBArrayType(BArrayType bType, int tabs) {

        String arrStr = emitTypeRef(bType.eType, 0);
        arrStr += "[";
        if (bType.size > 0) {
            arrStr += bType.size;
        }
        arrStr += "]";
        return arrStr;
    }

    private static String emitBRecordType(BRecordType bType, int tabs) {

        StringBuilder recordStr = new StringBuilder("record");
        recordStr.append(emitSpaces(1));
        recordStr.append("{");
        recordStr.append(emitLBreaks(1));
        for (BField bField : bType.fields.values()) {
            if (bField != null) {
                recordStr.append(emitTabs(tabs + 1));
                String flags = emitFlags(bField.type.flags);
                recordStr.append(flags);
                if (!flags.equals("")) {
                    recordStr.append(emitSpaces(1));
                }
                recordStr.append(emitTypeRef(bField.type, tabs + 1));
                recordStr.append(emitSpaces(1));
                recordStr.append(emitName(bField.name));
                recordStr.append(";");
                recordStr.append(emitLBreaks(1));
            }
        }
        recordStr.append("}");
        return recordStr.toString();
    }

    private static String emitBObjectType(BObjectType bType, int tabs) {

        StringBuilder str = new StringBuilder("object");
        str.append(emitSpaces(1));
        str.append("{");
        str.append(emitLBreaks(1));
        for (BField bField : bType.fields.values()) {
            if (bField != null) {
                str.append(emitTabs(tabs + 1));
                String flags = emitFlags(bField.type.flags);
                str.append(flags);
                if (!flags.equals("")) {
                    str.append(emitSpaces(1));
                }
                str.append(emitTypeRef(bField.type, tabs + 1));
                str.append(emitSpaces(1));
                str.append(emitName(bField.name));
                str.append(";");
                str.append(emitLBreaks(1));
            }
        }
        int nTab = tabs + 1;
        BObjectTypeSymbol objectSymbol = (BObjectTypeSymbol) bType.tsymbol;

        BAttachedFunction generatedInitializerFunc = objectSymbol.generatedInitializerFunc;
        if (generatedInitializerFunc != null) {
            str.append(emitBAttachedFunction(generatedInitializerFunc, nTab));
        }
        str.append(emitLBreaks(1));

        BAttachedFunction initializerFunc = objectSymbol.initializerFunc;
        if (initializerFunc != null) {
            str.append(emitBAttachedFunction(initializerFunc, nTab));
        }
        str.append(emitLBreaks(1));

        for (BAttachedFunction attachedFunc : objectSymbol.attachedFuncs) {
            if (attachedFunc != null) {
                str.append(emitBAttachedFunction(attachedFunc, nTab));
                str.append(emitLBreaks(1));
            }
        }

        str.append("}");
        return str.toString();
    }

    private static String emitBAttachedFunction(BAttachedFunction func, int tabs) {

        String str = "";
        str += emitTabs(tabs);
        str += emitName(func.funcName);
        str += emitSpaces(1);
        str += emitType(func.type, 0);
        return str;
    }

    private static String emitBMapType(BMapType bType, int tabs) {

        String str = "map";
        str += "<";
        str += emitTypeRef(bType.constraint, 0);
        str += ">";
        return str;
    }

    private static String emitBErrorType(BErrorType bType, int tabs) {

        String errStr = "error{";
        errStr += emitTypeRef(bType.detailType, tabs);
        errStr += "}";
        return errStr;
    }

    private static String emitBFutureType(BFutureType bType, int tabs) {

        String str = "future";
        str += "<";
        str += emitTypeRef(bType.getReturnType(), 0);
        str += ">";
        return str;
    }

    private static String emitBTypeDesc(BTypedescType bType, int tabs) {

        String str = "typeDesc";
        str += "<";
        str += emitTypeRef(bType.constraint, 0);
        str += ">";
        return str;
    }

    private static String emitBServiceType(BServiceType bType, int tabs) {

        String str = "service";
        str += "<";
        str += emitTypeRef(bType.tsymbol.type, 0);
        str += ">";
        return str;
    }

    private static String emitBFiniteType(BFiniteType bType, int tabs) {

        StringBuilder str = new StringBuilder();
        str.append("[");
        int i = 0;
        int length = bType.getValueSpace().size();
        for (Object v : bType.getValueSpace()) {
            str.append(v.toString());
            i += 1;
            if (i < length) {
                str.append(",");
                str.append(emitSpaces(1));
            }
        }
        str.append("]");
        return str.toString();
    }

    private static String emitBTypeHandle(BHandleType bType, int tabs) {

        String str = "handle";
        str += "<";
        // TODO check for constraint of handle ?
        str += ">";
        return str;
    }

    /////////////////////// Emitting type reference ///////////////////////////
    static String emitTypeRef(BType bType, int tabs) {

        String tName = getTypeName(bType);
        if (!("".equals(tName))) {
            return tName;
        }
        if (bType.tag == TypeTags.ERROR) {
            B_TYPES.put(bType.tsymbol.toString(), bType);
        }
        if (bType.tag == TypeTags.RECORD || bType.tag == TypeTags.OBJECT) {
            return bType.tsymbol.toString();
        }
        return emitType(bType, tabs);
    }
}


