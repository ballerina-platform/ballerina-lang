// The concept here is to have an API that can both
// be implemented simply on top of the LLVM C API
// and can be used to generate .ll format directly.
// In the C API version, distinct classes would have
// private members that are handles referring to the
// C void* values.

import nballerina.err;
import ballerina/io;

// Operand of an unnamed variable/basic block
type Unnamed int;

# Corresponds to LLVMValueRef 
public readonly distinct class Value {
    string|Unnamed operand;
    Type ty;
    function init(Type ty, string|Unnamed operand) {
        self.ty = ty;
        self.operand = operand;
    }
}

# Subtype of Value that refers to a pointer
# Ensures compile-time checking that stores and loads use the right kinds of Value
public readonly class PointerValue {
    *Value;
    string|Unnamed operand;
    PointerType ty;
    function init(PointerType ty, string|Unnamed operand) {
        self.ty = ty;
        self.operand = operand;
    }
}

# Subtype of Value that refers to a constant
public readonly class ConstValue {
    *Value;
    string operand;
    Type ty;
    function init(Type ty, string operand) {
        self.ty = ty;
        self.operand = operand;
    }
}

public readonly class ConstPointerValue {
    *PointerValue;
    *ConstValue;
    string operand;
    PointerType ty;
    function init(PointerType ty, string operand) {
        self.ty = ty;
        self.operand = operand;
    }
}

function constValueWithBody(PointerType ty, string[] body) returns ConstPointerValue {
    string[] words = [];
    foreach var word in body {
        words.push(word);
    }
    string operand = concat(...words);
    return new (ty, operand);
}
// Corresponds to LLVMConstInt
// XXX Need to think about SignExtend argument
public function constInt(IntType ty, int val) returns ConstValue {
    return new ConstValue(ty, val.toString());
}

final string posInf = "0x" + (1.0/0.0).toBitsInt().toHexString().toUpperAscii();
final string negInf = "0x" + (-1.0/0.0).toBitsInt().toHexString().toUpperAscii();
final string NAN = "0x" + (0.0/0.0).toBitsInt().toHexString().toUpperAscii();

// Corresponds to LLVMConstReal
public function constFloat(FloatType ty, float val) returns ConstValue {
    string valRep;
    // Special cases
    if val.isInfinite() {
        if val > 0.0 {
            valRep = posInf;
        }
        else {
            valRep = negInf;
        }
    }
    else if val.isNaN() {
        valRep = NAN;
    }
    else {
        // General case
        if float:abs(val).toString().length() > 7 {
            // Resulting number has more than 7 digits so convert to hex format
            int repInt = float:toBitsInt(val);
            valRep = "0x" + int:toHexString(repInt).toUpperAscii();
        }
        else {
            // Set number in decimal format
            // Change to E notation once balspec:#770 is done
            valRep = val.toString();
        }
    }
    return new ConstValue(ty, valRep);
}

// Corresponds to LLVMConstNull
public function constNull(PointerType ty) returns ConstPointerValue {
    return new ConstPointerValue(ty, "null");
}

// Corresponds to LLVMContextRef
public class Context {
    // Corresponds to LLVMContextCreate
    public function init() {
        
    }

    public function createModule() returns Module {
        return new(self);
    }

    public function createBuilder() returns Builder {
        return new(self);
    }

    public function constStruct(Value[] elements) returns ConstValue {
        string[] structBody = [];
        Type[] elemTypes = [];
        structBody.push("{");
        foreach int i in 0 ..< elements.length() {
            final Value element = elements[i];
            if i > 0 {
                structBody.push(",");
            }
            structBody.push(typeToString(element.ty));
            if element.operand is Unnamed {
                panic err:illegalArgument("All elements must be constants");
            } else {
                structBody.push(<string>element.operand);
            }
            elemTypes.push(element.ty);
        }
        structBody.push("}");
        Type structTy = structType(elemTypes);
        return new(structTy, concat(...structBody));
    }

    // Corresponds to LLVMConstStringInContext
    public function constString(byte[] bytes) returns ConstValue {
        ArrayType ty = arrayType("i8", bytes.length());
        ConstValue val = new(ty, charArray(bytes));
        return val;
    }

    // Corresponds to LLVMConstGEP
    public function constGetElementPtr(ConstPointerValue ptr, ConstValue[] indices, "inbounds"? inbounds=()) returns ConstPointerValue {
        string[] words = [];
        words.push("getelementptr");
        if inbounds != () {
            words.push(inbounds);
        }
        words.push("(");
        PointerType destTy = gepArgs(words, ptr, indices, inbounds);
        words.push(")");
        return constValueWithBody(destTy, words);
    }

    // Corresponds to LLVMConstBitCast
    public function constBitCast(ConstPointerValue ptr, PointerType destTy) returns ConstPointerValue {
        string[] words = [];
        words.push("bitcast", "(");
        bitCastArgs(words, ptr, destTy);
        words.push(")");
        return constValueWithBody(destTy, words);
    }

    // Corresponds to LLVMConstAddrSpaceCast
    public function constAddrSpaceCast(ConstPointerValue ptr, PointerType destTy) returns ConstPointerValue {
        string[] words = [];
        words.push("addrspacecast", "(");
        addrSpaceCastArgs(words, ptr, destTy);
        words.push(")");
        return constValueWithBody(destTy, words);
    }
}

