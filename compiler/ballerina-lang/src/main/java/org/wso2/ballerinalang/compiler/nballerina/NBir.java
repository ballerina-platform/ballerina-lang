package org.wso2.ballerinalang.compiler.nballerina;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.TupleType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.values.BmpStringValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


class JNModule {
    public ArrayList<FunctionCode> code = new ArrayList<>();
    public Map<String, FunctionDefn> functionDefns = new LinkedHashMap<>();
    public ModuleId moduleId = new ModuleId();
    public static ArrayType insnArrType;

    public JNModule() {
        insnArrType = getInsnArrTyp();
    }

    BArray getFuncDefsArray() {
        BMap<BString, Object> tmpVal = ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR,
                NBTypeNames.FUNCTION_DEFN, new HashMap<>());
        ArrayType arrTyp = TypeCreator.createArrayType(tmpVal.getType());
        BArray arr = ValueCreator.createArrayValue(arrTyp);
        functionDefns.forEach((id, def) -> arr.append(def.getRecord()));
        return arr;
    }

    BArray getCodeArray() {
        RecordType typ = TypeCreator.createRecordType("FunctionCode", ModuleGen.MODBIR,
                1, true, 0);
        ArrayType arrTyp = TypeCreator.createArrayType(typ);
        BArray arr = ValueCreator.createArrayValue(arrTyp);
        code.forEach(code -> arr.append(code.getRecord()));
        return arr;
    }

    io.ballerina.runtime.api.types.ArrayType getInsnArrTyp() {
        BMap<BString, Object> tmpVal1 = ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.RET_INSN,
                new HashMap<>());
        BMap<BString, Object> tmpVal2 = ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR,
                NBTypeNames.BOOLNOT_INSN, new HashMap<>());
        BMap<BString, Object> tmpVal3 = ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR,
                NBTypeNames.CALL_INSN, new HashMap<>());
        BMap<BString, Object> tmpVal4 = ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR,
                NBTypeNames.INT_ARITHMETIC_BINARY_INSN, new HashMap<>());
        BMap<BString, Object> tmpVal5 = ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR,
                NBTypeNames.ASSIGN_INSN, new HashMap<>());
        BMap<BString, Object> tmpVal6 = ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR,
                NBTypeNames.LIST_CON_INSN, new HashMap<>());
        BMap<BString, Object> tmpVal7 = ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR,
                NBTypeNames.CATCH_INSN, new HashMap<>());
        BMap<BString, Object> tmpVal8 = ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR,
                NBTypeNames.ABN_RET_INSN, new HashMap<>());
        BMap<BString, Object> tmpVal9 = ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR,
                NBTypeNames.COND_BRANCH_INSN, new HashMap<>());
        BMap<BString, Object> tmpVal10 = ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR,
                NBTypeNames.BRANCH_INSN, new HashMap<>());
        BMap<BString, Object> tmpVal11 = ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR,
                NBTypeNames.BRANCH_INSN, new HashMap<>());
        BMap<BString, Object> tmpVal12 = ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR,
                NBTypeNames.EQUALITY_INSN, new HashMap<>());
        BMap<BString, Object> tmpVal13 = ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR,
                NBTypeNames.COMPARE_INSN, new HashMap<>());
        BMap<BString, Object> tmpVal14 = ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR,
                NBTypeNames.INT_BITWISE_INSN, new HashMap<>());
        BMap<BString, Object> tmpVal15 = ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR,
                NBTypeNames.MAP_CON_INSN, new HashMap<>());
        BMap<BString, Object> tmpVal16 = ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR,
                NBTypeNames.LIST_GET_INSN, new HashMap<>());
        BMap<BString, Object> tmpVal17 = ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR,
                NBTypeNames.INT_NOPANIC_ARITHMETIC_BINARY_INSN, new HashMap<>());
        BMap<BString, Object> tmpVal18 = ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR,
                NBTypeNames.MAP_GET_INSN, new HashMap<>());
        BMap<BString, Object> tmpVal19 = ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR,
                NBTypeNames.STRING_CONCAT_INSN, new HashMap<>()); //TODO add other insns

        UnionType insnTyp = TypeCreator.createUnionType(tmpVal1.getType(), tmpVal2.getType(), tmpVal3.getType(),
                tmpVal4.getType(), tmpVal5.getType(), tmpVal6.getType(), tmpVal7.getType(), tmpVal8.getType(),
                tmpVal9.getType(), tmpVal10.getType(), tmpVal11.getType(), tmpVal12.getType(), tmpVal13.getType(),
                tmpVal14.getType(), tmpVal15.getType(), tmpVal16.getType(), tmpVal17.getType(), tmpVal18.getType(),
                tmpVal19.getType());
        return TypeCreator.createArrayType(insnTyp);
    }
}

