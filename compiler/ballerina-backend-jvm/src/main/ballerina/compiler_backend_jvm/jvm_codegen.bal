import ballerina/io;
import ballerina/bir;
import ballerina/jvm;
import ballerina/reflect;

BalToJVMIndexMap indexMap = new;
int returnVarRefIndex = -1;
string currentFuncName = "";
string currentBBName = "";
string className = "DEFAULT";

bir:Function currentFunc = {};

public function main(string... args) {
    //do nothing
}

function generateJVMExecutable(byte[] birBinary, string progName) returns JarFile {
    className = progName;
    io:ReadableByteChannel byteChannel = io:createReadableChannel(birBinary);
    bir:ChannelReader reader = new(byteChannel);
    checkValidBirChannel(reader);
    bir:ConstPoolParser cpParser = new(reader);
    bir:BirChannelReader birReader = new(reader, cpParser.parse());
    bir:TypeParser typeParser = new (birReader);
    bir:PackageParser pkgParser = new(birReader, typeParser);
    bir:Package pkg = pkgParser.parsePackage();
    return generateJarFile(pkg);
}

function generateJarFile(bir:Package pkg) returns JarFile {
    //todo : need to generate java package here based on BIR package(s)
    jvm:classWriterInit();
    jvm:classWriterVisit(className);

    map<byte[]> jarEntries = {};
    map<string> manifestEntries = {};

    bir:Function? mainFunc = getMainFunc(pkg.functions);
    if (mainFunc is bir:Function) {
        generateMainMethod(mainFunc);
        manifestEntries["Main-Class"] = className;
    }

    generateMethods(pkg.functions);
    jvm:classWriterEnd();
    byte[] classContent = jvm:getClassFileContent();

    jarEntries[className + ".class"] = classContent;

    JarFile jarFile = {jarEntries : jarEntries, manifestEntries : manifestEntries};
    return jarFile;
}

function generateMethods(bir:Function[] funcs) {
    foreach var func in funcs {
        indexMap = new ();
        currentFunc = untaint func;
        generateMethodDesc(func);
        generateMethodBody(func);
    }
}

function getMainFunc(bir:Function[] funcs) returns bir:Function? {
    bir:Function? userMainFunc = ();
    foreach var func in funcs {
        if(func.name.value == "main") {
            userMainFunc = untaint func;
            break;
        }
    }

    return userMainFunc;
}


function generateMethodDesc(bir:Function func) {
    currentFuncName = untaint func.name.value;
    string desc = getMethodDesc(func);
    jvm:visitMethodInit(ACC_PUBLIC + ACC_STATIC, currentFuncName, desc);
}

function getMethodDesc(bir:Function func) returns string {
    string desc = "(";
    int i = 0;
    while (i < func.argsCount) {
        desc = desc + getFunctionArgDesc(func.typeValue.paramTypes[i]);
        i = i + 1;
    }

    string returnType = generateReturnType(func.typeValue.retType);
    return desc + returnType;
}

function getFunctionArgDesc(bir:BType bType) returns string {
    if (bType is bir:BTypeInt) {
        return "J";
    } else if (bType is bir:BTypeString) {
        return "Ljava/lang/String;";
    } else {
        error err = error( "JVM generation is not supported for type " + io:sprintf("%s", bType));
        panic err;
    }
}