# Corresponds to llvm::Module class
public class Module {
    private final map<FunctionDefn|FunctionDecl|PointerValue> globals = {};
    // We have these because we don't rely on order of iteration over map.
    private [PointerValue, GlobalProperties][] globalVariables = [];
    private FunctionDecl[] functionDecls = [];
    private FunctionDefn[] functionDefns = [];

    private final Context context;
    private TargetTriple? target = ();

    function init(Context context) {
        self.context = context;
    }

    // Corresponds to LLVMAddFunction
    public function addFunctionDefn(string name, FunctionType fnType) returns FunctionDefn {
        string fnName = self.escapeGlobalIdent(name);
        FunctionDefn fn = new (self.context, fnName, fnType);
        self.globals[fnName] = fn;
        self.functionDefns.push(fn);
        return fn;
    }

    public function addFunctionDecl(string name, FunctionType fnType) returns FunctionDecl {
        string fnName = self.escapeGlobalIdent(name);
        FunctionDecl fn = new(self.context, fnName, fnType);
        self.globals[fnName] = fn;
        self.functionDecls.push(fn);
        return fn;
    }

    // Corresponds to LLVMSetTarget
    public function setTarget(TargetTriple targetTriple){
        self.target = targetTriple;
    }

    // Corresponds to LLVMGetIntrinsicDeclaration
    public function getIntrinsicDeclaration(IntrinsicFunctionName name) returns FunctionDecl {
        FunctionDecl? fnExisting = <FunctionDecl?>self.globals[name];
        if !(fnExisting is ()) {
            return fnExisting;
        }
        if name is IntegerArithmeticIntrinsicName {
            return self.addIntrinsic(name,
                                     { returnType: structType(["i64", "i1"]), paramTypes: ["i64", "i64"] },
                                     ["nounwind", "readnone", "speculatable", "willreturn"]);

        }
        else if name == "ptrmask.p1i8.i64" {
            return self.addIntrinsic(name,
                                     { returnType: pointerType("i8", 1), paramTypes: [pointerType("i8", 1), "i64"] },
                                     ["readnone", "speculatable"]);
        }
        else {
            panic err:impossible();
        }
    }

    private function addIntrinsic(IntrinsicFunctionName name, FunctionType fnType, EnumAttribute[] attrs) returns FunctionDecl {
        FunctionDecl fn = new(self.context, "llvm." + name, fnType);
        foreach var attr in attrs {
            fn.addEnumAttribute(attr);
        }
        self.globals[name] = fn;
        self.functionDecls.push(fn);
        return fn;
    }

    // Corresponds to LLVMAddGlobal
    public function addGlobal(Type ty, string name, *GlobalProperties props) returns ConstPointerValue {
        string varName = self.escapeGlobalIdent(name);
        PointerType ptrType = pointerType(ty, props.addressSpace);
        ConstPointerValue val = new ConstPointerValue(ptrType, "@" + varName); 
        self.globals[varName] = val;
        self.globalVariables.push([val, props]);
        return val;
    }

    private function escapeGlobalIdent(string name) returns string {
        string varName = escapeIdent(name);
        if varName is IntrinsicFunctionName {
            panic err:illegalArgument("reserved intrinsic function name");
        }
        if self.globals.hasKey(varName) {
            panic err:illegalArgument("this module already has a declaration by that name");
        }
        return varName;
    }
 
    // Corresponds to LLVMPrintModuleToFile
    public function printModuleToFile(string path) returns io:Error? {
        Output out = new;
        self.output(out);
        return out.writeFile(path);
    }

    // Corresponds to LLVMPrintModuleToString
    public function printModuleToString() returns string {
        Output out = new;
        self.output(out);
        return out.toString();
    }

    function output(Output out) {
        if self.target is TargetTriple {
            string[] words = ["target", "triple", "=", "\"", <TargetTriple>self.target, "\""];
            out.push(createLine(words));
        }
        foreach var val in self.globalVariables {
            self.outputGlobalVar(val[0], val[1], out);
        }
        foreach var fn in self.functionDecls {
            fn.output(out);
        }
        foreach var fn in self.functionDefns {
            fn.output(out);
        }
    }

    function outputGlobalVar(PointerValue val, GlobalProperties prop, Output out){
        string[] words = [];
        words.push(<string> val.operand, "=");
        if prop.initializer is Value {
            if prop.linkage == "internal"{
                words.push(prop.linkage);
            }
        } else {
            words.push(prop.linkage);
        }
        if prop.unnamedAddr {
            words.push("unnamed_addr");
        }
        if val.ty.addressSpace != 0 {
            words.push("addrspace", "(", val.ty.addressSpace.toString(), ")");
        }
        if prop.isConstant {
            words.push("constant");
        } else {
            words.push("global");
        }
        words.push(typeToString(val.ty.pointsTo));
        ConstValue? initializer = prop.initializer;
        if initializer is ConstValue {
            words.push(<string>initializer.operand);
        }
        if prop.align is int {
            words.push(",", "align", prop.align.toString());
        }
        out.push(createLine(words));
    }
}

