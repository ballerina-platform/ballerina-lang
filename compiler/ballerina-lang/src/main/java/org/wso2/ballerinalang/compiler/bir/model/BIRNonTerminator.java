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
package org.wso2.ballerinalang.compiler.bir.model;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SchedulerPolicy;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.List;

/**
 * A non-terminating instruction.
 * <p>
 * Non-terminating instructions do not terminate a basic block.
 *
 * @since 0.980.0
 */
public abstract class BIRNonTerminator extends BIRAbstractInstruction implements BIRInstruction {

    public BIRNonTerminator(DiagnosticPos pos, InstructionKind kind) {
        super(pos, kind);
    }

    @Override
    public InstructionKind getKind() {
        return this.kind;
    }

    /**
     * A move instruction that copy a value from variable to a temp location, vice versa.
     * <p>
     * e.g., _1 = move _2
     *
     * @since 0.980.0
     */
    public static class Move extends BIRNonTerminator implements BIRAssignInstruction {
        public BIROperand rhsOp;

        public Move(DiagnosticPos pos, BIROperand fromOperand, BIROperand toOperand) {
            super(pos, InstructionKind.MOVE);
            this.rhsOp = fromOperand;
            this.lhsOp = toOperand;
        }

        @Override
        public BIROperand getLhsOperand() {
            return lhsOp;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A binary operator instruction.
     * <p>
     * e.g., _1 = add _2 _3
     *
     * @since 0.980.0
     */
    public static class BinaryOp extends BIRNonTerminator implements BIRAssignInstruction {
        public BIROperand rhsOp1;
        public BIROperand rhsOp2;

        public BinaryOp(DiagnosticPos pos,
                        InstructionKind kind,
                        BType type,
                        BIROperand lhsOp,
                        BIROperand rhsOp1,
                        BIROperand rhsOp2) {
            super(pos, kind);
            this.lhsOp = lhsOp;
            this.rhsOp1 = rhsOp1;
            this.rhsOp2 = rhsOp2;
        }

        @Override
        public BIROperand getLhsOperand() {
            return lhsOp;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A unary operator instruction.
     * <p>
     * e.g., _1 = minus _2
     *
     * @since 0.980.0
     */
    public static class UnaryOP extends BIRNonTerminator implements BIRAssignInstruction {
        public BIROperand rhsOp;

        public UnaryOP(DiagnosticPos pos, InstructionKind kind, BIROperand lhsOp, BIROperand rhsOp) {
            super(pos, kind);
            this.lhsOp = lhsOp;
            this.rhsOp = rhsOp;
        }

        @Override
        public BIROperand getLhsOperand() {
            return lhsOp;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A constant value load instruction.
     * <p>
     * e.g., _1 = const 10 (int)
     *
     * @since 0.980.0
     */
    public static class ConstantLoad extends BIRNonTerminator implements BIRAssignInstruction {
        public Object value;
        public BType type;

        public ConstantLoad(DiagnosticPos pos, Object value, BType type, BIROperand lhsOp) {
            super(pos, InstructionKind.CONST_LOAD);
            this.value = value;
            this.type = type;
            this.lhsOp = lhsOp;
        }

        @Override
        public BIROperand getLhsOperand() {
            return lhsOp;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A new map instruction.
     * <p>
     * e.g., map a = {}
     *
     * @since 0.980.0
     */
    public static class NewStructure extends BIRNonTerminator {
        public BIROperand rhsOp;
        public List<BIRMappingConstructorEntry> initialValues;

        public NewStructure(DiagnosticPos pos, BIROperand lhsOp, BIROperand rhsOp) {
            super(pos, InstructionKind.NEW_STRUCTURE);
            this.lhsOp = lhsOp;
            this.rhsOp = rhsOp;
            this.initialValues = new ArrayList<>();
        }

        public NewStructure(DiagnosticPos pos, BIROperand lhsOp, BIROperand rhsOp,
                            List<BIRMappingConstructorEntry> initialValues) {
            super(pos, InstructionKind.NEW_STRUCTURE);
            this.lhsOp = lhsOp;
            this.rhsOp = rhsOp;
            this.initialValues = initialValues;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A new instruction.
     * <p>
     * e.g., object{int i;}  a = new;
     *
     * @since 0.995.0
     */
    public static class NewInstance extends BIRNonTerminator {
        public final boolean isExternalDef;
        public final PackageID externalPackageId;
        public BIRTypeDefinition def;
        public final String objectName;

        public NewInstance(DiagnosticPos pos, BIRTypeDefinition def, BIROperand lhsOp) {
            super(pos, InstructionKind.NEW_INSTANCE);
            this.lhsOp = lhsOp;
            this.def = def;
            this.objectName = null;
            this.externalPackageId = null;
            this.isExternalDef = false;
        }

        public NewInstance(DiagnosticPos pos, PackageID externalPackageId, String objectName, BIROperand lhsOp) {
            super(pos, InstructionKind.NEW_INSTANCE);
            this.objectName = objectName;
            this.lhsOp = lhsOp;
            this.def = null;
            this.externalPackageId = externalPackageId;
            this.isExternalDef = true;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A new array instruction.
     * <p>
     * e.g., int[] a = {}
     *
     * @since 0.980.0
     */
    public static class NewArray extends BIRNonTerminator {
        public BIROperand sizeOp;
        public BType type;
        public List<BIROperand> values;

        public NewArray(DiagnosticPos pos, BType type, BIROperand lhsOp, BIROperand sizeOp, List<BIROperand> values) {
            super(pos, InstructionKind.NEW_ARRAY);
            this.type = type;
            this.lhsOp = lhsOp;
            this.sizeOp = sizeOp;
            this.values = values;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A field access expression.
     * <p>
     * e.g., a["b"] = 10 (int)
     * or
     * _1 = mapload _3 _2
     *
     * @since 0.980.0
     */
    public static class FieldAccess extends BIRNonTerminator {
        public BIROperand keyOp;
        public BIROperand rhsOp;
        public boolean optionalFieldAccess = false;
        public boolean fillingRead = false;
        public boolean onInitialization = false;

        public FieldAccess(DiagnosticPos pos, InstructionKind kind,
                           BIROperand lhsOp, BIROperand keyOp, BIROperand rhsOp) {
            super(pos, kind);
            this.lhsOp = lhsOp;
            this.keyOp = keyOp;
            this.rhsOp = rhsOp;
        }

        public FieldAccess(DiagnosticPos pos, InstructionKind kind,
                           BIROperand lhsOp, BIROperand keyOp, BIROperand rhsOp, boolean onInitialization) {
            super(pos, kind);
            this.lhsOp = lhsOp;
            this.keyOp = keyOp;
            this.rhsOp = rhsOp;
            this.onInitialization = onInitialization;
        }

        public FieldAccess(DiagnosticPos pos, InstructionKind kind,
                           BIROperand lhsOp, BIROperand keyOp, BIROperand rhsOp, boolean optionalFieldAccess,
                           boolean fillingRead) {
            super(pos, kind);
            this.lhsOp = lhsOp;
            this.keyOp = keyOp;
            this.rhsOp = rhsOp;
            this.optionalFieldAccess = optionalFieldAccess;
            this.fillingRead = fillingRead;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * An error constructor expression.
     * <p>
     * error(reason as string, detail as map)
     *
     * @since 0.995.0
     */
    public static class NewError extends BIRNonTerminator {

        public BType type;

        public BIROperand messageOp;
        public BIROperand causeOp;
        public BIROperand detailOp;
        
        public NewError(DiagnosticPos pos, BType type, BIROperand lhsOp, BIROperand messageOp, BIROperand causeOp,
                        BIROperand detailOp) {
            super(pos, InstructionKind.NEW_ERROR);
            this.type = type;
            this.lhsOp = lhsOp;
            this.messageOp = messageOp;
            this.causeOp = causeOp;
            this.detailOp = detailOp;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A type cast expression.
     * <p>
     * e.g., int a = cast(int) b;
     *
     * @since 0.980.0
     */
    public static class TypeCast extends BIRNonTerminator {
        public BIROperand rhsOp;
        public BType type;
        public boolean checkTypes;

        public TypeCast(DiagnosticPos pos, BIROperand lhsOp, BIROperand rhsOp, BType castType, boolean checkTypes) {
            super(pos, InstructionKind.TYPE_CAST);
            this.lhsOp = lhsOp;
            this.rhsOp = rhsOp;
            this.type = castType;
            this.checkTypes = checkTypes;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A is like instruction.
     * <p>
     * e.g., a isLike b
     *
     * @since 0.980.0
     */
    public static class IsLike extends BIRNonTerminator {
        public BIROperand rhsOp;
        public BType type;

        public IsLike(DiagnosticPos pos, BType type, BIROperand lhsOp, BIROperand rhsOp) {
            super(pos, InstructionKind.IS_LIKE);
            this.type = type;
            this.lhsOp = lhsOp;
            this.rhsOp = rhsOp;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A type test instruction.
     * <p>
     * e.g., a is int
     *
     * @since 0.980.0
     */
    public static class TypeTest extends BIRNonTerminator {
        public BIROperand rhsOp;
        public BType type;

        public TypeTest(DiagnosticPos pos, BType type, BIROperand lhsOp, BIROperand rhsOp) {
            super(pos, InstructionKind.TYPE_TEST);
            this.type = type;
            this.lhsOp = lhsOp;
            this.rhsOp = rhsOp;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * New XML element instruction.
     * 
     * @since 0.995.0
     */
    public static class NewXMLElement extends BIRNonTerminator {
        public BIROperand startTagOp;
        public BIROperand defaultNsURIOp;
        public boolean readonly;

        public NewXMLElement(DiagnosticPos pos, BIROperand lhsOp, BIROperand startTagOp, BIROperand defaultNsURIOp,
                             boolean readonly) {
            super(pos, InstructionKind.NEW_XML_ELEMENT);
            this.lhsOp = lhsOp;
            this.startTagOp = startTagOp;
            this.defaultNsURIOp = defaultNsURIOp;
            this.readonly = readonly;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * New XML QName instruction.
     * <p>
     * e.g.: {@code ns0:foo}
     * 
     * @since 0.995.0
     */
    public static class NewXMLQName extends BIRNonTerminator {
        public BIROperand localnameOp;
        public BIROperand nsURIOp;
        public BIROperand prefixOp;

        public NewXMLQName(DiagnosticPos pos, BIROperand lhsOp, BIROperand localnameOp, BIROperand nsURIOp,
                BIROperand prefixOp) {
            super(pos, InstructionKind.NEW_XML_QNAME);
            this.lhsOp = lhsOp;
            this.localnameOp = localnameOp;
            this.nsURIOp = nsURIOp;
            this.prefixOp = prefixOp;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * New XML QName from a string.
     * <p>
     * e.g.: {@code "{http://nsuri/}foo"}
     * 
     * @since 0.995.0
     */
    public static class NewStringXMLQName extends BIRNonTerminator {
        public BIROperand stringQNameOP;

        public NewStringXMLQName(DiagnosticPos pos, BIROperand lhsOp, BIROperand stringQName) {
            super(pos, InstructionKind.NEW_STRING_XML_QNAME);
            this.lhsOp = lhsOp;
            this.stringQNameOP = stringQName;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * New XML text instruction.
     * 
     * @since 0.995.0
     */
    public static class NewXMLText extends BIRNonTerminator {
        public BIROperand textOp;

        public NewXMLText(DiagnosticPos pos, BIROperand lhsOp, BIROperand textOp) {
            super(pos, InstructionKind.NEW_XML_TEXT);
            this.lhsOp = lhsOp;
            this.textOp = textOp;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * New XML text instruction.
     * 
     * @since 0.995.0
     */
    public static class NewXMLProcIns extends BIRNonTerminator {
        public BIROperand dataOp;
        public BIROperand targetOp;
        public boolean readonly;

        public NewXMLProcIns(DiagnosticPos pos, BIROperand lhsOp, BIROperand dataOp, BIROperand targetOp,
                             boolean readonly) {
            super(pos, InstructionKind.NEW_XML_PI);
            this.lhsOp = lhsOp;
            this.dataOp = dataOp;
            this.targetOp = targetOp;
            this.readonly = readonly;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * New XML comment instruction.
     * 
     * @since 0.995.0
     */
    public static class NewXMLComment extends BIRNonTerminator {
        public BIROperand textOp;
        public boolean readonly;

        public NewXMLComment(DiagnosticPos pos, BIROperand lhsOp, BIROperand textOp, boolean readonly) {
            super(pos, InstructionKind.NEW_XML_COMMENT);
            this.lhsOp = lhsOp;
            this.textOp = textOp;
            this.readonly = readonly;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * XML access expression with two operands.
     * e.g: {@code InstructionKind.XML_SEQ_STORE}, {@code InstructionKind.XML_LOAD_ALL}
     *
     * @since 0.995.0
     */
    public static class XMLAccess extends BIRNonTerminator {
        public BIROperand rhsOp;

        public XMLAccess(DiagnosticPos pos, InstructionKind kind, BIROperand lhsOp, BIROperand rhsOp) {
            super(pos, kind);
            this.lhsOp = lhsOp;
            this.rhsOp = rhsOp;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A FP load instruction.
     * <p>
     * e.g., function (string, string) returns (string) anonFunction =
     *             function (string x, string y) returns (string) {
     *                 return x + y;
     *             };
     *
     * @since 0.995.0
     */
    public static class FPLoad extends BIRNonTerminator {
        public SchedulerPolicy schedulerPolicy;
        public Name funcName;
        public PackageID pkgId;
        public List<BIRVariableDcl> params;
        public List<BIROperand> closureMaps;
        public BType retType;

        public FPLoad(DiagnosticPos pos, PackageID pkgId, Name funcName, BIROperand lhsOp,
                      List<BIRVariableDcl> params, List<BIROperand> closureMaps, BType retType,
                      SchedulerPolicy schedulerPolicy) {
            super(pos, InstructionKind.FP_LOAD);
            this.schedulerPolicy = schedulerPolicy;
            this.lhsOp = lhsOp;
            this.funcName = funcName;
            this.pkgId = pkgId;
            this.params = params;
            this.closureMaps = closureMaps;
            this.retType = retType;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * The new table instruction.
     */
    public static class NewTable extends BIRNonTerminator {
        public BIROperand keyColOp;
        public BIROperand dataOp;
        public BType type;

        public NewTable(DiagnosticPos pos, BType type, BIROperand lhsOp, BIROperand keyColOp,
                        BIROperand dataOp) {
            super(pos, InstructionKind.NEW_TABLE);
            this.type = type;
            this.lhsOp = lhsOp;
            this.keyColOp = keyColOp;
            this.dataOp = dataOp;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A type cast expression.
     * <p>
     * e.g., int a = cast(int) b;
     *
     * @since 0.995.0
     */
    public static class NewTypeDesc extends BIRNonTerminator {
        public final List<BIROperand> closureVars;
        public BType type;

        public NewTypeDesc(DiagnosticPos pos, BIROperand lhsOp, BType type, List<BIROperand> closureVars) {
            super(pos, InstructionKind.NEW_TYPEDESC);
            this.closureVars = closureVars;
            this.lhsOp = lhsOp;
            this.type = type;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

}
