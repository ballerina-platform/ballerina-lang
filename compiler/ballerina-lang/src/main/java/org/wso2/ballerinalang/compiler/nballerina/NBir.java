package org.wso2.ballerinalang.compiler.nballerina;

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.values.BmpStringValue;
import org.ballerinalang.model.types.TypeKind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class JNModule {
    public ArrayList<FunctionCode> code = new ArrayList<>();
    public ArrayList<FunctionDefn> functionDefns = new ArrayList<>();
}

class FunctionDefn{
    boolean isPublic;
    String identifier;
    TypeKind returnType;
    ArrayList<TypeKind> paramTypes = new ArrayList<>();
    int restParamType;

    public FunctionDefn(boolean isPublic, String identifier, TypeKind returnType) {
        this.isPublic = isPublic;
        this.identifier = identifier;
        this.returnType = returnType;
    }
}

class FunctionCode {
    public ArrayList<BasicBlock> blocks = new ArrayList<>();
    public ArrayList<Register> registers = new ArrayList<>();

    BasicBlock createBasicBlock(){
        BasicBlock bb = new BasicBlock(this.blocks.size());
        this.blocks.add(bb);
        return bb;
    }

    Register createRegister(TypeKind semType, String varName){
        Register r = new Register(this.registers.size(), semType, varName);
        this.registers.add(r);
        return r;
    }
}

class Register {
    public int number;
    public TypeKind semType;
    public String varName;

    public Register(int number, TypeKind semType, String varName) {
        this.number = number;
        this.semType = semType;
        this.varName = varName;
    }

    public Object getRecord(){
        Map<String, Object> fields = new HashMap<>();
        fields.put("number", number);
        fields.put("varName", varName);
        fields.put("semType", semType); //TODO change to SemType Value
        return ValueCreator.createRecordValue(ModuleGen.modBir, NBTypeNames.Register, fields);
    }
}

class BasicBlock {
    public int label;
    public int onPanic;
    public ArrayList<InsnBase> insns;

    public BasicBlock(int label) {
        this.label = label;
    }

    BMap<BString, Object> getRecord(){
        Map<String, Object> fields = new HashMap<>();
        fields.put("label", label);
        fields.put("insn", insns); //TODO create insn BArray
        return ValueCreator.createRecordValue(ModuleGen.modBir, "BasicBlock", fields);
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
        return ValueCreator.createRecordValue(ModuleGen.modBir, NBTypeNames.IntArithmeticBinaryInsn,
                fields);
    }
}

class RetInsn extends InsnBase {
    public Register operandr;
    public Object operandc;

    public RetInsn(Object operandc) {
        this.operandc = operandc;
    }

    @Override
    public BMap<BString, Object> getRecord() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("operand", operandr.getRecord());
        return ValueCreator.createRecordValue(ModuleGen.modBir, NBTypeNames.RetInsn,
                fields);
    }
}