# Corresponds to an LLVMValueRef that corresponds to an llvm::Function
public type Function FunctionDecl|FunctionDefn;

public class FunctionDecl {
    final false isDefn = false;
    final FunctionType functionType;
    final string functionName;
    string? gcName = ();
    final FunctionEnumAttribute[] functionAttributes = [];
    final ReturnEnumAttribute[] returnAttributes = [];
    final ParamEnumAttribute[][] paramAttributes = [];

    function init(Context context, string functionName, FunctionType functionType) {
        self.functionName = functionName;
        self.functionType = functionType;
        self.paramAttributes.setLength(functionType.paramTypes.length());
    }

    function output(Output out) {
        out.push(functionHeader(self));
    }

    public function addEnumAttribute(EnumAttribute attribute) {
        if attribute is FunctionEnumAttribute {
            self.addAttributeToSet(self.functionAttributes, attribute);
        }
        else {
            if attribute is (readonly & ["return", ReturnEnumAttribute]) {
                ReturnEnumAttribute attrib = attribute[1];
                self.addAttributeToSet(self.returnAttributes, attrib);
            } else {
                ParamEnumAttribute attrib = attribute[1];
                int paramIndex = attribute[0];
                if paramIndex >= self.paramAttributes.length() {
                    panic err:illegalArgument("Invalid index for parameter attribute");
                } else {
                    self.addAttributeToSet(self.paramAttributes[paramIndex], attrib);
                }
            }
        }
    }

    private function addAttributeToSet(string[] container, string attribute) {
        if container.indexOf(attribute) == () {
            container.push(attribute);
        }
    }


    // Corresponds to LLVMSetGC
    public function setGC(string? name) {
        self.gcName = name;
    }
}

public class FunctionDefn {
    final true isDefn = true;
    final FunctionType functionType;
    final string functionName;
    final FunctionEnumAttribute[] functionAttributes = [];
    final ReturnEnumAttribute[] returnAttributes = [];
    final ParamEnumAttribute[][] paramAttributes = [];
    string? gcName = ();

    private BasicBlock[] basicBlocks = [];
    private map<int> variableNames = {};
    private int unnamedLabelCount = 0;
    private Value[] paramValues;
    private Linkage linkage = "external";
    private final Context context;
    private string[] nameTranslation = [];
    private int nameCounter;
    private final boolean[] isBasicBlock = [];

    function init(Context context, string functionName, FunctionType functionType) {
        self.context = context;
        self.functionName = functionName;
        self.functionType = functionType;
        self.paramValues = [];
        self.nameCounter = functionType.paramTypes.length() + 1;
        foreach int i in 0 ..< functionType.paramTypes.length() {
            final Type paramType = functionType.paramTypes[i];
            string register = "%" + i.toString();
            Value arg = new (paramType, register);
            self.paramValues.push(arg);
        }
        self.paramAttributes.setLength(functionType.paramTypes.length());
    }

    // Correspond to LLVMGetParam
    public function getParam(int index) returns Value {
        return self.paramValues[index];
    }

    // Corresponds to LLVMSetLinkage
    // XXX Maybe better done with included record parameter
    public function setLinkage(Linkage linkage) {
        self.linkage = linkage;
    }

    public function getLinkage() returns Linkage {
        return self.linkage;
    }

    function output(Output out) {
        out.push(functionHeader(self));
        self.outputBody(out);
        out.push("}");
    }

    function outputBody(Output out) {
        // This pass will update the unnamed variables and basic block declarations
        foreach var b in self.basicBlocks {
            b.updateUnnamed();
        }
        // This pass will fix the basic block references
        boolean isFirst = true;
        foreach var b in self.basicBlocks {
            b.output(out, isFirst);
            isFirst = false;
        }
    }

    // Corresponds to LLVMAppendBasicBlock
    public function appendBasicBlock(string? name=()) returns BasicBlock {
        string|Unnamed bbName = self.genName(name);
        BasicBlock bb = new (self.context, bbName, self);
        if bbName is Unnamed {
            self.isBasicBlock[bbName] = true;
        }
        self.basicBlocks.push(bb);
        return bb;
    }


    function genName(string? name = ()) returns string|Unnamed {
        if name is string {
            string varName = name;
            if self.variableNames.hasKey(varName) {
                int count = self.variableNames.get(varName);
                self.variableNames[varName] = count + 1; // increment the count of the base name
                string newName = varName + "." + count.toString();
                while self.variableNames.hasKey(newName) {
                    count += 1;
                    newName = varName + "." + count.toString();
                }
                varName = newName;
                self.variableNames[varName] = 1; // save the augmented name in case user use the same  
            }
            else {
                self.variableNames[varName] = 1;
            }
            varName = escapeIdent(varName);
            return varName;
        } else {
            int varName = self.unnamedLabelCount;
            self.unnamedLabelCount += 1;
            return varName;
        }
    }

    function genReg(string? name = ()) returns string|Unnamed {
        string|Unnamed reg = self.genName(name);
        if reg is string {
            return "%" + reg;
        }
        else {
            self.isBasicBlock[reg] = false;
            return reg;
        }
    }

