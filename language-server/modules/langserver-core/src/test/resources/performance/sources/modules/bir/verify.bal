// Use the types module to type-check the BIR

import wso2/nballerina.types as t;
import wso2/nballerina.err;

class VerifyContext {
    private final Module mod;
    private final t:TypeCheckContext tc;
    private final FunctionDefn defn;

    function init(Module mod, FunctionDefn defn) {
        self.mod = mod;
        self.tc = mod.getTypeCheckContext();
        self.defn = defn;
    }

    function isSubtype(t:SemType s, t:SemType t) returns boolean {
        return t:isSubtype(self.tc, s, t);
    }

    function isEmpty(t:SemType t) returns boolean {
        return t:isEmpty(self.tc, t);
    }

    function err(err:Message msg) returns err:Semantic {
        return err:semantic(msg, functionName=self.defn.symbol.identifier);
    }

    function returnType() returns t:SemType => self.defn.signature.returnType;

    function symbolToString(Symbol sym) returns string {
        return symbolToString(self.mod, sym);
    }
}

public function verifyFunctionCode(Module mod, FunctionDefn defn, FunctionCode code) returns err:Semantic? {
    VerifyContext cx = new(mod, defn);
    foreach BasicBlock b in code.blocks {
        check verifyBasicBlock(cx, b);
    }
}

type IntBinaryInsn IntArithmeticBinaryInsn|IntBitwiseBinaryInsn;

function verifyBasicBlock(VerifyContext vc, BasicBlock bb) returns err:Semantic? {
    foreach Insn insn in bb.insns {
        check verifyInsn(vc, insn);
    }
}

function verifyInsn(VerifyContext vc, Insn insn) returns err:Semantic? {
    string name = insn.name;
    if insn is IntBinaryInsn {
        // XXX need to check result also
        // different rules for bitwise
        check verifyOperandInt(vc, name, insn.operands[0]);
        check verifyOperandInt(vc, name, insn.operands[1]);
    }
    if insn is FloatArithmeticBinaryInsn {
        check verifyOperandFloat(vc, name, insn.operands[0]);
        check verifyOperandFloat(vc, name, insn.operands[1]);
    }
    if insn is FloatNegateInsn {
        check verifyOperandFloat(vc, name, insn.operand);
    }
    else if insn is BooleanNotInsn {
        check verifyOperandBoolean(vc, name, insn.operand);
    }
    else if insn is CompareInsn {
        check verifyCompare(vc, insn);
    }
    else if insn is EqualityInsn {
        check verifyEquality(vc, insn);
    }
    else if insn is AssignInsn {
        check verifyOperandType(vc, insn.operand, insn.result.semType, "value is not a subtype of the LHS");
    }
    else if insn is CondBranchInsn {
        check verifyOperandBoolean(vc, name, insn.operand);
    }
    else if insn is RetInsn {
        check verifyOperandType(vc, insn.operand, vc.returnType(), "value is not a subtype of the return type");
    }
    else if insn is CallInsn {
        check verifyCall(vc, insn);
    }
    else if insn is TypeCastInsn {
        check verifyTypeCast(vc, insn);
    }
    else if insn is ConvertToIntInsn {
        check verifyConvertToIntInsn(vc, insn);
    }
    else if insn is ConvertToFloatInsn {
        check verifyConvertToFloatInsn(vc, insn);
    }
    else if insn is ListConstructInsn {
        check verifyListConstruct(vc, insn);
    }
    else if insn is MappingConstructInsn {
        check verifyMappingConstruct(vc, insn);
    }
    else if insn is ListGetInsn {
        check verifyListGet(vc, insn);
    }
    else if insn is ListSetInsn {
        check verifyListSet(vc, insn);
    }
     else if insn is MappingGetInsn {
        check verifyMappingGet(vc, insn);
    }
    else if insn is MappingSetInsn {
        check verifyMappingSet(vc, insn);
    }
}

function verifyCall(VerifyContext vc, CallInsn insn) returns err:Semantic? {
    FunctionRef func = <FunctionRef>insn.func;
    FunctionSignature sig = func.signature;
    int nSuppliedArgs = insn.args.length();
    int nExpectedArgs = sig.paramTypes.length();
    if nSuppliedArgs != nExpectedArgs {
        string name = vc.symbolToString(func.symbol);
        if nSuppliedArgs < nExpectedArgs {
            return vc.err(`too few arguments for call to function ${name}`);
        }
        else {
            return vc.err(`too many arguments for call to function ${name}`);
        }
    }
    foreach int i in 0 ..< nSuppliedArgs {
        check verifyOperandType(vc, insn.args[i], sig.paramTypes[i], `wrong argument type for parameter ${i + 1} in call to function ${vc.symbolToString(func.symbol)}`);
    }
}