class ModuleId {
    String organization;
    ArrayList<String> names = new ArrayList<>();

    BMap<BString, Object> getRecord() {
        ArrayList<Type> typarr = new ArrayList<>();
        typarr.add(PredefinedTypes.TYPE_STRING);
        TupleType tupTyp = TypeCreator.createTupleType(typarr, PredefinedTypes.TYPE_STRING, 6, false);
        BArray tup = ValueCreator.createTupleValue(tupTyp);

        names.forEach(name -> tup.append(new BmpStringValue(name)));
        Map<String, Object> fields = new HashMap<>();
        if (organization != null) {
            fields.put("organization", new BmpStringValue(organization));
        }
        fields.put("names", tup);
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.MODULE_ID, fields);
    }
}

class FunctionDefn {
    InternalSymbol symbol = new InternalSymbol();
    FunctionSignature signature = new FunctionSignature();
    Position position;

    public FunctionDefn(boolean isPublic, String identifier, long returnType, Position position) {
        this.symbol.isPublic = isPublic;
        this.symbol.identifier = identifier;
        this.signature.returnType = returnType;
        this.position = position;
    }

    BMap<BString, Object> getRecord() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("symbol", symbol.getRecord());
        fields.put("signature", signature.getRecord());
        fields.put("position", position.getRecord());
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.FUNCTION_DEFN, fields);
    }
}

class FunctionSignature {
    long returnType;
    ArrayList<Long> paramTypes = new ArrayList<>();
    long restParamType;

    public BMap<BString, Object> getRecord() {
        ObjectType complexSem = TypeCreator.createObjectType("ComplexSemType", ModuleGen.MODTYPES, 268435489);
        UnionType typ = TypeCreator.createUnionType(complexSem, PredefinedTypes.TYPE_INT_UNSIGNED_32);
        ArrayType arrTyp = TypeCreator.createArrayType(typ);
        BArray arr = ValueCreator.createArrayValue(arrTyp);
        paramTypes.forEach(arr::append);
        Map<String, Object> fields = new HashMap<>();
        fields.put("returnType", returnType); //TODO convert to SemType
        fields.put("paramTypes", arr); //TODO create SemType BArray
        if (restParamType != 0) {
            fields.put("restParamType", restParamType);
        }
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.FUNCTION_SIGNATURE, fields);
    }
}

class InternalSymbol extends Symbol {
    public boolean isPublic;
    public String identifier;

    @Override
    public BMap<BString, Object> getRecord() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("isPublic", isPublic);
        fields.put("identifier", identifier);
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.INTERNAL_SYMBOL, fields);
    }
}

class FunctionCode {
    public ArrayList<BasicBlock> blocks = new ArrayList<>();
    public ArrayList<Register> registers = new ArrayList<>();
    public Map<String, Integer> registerMap = new LinkedHashMap<>();
    public LoopContext loopContext;

    BasicBlock createBasicBlock() {
        BasicBlock bb = new BasicBlock(this.blocks.size());
        this.blocks.add(bb);
        return bb;
    }

    Register createRegister(long semType, String varName) {
        Register r = new Register(this.registers.size(), semType, varName);
        this.registers.add(r);
        registerMap.put(varName, r.number);
        return r;
    }

    Register createRegister(long semType) {
        Register r = new Register(this.registers.size(), semType, null);
        this.registers.add(r);
        return r;
    }