function generateMethodBody(bir:Function func) {
    jvm:visitMethodCode();

    // body visit
    int i = 0;
    int k = 1;
    boolean isVoidFunc = false;
    if (func.typeValue.retType is bir:BTypeNil) {
        isVoidFunc = true;
        k = 0;
    }

    bir:VariableDcl[] localVars = func.localVars;
    while (k < localVars.length()) {
        bir:VariableDcl localVar = localVars[k];
        _ = getJVMIndexOfVarRef(localVar);
        k = k + 1;
    }

    if (!isVoidFunc) {
        returnVarRefIndex = getJVMIndexOfVarRef(localVars[0]);
    }

    bir:BasicBlock[] basicBlocks = func.basicBlocks;
    while (i < basicBlocks.length()) {
        bir:BasicBlock bb = basicBlocks[i];
        //io:println("Basic Block Is : ", bb.id.value);
        currentBBName = io:sprintf("%s", bb.id.value);

        // create jvm label
        jvm:visitLabel(currentFuncName + bb.id.value);

        // visit instructions
        int j = 0;
        while (j < bb.instructions.length()) {
            bir:Instruction inst = bb.instructions[j];
            if (inst is bir:ConstantLoad) {
                visitConstantLoadIns(inst);
            } else if (inst is bir:Move) {
                visitMoveIns(inst);
            } else if (inst is bir:BinaryOp) {
                visitBinaryOpIns(inst);
            } else {
                error err = error( "JVM generation is not supported for operation " + io:sprintf("%s", inst));
                panic err;
            }

            j = j + 1;
        }

        // visit terminator
        visitTerminator(bb);
        i = i + 1;
    }

    jvm:visitMaxStackValues(100, 400);
    jvm:visitMethodEnd();
}

function visitConstantLoadIns(bir:ConstantLoad loadIns) {
    bir:BType bType = loadIns.typeValue;

    if (bType is bir:BTypeInt) {
        any val = loadIns.value;
        jvm:visitLoadConstantInstruction(val);

        //store
        int index = getJVMIndexOfVarRef(loadIns.lhsOp.variableDcl);
        //io:println("Const Store Index is :::::::::::", index);
        jvm:visitVariableInstruction(LSTORE, index);
    } else if (bType is bir:BTypeString) {
        any val = loadIns.value;
        jvm:visitLoadConstantInstruction(val);

        //store
        int index = getJVMIndexOfVarRef(loadIns.lhsOp.variableDcl);
        //io:println("Const Store Index is :::::::::::", index);
        jvm:visitVariableInstruction(ASTORE, index);
    } else {
        error err = error( "JVM generation is not supported for type : " + io:sprintf("%s", bType));
        panic err;
    }
}

function visitMoveIns(bir:Move moveIns) {
    int rhsIndex = getJVMIndexOfVarRef(moveIns.rhsOp.variableDcl);
    //io:println("RHS Index is :::::::::::", rhsIndex);
    int lhsLndex = getJVMIndexOfVarRef(moveIns.lhsOp.variableDcl);
    //io:println("LHS Index is :::::::::::", lhsLndex);

    bir:BType bType = moveIns.rhsOp.typeValue;

    if (bType is bir:BTypeInt) {
        jvm:visitVariableInstruction(LLOAD, rhsIndex);
        jvm:visitVariableInstruction(LSTORE, lhsLndex);
    } else if (bType is bir:BTypeBoolean) {
        jvm:visitVariableInstruction(ILOAD, rhsIndex);
        jvm:visitVariableInstruction(ISTORE, lhsLndex);
    } else if (bType is bir:BTypeString) {
        jvm:visitVariableInstruction(ALOAD, rhsIndex);
        jvm:visitVariableInstruction(ASTORE, lhsLndex);
    } else {
        error err = error( "JVM generation is not supported for type " +
                                    io:sprintf("%s", moveIns.rhsOp.typeValue));
        panic err;
    }
}

function visitBinaryOpIns(bir:BinaryOp binaryIns) {
    if (binaryIns.kind is bir:LESS_THAN) {
        visitLessThanIns(binaryIns);
    } else if (binaryIns.kind is bir:ADD) {
        visitAddIns(binaryIns);
    } else if (binaryIns.kind is bir:EQUAL) {
        visitEqualIns(binaryIns);
    } else if (binaryIns.kind is bir:SUB) {
        visitSubIns(binaryIns);
    } else if (binaryIns.kind is bir:DIV) {
        visitDivIns(binaryIns);
    } else if (binaryIns.kind is bir:MUL) {
        visitMulIns(binaryIns);
    } else if (binaryIns.kind is bir:AND) {
        visitAndIns(binaryIns);
    } else if (binaryIns.kind is bir:OR) {
        visitOrIns(binaryIns);
    } else if (binaryIns.kind is bir:LESS_EQUAL) {
        visitLessEqualIns(binaryIns);
    } else {
        error err = error("JVM generation is not supported for type : " + io:sprintf("%s", binaryIns.kind));
        panic err;
    }
}

