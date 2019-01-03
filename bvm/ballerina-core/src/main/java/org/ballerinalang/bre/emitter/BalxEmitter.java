/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.bre.emitter;

import org.ballerinalang.util.codegen.ErrorTableEntry;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.Instruction;
import org.ballerinalang.util.codegen.LocalVariableInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.attributes.AttributeInfo;
import org.ballerinalang.util.codegen.attributes.CodeAttributeInfo;
import org.ballerinalang.util.codegen.attributes.ErrorTableAttributeInfo;
import org.ballerinalang.util.codegen.attributes.LocalVariableAttributeInfo;
import org.ballerinalang.util.codegen.attributes.ParameterAttributeInfo;
import org.ballerinalang.util.codegen.attributes.VarTypeCountAttributeInfo;
import org.ballerinalang.util.codegen.cpentries.ConstantPoolEntry;
import org.ballerinalang.util.codegen.cpentries.FloatCPEntry;
import org.ballerinalang.util.codegen.cpentries.FunctionRefCPEntry;
import org.ballerinalang.util.codegen.cpentries.IntegerCPEntry;
import org.ballerinalang.util.codegen.cpentries.PackageRefCPEntry;
import org.ballerinalang.util.codegen.cpentries.StringCPEntry;
import org.ballerinalang.util.codegen.cpentries.UTF8CPEntry;
import org.ballerinalang.util.codegen.cpentries.WorkerDataChannelRefCPEntry;

import java.io.PrintStream;

/**
 * This is a balx emitter to emit balx file in a readable format.
 *
 * @since 0.985.0
 */
public class BalxEmitter {

    private PrintStream printStream;

    public BalxEmitter() {
        printStream = System.out;
    }

    /**
     * Method to emit balx program file.
     *
     * @param programFile to be emitted
     */
    public void emit(ProgramFile programFile) {
        println("################################# Begin Final balx program #################################");
//        println("org - " + programFile.org);
        println("name - " + programFile.getEntryPackage().getPkgPath());
        println("version - " + programFile.getEntryPackage().getPackageVersion());

        emit(programFile.getConstPoolEntries(), "");
        emit(programFile.getPackageInfoEntries());
        emit(programFile.getAttributeInfoEntries(), "");
        println("################################## End Final balx program ##################################");
    }

    private void emit(ConstantPoolEntry[] constPool, String tabs) {
        println(tabs + "Constant pool {");
        for (int i = 0; i < constPool.length; i++) {
            emit(i, constPool[i], tabs + "\t");
        }
        println(tabs + "}");
    }

    private void emit(int index, ConstantPoolEntry cp, String tabs) {
        print(tabs + index + "\t T ");
        switch (cp.getEntryType()) {
            case CP_ENTRY_UTF8:
                println("CP_ENTRY_UTF8 - " + ((UTF8CPEntry) cp).getValue());
                break;
            case CP_ENTRY_INTEGER:
                println("CP_ENTRY_INTEGER - " + ((IntegerCPEntry) cp).getValue());
                break;
            case CP_ENTRY_FLOAT:
                println("CP_ENTRY_FLOAT - " + ((FloatCPEntry) cp).getValue());
                break;
            case CP_ENTRY_STRING:
                println("CP_ENTRY_STRING - " + ((StringCPEntry) cp).getValue());
                break;
            case CP_ENTRY_PACKAGE:
                PackageRefCPEntry pkgRefCP = (PackageRefCPEntry) cp;
                println("CP_ENTRY_PACKAGE - " + "nameCPIndex - " + pkgRefCP.getNameCPIndex());
                break;
            case CP_ENTRY_FUNCTION_REF:
                println("CP_ENTRY_FUNCTION_REF - " + ((FunctionRefCPEntry) cp).toString()); //TODO fix properly
                break;
            case CP_ENTRY_ACTION_REF:
                println("CP_ENTRY_ACTION_REF - ");
                break;
            case CP_ENTRY_FUNCTION_CALL_ARGS:
                println("CP_ENTRY_FUNCTION_CALL_ARGS - ");
                break;
            case CP_ENTRY_STRUCTURE_REF:
                println("CP_ENTRY_STRUCTURE_REF - ");
                break;
            case CP_ENTRY_TYPE_REF:
                println("CP_ENTRY_TYPE_REF - ");
                break;
            case CP_ENTRY_FORK_JOIN:
                println("CP_ENTRY_FORK_JOIN - ");
                break;
            case CP_ENTRY_WRKR_DATA_CHNL_REF:
                println("CP_ENTRY_WRKR_DATA_CHNL_REF - " + ((WorkerDataChannelRefCPEntry) cp).getUniqueName());
                break;
            case CP_ENTRY_BLOB:
                println("CP_ENTRY_BLOB - ");
                break;
            case CP_ENTRY_BYTE:
                println("CP_ENTRY_BYTE - ");
                break;
        }
    }

    private void emit(PackageInfo[] packages) {
        for (PackageInfo pkg : packages) {
            emit(pkg);
        }
    }

    private void emit(PackageInfo pkg) {
        println("Package - " + getStringCPValue(pkg.getConstPoolEntries(), pkg.orgNameCPIndex)
                + "/" + pkg.pkgPath + ":" + pkg.pkgVersion + " {");

        emit(pkg.getConstPoolEntries(), "\t");

        emit(pkg.getInstructions());

        emit(pkg.getFunctionInfoEntries());

        emit(pkg.getAttributeInfoEntries(), "\t");

        println("}");
    }


