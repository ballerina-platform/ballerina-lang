// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/io;

public type BirEmitter object {

    private Package pkg;
    private TypeEmitter typeEmitter;
    private InstructionEmitter insEmitter;
    private TerminalEmitter termEmitter;
    private OperandEmitter opEmitter;
    private PositionEmitter posEmitter;

    public function init (Package pkg){
        self.pkg = pkg;
        self.typeEmitter = new;
        self.insEmitter = new;
        self.termEmitter = new;
        self.opEmitter = new;
        self.posEmitter= new;
    }


    public function emitPackage() {
        println("################################# Begin bir program #################################");
        println();
        println("package ", self.pkg.org.value, "/", self.pkg.name.value, ";");
        // println("version - " + pkg.versionValue);
        
        println(); // empty line
        println("// Import Declarations");
        self.emitImports();
        println();
        println("// Type Definitions");
        self.emitTypeDefs();
        println();
        println("// Global Variables");
        self.emitGlobalVars();
        println();
        println("// Function Definitions");
        self.emitFunctions(self.pkg.functions, "");
        println("################################## End bir program ##################################");
    }
    
    function emitImports() {
        foreach var i in self.pkg.importModules {
            println("import ", i.modOrg.value, "/", i.modName.value, " ", i.modVersion.value, ";");
        }
    }

    function emitTypeDefs() {
        foreach var bTypeDef in self.pkg.typeDefs {
            if (bTypeDef is TypeDef) {
                self.emitTypeDef(bTypeDef);
                println();
            }
        }
    }

    function emitTypeDef(TypeDef bTypeDef) {
        string visibility =  getVisibility(bTypeDef.flags);
        print(visibility, " type ", bTypeDef.name.value, " ");
        var typeValue = bTypeDef.typeValue;
        if (typeValue is BObjectType){
            emitObjectTypeWithFields(typeValue, self.typeEmitter, "");
            println();
            self.emitFunctions(bTypeDef.attachedFuncs ?: [], "\t");
            print("}");
        } else if (typeValue is BRecordType) {
            self.typeEmitter.emitRecordType(typeValue, "");
            println();
            self.emitFunctions(bTypeDef.attachedFuncs ?: [], "\t");
            print("}");
        } else {
            self.typeEmitter.emitType(bTypeDef.typeValue);
        }
        println(";");
    }

    function emitGlobalVars() {
        foreach var bGlobalVar in self.pkg.globalVars {
            if (bGlobalVar is GlobalVariableDcl) {
                print(getVisibility(bGlobalVar.flags), " ");
                self.typeEmitter.emitType(bGlobalVar.typeValue);
                println(" ", bGlobalVar.name.value, ";");
            }
        }
    }

    function emitFunctions(Function?[] funcs, string tabs) {
        foreach var bFunction in funcs {
            if (bFunction is Function) {
                self.emitFunction(bFunction, tabs);
                println();
            }
        }
    }

    function emitFunction(Function bFunction, string tabs) {
        self.posEmitter.emitPosition(bFunction.pos);
        string visibility =  getVisibility(bFunction.flags);
        print(tabs, visibility, " function ", bFunction.name.value, " ");
        self.typeEmitter.emitType(bFunction.typeValue);
        println(" {", (bFunction.flags & NATIVE) == NATIVE ? "\t// extern" :
                (bFunction.flags & INTERFACE) == INTERFACE ? "\t// interface" :"");
        int i = 0;
        foreach var v in bFunction.localVars {
            if v is FunctionParam {
                self.typeEmitter.emitType(v.typeValue, tabs = tabs + "\t");
                print(" ");
                print(v.name.value);
                if (!(v.kind is TempVarKind)) {
                    print(" %meta ");
                    print(v.meta?.name);
                }
                print("\t// ", v.kind);
                if (v.hasDefaultExpr) {
                    print("\t// defaultable -> ");
                    var bb = bFunction.paramDefaultBBs[i][0];
                    if (bb is BasicBlock) {
                        print(bb.id.value);
                    }
                    i = i + 1;
                }
                println();
                continue;
            }
            VariableDcl varDecl = getVariableDcl(v);
            self.typeEmitter.emitType(varDecl.typeValue, tabs = tabs + "\t");
            print(" ");
            if (varDecl.kind == VAR_KIND_RETURN) {
                print("%ret");
            } else {
                print(varDecl.name.value);
                print(" ");
                if (!(varDecl.kind is TempVarKind)) {
                    print("%meta ");
                    print(varDecl.meta?.name);
                }
                if (varDecl.kind is LocalVarKind) {
                    print(" %endBBID ");
                    print(varDecl.meta?.endBBID);
                    print(" %startBBID ");
                    print(varDecl.meta?.startBBID);
                    print(" %insOffset ");
                    print(varDecl.meta?.insOffset);
                }
            }
            println("\t// ", varDecl.kind);
        }
        println();// empty line
        i = 0;
        foreach var v in bFunction.localVars {
            if v is FunctionParam {
                if (v.hasDefaultExpr) {
                    var bb = bFunction.paramDefaultBBs[i];
                    foreach var b in bb {
                        if (b is BasicBlock) {
                            self.emitBasicBlock(b, tabs + "\t");
                            println();// empty line
                        }
                    }
                    i = i + 1;
                }
            }
        }
        foreach var b in bFunction.basicBlocks {
            if (b is BasicBlock) {
                self.emitBasicBlock(b, tabs + "\t");
                println();// empty line
            }
        }
        if (bFunction.errorEntries.length() > 0 ) {
            println("\t\t\tError Table \n\t\t\tBB\t\t|  errorOp\t| targetBB");
        }
        foreach var e in bFunction.errorEntries {
            if (e is ErrorEntry) {
                self.emitErrorEntry(e);
                println();// empty line
            }
        }
        if (bFunction.workerChannels.length() > 0) {
            print("WORKER_CHANNELS: ");
        }

        int channelsSize = bFunction.workerChannels.length();
        foreach ChannelDetail ch in bFunction.workerChannels {
            channelsSize -= 1;
            print(ch.name.value);
            if (channelsSize > 0) {
                print(",");
            } else {
                println(";");
            }
        }
        println(tabs, "}");
    }

    function emitBasicBlock(BasicBlock bBasicBlock, string tabs) {
        println(tabs, bBasicBlock.id.value, " {");
        foreach var ins in bBasicBlock.instructions {
            if (ins is Instruction) {
                self.insEmitter.emitIns(ins, tabs = tabs);
            }
        }
        self.termEmitter.emitTerminal(bBasicBlock.terminator, tabs = tabs);
        println(tabs, "}");
    }

    function emitErrorEntry(ErrorEntry errorEntry) {
        print("\t\t\t");
        print(errorEntry.trapBB.id.value);
        print("\t\t| ");
        self.opEmitter.emitOp(errorEntry.errorOp);
        print("\t\t| ");
        print(errorEntry.targetBB.id.value);
    }
};