function visitBinaryRhsAndLhsLoad(bir:BinaryOp binaryIns) {
    int rhsOps1Index = getJVMIndexOfVarRef(binaryIns.rhsOp1.variableDcl);
    jvm:visitVariableInstruction(LLOAD, rhsOps1Index);

    int rhsOps2Index = getJVMIndexOfVarRef(binaryIns.rhsOp2.variableDcl);
    jvm:visitVariableInstruction(LLOAD, rhsOps2Index);
}

function visitLessThanIns(bir:BinaryOp binaryIns) {
    bir:VarRef lhsOp = binaryIns.lhsOp;
    visitBinaryRhsAndLhsLoad(binaryIns);
    int lhsOpIndex = getJVMIndexOfVarRef(lhsOp.variableDcl);

    string label1 = currentFuncName + currentBBName + io:sprintf("%s", lhsOp.variableDcl) + "01";
    string label2 = currentFuncName + currentBBName + io:sprintf("%s", lhsOp.variableDcl) + "02";

    jvm:createLabel(label1);
    jvm:createLabel(label2);

    jvm:visitNoOperandInstruction(LCMP);
    jvm:visitJumpInstruction(LESS_THAN_ZERO, label1);

    jvm:visitNoOperandInstruction(ICONST_0);
    jvm:visitJumpInstruction(JUMP, label2);

    jvm:visitLabel(label1);
    jvm:visitNoOperandInstruction(ICONST_1);

    jvm:visitLabel(label2);
    jvm:visitVariableInstruction(ISTORE, lhsOpIndex);
}

function visitLessEqualIns(bir:BinaryOp binaryIns) {
    bir:VarRef lhsOp = binaryIns.lhsOp;
    visitBinaryRhsAndLhsLoad(binaryIns);
    int lhsOpIndex = getJVMIndexOfVarRef(lhsOp.variableDcl);

    string label1 = currentFuncName + currentBBName + io:sprintf("%s", lhsOp.variableDcl) + "01";
    string label2 = currentFuncName + currentBBName + io:sprintf("%s", lhsOp.variableDcl) + "02";

    jvm:createLabel(label1);
    jvm:createLabel(label2);

    jvm:visitNoOperandInstruction(LCMP);
    jvm:visitJumpInstruction(LESS_THAN_EQUAL_ZERO, label1);

    jvm:visitNoOperandInstruction(ICONST_0);
    jvm:visitJumpInstruction(JUMP, label2);

    jvm:visitLabel(label1);
    jvm:visitNoOperandInstruction(ICONST_1);

    jvm:visitLabel(label2);
    jvm:visitVariableInstruction(ISTORE, lhsOpIndex);
}

function visitEqualIns(bir:BinaryOp binaryIns) {
    bir:VarRef lhsOp = binaryIns.lhsOp;
    visitBinaryRhsAndLhsLoad(binaryIns);
    int lhsOpIndex = getJVMIndexOfVarRef(lhsOp.variableDcl);

    string label1 = currentFuncName + currentBBName + io:sprintf("%s", lhsOp.variableDcl) + "01";
    string label2 = currentFuncName + currentBBName + io:sprintf("%s", lhsOp.variableDcl) + "02";

    jvm:createLabel(label1);
    jvm:createLabel(label2);

    jvm:visitNoOperandInstruction(LCMP);
    jvm:visitJumpInstruction(NOT_EQUAL_TO_ZERO, label1);

    jvm:visitNoOperandInstruction(ICONST_1);
    jvm:visitJumpInstruction(JUMP, label2);

    jvm:visitLabel(label1);
    jvm:visitNoOperandInstruction(ICONST_0);

    jvm:visitLabel(label2);
    jvm:visitVariableInstruction(ISTORE, lhsOpIndex);
}