    public BMap<BString, Object> getRecord() {
        RecordType bTyp = TypeCreator.createRecordType("BasicBlock", ModuleGen.MODBIR, 1, true, 0);
        ArrayType bArrTyp = TypeCreator.createArrayType(bTyp);
        BArray bArr = ValueCreator.createArrayValue(bArrTyp);
        blocks.forEach(block -> bArr.append(block.getRecord()));
        BMap<BString, Object> tempVal = ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR,
                NBTypeNames.REGISTER, new HashMap<>());
        ArrayType rArrTyp = TypeCreator.createArrayType(tempVal.getType());
        BArray rArr = ValueCreator.createArrayValue(rArrTyp);
        registers.forEach(register -> rArr.append(register.getRecord()));
        Map<String, Object> fields = new HashMap<>();
        fields.put("blocks", bArr);
        fields.put("registers", rArr);
        return ValueCreator.createRecordValue(ModuleGen.MODBIR, "FunctionCode", fields);
    }
}

class LoopContext {
    public BasicBlock onBreak;
    public BasicBlock onContinue;
    public LoopContext enclosing;
    public boolean breakUsed = false;
}

class Register {
    public int number;
    public long semType;
    public String varName;

    public Register(int number, long semType, String varName) {
        this.number = number;
        this.semType = semType; //TODO convert to SemType
        this.varName = varName;
    }

    public BMap<BString, Object> getRecord() {
        ObjectType complexSem = TypeCreator.createObjectType("ComplexSemType", ModuleGen.MODTYPES, 268435489);
        UnionType typ = TypeCreator.createUnionType(complexSem, PredefinedTypes.TYPE_INT_UNSIGNED_32);
        ArrayType arrTyp = TypeCreator.createArrayType(typ);
        BArray arr = ValueCreator.createArrayValue(arrTyp);
        arr.append(semType);
        Map<String, Object> fields = new HashMap<>();
        fields.put("number", number);
        if (varName != null) {
            fields.put("varName", StringUtils.fromString(varName));
        }
        fields.put("semType", arr.get(0)); //TODO change to SemType Value
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.REGISTER, fields);
    }
}

class BasicBlock {
    public int label;
    public int onPanic;
    public boolean ppb;
    public ArrayList<InsnBase> insns = new ArrayList<>();

    public BasicBlock(int label) {
        this.label = label;
    }

    public BMap<BString, Object> getRecord() {

        //TODO Move to a singleton
        BArray arr = ValueCreator.createArrayValue(JNModule.insnArrType);
        insns.forEach(insn -> arr.append(insn.getRecord()));
        Map<String, Object> fields = new HashMap<>();
        fields.put("label", label);
        fields.put("insns", arr);
        if (ppb) {
            fields.put("onPanic", onPanic);
        }
        return ValueCreator.createRecordValue(ModuleGen.MODBIR, "BasicBlock", fields);
    }
}

abstract class InsnBase {
    public String name; //InsnName
    public abstract BMap<BString, Object> getRecord();

}

class CondBranchInsn extends InsnBase {
    public Register operand;
    int ifTrue;
    int ifFalse;

    public CondBranchInsn(Register operand, int ifTrue, int ifFalse) {
        this.operand = operand;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    @Override
    public BMap<BString, Object> getRecord() {
        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        fields.put("operand", operand.getRecord());
        fields.put("ifTrue", ifTrue);
        fields.put("ifFalse", ifFalse);
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.COND_BRANCH_INSN, fields);
    }
}

class BranchInsn extends InsnBase {
    int dest;

    public BranchInsn(int dest) {
        this.dest = dest;
    }

    @Override
    public BMap<BString, Object> getRecord() {
        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        fields.put("dest", dest);
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.BRANCH_INSN, fields);
    }
}

class IntArithmeticBinaryInsn extends InsnBase {
    public String op;
    public Register result;
    public Operand[] operands = new Operand[2];
    public Position position;

    @Override
    public BMap<BString, Object> getRecord() {
        BMap<BString, Object> tmpRegVal = ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.REGISTER,
                new HashMap<>());
        UnionType typ = TypeCreator.createUnionType(tmpRegVal.getType(), PredefinedTypes.TYPE_INT);
        ArrayType arrTyp = TypeCreator.createArrayType(typ, 2);
        BArray arr = ValueCreator.createArrayValue(arrTyp);
        arr.add(0, operands[0].getOperand());
        arr.add(1, operands[1].getOperand());
        Map<String, Object> fields = new HashMap<>();
        fields.put("op", new BmpStringValue(op));
        fields.put("result", result.getRecord());
        fields.put("position", position.getRecord());
        fields.put("operands", arr);
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.INT_ARITHMETIC_BINARY_INSN, fields);
    }
}