type InstructionEmitter object {
    private OperandEmitter opEmitter;
    private TypeEmitter typeEmitter;
    private PositionEmitter posEmitter;

    function init() {
        self.opEmitter = new;
        self.typeEmitter = new;
        self.posEmitter = new;
    }

    function emitIns(Instruction ins, string tabs = "") {
        self.posEmitter.emitPosition(ins.pos);
        if (ins is FieldAccess) {
            print(tabs);
            self.opEmitter.emitOp(ins.lhsOp);
            if (ins.kind == INS_KIND_MAP_STORE || ins.kind == INS_KIND_ARRAY_STORE) {
                print("[");
                self.opEmitter.emitOp(ins.keyOp);
                print("] = ", ins.kind, " ");
                self.opEmitter.emitOp(ins.rhsOp);
            } else {
                print(" = ", ins.kind, " ");
                self.opEmitter.emitOp(ins.rhsOp);
                print("[");
                self.opEmitter.emitOp(ins.keyOp);
                print("]");
            }
            println(";");
        } else if (ins is BinaryOp) {
            print(tabs);
            self.opEmitter.emitOp(ins.lhsOp);
            print(" = ", ins.kind, " ");
            self.opEmitter.emitOp(ins.rhsOp1);
            print(" ");
            self.opEmitter.emitOp(ins.rhsOp2);
            println(";");
        }  else if (ins is UnaryOp) {
            print(tabs);
            self.opEmitter.emitOp(ins.lhsOp);
            print(" = ", ins.kind, " ");
            self.opEmitter.emitOp(ins.rhsOp);
            println(";");
        } else if (ins is Move) {
            print(tabs);
            self.opEmitter.emitOp(ins.lhsOp);
            print(" = ", ins.kind, " ");
            self.opEmitter.emitOp(ins.rhsOp);
            println(";");
        } else if (ins is ConstantLoad) {
            print(tabs);
            self.opEmitter.emitOp(ins.lhsOp);
            print(" = ", ins.kind, " <");
            self.typeEmitter.emitType(ins.typeValue);
            println("> ", ins.value, ";");
        } else if (ins is NewArray) {
            print(tabs);
            self.opEmitter.emitOp(ins.lhsOp);
            print(" = ", ins.kind, " [");
            self.opEmitter.emitOp(ins.sizeOp);
            println("];");
        } else if (ins is NewMap) {
            print(tabs);
            self.opEmitter.emitOp(ins.lhsOp);
            print(" = ", ins.kind, " ");
            self.typeEmitter.emitType(ins.bType);
            println(";");
        } else if (ins is NewTable) {
            print(tabs);
            self.opEmitter.emitOp(ins.lhsOp);
            print(" = ", ins.kind, " ");
            self.typeEmitter.emitType(ins.typeValue);
            print(", ");
            self.opEmitter.emitOp(ins.columnsOp);
            print(", ");
            self.opEmitter.emitOp(ins.dataOp);
            print(", ");
            self.opEmitter.emitOp(ins.keyColOp);
            println(";");
        } else if (ins is NewInstance) {
            print(tabs);
            self.opEmitter.emitOp(ins.lhsOp);
            print(" = ", ins.kind, " ");
            var typeDefRef = ins.typeDefRef;
            if (typeDefRef is TypeDef) {
                print(typeDefRef.name.value);
            } else {
                print(typeDefRef.externalPkg.org);
                print("/");
                print(typeDefRef.externalPkg.name);
                print(" ");

                print(typeDefRef.name.value);
            }
            println(";");
        } else if (ins is NewError) {
            print(tabs);
            self.opEmitter.emitOp(ins.lhsOp);
            print(" = ", ins.kind, " ");
            self.opEmitter.emitOp(ins.reasonOp);
            print(" ");
            self.opEmitter.emitOp(ins.detailsOp);
            println(";");
        } else if (ins is TypeCast) {
            print(tabs);
            self.opEmitter.emitOp(ins.lhsOp);
            print(" = ", ins.kind, " (");
            self.typeEmitter.emitType(ins.castType);
            print(" )");
            self.opEmitter.emitOp(ins.rhsOp);
            println(";");
        } else if (ins is IsLike) {
            print(tabs);
            self.opEmitter.emitOp(ins.lhsOp);
            print(" = ");
            self.opEmitter.emitOp(ins.rhsOp);
            print(" ", ins.kind, " ");
            self.typeEmitter.emitType(ins.typeVal);
            println(";");
        } else if (ins is FPLoad) {
            print(tabs);
            self.opEmitter.emitOp(ins.lhsOp);
            print(" = ");
            print(ins.kind, " ");
            print(ins.pkgID.org, "/", ins.pkgID.name, "::", ins.pkgID.modVersion, ":", ins.name.value, "(");

            foreach var v in ins.closureMaps {
                if (v is VarRef) {
                    self.opEmitter.emitOp(v);
                    print(",");
                }
            }
            int i = 0;
            foreach var v in ins.params {
                if (i != 0) {
                    print (",");
                }
                VariableDcl varDecl = getVariableDcl(v);
                self.typeEmitter.emitType(varDecl.typeValue);
                i += 1;
            }
            println(");");
        }
    }
};