function visitAddIns(bir:BinaryOp binaryIns) {
    //io:println("ADD Ins " + io:sprintf("%s", binaryIns));

    bir:BType bType = binaryIns.lhsOp.typeValue;

    if (bType is bir:BTypeInt) {
        bir:VarRef lhsOp = binaryIns.lhsOp;
        visitBinaryRhsAndLhsLoad(binaryIns);
        int lhsOpIndex = getJVMIndexOfVarRef(lhsOp.variableDcl);

        jvm:visitNoOperandInstruction(LADD);
        jvm:visitVariableInstruction(LSTORE, lhsOpIndex);
    } else if (bType is bir:BTypeString) {

        int rhsOps1Index = getJVMIndexOfVarRef(binaryIns.rhsOp1.variableDcl);
        jvm:visitVariableInstruction(ALOAD, rhsOps1Index);

        int rhsOps2Index = getJVMIndexOfVarRef(binaryIns.rhsOp2.variableDcl);
        jvm:visitVariableInstruction(ALOAD, rhsOps2Index);
        jvm:visitMethodInstruction(INVOKEVIRTUAL, "java/lang/String", "concat",
                                     "(Ljava/lang/String;)Ljava/lang/String;", false);

        bir:VarRef lhsVarRef = binaryIns.lhsOp;
        int lhsIndex = getJVMIndexOfVarRef(lhsVarRef.variableDcl);
        jvm:visitVariableInstruction(ASTORE, lhsIndex);
    } else {
        error err = error( "JVM generation is not supported for type " +
                        io:sprintf("%s", binaryIns.lhsOp.typeValue));
        panic err;
    }
}

function visitSubIns(bir:BinaryOp binaryIns) {
    bir:VarRef lhsOp = binaryIns.lhsOp;
    visitBinaryRhsAndLhsLoad(binaryIns);
    int lhsOpIndex = getJVMIndexOfVarRef(lhsOp.variableDcl);

    jvm:visitNoOperandInstruction(LSUB);
    jvm:visitVariableInstruction(LSTORE, lhsOpIndex);
}

function visitDivIns(bir:BinaryOp binaryIns) {
    bir:VarRef lhsOp = binaryIns.lhsOp;
    visitBinaryRhsAndLhsLoad(binaryIns);
    //io:println("DIV ins : " + io:sprintf("%s", lhsOp));
    int lhsOpIndex = getJVMIndexOfVarRef(lhsOp.variableDcl);

    jvm:visitNoOperandInstruction(LDIV);
    jvm:visitVariableInstruction(LSTORE, lhsOpIndex);
}

function visitMulIns(bir:BinaryOp binaryIns) {
    bir:VarRef lhsOp = binaryIns.lhsOp;
    visitBinaryRhsAndLhsLoad(binaryIns);
    //io:println("DIV ins : " + io:sprintf("%s", lhsOp));
    int lhsOpIndex = getJVMIndexOfVarRef(lhsOp.variableDcl);

    jvm:visitNoOperandInstruction(LMUL);
    jvm:visitVariableInstruction(LSTORE, lhsOpIndex);
}