class StringConcatInsn extends InsnBase {
    public Register result;
    public Operand[] operands = new Operand[2];

    @Override
    public BMap<BString, Object> getRecord() {
        BMap<BString, Object> tmpRegVal = ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.REGISTER,
                new HashMap<>());
        UnionType typ = TypeCreator.createUnionType(tmpRegVal.getType(), PredefinedTypes.TYPE_STRING);
        ArrayType arrTyp = TypeCreator.createArrayType(typ, 2);
        BArray arr = ValueCreator.createArrayValue(arrTyp);
        arr.add(0, operands[0].getOperand());
        arr.add(1, operands[1].getOperand());
        Map<String, Object> fields = new HashMap<>();
        fields.put("result", result.getRecord());
        fields.put("operands", arr);
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.STRING_CONCAT_INSN, fields);
    }
}

class IntNoPanicArithmeticBinaryInsn extends InsnBase {
    public String op;
    public Register result;
    public Operand[] operands = new Operand[2];

    @Override
    public BMap<BString, Object> getRecord() {
        BMap<BString, Object> tmpRegVal = ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.REGISTER,
                new HashMap<>());
        UnionType typ = TypeCreator.createUnionType(tmpRegVal.getType(), PredefinedTypes.TYPE_INT);
        ArrayType arrTyp = TypeCreator.createArrayType(typ, 2);
        BArray arr = ValueCreator.createArrayValue(arrTyp);
        arr.add(0, operands[0].getOperand());
        arr.add(1, operands[1].getOperand());
        Map<String, Object> fields = new HashMap<>();
        fields.put("op", new BmpStringValue(op));
        fields.put("result", result.getRecord());
        fields.put("operands", arr); //TODO create Ballerina array
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.INT_NOPANIC_ARITHMETIC_BINARY_INSN,
                fields);
    }
}

class RetInsn extends InsnBase {
    public Operand operand;

    public RetInsn(Operand operand) {
        this.operand = operand;
    }

    @Override
    public BMap<BString, Object> getRecord() {
        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        Object op = operand.getOperand();
        if (op != null) {
            fields.put("operand", op);
        }
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.RET_INSN, fields);
    }
}

class NullRetInsn extends InsnBase {

    @Override
    public BMap<BString, Object> getRecord() {
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.RET_INSN, new HashMap<>());
    }
}

class TypeCastInsn extends InsnBase {
    public Register result;
    public Register operand;
    public long semType;
    public Position position;

    public TypeCastInsn(Register result, Register operand, long semType, Position position) {
        this.result = result;
        this.operand = operand;
        this.semType = semType;
        this.position = position;
    }

    @Override
    public BMap<BString, Object> getRecord() {
        ObjectType complexSem = TypeCreator.createObjectType("ComplexSemType", ModuleGen.MODTYPES, 268435489);
        UnionType typ = TypeCreator.createUnionType(complexSem, PredefinedTypes.TYPE_INT_UNSIGNED_32);
        ArrayType arrTyp = TypeCreator.createArrayType(typ);
        BArray arr = ValueCreator.createArrayValue(arrTyp);
        arr.append(semType);

        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        fields.put("result", result.getRecord());
        fields.put("operand", operand.getRecord());
        fields.put("semType", arr.get(0));
        fields.put("position", position.getRecord());
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.TYPECAST_INSN, fields);
    }
}

class IntNegateInsn extends InsnBase {
    public Register operand;
    public Register result;

    public IntNegateInsn(Register operand, Register result) {
        this.operand = operand;
        this.result = result;
    }

    @Override
    public BMap<BString, Object> getRecord() {
        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        fields.put("operand", operand.getRecord());
        fields.put("result", result.getRecord());
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.INTNEG_INSN, fields);
    }
}

class BoolNotInsn extends InsnBase {
    public Register operand;
    public Register result;

    public BoolNotInsn(Register operand, Register result) {
        this.operand = operand;
        this.result = result;
    }

    @Override
    public BMap<BString, Object> getRecord() {
        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        fields.put("operand", operand.getRecord());
        fields.put("result", result.getRecord());
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.BOOLNOT_INSN, fields);
    }
}

class AssignInsn extends InsnBase {
    public Register result;
    public Operand operand;

    public AssignInsn(Register result, Operand operand) {
        this.result = result;
        this.operand = operand;
    }