    public function addEnumAttribute(EnumAttribute attribute) {
        if attribute is FunctionEnumAttribute {
            self.addAttributeToSet(self.functionAttributes, attribute);
        }
        else {
            if attribute is (readonly & ["return", ReturnEnumAttribute]) {
                ReturnEnumAttribute attrib = attribute[1];
                self.addAttributeToSet(self.returnAttributes, attrib);
            } else {
                ParamEnumAttribute attrib = attribute[1];
                int paramIndex = attribute[0];
                if paramIndex >= self.paramAttributes.length() {
                    panic err:illegalArgument("Invalid index for parameter attribute");
                } else {
                    self.addAttributeToSet(self.paramAttributes[paramIndex], attrib);
                }
            }
        }
    }

    private function addAttributeToSet(string[] container, string attribute) {
        if container.indexOf(attribute) == () {
            container.push(attribute);
        }
    }

    // Corresponds to LLVMSetGC
    public function setGC(string? name) {
        self.gcName = name;
    }

    function updateBasicBlockLabel(Unnamed label) returns string {
        string newLabel = self.nameCounter.toString();
        self.nameCounter += 1;
        self.nameTranslation[label] = "%" + newLabel;
        return newLabel;
    }

    function updateUnnamed(string|Unnamed name) returns string|Unnamed {
        if name is Unnamed {
            if self.isBasicBlock[name] {
                // unnamed basic block
                return name;
            }
            if self.nameTranslation.length() > name && self.nameTranslation[name] != "" {
                return self.nameTranslation[name];
            }
            string newName = "%" + self.nameCounter.toString();
            self.nameCounter += 1;
            self.nameTranslation[name] = newName;
            return newName;
        } else {
            return name;
        }
    }

    function updateBasicBlockRef(string|Unnamed name) returns string {
        if name is Unnamed {
            if !self.isBasicBlock[name] {
                panic error("Variable names must be updated before updating basic block names");
            }
            string newName =  self.nameTranslation[name];
            return newName;
        } else {
            return name;
        }
    }
}

# Corresponds to LLVMBuilderRef  
public class Builder {
    private BasicBlock? currentBlock = ();

    // Corresponds to LLVMCreateBuilder
    public function init(Context context) { }

    // Corresponds to LLVMPositionBuilderAtEnd
    public function positionAtEnd(BasicBlock block) {
        self.currentBlock = block;
    }

    // Corresponds to LLVMBuildAlloca
    public function alloca(SingleValueType ty, Alignment? align=(), string? name=()) returns PointerValue {
        BasicBlock bb = self.bb();
        string|Unnamed reg = bb.func.genReg(name);
        PointerType ptrTy = pointerType(ty);
        addInsnWithAlign(bb, [reg, "=", "alloca", typeToString(ty)], align);
        return new PointerValue(ptrTy, reg);
    }

    // Corresponds to LLVMBuildLoad
    public function load(PointerValue ptr, Alignment? align=(), string? name=()) returns Value {
        BasicBlock bb = self.bb();
        Type ty = ptr.ty.pointsTo;
        string|Unnamed reg = bb.func.genReg(name);
        addInsnWithAlign(bb, [reg, "=", "load", typeToString(ty), ",", typeToString(ptr.ty), ptr.operand], align);
        return new Value(ty, reg);
    }

    // Corresponds to LLVMBuildStore
    public function store(Value val, PointerValue ptr, Alignment? align=()) {
        Type ty = ptr.ty.pointsTo;
        if ty != val.ty {
            panic err:illegalArgument("store type mismatch: " + typeToString(val.ty) + ", " + typeToString(ptr.ty));
        }
        addInsnWithAlign(self.bb(), ["store", typeToString(ty), val.operand, ",", typeToString(ptr.ty), ptr.operand], align);
    }

    // Corresponds to LLVMBuild{FAdd,FSub,FMul,FDiv,FRem}
    public function fArithmetic(FloatArithmeticOp op, Value lhs, Value rhs, string? name=()) returns Value {
        return self.binaryOpWrap(op, lhs, rhs, name);
    }

    // Corresponds to LLVMBuildNSW{Add,Mul,Sub}
    public function iArithmeticNoWrap(IntArithmeticOp op, Value lhs, Value rhs, string? name=()) returns Value {
        BasicBlock bb = self.bb();
        string|Unnamed reg = bb.func.genReg(name);
        IntType|FloatType ty = sameNumberType(lhs, rhs);
        bb.addInsn(reg, "=", op, "nsw", ty, lhs.operand, ",", rhs.operand);
        return new Value(ty, reg);
    }
    // Corresponds to LLVMBuild{Add,Mul,Sub}
    public function iArithmeticWrap(IntArithmeticOp op, Value lhs, Value rhs, string? name=()) returns Value {
        return self.binaryOpWrap(op, lhs, rhs, name);
    }

    // Corresponds to LLVMBuild{SDiv,SRem}
    public function iArithmeticSigned(IntArithmeticSignedOp op, Value lhs, Value rhs, string? name=()) returns Value {
        return self.binaryOpWrap(op, lhs, rhs, name);
    }

    // Corresponds to LLVMBuild{And, Or, Xor}
    public function iBitwise(IntBitwiseOp op, Value lhs, Value rhs, string? name=()) returns Value {
        return self.binaryOpWrap(op, lhs, rhs, name);
    }