type TerminalEmitter object {
    private OperandEmitter opEmitter;
    private PositionEmitter posEmitter;

    function init() {
        self.opEmitter = new;
        self.posEmitter = new;
    }

    function emitTerminal(Terminator term, string tabs = "") {
        self.posEmitter.emitPosition(term.pos);
        if (term is Call) {
            print(tabs);
            VarRef? lhsOp = term.lhsOp;
            if (lhsOp is VarRef) {
                self.opEmitter.emitOp(lhsOp);
                print(" = ");
            }
            print(term.pkgID.org, "/", term.pkgID.name, "::", term.pkgID.modVersion, ":", term.name.value, "(");
            int i = 0;
            foreach var arg in term.args {
                if (arg is VarRef) {
                    if (i != 0) {
                        print(", ");
                    }
                    self.opEmitter.emitOp(arg);
                    i = i + 1;
                }
            }
            println(") -> ", term.thenBB.id.value, ";");
        } else if (term is Branch) {
            print(tabs, "branch ");
            self.opEmitter.emitOp(term.op);
            println(" [true:", term.trueBB.id.value, ", false:", term.falseBB.id.value,"];");
        } else if (term is GOTO) {
            println(tabs, "goto ", term.targetBB.id.value, ";");
        } else if (term is Panic) {
            print(tabs, "panic ");
            self.opEmitter.emitOp(term.errorOp);
            println(";");
        } else if (term is Wait) {
            print(tabs);
            self.opEmitter.emitOp(term.lhsOp);
            print(" = ");
            print(term.kind, " ");
            int i = 0;
            foreach var expr in term.exprList {
                if (i != 0) {
                    print("|");
                }
                if (expr is VarRef) {
                    self.opEmitter.emitOp(expr);
                }
                i = i + 1;
            }
            println(";");
        } else if (term is WaitAll) {
            print(tabs);
            self.opEmitter.emitOp(term.lhsOp);
            print(" = ");
            print(term.kind, " ");
            print("{");
            int i = 0;
            while (i < term.keys.length()) {
                if (i != 0) {
                    print(",");
                }
                print(term.keys[i], ":");
                VarRef? expr = term.futures[i];
                if (expr is VarRef) {
                    self.opEmitter.emitOp(expr);
                }
                i = i + 1;
            }
            println("} -> ", term.thenBB.id.value, ";");
        } else if (term is Flush) {
            print(tabs);
            self.opEmitter.emitOp(term.lhsOp);
            print(" = ");
            print(term.kind, " ");
            int i = 0;
            foreach var detail in term.workerChannels {
                if (i != 0) {
                    print(",");
                }
                print(detail.name);
                i += 1;
            }
            println(";");
        } else if (term is WorkerReceive) {
            print(tabs);
            self.opEmitter.emitOp(term.lhsOp);
            print(" = ");
            print(term.kind, " ");
            print(term.channelName.value);
            println(";");
        } else if (term is WorkerSend) {
            print(tabs);
            if (term.isSync) {
                VarRef? ref = term.lhsOp;
                if (ref is VarRef) {
                    self.opEmitter.emitOp(ref);
                    print(" = ");
                }
            }
            self.opEmitter.emitOp(term.dataOp);
            print(" ", term.kind, " ");
            print(term.channelName.value);
            println(";");
        } else if (term is AsyncCall) {
            print(tabs);
            VarRef? lhsOp = term.lhsOp;
            if (lhsOp is VarRef) {
                self.opEmitter.emitOp(lhsOp);
                print(" = ");
            }
            print("START ");
            print(term.pkgID.org, "/", term.pkgID.name, "::", term.pkgID.modVersion, ":", term.name.value, "(");
            int i = 0;
            foreach var arg in term.args {
                if (arg is VarRef) {
                    if (i != 0) {
                        print(", ");
                    }
                    self.opEmitter.emitOp(arg);
                    i = i + 1;
                }
            }
            println(") -> ", term.thenBB.id.value, ";");
        } else if (term is FPCall) {
            print(tabs);
            VarRef? lhsOp = term.lhsOp;
            if (lhsOp is VarRef) {
                self.opEmitter.emitOp(lhsOp);
                print(" = ");
            }
            if (term.isAsync) {
                print("START ");
            }
            print(term.kind, " ");
            self.opEmitter.emitOp(term.fp);
            print("(");
            int i = 0;
            foreach var arg in term.args {
              if (arg is VarRef) {
                  if (i != 0) {
                      print(", ");
                    }
                  self.opEmitter.emitOp(arg);
                  i = i + 1;
                }
            }
            println(") -> ", term.thenBB.id.value, ";");
        } else if(term is Lock) {
            println(tabs, "lock -> ", term.lockBB.id.value, ";");
        } else if(term is Unlock) {
            println(tabs, "unLock -> ", term.unlockBB.id.value, ";");
        } else { //if (term is Return) {
            println(tabs, "return;");
        }
    }
};