    @Override
    public BMap<BString, Object> getRecord() {
        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        fields.put("operand", operand.getOperand());
        fields.put("result", result.getRecord());
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.ASSIGN_INSN, fields);
    }
}

class ListConstructInsn extends InsnBase {
    public Register result;
    public ArrayList<Operand> operands;

    public ListConstructInsn(Register result, ArrayList<Operand> operands) {
        this.result = result;
        this.operands = operands;
    }

    @Override
    public BMap<BString, Object> getRecord() {
        BMap<BString, Object> tmpRegVal = ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.REGISTER,
                new HashMap<>());
        UnionType typ = TypeCreator.createUnionType(tmpRegVal.getType(), PredefinedTypes.TYPE_INT,
                PredefinedTypes.TYPE_BOOLEAN, PredefinedTypes.TYPE_NULL, PredefinedTypes.TYPE_STRING);
        ArrayType arrTyp = TypeCreator.createArrayType(typ);
        BArray arr = ValueCreator.createArrayValue(arrTyp);
        operands.forEach(op -> arr.append(op.getOperand()));
        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        fields.put("operands", arr);
        fields.put("result", result.getRecord());
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.LIST_CON_INSN, fields);
    }
}

class MapConstructInsn extends InsnBase {
    public Register result;
    public ArrayList<Operand> operands;
    public ArrayList<String> fieldNames;

    public MapConstructInsn(Register result, ArrayList<Operand> operands, ArrayList<String> fieldNames) {
        this.result = result;
        this.operands = operands;
        this.fieldNames = fieldNames;
    }

    @Override
    public BMap<BString, Object> getRecord() {
        BMap<BString, Object> tmpRegVal = ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.REGISTER,
                new HashMap<>());
        UnionType typ = TypeCreator.createUnionType(tmpRegVal.getType(), PredefinedTypes.TYPE_INT,
                PredefinedTypes.TYPE_BOOLEAN, PredefinedTypes.TYPE_NULL, PredefinedTypes.TYPE_STRING);
        ArrayType arrTypOp = TypeCreator.createArrayType(typ);
        BArray arrOp = ValueCreator.createArrayValue(arrTypOp);
        operands.forEach(op -> arrOp.append(op.getOperand()));
        ArrayType arrTypFields = TypeCreator.createArrayType(PredefinedTypes.TYPE_STRING);
        BArray arrFields = ValueCreator.createArrayValue(arrTypFields);
        fieldNames.forEach(field -> arrFields.append(new BmpStringValue(field)));
        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        fields.put("operands", arrOp);
        fields.put("fieldNames", arrFields);
        fields.put("result", result.getRecord());
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.MAP_CON_INSN, fields);
    }
}

class Operand {
    public Register register;
    public Object value;
    public boolean isReg;

    public Operand(boolean isReg) {
        this.isReg = isReg;
    }

    public Object getOperand() {
        if (this.isReg) {
            return this.register.getRecord();
        }
        if (value instanceof String) {
            return StringUtils.fromString((String) value);
        }
        return this.value;
    }
}

class OpBlockHolder {
    public Operand operand;
    public BasicBlock nextBlock;

    public OpBlockHolder(Operand operand, BasicBlock nextBlock) {
        this.operand = operand;
        this.nextBlock = nextBlock;
    }
}

class FunctionRef {
    public Symbol symbol;
    public FunctionSignature signature;

    public FunctionRef(Symbol symbol, FunctionSignature signature) {
        this.symbol = symbol;
        this.signature = signature;
    }

    public Object getRecord() {
        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        fields.put("symbol", symbol.getRecord());
        fields.put("signature", signature.getRecord());
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.FUNCTION_REF, fields);
    }
}

abstract class Symbol {
    public abstract Object getRecord();
};

class ExternalSymbol extends Symbol {
    ModuleId module;
    String identifier;

    public ExternalSymbol(ModuleId module, String identifier) {
        this.module = module;
        this.identifier = identifier;
    }

    @Override
    public BMap<BString, Object> getRecord() {
        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        fields.put("module", module.getRecord());
        fields.put("identifier", new BmpStringValue(identifier));
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.EXTERNAL_SYMBOL, fields);
    }
}

class CallInsn extends InsnBase {
    Register result;
    FunctionRef func;
    ArrayList<Operand> args;

