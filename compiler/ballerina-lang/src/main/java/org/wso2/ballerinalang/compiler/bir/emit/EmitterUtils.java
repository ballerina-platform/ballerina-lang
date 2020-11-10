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

import io.ballerina.runtime.util.Flags;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.Map;

/**
 * A utility class with common emit and helper functions.
 *
 * @since 1.2.0
 */
class EmitterUtils {

    static String emitName(Name name) {

        return name.value;
    }

    static String emitVarRef(BIROperand ref) {

        return emitName(ref.variableDcl.name);
    }

    static String emitBasicBlockRef(BIRNode.BIRBasicBlock basicBlock) {

        return emitName(basicBlock.id);
    }

    static String emitModuleID(PackageID modId) {

        String str = "";
        str += modId.orgName + "/";
        str += modId.name;
        if (!modId.version.value.equals("")) {
            str += ":";
            str += modId.version.value;
        }
        return str;
    }

    static String emitBinaryOpInstructionKind(InstructionKind kind) {

        switch (kind) {
            case ADD:
                return "+";
            case SUB:
                return "-";
            case MUL:
                return "*";
            case DIV:
                return "/";
            case MOD:
                return "%";
            case EQUAL:
                return "==";
            case NOT_EQUAL:
                return "!=";
            case GREATER_THAN:
                return ">";
            case GREATER_EQUAL:
                return ">=";
            case LESS_THAN:
                return "<";
            case LESS_EQUAL:
                return "<=";
            case REF_EQUAL:
                return "===";
            case REF_NOT_EQUAL:
                return "!==";
            case CLOSED_RANGE:
                return "...";
            case HALF_OPEN_RANGE:
                return "..<";
            case ANNOT_ACCESS:
                return ".@";
            case BITWISE_AND:
                return "&";
            case BITWISE_OR:
                return "|";
            case BITWISE_XOR:
                return "^";
            case BITWISE_LEFT_SHIFT:
                return "<<";
            case BITWISE_RIGHT_SHIFT:
                return ">>";
            case BITWISE_UNSIGNED_RIGHT_SHIFT:
                return ">>>";
            default:
                throw new IllegalStateException("Not a binary opkind");
        }
    }

    static String emitFlags(int flag) {

        if (Flags.isFlagOn(Flags.PRIVATE, flag)) {
            return "private";
        } else if (Flags.isFlagOn(Flags.PUBLIC, flag)) {
            return "public";
        }
        return "";
    }

    static String emitTabs(int tabs) {

        StringBuilder tab = new StringBuilder();
        int i = 0;
        while (i < tabs) {
            tab.append(emitSpaces(4));
            i += 1;
        }
        return tab.toString();
    }

    static String emitSpaces(int spaces) {

        StringBuilder spacesString = new StringBuilder();
        int i = 0;
        while (i < spaces) {
            spacesString.append(" ");
            i += 1;
        }
        return spacesString.toString();
    }

    static String emitLBreaks(int breaks) {

        StringBuilder lineBreaks = new StringBuilder();
        int i = 0;
        while (i < breaks) {
            lineBreaks.append("\n");
            i += 1;
        }
        return lineBreaks.toString();
    }

    static String getTypeName(BType bType) {

        for (Map.Entry<String, BType> entry : TypeEmitter.B_TYPES.entrySet()) {
            if (entry.getValue().equals(bType)) {
                return entry.getKey();
            }
        }

        return "";
    }

    static boolean isEmpty(Name nameVal) {

        return nameVal.value.equals("");
    }

    static String emitValue(Object value, BType type) {

        if (value == null || TypeTags.NIL == type.tag) {
            return "0";
        } else {
            return value.toString();
        }
    }

    static boolean isBinaryInstructionKind(InstructionKind insKind) {

        switch (insKind) {
            case ADD:
            case SUB:
            case MUL:
            case DIV:
            case MOD:
            case EQUAL:
            case NOT_EQUAL:
            case GREATER_THAN:
            case GREATER_EQUAL:
            case LESS_THAN:
            case LESS_EQUAL:
            case AND:
            case OR:
            case REF_EQUAL:
            case REF_NOT_EQUAL:
            case CLOSED_RANGE:
            case HALF_OPEN_RANGE:
            case ANNOT_ACCESS:
            case BITWISE_AND:
            case BITWISE_OR:
            case BITWISE_XOR:
            case BITWISE_LEFT_SHIFT:
            case BITWISE_RIGHT_SHIFT:
            case BITWISE_UNSIGNED_RIGHT_SHIFT:
                return true;
            default:
                return false;
        }
    }

    static boolean isUnaryInstructionKind(InstructionKind insKind) {

        switch (insKind) {
            case TYPEOF:
            case NOT:
            case NEGATE:
            case SUB:
            case ADD:
                return true;
            default:
                return false;
        }
    }
}