function visitAndIns(bir:BinaryOp binaryIns) {
    // ILOAD
    // ICONST_1
    // IF_ICMPNE L0
    // ILOAD
    // ICONST_1
    // IF_ICMPNE L0
    // ICONST_1
    // ISTORE

    bir:VarRef lhsOp = binaryIns.lhsOp;

    //io:println("AND ins : " + io:sprintf("%s", binaryIns));

    string label1 = currentFuncName + currentBBName + io:sprintf("%s", lhsOp.variableDcl) + "01";
    string label2 = currentFuncName + currentBBName + io:sprintf("%s", lhsOp.variableDcl) + "02";

    jvm:createLabel(label1);
    jvm:createLabel(label2);

    int rhsOps1Index = getJVMIndexOfVarRef(binaryIns.rhsOp1.variableDcl);
    jvm:visitVariableInstruction(ILOAD, rhsOps1Index);

    jvm:visitNoOperandInstruction(ICONST_1);
    jvm:visitJumpInstruction(IF_NOT_EQUAL, label1);

    int rhsOps2Index = getJVMIndexOfVarRef(binaryIns.rhsOp2.variableDcl);
    jvm:visitVariableInstruction(ILOAD, rhsOps2Index);

    jvm:visitNoOperandInstruction(ICONST_1);
    jvm:visitJumpInstruction(IF_NOT_EQUAL, label1);

    jvm:visitNoOperandInstruction(ICONST_1);
    jvm:visitJumpInstruction(JUMP, label2);

    jvm:visitLabel(label1);
    jvm:visitNoOperandInstruction(ICONST_0);

    jvm:visitLabel(label2);

    int lhsOpIndex = getJVMIndexOfVarRef(lhsOp.variableDcl);
    jvm:visitVariableInstruction(ISTORE, lhsOpIndex);
}

function visitOrIns(bir:BinaryOp binaryIns) {
    // ILOAD
    // ICONST_1
    // IF_ICMPNE L0
    // ILOAD
    // ICONST_1
    // IF_ICMPNE L0
    // ICONST_1
    // ISTORE

    bir:VarRef lhsOp = binaryIns.lhsOp;

    //io:println("OR ins : " + io:sprintf("%s", binaryIns));

    string label1 = currentFuncName + currentBBName + io:sprintf("%s", lhsOp.variableDcl) + "01";
    string label2 = currentFuncName + currentBBName + io:sprintf("%s", lhsOp.variableDcl) + "02";

    jvm:createLabel(label1);
    jvm:createLabel(label2);

    int rhsOps1Index = getJVMIndexOfVarRef(binaryIns.rhsOp1.variableDcl);
    jvm:visitVariableInstruction(ILOAD, rhsOps1Index);

    jvm:visitNoOperandInstruction(ICONST_1);
    jvm:visitJumpInstruction(IF_EQUAL, label1);

    int rhsOps2Index = getJVMIndexOfVarRef(binaryIns.rhsOp2.variableDcl);
    jvm:visitVariableInstruction(ILOAD, rhsOps2Index);

    jvm:visitNoOperandInstruction(ICONST_1);
    jvm:visitJumpInstruction(IF_EQUAL, label1);

    jvm:visitNoOperandInstruction(ICONST_0);
    jvm:visitJumpInstruction(JUMP, label2);

    jvm:visitLabel(label1);
    jvm:visitNoOperandInstruction(ICONST_1);

    jvm:visitLabel(label2);

    int lhsOpIndex = getJVMIndexOfVarRef(lhsOp.variableDcl);
    jvm:visitVariableInstruction(ISTORE, lhsOpIndex);
}

function visitTerminator(bir:BasicBlock bb) {
    var termIns = bb.terminator;

    if (termIns is bir:GOTO) {
        genGoToTerm(termIns);
    } else if (termIns is bir:Call) {
        genCallTerm(termIns);
    } else if (termIns is bir:Branch) {
        genBranchTerm(termIns);
    } else {
        genReturnTerm(termIns);
    }
}

function genGoToTerm(bir:GOTO gotoIns) {
    jvm:visitJumpInstruction(JUMP, currentFuncName + gotoIns.targetBB.id.value);
}

