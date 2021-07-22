package org.wso2.ballerinalang.compiler.nballerina;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.values.BmpStringValue;
import org.ballerinalang.model.types.TypeKind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

class JNModule {
    public ArrayList<FunctionCode> code = new ArrayList<>();
    public ArrayList<FunctionDefn> functionDefns = new ArrayList<>();
    public ModuleId moduleId;

    BArray getFuncDefsArray() {
        BMap<BString, Object> tmpVal = ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR,
                NBTypeNames.FUNCTION_DEFN, new HashMap<>());
        ArrayType arrTyp = TypeCreator.createArrayType(tmpVal.getType());
        BArray arr = ValueCreator.createArrayValue(arrTyp);
        functionDefns.forEach(def -> arr.append(def.getRecord()));
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
}

class ModuleId {
    String organization;
    ArrayList<String> names = new ArrayList<>();

    BMap<BString, Object> getRecord() {
        ArrayType arrTyp = TypeCreator.createArrayType(PredefinedTypes.TYPE_STRING);
        BArray arr = ValueCreator.createArrayValue(arrTyp);
        names.forEach(name -> arr.append(new BmpStringValue(name)));
        Map<String, Object> fields = new HashMap<>();
        if (organization != null) {
            fields.put("organization", new BmpStringValue(organization));
        }
        fields.put("names", arr);
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.MODULE_ID, fields);
    }
}

class FunctionDefn {
    InternalSymbol symbol = new InternalSymbol();
    FunctionSignature signature = new FunctionSignature();

    public FunctionDefn(boolean isPublic, String identifier, TypeKind returnType) {
        this.symbol.isPublic = isPublic;
        this.symbol.identifier = identifier;
        this.signature.returnType = returnType;
    }

    BMap<BString, Object> getRecord() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("symbol", symbol.getRecord());
        fields.put("signature", signature.getRecord());
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.FUNCTION_DEFN, fields);
    }
}

class FunctionSignature {
    TypeKind returnType;
    ArrayList<TypeKind> paramTypes = new ArrayList<>();
    int restParamType;

    public BMap<BString, Object> getRecord() {
        ObjectType complexSem = TypeCreator.createObjectType("ComplexSemType", ModuleGen.MODTYPES, 268435489);
        UnionType typ = TypeCreator.createUnionType(complexSem, PredefinedTypes.TYPE_INT_UNSIGNED_32);
        ArrayType arrTyp = TypeCreator.createArrayType(typ);
        BArray arr = ValueCreator.createArrayValue(arrTyp);
        paramTypes.forEach(param -> arr.append(ModuleGen.convertSimpleSemType(param)));
        Map<String, Object> fields = new HashMap<>();
        fields.put("returnType", ModuleGen.convertSimpleSemType(returnType)); //TODO convert to SemType
        fields.put("paramTypes", arr); //TODO create SemType BArray
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.FUNCTION_SIGNATURE, fields);
    }
}

class InternalSymbol {
    public boolean isPublic;
    public String identifier;

    public BMap<BString, Object> getRecord() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("isPublic", isPublic);
        fields.put("identifier", identifier);
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.INTERNAL_SYMBOL, fields);
    }
}

class FunctionCode {
    public ArrayList<BasicBlock> blocks = new ArrayList<>();
    public Map<String, Register> registers = new HashMap<>();

    BasicBlock createBasicBlock() {
        BasicBlock bb = new BasicBlock(this.blocks.size());
        this.blocks.add(bb);
        return bb;
    }

    Register createRegister(TypeKind semType, String varName) {
        Register r = new Register(this.registers.size(), semType, varName);
        this.registers.put(varName, r);
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
        registers.forEach((name, register) -> rArr.append(register.getRecord()));
        Map<String, Object> fields = new HashMap<>();
        fields.put("blocks", bArr);
        fields.put("registers", rArr);
        return ValueCreator.createRecordValue(ModuleGen.MODBIR, "FunctionCode", fields);
    }
}

class Register {
    public int number;
    public TypeKind semType;
    public String varName;

    public Register(int number, TypeKind semType, String varName) {
        this.number = number;
        this.semType = semType; //TODO convert to SemType
        this.varName = varName;
    }

    public BMap<BString, Object> getRecord() {
        ObjectType complexSem = TypeCreator.createObjectType("ComplexSemType", ModuleGen.MODTYPES, 268435489);
        UnionType typ = TypeCreator.createUnionType(complexSem, PredefinedTypes.TYPE_INT_UNSIGNED_32);
        ArrayType arrTyp = TypeCreator.createArrayType(typ);
        BArray arr = ValueCreator.createArrayValue(arrTyp);
        arr.append(ModuleGen.convertSimpleSemType(semType));
        Map<String, Object> fields = new HashMap<>();
        fields.put("number", number);
        if (varName != null) {
            fields.put("varName", new BmpStringValue(varName));
        }
        fields.put("semType", arr.get(0)); //TODO change to SemType Value
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.REGISTER, fields);
    }
}

class BasicBlock {
    public int label;
    public int onPanic;
    public ArrayList<InsnBase> insns = new ArrayList<>();

    public BasicBlock(int label) {
        this.label = label;
    }

    public BMap<BString, Object> getRecord() {
        BMap<BString, Object> tmpVal = ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.RET_INSN,
                new HashMap<>()); //TODO add other insns
        UnionType insnTyp = TypeCreator.createUnionType(tmpVal.getType());
        ArrayType arrTyp = TypeCreator.createArrayType(insnTyp);
        BArray arr = ValueCreator.createArrayValue(arrTyp);
        insns.forEach(insn -> arr.append(insn.getRecord()));
        Map<String, Object> fields = new HashMap<>();
        fields.put("label", label);
        fields.put("insns", arr);
        return ValueCreator.createRecordValue(ModuleGen.MODBIR, "BasicBlock", fields);
    }
}

abstract class InsnBase {
    public String name; //InsnName
    public abstract BMap<BString, Object> getRecord();

}

class IntArithmeticBinaryInsn extends InsnBase {
    public String op;
    public Register result;
    public Object[] operands = new Object[2];

    @Override
    public BMap<BString, Object> getRecord() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("op", new BmpStringValue(op));
        fields.put("result", result.getRecord());
        fields.put("operands", operands); //TODO create Ballerina array
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.INT_ARITHMETIC_BINARY_INSN, fields);
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
        Object op = operand.isReg ? operand.register.getRecord() : operand.constant;
        fields.put("operand", op);
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.RET_INSN, fields);
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
        fields.put("operand", operand);
        fields.put("result", result);
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
        fields.put("operand", operand);
        fields.put("result", result);
        return ValueCreator.createReadonlyRecordValue(ModuleGen.MODBIR, NBTypeNames.BOOLNOT_INSN, fields);
    }
}

class Operand {
    public Register register;
    public Object constant;
    public boolean isReg;

    public Operand(boolean isReg) {
        this.isReg = isReg;
    }
}