type OperandEmitter object {
    function emitOp(VarRef op, string tabs = "") {
        if (op.variableDcl.name.value == "%0") {
            print("%ret");
        } else {
            print(op.variableDcl.name.value);
        }
        // TODO add the rest, currently only have var ref
    }
};

type TypeEmitter object {

    function emitType(BType typeVal, string tabs = "") {
        if (typeVal is BTypeAny || typeVal is BTypeInt || typeVal is BTypeString || typeVal is BTypeBoolean
                || typeVal is BTypeFloat || typeVal is BTypeByte || typeVal is BTypeAnyData || typeVal is BTypeNone
                || typeVal is BServiceType || typeVal is BTypeDecimal) {
            print(tabs, typeVal);
        } else if (typeVal is BRecordType) {
            self.emitRecordType(typeVal, tabs);
        } else if (typeVal is BObjectType) {
            self.emitObjectType(typeVal, tabs);
        } else if (typeVal is BInvokableType) {
            self.emitInvokableType(typeVal, tabs);
        } else if (typeVal is BArrayType) {
            self.emitArrayType(typeVal, tabs);
        } else if (typeVal is BUnionType) {
            self.emitUnionType(typeVal, tabs);
        } else if (typeVal is BTupleType) {
            self.emitTupleType(typeVal, tabs);
        } else if (typeVal is BMapType) {
            self.emitMapType(typeVal, tabs);
        } else if (typeVal is BTableType) {
            self.emitTableType(typeVal, tabs);
        } else if (typeVal is BFutureType) {
            self.emitFutureType(typeVal, tabs);
        } else if (typeVal is BTypeNil) {
            print(tabs + "()");
        } else if (typeVal is BFiniteType) {
            print(tabs + typeVal.name.value);
        } else if (typeVal is BErrorType) {
            //self.emitErrorType(typeVal, tabs);
        }
    }

    function emitRecordType(BRecordType bRecordType, string tabs) {
        print(tabs);
        if (bRecordType.sealed) {
            print("sealed ");
        }
        println("record { ");
        foreach var f in bRecordType.fields {
            BRecordField recField = getRecordField(f);
            self.emitType(recField.typeValue, tabs = tabs + "\t");
            println(" ", recField.name.value, ";");
        }
        self.emitType(bRecordType.restFieldType, tabs = tabs + "\t");
        println("...", ";");
        print(tabs, "}");
    }

    function emitObjectType(BObjectType bObjectType, string tabs) {
        emitObjectTypeWithFields(bObjectType, self, tabs);
        print(tabs, "}");
    }

    function emitInvokableType(BInvokableType bInvokableType, string tabs) {
        print(tabs, "(");
        // int pCount = bInvokableType.paramTypes.size();
        int i = 0;
        foreach var p in bInvokableType.paramTypes {
            BType pType = getType(p);
            if (i != 0) {
                print(", ");
            }
            self.emitType(pType);
            i = i + 1;
        }
        print(") -> ");
        self.emitType(<BType> bInvokableType?.retType);
    }

    function emitArrayType(BArrayType bArrayType, string tabs) {
        print(tabs);
        self.emitType(bArrayType.eType);
        print("<", bArrayType.state, ">");
        print("[]");
    }

    function emitUnionType(BUnionType bUnionType, string tabs) {
        int i = 0;
        string tabst = tabs;
        foreach var t in bUnionType.members {
            BType mType = getType(t);
            if (i != 0) {
                print(" | ");
                tabst = "";
            }
            self.emitType(mType, tabs = tabst);
            i = i + 1;
        }
    }

    function emitTupleType(BTupleType bUnionType, string tabs) {
        int i = 0;
        print(tabs, "(");
        foreach var t in bUnionType.tupleTypes {
            BType tType = getType(t);
            if (i != 0) {
                print(", ");
            }
            self.emitType(tType);
            i = i + 1;
        }
        print(")");
    }

    function emitMapType(BMapType bMapType, string tabs) {
        print(tabs, "map<");
        self.emitType(bMapType.constraint);
        print(">");
    }

    function emitTableType(BTableType bTableType, string tabs) {
        print(tabs, "table<");
        self.emitType(bTableType.tConstraint);
        print(">");
    }

    function emitFutureType(BFutureType bFutureType, string tabs) {
        print(tabs, "future<");
        self.emitType(bFutureType.returnType);
        print(">");
    }

    function emitErrorType(BErrorType bErrorType, string tabs) {
        print(tabs, "error{r-");
        self.emitType(bErrorType.reasonType);
        print(", d-");
        self.emitType(bErrorType.detailType);
        print("}");
    }
};