    // Internally handle binary int operations without wrapping
    function binaryOpWrap(BinaryOp op, Value lhs, Value rhs, string? name=()) returns Value {
        BasicBlock bb = self.bb();
        string|Unnamed reg = bb.func.genReg(name);
        IntType|FloatType ty = sameNumberType(lhs, rhs);
        bb.addInsn(reg, "=", op, ty, lhs.operand, ",", rhs.operand);
        return new Value(ty, reg);
    }

    // Corresponds to LLVMBuildICmp
    public function iCmp(IntPredicate op, Value lhs, Value rhs, string? name=()) returns Value {
        BasicBlock bb = self.bb();
        string|Unnamed reg = bb.func.genReg(name);
        IntegralType ty = sameIntegralType(lhs, rhs);
        bb.addInsn(reg, "=", "icmp", op, typeToString(ty), lhs.operand, ",", rhs.operand);
        return new Value("i1", reg);
    }

    // Corresponds to LLVMBuildFCmp
    public function fCmp(FloatPredicate op, Value lhs, Value rhs, string? name=()) returns Value {
        BasicBlock bb = self.bb();
        string|Unnamed reg = bb.func.genReg(name);
        IntType|FloatType ty = sameNumberType(lhs, rhs);
        if ty is FloatType {
            bb.addInsn(reg, "=", "fcmp", op, typeToString(ty), lhs.operand, ",", rhs.operand);
            return new Value("i1", reg);
        }
        else {
            panic err:illegalArgument("values must be a real type");
        }
    }

    // Corresponds to LLVMBuildBitCast
    public function bitCast(PointerValue val, PointerType destTy, string? name=()) returns PointerValue {
        BasicBlock bb = self.bb();
        string|Unnamed reg = bb.func.genReg(name);
        (string|Unnamed)[] words = [];
        words.push("bitcast");
        bitCastArgs(words, val, destTy);
        bb.addInsn(reg, "=", ...words);
        return new (destTy, reg);
    }

    // Corresponds to LLVMBuildRet/LLVMBuildRetVoid
    // value of () represents void return value
    public function ret(Value? value=()) {
        BasicBlock bb = self.bb();
        if value is () {
            bb.addInsn("ret", "void");
        }
        else {
            bb.addInsn("ret", typeToString(value.ty), value.operand);
        }
    }

    // Corresponds to LLVMBuildPtrToInt
    public function ptrToInt(PointerValue ptr, IntType destTy, string? name=()) returns Value {
        BasicBlock bb = self.bb();
        string|Unnamed reg = bb.func.genReg(name);
        bb.addInsn(reg, "=", "ptrtoint", typeToString(ptr.ty), ptr.operand, "to", typeToString(destTy));
        return new Value(destTy, reg);
    }

    // Corresponds to LLVMBuildZExt
    public function zExt(Value val, IntType destTy, string? name=()) returns Value {
        BasicBlock bb = self.bb();
        string|Unnamed reg = bb.func.genReg(name);
        bb.addInsn(reg, "=", "zext", typeToString(val.ty), val.operand, "to", typeToString(destTy));
        return new Value(destTy, reg);
    }

    // Corresponds to LLVMBuildSExt
    public function sExt(Value val, IntType destTy, string? name=()) returns Value {
        BasicBlock bb = self.bb();
        string|Unnamed reg = bb.func.genReg(name);
        bb.addInsn(reg, "=", "sext", typeToString(val.ty), val.operand, "to", typeToString(destTy));
        return new Value(destTy, reg);
    }

    // Corresponds to LLVMBuildTrunc
    public function trunc(Value val, IntType destinationType, string? name=()) returns Value {
        if val.ty is IntType {
            if val.ty == destinationType {
                panic err:illegalArgument("equal sized types are not allowed");
            }
            BasicBlock bb = self.bb();
            string|Unnamed reg = bb.func.genReg(name);
            bb.addInsn(reg, "=", "trunc", typeToString(val.ty), val.operand, "to", typeToString(destinationType));
            return new Value(destinationType, reg);
        } 
        else {
            panic err:illegalArgument("value must be an integer type");
        }
    }

    // Corresponds to LLVMBuildFNeg
    public function fNeg(Value val, string? name=()) returns Value {
        if val.ty is FloatType {
            BasicBlock bb = self.bb();
            string|Unnamed reg = bb.func.genReg(name);
            bb.addInsn(reg, "=", "fneg", typeToString(val.ty), val.operand);
            return new Value(val.ty, reg);
        }
        else {
            panic err:illegalArgument("value must be an real type");
        }
    }

    // Corresponds to LLVMBuildSIToFP
    public function sIToFP(Value val, FloatType destTy, string? name=()) returns Value {
        if val.ty is IntType {
            BasicBlock bb = self.bb();
            string|Unnamed reg = bb.func.genReg(name);
            bb.addInsn(reg, "=", "sitofp", typeToString(val.ty), val.operand, "to", typeToString(destTy));
            return new Value(destTy, reg);
        }
        else {
            panic err:illegalArgument("value must be int type");
        }
    }

    public function unreachable() {
        BasicBlock bb = self.bb();
        bb.addInsn("unreachable");
    }