function genReturnTerm(bir:Return returnIns) {
    if (currentFunc.typeValue.retType is bir:BTypeNil) {
        jvm:visitNoOperandInstruction(RETURN);
    } else {
        bir:BType bType = currentFunc.typeValue.retType;
        if (bType is bir:BTypeInt) {
            jvm:visitVariableInstruction(LLOAD, returnVarRefIndex);
            jvm:visitNoOperandInstruction(LRETURN);
        } else if (bType is bir:BTypeString) {
            jvm:visitVariableInstruction(ALOAD, returnVarRefIndex);
            jvm:visitNoOperandInstruction(ARETURN);
        } else {
            error err = error( "JVM generation is not supported for type " +
                            io:sprintf("%s", currentFunc.typeValue.retType));
            panic err;
        }
    }
}

function genBranchTerm(bir:Branch branchIns) {
    string trueBBId = branchIns.trueBB.id.value;
    string falseBBId = branchIns.falseBB.id.value;

    int opIndex = getJVMIndexOfVarRef(branchIns.op.variableDcl);
    jvm:visitVariableInstruction(ILOAD, opIndex);
    jvm:visitJumpInstruction(GREATER_THAN_ZERO, currentFuncName + trueBBId);
    jvm:visitJumpInstruction(JUMP, currentFuncName + falseBBId);
}

function genCallTerm(bir:Call callIns) {
    //io:println("Call Ins : " + io:sprintf("%s", callIns));
    string jvmClass = className; //todo get the correct class name
    string methodName = callIns.name.value;
    string methodDesc = "(";
    foreach var arg in callIns.args {

        int argIndex = getJVMIndexOfVarRef(arg.variableDcl);

        bir:BType bType = arg.typeValue;

        if (bType is bir:BTypeInt) {
            jvm:visitVariableInstruction(LLOAD, argIndex);
            methodDesc = methodDesc + "J";
        } else if (bType is bir:BTypeString) {
            jvm:visitVariableInstruction(ALOAD, argIndex);
            methodDesc = methodDesc + "Ljava/lang/String;";
        } else {
            error err = error( "JVM generation is not supported for type " +
                                                io:sprintf("%s", arg.typeValue));
            panic err;
        }
    }


    bir:BType? returnType = callIns.lhsOp.typeValue;

    string returnTypeDesc = generateReturnType(returnType);

    methodDesc = methodDesc + returnTypeDesc;

    // call method
    jvm:visitMethodInstruction(INVOKESTATIC, jvmClass, methodName, methodDesc, false);

    // store return
    bir:VariableDcl? lhsOpVarDcl = callIns.lhsOp.variableDcl;

    if (lhsOpVarDcl is bir:VariableDcl) {

        int lhsLndex = getJVMIndexOfVarRef(lhsOpVarDcl);

        bir:BType? bType = callIns.lhsOp.typeValue;

        if (bType is bir:BTypeInt) {
            jvm:visitVariableInstruction(LSTORE, lhsLndex);
        } else if (bType is bir:BTypeString) {
            jvm:visitVariableInstruction(ASTORE, lhsLndex);
        } else if (bType is bir:BTypeBoolean) {
            jvm:visitVariableInstruction(ISTORE, lhsLndex);
        } else {
            error err = error( "JVM generation is not supported for type " +
                                        io:sprintf("%s", callIns.lhsOp.typeValue));
            panic err;
        }

    }
    // goto thenBB
    jvm:visitJumpInstruction(JUMP, currentFuncName + callIns.thenBB.id.value);
}

function generateReturnType(bir:BType? bType) returns string {
    if (bType is bir:BTypeNil) {
        return ")V";
    } else if (bType is bir:BTypeInt) {
        return ")J";
    } else if (bType is bir:BTypeString) {
        return ")Ljava/lang/String;";
    } else {
        error err = error( "JVM generation is not supported for type " + io:sprintf("%s", bType));
        panic err;
    }
}

function checkValidBirChannel(bir:ChannelReader reader) {
    checkMagic(reader);
    checkVersion(reader);
}