    public CallInsn(Register result, FunctionRef func, ArrayList<Operand> args) {
        this.result = result;
        this.func = func;
        this.args = args;
    }

    @Override
    public BMap<BString, Object> getRecord() {
        BMap<BString, Object> tmpRegVal = ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.REGISTER,
                new HashMap<>());
        UnionType typ = TypeCreator.createUnionType(tmpRegVal.getType(), PredefinedTypes.TYPE_INT,
                PredefinedTypes.TYPE_BOOLEAN, PredefinedTypes.TYPE_NULL, PredefinedTypes.TYPE_STRING);
        ArrayType arrTyp = TypeCreator.createArrayType(typ);
        BArray arr = ValueCreator.createArrayValue(arrTyp);
        args.forEach(arg -> arr.append(arg.getOperand()));
        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        fields.put("result", result.getRecord());
        fields.put("func", func.getRecord());
        fields.put("args", arr);
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.CALL_INSN, fields);
    }
}

class CatchInsn extends InsnBase {
    Register result;

    public CatchInsn(Register result) {
        this.result = result;
    }

    @Override
    public BMap<BString, Object> getRecord() {
        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        fields.put("result", result.getRecord());
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.CATCH_INSN, fields);
    }
}

class AbnormalRetInsn extends InsnBase {
    Register operand;

    public AbnormalRetInsn(Register operand) {
        this.operand = operand;
    }

    @Override
    public BMap<BString, Object> getRecord() {
        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        fields.put("operand", operand.getRecord());
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.ABN_RET_INSN, fields);
    }
}

class EqualityInsn extends InsnBase {
    String op;
    Register result;
    Operand[] operands = new Operand[2];

    public EqualityInsn(String op, Register result) {
        this.op = op;
        this.result = result;
    }

    @Override
    public BMap<BString, Object> getRecord() {
        BMap<BString, Object> tmpRegVal = ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.REGISTER,
                new HashMap<>());
        UnionType typ = TypeCreator.createUnionType(tmpRegVal.getType(), PredefinedTypes.TYPE_INT,
                PredefinedTypes.TYPE_BOOLEAN, PredefinedTypes.TYPE_STRING, PredefinedTypes.TYPE_NULL);
        ArrayType arrTyp = TypeCreator.createArrayType(typ, 2);
        BArray arr = ValueCreator.createArrayValue(arrTyp);
        arr.add(0, operands[0].getOperand());
        arr.add(1, operands[1].getOperand());

        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        fields.put("op", new BmpStringValue(op));
        fields.put("result", result.getRecord());
        fields.put("operands", arr);
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.EQUALITY_INSN, fields);
    }
}

class CompareInsn extends InsnBase {
    String op;
    String orderType;
    Register result;
    Operand[] operands = new Operand[2];

    public CompareInsn(String op, String orderType, Register result) {
        this.op = op;
        this.orderType = orderType;
        this.result = result;
    }

    @Override
    public BMap<BString, Object> getRecord() {
        BMap<BString, Object> tmpRegVal = ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.REGISTER,
                new HashMap<>());
        UnionType typ = TypeCreator.createUnionType(tmpRegVal.getType(), PredefinedTypes.TYPE_INT,
                PredefinedTypes.TYPE_BOOLEAN, PredefinedTypes.TYPE_STRING, PredefinedTypes.TYPE_NULL);
        ArrayType arrTyp = TypeCreator.createArrayType(typ, 2);
        BArray arr = ValueCreator.createArrayValue(arrTyp);
        arr.add(0, operands[0].getOperand());
        arr.add(1, operands[1].getOperand());

        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        fields.put("op", new BmpStringValue(op));
        fields.put("orderType", new BmpStringValue(orderType));
        fields.put("result", result.getRecord());
        fields.put("operands", arr);
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.COMPARE_INSN, fields);
    }
}

class IntBitwiseInsn extends InsnBase {
    String op;
    Register result;
    Operand[] operands = new Operand[2];

    public IntBitwiseInsn(String op, Register result) {
        this.op = op;
        this.result = result;
    }