function verifyListConstruct(VerifyContext vc, ListConstructInsn insn) returns err:Semantic? {
    t:SemType ty = insn.result.semType;
    if !vc.isSubtype(ty, t:LIST_RW) {
        return vc.err("bad BIR: inherent type of list construct is not a list");
    }
    // XXX we should also check that ty is a single mutable list
    // and that each argument has the right type
}

function verifyMappingConstruct(VerifyContext vc, MappingConstructInsn insn) returns err:Semantic? {
    t:SemType ty = insn.result.semType;
    if !vc.isSubtype(ty, t:MAPPING_RW) {
        return vc.err("bad BIR: inherent type of list construct is not a list");
    }
    // XXX we should also check that ty is a single mutable mapping
    // and that each argument has the right type
}

function verifyListGet(VerifyContext vc, ListGetInsn insn) returns err:Semantic? {
    check verifyOperandInt(vc, insn.name, insn.operand);
    if !vc.isSubtype(insn.list.semType, t:LIST) {
        return vc.err("list get applied to non-list");
    }
    // XXX generalize this to array types other than `any[]`
    if insn.result.semType !== t:ANY {
        return vc.err("bad BIR: only any supported as list member type");
    }  
}

function verifyListSet(VerifyContext vc, ListSetInsn insn) returns err:Semantic? {
    check verifyOperandInt(vc, insn.name, insn.index);
    if !vc.isSubtype(insn.list.semType, t:LIST) {
        return vc.err("list set applied to non-list");
    }
    // XXX also check type compatibility of operand and list type
}

function verifyMappingGet(VerifyContext vc, MappingGetInsn insn) returns err:Semantic? {
    check verifyOperandString(vc, insn.name, insn.operands[1]);
    if !vc.isSubtype(insn.operands[0].semType, t:MAPPING) {
        return vc.err("mapping get applied to non-mapping");
    }
    // XXX generalize this to mapping types other than `map<any>`
    if insn.result.semType !== t:ANY {
        return vc.err("bad BIR: only any supported as mapping member type");
    }  
}

function verifyMappingSet(VerifyContext vc, MappingSetInsn insn) returns err:Semantic? {
    check verifyOperandString(vc, insn.name, insn.operands[1]);
    if !vc.isSubtype(insn.operands[0].semType, t:MAPPING) {
        return vc.err("mapping set applied to non-mapping");
    }
    // XXX also check type compatibility of operand and mapping type
}

function verifyTypeCast(VerifyContext vc, TypeCastInsn insn) returns err:Semantic? {
    if vc.isEmpty(insn.result.semType) {
        return vc.err("type cast cannot succeed");
    }
    // These should not happen with the nballerina front-end
    if !vc.isSubtype(insn.result.semType, insn.operand.semType) {
        return vc.err("bad BIR: result of type cast is not subtype of operand");
    }
    if !vc.isSubtype(insn.result.semType, insn.semType) {
        return vc.err("bad BIR: result of type cast is not subtype of cast to type");
    }
}

function verifyConvertToIntInsn(VerifyContext vc, ConvertToIntInsn insn) returns err:Semantic? {
    if vc.isEmpty(t:intersect(t:diff(insn.operand.semType, t:INT), t:NUMBER)) {
        return vc.err("bad BIR: operand type of ConvertToInt has no non-integral numeric component");
    }
    if !vc.isSubtype(t:union(t:diff(insn.operand.semType, t:NUMBER), t:INT), insn.result.semType) {
        return vc.err("bad BIR: result type of ConvertToInt does not contain everything it should");
    }
    if !vc.isEmpty(t:intersect(t:diff(insn.result.semType, t:INT), t:NUMBER)) {
        return vc.err("bad BIR: result type of ConvertToInt contains non-integral numeric type");
    }
}