    private void emit(Instruction[] ins) {
        String tab = "\t";
        println(tab + "Instructions {");
        for (int i = 0; i < ins.length; i++) {
            println("\t\t" + i + " : " + ins[i].toString());
        }
        println(tab + "}");
    }

    private void emit(FunctionInfo[] funcs) {
        for (FunctionInfo f : funcs) {
            emit(f);
        }
    }

    private void emit(FunctionInfo f) {
        println("Function " + f.getName() + " {");
        String tabs = "\t";
        println(tabs + "flags - " + f.flags);
        emit(f.getAttributeInfoEntries(), tabs = tabs);
        println("}");
    }


    private void emit(AttributeInfo[] attrs, String tabs) {
        println(tabs + "Attributes {");
        for (AttributeInfo a : attrs) {
            emit(a, tabs + "\t");
        }
        println(tabs + "}");
    }


    private void emit(AttributeInfo attr, String tabs) {
        print(tabs + attr.getKind().toString());
        switch (attr.getKind()) {
            case LOCAL_VARIABLES_ATTRIBUTE:
                LocalVariableAttributeInfo localVarAttr = (LocalVariableAttributeInfo) attr;
                println(" {");
                for (LocalVariableInfo lv : localVarAttr.getLocalVariables()) {
                    println(tabs + "\t" + "name - " + lv.getVariableName());
                    println(tabs + "\t" + "type - " + lv.getVariableType().toString());
                    println(tabs + "\t" + "varIndex - " + lv.getVariableIndex());
                }
                println(tabs + "}");
                break;
            case PARAMETERS_ATTRIBUTE:
                ParameterAttributeInfo paramAttr = (ParameterAttributeInfo) attr;
                println(" {");
                println(tabs + "\t" + "requiredParamsCount - " + paramAttr.requiredParamsCount);
                println(tabs + "\t" + "defaultableParamsCount - " + paramAttr.defaultableParamsCount);
                println(tabs + "\t" + "restParamCount - " + paramAttr.restParamCount);
                println(tabs + "}");
                break;
            case VARIABLE_TYPE_COUNT_ATTRIBUTE:
                VarTypeCountAttributeInfo varTypeAttr = (VarTypeCountAttributeInfo) attr;
                println(" {");
                println(tabs + "\t" + "maxLongVars - " + varTypeAttr.getMaxLongVars());
                println(tabs + "\t" + "maxDoubleVars - " + varTypeAttr.getMaxDoubleVars());
                println(tabs + "\t" + "maxStringVars - " + varTypeAttr.getMaxStringVars());
                println(tabs + "\t" + "maxIntVars - " + varTypeAttr.getMaxIntVars());
                println(tabs + "\t" + "maxRefVars - " + varTypeAttr.getMaxRefVars());
                println(tabs + "}");
                break;
            case CODE_ATTRIBUTE:
                CodeAttributeInfo codeAttr = (CodeAttributeInfo) attr;
                println(" {");
                println(tabs + "\t" + "maxLongLocalVars - " + codeAttr.getMaxLongLocalVars());
                println(tabs + "\t" + "maxDoubleLocalVars - " + codeAttr.getMaxDoubleLocalVars());
                println(tabs + "\t" + "maxStringLocalVars - " + codeAttr.getMaxStringLocalVars());
                println(tabs + "\t" + "maxIntLocalVars - " + codeAttr.getMaxIntLocalVars());
                println(tabs + "\t" + "maxRefLocalVars - " + codeAttr.getMaxRefLocalVars());
                println();
                println(tabs + "\t" + "maxLongRegs - " + codeAttr.getMaxLongRegs());
                println(tabs + "\t" + "maxDoubleRegs - " + codeAttr.getMaxDoubleRegs());
                println(tabs + "\t" + "maxStringRegs - " + codeAttr.getMaxStringRegs());
                println(tabs + "\t" + "maxIntRegs - " + codeAttr.getMaxIntRegs());
                println(tabs + "\t" + "maxRefRegs - " + codeAttr.getMaxRefRegs());
                println();
                println(tabs + "\t" + "codeAddrs - " + codeAttr.getCodeAddrs());
                println(tabs + "}");
                break;
            case ERROR_TABLE:
                ErrorTableAttributeInfo errAttr = (ErrorTableAttributeInfo) attr;
                println(" {");
                for (ErrorTableEntry ete : errAttr.getErrorTableEntriesList()) {
                    println(tabs + "\t" + "ipFrom " + tabs + "\t" + "ipTo " +
                            tabs + "\t" + "ipTarget " + tabs + "\t" + "regIndex " + tabs + "\t" + ete.toString());
                }
                println(tabs + "}");
                break;
            case TAINT_TABLE:
            case ANNOTATIONS_ATTRIBUTE:
            case DEFAULT_VALUE_ATTRIBUTE:
            case LINE_NUMBER_TABLE_ATTRIBUTE:
            case PARAMETER_DEFAULTS_ATTRIBUTE:
            case DOCUMENT_ATTACHMENT_ATTRIBUTE:
            case PARAMETER_ANNOTATIONS_ATTRIBUTE:
                println();
                break;
        }
    }





    private String getStringCPValue(ConstantPoolEntry[] cps, int index) {
        ConstantPoolEntry cp = cps[index];
        switch (cp.getEntryType()) {
            case CP_ENTRY_UTF8:
                return ((UTF8CPEntry) cp).getValue();
            case CP_ENTRY_STRING:
                return ((StringCPEntry) cp).getValue();
        }
        return "Invalid CP Entry";
    }


    // Writer helper methods
    private void print(String value) {
        printStream.print(value);
    }


    private void println(String value) {
        printStream.println(value);
    }

    private void println() {
        printStream.println();
    }
}