function checkMagic(bir:ChannelReader reader) {
    byte[] baloCodeHexSpeak = [0xba, 0x10, 0xc0, 0xde];
    var magic = reader.readByteArray(4);

    if (!arrayEq(baloCodeHexSpeak, magic)){
        error err = error( "Invalid BIR binary content, unexptected header" );
        panic err;
    }
}

function checkVersion(bir:ChannelReader reader) {
    var birVersion = reader.readInt32();
    var supportedBirVersion = 1;
    if (birVersion != 1){
        error err = error( "Unsupported BIR version " + birVersion + ", supports version " + supportedBirVersion);
        panic err;
    }
}

function openReadableFile(string filePath) returns io:ReadableByteChannel {
    io:ReadableByteChannel byteChannel = io:openReadableFile(filePath);
    return byteChannel;
}

function arrayEq(byte[] x, byte[] y) returns boolean {
    var xLen = x.length();

    if xLen != y.length() {
        return false;
    }

    int i = 0;
    while i < xLen {
        if (x[i] != y[i]){
            return false;
        }
        i = i + 1;
    }
    return true;
}

function generateMainMethod(bir:Function userMainFunc) {
    jvm:visitMethodInit(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V");

    // todo : generate the global var init class and other crt0 loading

    boolean isVoidFunction = userMainFunc.typeValue.retType is bir:BTypeNil;

    if (!isVoidFunction) {
        jvm:visitFieldInstruction(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
    }

    string desc = getMethodDesc(userMainFunc);
    bir:BType[] paramTypes = userMainFunc.typeValue.paramTypes;

    // load and cast param values
    int paramIndex = 0;
    foreach var paramType in paramTypes {
        generateCast(paramIndex, paramType);
        paramIndex += 1;
    }

    // invoke the user's main method
    jvm:visitMethodInstruction(INVOKESTATIC, className, "main", desc, false);

    if (!isVoidFunction) {
        jvm:visitMethodInstruction(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(J)V", false);
    }

    jvm:visitNoOperandInstruction(RETURN);
    jvm:visitMaxStackValues(paramTypes.length() + 5, 10);
    jvm:visitMethodEnd();
}

function generateCast(int paramIndex, bir:BType targetType) {
    // load BValue array
    jvm:visitVariableInstruction(ALOAD, 0);

    // load value[i]
    jvm:visitLoadConstantInstruction(paramIndex);
    jvm:visitNoOperandInstruction(L2I);
    jvm:visitNoOperandInstruction(AALOAD);

    if (targetType is bir:BTypeInt) {
        jvm:visitMethodInstruction(INVOKESTATIC, "java/lang/Long", "parseLong", "(Ljava/lang/String;)J", false);
    } else {
        error err = error("JVM generation is not supported for type " + io:sprintf("%s", targetType));
        panic err;
    }
}

function getJVMIndexOfVarRef(bir:VariableDcl varDcl) returns int {
    if (indexMap.getIndex(varDcl) == -1) {
        indexMap.add(varDcl);
    }
    return indexMap.getIndex(varDcl);
}

type BalToJVMIndexMap object {
    private int localVarIndex = 0;
    private map<int> jvmLocalVarIndexMap = {};

    function add(bir:VariableDcl varDcl) {
        string varRefName = self.getVarRefName(varDcl);
        self.jvmLocalVarIndexMap[varRefName] = self.localVarIndex;

        bir:BType bType = varDcl.typeValue;

        if (bType is bir:BTypeInt) {
            self.localVarIndex = self.localVarIndex + 2;
        } else {
            self.localVarIndex = self.localVarIndex + 1;
        }
    }

    function getIndex(bir:VariableDcl varDcl) returns int {
        string varRefName = self.getVarRefName(varDcl);
        if (!(self.jvmLocalVarIndexMap.hasKey(varRefName))) {
            return -1;
        }

        return self.jvmLocalVarIndexMap[varRefName] ?: -1;
    }

    function getVarRefName(bir:VariableDcl varDcl) returns string {
        return io:sprintf("%s", varDcl);
    }
};