    // Corresponds to LLVMBuildCall
    // Returns () if there is no result i.e. function return type is void
    public function call(Function fn, Value[] args, string? name=()) returns Value? {
        if fn.functionType.paramTypes.length() != args.length() {
            panic err:illegalArgument(`number of arguments is invalid for function ${fn.functionName}`);
        }
        BasicBlock bb = self.bb();
        (string|Unnamed)[] insnWords = [];
        RetType retType = fn.functionType.returnType;
        if retType != "void" {
            insnWords.push("=");
        }
        insnWords.push("call");
        insnWords.push(typeToString(retType));
        insnWords.push("@" + fn.functionName);
        insnWords.push("(");
        foreach int i in 0 ..< args.length() {
            final Value arg = args[i];
            if i > 0 {
                insnWords.push(",");
            }
            insnWords.push(typeToString(arg.ty));
            insnWords.push(arg.operand);
        }
        insnWords.push(")");
        if retType != "void" {
            string|Unnamed reg = bb.func.genReg(name);
            bb.addInsn(reg, ...insnWords);
            return new Value(retType, reg);
        } else {
            bb.addInsn(...insnWords);
        }
    }

    // Corresponds to LLVMBuildExtractValue
    public function extractValue(Value value, int index, string? name=()) returns Value {
        if value.ty is StructType {
            BasicBlock bb = self.bb();
            string|Unnamed reg = bb.func.genReg(name);
            bb.addInsn(reg, "=", "extractvalue", typeToString(value.ty), value.operand, ",", index.toString());
            Type elementType = getTypeAtIndex(<StructType>value.ty, index);
            return new Value(elementType, reg);
        }
        else {
            panic err:illegalArgument("extract value from non aggregate data type");
        }
    }

    // Corresponds to LLVMBuildBr
    public function br(BasicBlock destination) {
        BasicBlock bb = self.bb();
        bb.addInsn("br", "label", destination.ref());
    }

    // Corresponds to LLVMBuildCondBr
    public function condBr(Value condition, BasicBlock ifTrue, BasicBlock ifFalse) {
        if condition.ty is "i1" {
            BasicBlock bb = self.bb();
            bb.addInsn("br", "i1", condition.operand, ",", "label", ifTrue.ref(), ",", "label", ifFalse.ref());
        } 
        else {
            panic err:illegalArgument("Condition must be a u1");
        }
    }

    // Corresponds to LLVMBuildGEP
    public function getElementPtr(PointerValue ptr, Value[] indices, "inbounds"? inbounds=(), string? name=()) returns PointerValue {
        BasicBlock bb = self.bb();
        string|Unnamed reg = bb.func.genReg(name);
        (string|Unnamed)[] words = [];
        words.push(reg, "=");
        words.push("getelementptr");
        if inbounds != () {
            words.push(inbounds);
        }
        PointerType destTy = gepArgs(words, ptr, indices, inbounds);
        bb.addInsn(...words);
        return new PointerValue(destTy, reg);
    }

    // Corresponds to LLVMBuildAddrSpaceCast
    public function addrSpaceCast(PointerValue val, PointerType destTy, string? name=()) returns PointerValue {
        BasicBlock bb = self.bb();
        string|Unnamed reg = bb.func.genReg(name);
        (string|Unnamed)[] words = [];
        words.push(reg, "=", "addrspacecast");
        addrSpaceCastArgs(words, val, destTy);
        bb.addInsn( ...words);
        return new PointerValue(destTy, reg);
    }

    private function bb() returns BasicBlock {
        BasicBlock? tem = self.currentBlock;
        if tem is () {
            panic err:impossible("no current basic block");
        }
        else {
            return tem;
        }
    }
}

function addInsnWithAlign(BasicBlock bb, (string|Unnamed)[] words, Alignment? align) {
    if !(align is ()) {
        words.push(",", "align", align.toString());
    }
    bb.addInsn(...words);
}

const INDENT = "  ";

# Corresponds to LLVMBasicBlockRef
public distinct class BasicBlock {
    final FunctionDefn func;
    private string|Unnamed label;
    private (string|Unnamed)[][] lines = [];
    private boolean isReferenced = false;

    function init(Context context, string|Unnamed label, FunctionDefn func) {
        self.label = label;
        self.func = func;
    }

    function ref() returns string|Unnamed {
        self.isReferenced = true;
        if self.label is Unnamed {
            return self.label;
        } else {
            return "%" + <string>self.label;
        }
    }

    function addInsn((string|Unnamed)... words) {
        (string|Unnamed)[] chunks = [];
        string[] currentChunk = [];
        foreach var word in words{
            if word is string{
                currentChunk.push(word);
            } else {
                chunks.push(concat(...currentChunk));
                currentChunk = [];
                chunks.push(word);
            }
        }
        if currentChunk.length() > 0 {
            chunks.push(concat(...currentChunk));
        }
        self.lines.push(chunks);
    }

    // Used to to update the unnamed variables and basic block labels
    function updateUnnamed() {
        (string|Unnamed)[][] newLines = [];
        if self.isReferenced && self.label is Unnamed {
            // unnamed basic block
            self.label = self.func.updateBasicBlockLabel(<Unnamed>self.label);
        }
        foreach var line in self.lines {
            (string|Unnamed)[] newLine = [];
            foreach var name in line {
                newLine.push(self.func.updateUnnamed(name));
            }
            newLines.push(newLine);
        }
        self.lines = newLines;
    }

    function output(Output out, boolean isFirst) {
        // This ensures we leave out the label in two cases
        // 1. the first block (provided it is not referenced)
        // 2. basic blocks that were in BIR but are unreferenced (and empty) in LL
        //    (happens at the moment for blocks starting with `catch`)
        if self.isReferenced {
            out.push(<string>self.label + ":");
        }
        else if !isFirst {
            panic err:impossible("unreferenced basic block");
        }
        foreach var line in self.lines {
            string[] newLine = [];
            foreach var name in line {
                newLine.push(self.func.updateBasicBlockRef(name));
            }
            string outputLine = createLine([concat(...newLine)], INDENT);
            out.push(outputLine);
        }
    }
}