type PositionEmitter object {
    function emitPosition(DiagnosticPos pos) {
        if (pos.sLine != 2147483648) {
            self.appendPos(pos.sLine);
            print("-");
            self.appendPos(pos.eLine);
            print(":");
            self.appendPos(pos.sCol);
            print("-");
            self.appendPos(pos.eCol);
        }
    }

     function appendPos(int line) {
        if (line < 10) {
            print("0");
        }
        print(line);
    }
};

function emitObjectTypeWithFields(BObjectType bObjectType, TypeEmitter typeEmitter, string tabs) {
    println(tabs, bObjectType.isAbstract ? "abstract " : "", "object {");
    foreach var f in bObjectType.fields {
        if (f is BObjectField) {
            string visibility = getVisibility(f.flags);
            print(tabs + "\t", visibility, " ");
            typeEmitter.emitType(f.typeValue);
            println(" ", f.name.value, ";");
        }
    }
}


function println(any... vals) {
    io:println(...vals);
}

function print(any... vals) {
    error? e = trap io:print(...vals);
    if (e is error) {
        io:print("Warning: Print failed:", e.reason());
    }

}

public function getVisibility(int flags) returns string {
    if ((flags & PRIVATE) == PRIVATE) {
        return "private";
    } else if ((flags & PUBLIC) == PUBLIC) {
        return "pubilic";
    } else if ((flags & OPTIONAL) == OPTIONAL) {
        return "optional";
    }
    return "package private";
}