    @Override
    public BMap<BString, Object> getRecord() {
        BMap<BString, Object> tmpRegVal = ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.REGISTER,
                new HashMap<>());
        UnionType typ = TypeCreator.createUnionType(tmpRegVal.getType(), PredefinedTypes.TYPE_INT);
        ArrayType arrTyp = TypeCreator.createArrayType(typ, 2);
        BArray arr = ValueCreator.createArrayValue(arrTyp);
        arr.add(0, operands[0].getOperand());
        arr.add(1, operands[1].getOperand());

        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        fields.put("op", new BmpStringValue(op));
        fields.put("result", result.getRecord());
        fields.put("operands", arr);
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.INT_BITWISE_INSN, fields);
    }
}

class Position {
    int lineNumber;
    int indexInLine;

    public Position(int lineNumber, int indexInLine) {
        this.lineNumber = lineNumber;
        this.indexInLine = indexInLine;
    }

    public BMap<BString, Object> getRecord() {
        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        fields.put("lineNumber", lineNumber);
        fields.put("indexInLine", indexInLine);
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODERROR, NBTypeNames.POSITION, fields);
    }
}

class ListGetInsn extends InsnBase {
    Register result;
    Register list;
    Operand operand;
    Position position;

    public ListGetInsn(Register result, Register list, Operand operand, Position position) {
        this.result = result;
        this.list = list;
        this.operand = operand;
        this.position = position;
    }

    @Override
    public BMap<BString, Object> getRecord() {
        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        fields.put("result", result.getRecord());
        fields.put("list", list.getRecord());
        fields.put("position", position.getRecord());
        fields.put("operand", operand.getOperand());
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.LIST_GET_INSN, fields);
    }
}

class ListSetInsn extends InsnBase {
    Register list;
    Operand index;
    Operand operand;
    Position position;

    public ListSetInsn(Register list, Operand index, Operand operand, Position position) {
        this.list = list;
        this.index = index;
        this.operand = operand;
        this.position = position;
    }


    @Override
    public BMap<BString, Object> getRecord() {
        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        fields.put("list", list.getRecord());
        fields.put("index", index.getOperand());
        fields.put("position", position.getRecord());
        fields.put("operand", operand.getOperand());
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.LIST_SET_INSN, fields);
    }
}

class MapGetInsn extends InsnBase {
    Register result;
    Operand[] operands = new Operand[2];

    public MapGetInsn(Register result) {
        this.result = result;
    }

    @Override
    public BMap<BString, Object> getRecord() {
        BMap<BString, Object> tmpRegVal = ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.REGISTER,
                new HashMap<>());
        UnionType typ = TypeCreator.createUnionType(tmpRegVal.getType(), PredefinedTypes.TYPE_STRING);
        ArrayList<Type> typarr = new ArrayList<>();
        typarr.add(tmpRegVal.getType());
        typarr.add(typ);
        TupleType tupTyp = TypeCreator.createTupleType(typarr, 6);
        BArray tup = ValueCreator.createTupleValue(tupTyp);

        tup.add(0, operands[0].getOperand());
        tup.add(1, operands[1].getOperand());
        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        fields.put("result", result.getRecord());
        fields.put("operands", tup);
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.MAP_GET_INSN, fields);
    }
}

class MapSetInsn extends InsnBase {
    Operand[] operands = new Operand[3];
    Position position;

    public MapSetInsn(Position position) {
        this.position = position;
    }


    @Override
    public BMap<BString, Object> getRecord() {
        BMap<BString, Object> tmpRegVal = ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.REGISTER,
                new HashMap<>());
        UnionType typ1 = TypeCreator.createUnionType(tmpRegVal.getType(), PredefinedTypes.TYPE_STRING);
        UnionType typ2 = TypeCreator.createUnionType(tmpRegVal.getType(), PredefinedTypes.TYPE_INT,
                PredefinedTypes.TYPE_BOOLEAN, PredefinedTypes.TYPE_STRING, PredefinedTypes.TYPE_NULL);
        ArrayList<Type> typarr = new ArrayList<>();
        typarr.add(tmpRegVal.getType());
        typarr.add(typ1);
        typarr.add(typ2);
        TupleType tupTyp = TypeCreator.createTupleType(typarr, 6);
        BArray tup = ValueCreator.createTupleValue(tupTyp);

        tup.add(0, operands[0].getOperand());
        tup.add(1, operands[1].getOperand());
        tup.add(2, operands[2].getOperand());
        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        fields.put("position", position.getRecord());
        fields.put("operands", tup);
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.MAP_SET_INSN, fields);
    }
}