function sameIntegralType(Value v1, Value v2) returns IntegralType {
    Type ty1 = v1.ty;
    Type ty2 = v2.ty;
    if ty1 != ty2 {
        panic err:illegalArgument("expected same types");
    }
    else if ty1 is IntegralType {
        return ty1;
    }
    panic err:illegalArgument("expected an integral type");
}

function sameNumberType(Value v1, Value v2) returns IntType|FloatType {
    Type ty1 = v1.ty;
    Type ty2 = v2.ty;
    if ty1 != ty2 {
        panic err:illegalArgument("expected same types");
    }
    else if ty1 is IntType || ty1 is FloatType {
        return ty1;
    }
    panic err:illegalArgument("expected a number type");
}

function typeToString(RetType ty) returns string {
    string typeTag;
    if ty is PointerType {
        if ty.addressSpace == 0 {
            typeTag = typeToString(ty.pointsTo) + "*";
        } else {
            typeTag = createLine([typeToString(ty.pointsTo), "addrspace", "(", ty.addressSpace.toString(), ")", "*"]);
        }
    }
    else if ty is StructType {
        string[] typeStringBody = [];
        typeStringBody.push("{");
        foreach int i in 0 ..< ty.elementTypes.length() {
            final Type elementType = ty.elementTypes[i];
            if i > 0 {
                typeStringBody.push(",");
            }
            typeStringBody.push(typeToString(elementType));
        }
        typeStringBody.push("}");
        typeTag = createLine(typeStringBody, "");
    }
    else if ty is ArrayType {
        string[] typeStringBody = [];
        typeStringBody.push("[");
        typeStringBody.push(ty.elementCount.toString());
        typeStringBody.push("x");
        typeStringBody.push(typeToString(ty.elementType));
        typeStringBody.push("]");
        typeTag = createLine(typeStringBody, "");
    }
    else {
        typeTag = ty;
    }
    return typeTag;
}

class Output {
    final string[] lines = [];

    function push(string line) {
        self.lines.push(line);
    }

    function writeFile(string path) returns io:Error? {
        return io:fileWriteLines(path, self.lines);
    }

    function toString() returns string {
        return "\n".'join(...self.lines);
    }
}

function functionHeader(Function fn) returns string {
    string[] words = [];
    if fn is FunctionDefn {
        words.push("define");
        if fn.getLinkage() != "external" {
            words.push(fn.getLinkage());
        }
    }
    else {
        words.push("declare");
    }
    foreach int i in 0 ..< fn.returnAttributes.length() {
        words.push(fn.returnAttributes[i]);
    }
    words.push(typeToString(fn.functionType.returnType));
    words.push("@" + fn.functionName);
    words.push("(");
    foreach int i in 0 ..< fn.functionType.paramTypes.length() {
        final Type ty = fn.functionType.paramTypes[i];
        if i > 0 {
            words.push(",");
        }
        words.push(typeToString(ty));
        foreach int j in 0 ..< fn.paramAttributes[i].length() {
            words.push(fn.paramAttributes[i][j]);
        }
        if fn is FunctionDefn {
            words.push(<string>fn.getParam(i).operand);
        }
    }
    words.push(")");
    foreach int i in 0 ..< fn.functionAttributes.length() {
        words.push(fn.functionAttributes[i]);
    }
    if fn.gcName is string {
        words.push("gc", string `"${<string>fn.gcName}"`);
    }
    if fn is FunctionDefn {
        words.push("{");
    }
    return concat(...words);
}

function createLine(string[] words, string indent = "") returns string {
    return string:concat(indent, concat(...words));
}

function concat(string... words) returns string {
    string[] parts = [];
    foreach string word in words {
        string lastTail = parts.length() > 0 ? parts[parts.length() - 1] : "";
        if lastTail.length() > 0 {
            lastTail = lastTail.substring(lastTail.length() - 1);
        }
        string head = word.length() > 0 ? word.substring(0, 1) : "";
        if !(omitSpaceBefore(word) || (head != "\"" && omitSpaceBefore(head))) 
                && parts.length() > 0 
                && !(omitSpaceAfter(parts[parts.length() - 1]) || (lastTail != "\"" && omitSpaceAfter(lastTail))) {
            parts.push(" ");
        }
        parts.push(word);
    }
    return string:concat(...parts).trim();
}

 // Identifier must be of the form [-a-zA-Z$._][-a-zA-Z$._0-9]* 