function verifyConvertToFloatInsn(VerifyContext vc, ConvertToFloatInsn insn) returns err:Semantic? {
    if vc.isEmpty(t:intersect(t:diff(insn.operand.semType, t:FLOAT), t:NUMBER)) {
        return vc.err("bad BIR: operand type of ConvertToFloat has no non-float numeric component");
    }
    if !vc.isSubtype(t:union(t:diff(insn.operand.semType, t:NUMBER), t:FLOAT), insn.result.semType) {
        return vc.err("bad BIR: result type of ConvertToFloat does not contain everything it should");
    }
    if !vc.isEmpty(t:intersect(t:diff(insn.result.semType, t:FLOAT), t:NUMBER)) {
        return vc.err("bad BIR: result type of ConvertToFloat contains non-float numeric type");
    }
}

function verifyCompare(VerifyContext vc, CompareInsn insn) returns err:Semantic? {
    t:UniformTypeBitSet expectType;
    OrderType ot = insn.orderType;
    if ot is OptOrderType {
        expectType = t:uniformTypeUnion((1 << t:UT_NIL) | (1 << ot.opt ));
    }
    else {
        expectType  = t:uniformType(ot);
    }
    foreach var operand in insn.operands {
        t:SemType operandType;
        if operand is Register {
            operandType = operand.semType;
        }
        else {
            operandType = t:constBasicType(operand);
        }
        if !t:isSubtypeSimple(operandType, expectType) {
            return vc.err(`operand of ${insn.op} does not match order type`);
        }
    }
}

function verifyEquality(VerifyContext vc, EqualityInsn insn) returns err:Semantic? {
    Operand lhs = insn.operands[0];
    Operand rhs = insn.operands[1];
    if lhs is Register {
        if rhs is Register {
            t:SemType intersectType = t:intersect(lhs.semType, rhs.semType);
            if !vc.isEmpty(intersectType) {
                // JBUG #31749 cast should not be needed
                if (<string>insn.op).length() == 2 && !vc.isSubtype(lhs.semType, t:SIMPLE_OR_STRING) && !vc.isSubtype(rhs.semType, t:SIMPLE_OR_STRING) {
                    return vc.err("at least one operand of an == or != expression must be a subtype of anydata");
                }
                return;
            }
        }
        else if t:containsConst(lhs.semType, rhs) {
            return;
        }
    }
    else if rhs is Register {
        if t:containsConst(rhs.semType, lhs) {
            return;
        }
    }
    else if isEqual(lhs, rhs) {
        return;
    }
    return vc.err(`intersection of operands of operator ${insn.op} is empty`);
}

// After JBUG #17977, #32245 is fixed, replace by ==
function isEqual(ConstOperand c1, ConstOperand c2) returns boolean {
    return c1 is float && c2 is float ? (c1 == c2 || (float:isNaN(c1) && float:isNaN(c2))) : c1 == c2;
}

function verifyOperandType(VerifyContext vc, Operand operand, t:SemType semType, err:Message msg) returns err:Semantic? {
    if operand is Register {
        if !vc.isSubtype(operand.semType, semType) {
            return vc.err(msg);
        }
    }
    else if !t:containsConst(semType, operand) {
        return vc.err(msg);
    }
}

function verifyOperandString(VerifyContext vc, string insnName, StringOperand operand) returns err:Semantic? {
    if operand is Register {
        return verifyRegisterSemType(vc, insnName, operand, t:STRING, "string");
    }
}

function verifyOperandInt(VerifyContext vc, string insnName, IntOperand operand) returns err:Semantic? {
    if operand is Register {
        return verifyRegisterSemType(vc, insnName, operand, t:INT, "int");
    }
}

function verifyOperandFloat(VerifyContext vc, string insnName, FloatOperand operand) returns err:Semantic? {
    if operand is Register {
        return verifyRegisterSemType(vc, insnName, operand, t:FLOAT, "float");
    }
}

function verifyOperandBoolean(VerifyContext vc, string insnName, BooleanOperand operand) returns err:Semantic? {
    if operand is Register {
        return verifyRegisterSemType(vc,insnName, operand, t:BOOLEAN, "boolean");
    }
}

function verifyRegisterSemType(VerifyContext vc, string insnName, Register operand, t:SemType semType, string typeName) returns err:Semantic? {
    if !vc.isSubtype(operand.semType, semType) {
        return operandTypeErr(vc, insnName, typeName);
    }
}

function operandTypeErr(VerifyContext vc, string insnName, string typeName) returns err:Semantic {
    return vc.err(`operands of ${insnName} must be subtype of ${typeName}`);
}