function escapeIdent(string name) returns string {
    if isIdent(name) {
        return name;
    }
    string escaped = "\"";
    foreach int i in 0 ..< name.length() { // JBUG issue:#31767
        string:Char ch = <string:Char>name[i];
        escaped += escapeIdentChar(ch);
    }
    escaped += "\"";
    return escaped;
}

function escapeIdentChar(string:Char ch) returns string {
    int cp = ch.toCodePointInt();
    if cp >= 0x20 && cp < 0x7F && ch != "\"" && ch != "\\" {
        return ch;
    }
    if cp > 0x7F {
        byte[] bytes = ch.toBytes();
        string result = "";
        foreach byte b in bytes {
            // JBUG #31776 int cast should not be required
            // UTF-8 representation of a code point >= 0x80 consists of bytes >= 0x80
            // so toHexString here will always produce two bytes
            result += "\\" + (<int>b).toHexString().toUpperAscii();
        } 
        return result;
    }
    string hex = cp.toHexString().toUpperAscii();
    if hex.length() == 1 {
        return "\\0" + hex;
    }
    else {
        return "\\" + hex;
    }
}

function gepArgs((string|Unnamed)[] words, Value ptr, Value[] indices, "inbounds"? inbounds) returns PointerType {
    Type ptrTy = ptr.ty;
    if ptrTy is PointerType {
        words.push(typeToString(ptrTy.pointsTo));
    } else {
        panic err:illegalArgument("GEP on non-pointer type value"); 
    }
    words.push(",", typeToString(ptr.ty), ptr.operand);
    Type resultType = ptr.ty;
    int resultAddressSpace = 0;
    foreach var index in indices {
        words.push(",");
        words.push(typeToString(index.ty));
        words.push(index.operand);
        if resultType is PointerType {
            resultAddressSpace = resultType.addressSpace;
            resultType = resultType.pointsTo;
        } 
        else {
            if resultType is ArrayType {
                resultType = resultType.elementType;
            } 
            else if resultType is StructType {
                int i;
                if index.operand is Unnamed {
                    i = <Unnamed>index.operand;
                } else {
                    i = checkpanic int:fromString(<string>index.operand);
                }
                if index.ty != "i32" {
                    panic err:illegalArgument("structures can be index only using i32 constants"); 
                } 
                else {
                    resultType = getTypeAtIndex(resultType, i);
                }
            } 
            else {
                panic err:illegalArgument(string `type  ${typeToString(resultType)} can't be indexed`);
            }
        }
    }
    return pointerType(resultType, resultAddressSpace);
}

function bitCastArgs((string|Unnamed)[] words, Value val, PointerType destTy) {
    words.push(typeToString(val.ty), val.operand, "to", typeToString(destTy));
}

function addrSpaceCastArgs((string|Unnamed)[] words, Value val, PointerType destTy) {
    words.push(typeToString(val.ty), val.operand, "to", typeToString(destTy));
}

// JBUG #31777 cast should not be necessary
final int CP_DOUBLE_QUOTE = (<string:Char>"\"").toCodePointInt();
final int CP_BACKSLASH = (<string:Char>"\\").toCodePointInt();

function charArray(byte[] bytes) returns string {
    string result = "c\"";
    foreach var b in bytes {
        // JBUG 31700 incorrect code generated if 32 is in hex
        if b >= 32 && b < 127 && b != CP_DOUBLE_QUOTE && b != CP_BACKSLASH {
            result += checkpanic string:fromBytes([b]);
        }
        else {
            result += "\\";
            // JBUG #31776 cast should not be necessary
            string hex = (<int>b).toHexString().toUpperAscii();
            if hex.length() == 1 {
                result += "0" + hex;
            }
            else {
                result += hex;
            }
        }
    }
    result += "\"";
    return result;
}

function isIdent(string name) returns boolean {
    if name.length() == 0 {
        return false;
    }
    // JBUG #31758 type of name[0] is string:Char
    if isDigit(<string:Char>name[0]) {
        return false;
    }

    foreach int i in 0 ..< name.length() {// JBUG issue:#31767
        string:Char ch = <string:Char>name[i];
        if !isIdentFollow(ch) {
            return false;
        }
    }
    return true;
}

function isIdentFollow(string:Char ch) returns boolean {
    return isAlnum(ch) || isIdentOther(ch);
}

function isIdentOther(string:Char ch) returns boolean {
     return ch == "$" || ch == "." || ch == "_" || ch == "-";
}

function isAlnum(string:Char ch) returns boolean {
    return isAlpha(ch) || isDigit(ch);
}

function isDigit(string:Char ch) returns boolean {
    return "0" <= ch && ch <= "9";
}

function isAlpha(string:Char ch) returns boolean {
    return ("a" <= ch && ch <= "z") || ("A" <= ch && ch <= "Z");
}

function omitSpaceBefore(string word) returns boolean {
    return word == "," || word == "(" || word == ")" || word == "}" || word == "\"" || word == "]" || word == "*";
}

function omitSpaceAfter(string word) returns boolean {
    return word == "(" || word == "{" || word == "\"" || word == "[";
}
