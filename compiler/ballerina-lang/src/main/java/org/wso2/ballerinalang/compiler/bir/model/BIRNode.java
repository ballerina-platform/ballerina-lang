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

import org.ballerinalang.model.elements.AttachPoint;
import org.ballerinalang.model.elements.MarkdownDocAttachment;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Root class of Ballerina intermediate representation-BIR.
 *
 * @since 0.980.0
 */
public abstract class BIRNode {
    public DiagnosticPos pos;

    public BIRNode(DiagnosticPos pos) {
        this.pos = pos;
    }

    public abstract void accept(BIRVisitor visitor);

    /**
     * A package definition.
     *
     * @since 0.980.0
     */
    public static class BIRPackage extends BIRNode {
        public Name org;
        public Name name;
        public Name version;
        public Name sourceFileName;
        public List<BIRImportModule> importModules;
        public List<BIRTypeDefinition> typeDefs;
        public List<BIRGlobalVariableDcl> globalVars;
        public List<BIRFunction> functions;
        public List<BIRAnnotation> annotations;
        public List<BIRConstant> constants;

        public BIRPackage(DiagnosticPos pos, Name org, Name name, Name version,
                          Name sourceFileName) {
            super(pos);
            this.org = org;
            this.name = name;
            this.version = version;
            this.sourceFileName = sourceFileName;
            this.importModules = new ArrayList<>();
            this.typeDefs = new ArrayList<>();
            this.globalVars = new ArrayList<>();
            this.functions = new ArrayList<>();
            this.annotations = new ArrayList<>();
            this.constants = new ArrayList<>();
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
        
        public Name getSourceFileName() {
            return sourceFileName;
        }
    }

    /**
     * An import package definition.
     *
     * @since 0.990.0
     */
    public static class BIRImportModule extends BIRNode {
        public Name org;
        public Name name;
        public Name version;

        public BIRImportModule(DiagnosticPos pos, Name org, Name name, Name version) {
            super(pos);
            this.org = org;
            this.name = name;
            this.version = version;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A variable declaration.
     *
     * @since 0.980.0
     */
    public static class BIRVariableDcl extends BIRDocumentableNode {
        public BType type;
        public Name name;
        public String metaVarName;
        public VarKind kind;
        public VarScope scope;
        public boolean ignoreVariable;
        public BIRBasicBlock endBB;
        public BIRBasicBlock startBB;
        public int insOffset;

        public BIRVariableDcl(DiagnosticPos pos, BType type, Name name, VarScope scope,
                              VarKind kind, String metaVarName) {
            super(pos);
            this.type = type;
            this.name = name;
            this.scope = scope;
            this.kind = kind;
            this.metaVarName = metaVarName;
        }

        public BIRVariableDcl(BType type, Name name, VarScope scope, VarKind kind) {
            this(null, type, name, scope, kind, null);
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (!(other instanceof BIRVariableDcl)) {
                return false;
            }

            BIRVariableDcl otherVarDecl = (BIRVariableDcl) other;

            // Here we assume names are unique.
            return this.name.equals(otherVarDecl.name);
        }

        @Override
        public int hashCode() {
            return this.name.value.hashCode();
        }
        
        @Override
        public String toString() {
            return name.toString();
        }
    }

    /**
     * A parameter.
     *
     * @since 0.995.0
     */
    public static class BIRParameter extends BIRNode {
        public Name name;
        public int flags;

        public BIRParameter(DiagnosticPos pos, Name name, int flags) {
            super(pos);
            this.name = name;
            this.flags = flags;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A global variable declaration.
     *
     * @since 0.980.0
     */
    public static class BIRGlobalVariableDcl extends BIRVariableDcl {
        /**
         * Value represents Flags.
         */
        public int flags;
        public PackageID pkgId;

        public BIRGlobalVariableDcl(DiagnosticPos pos, int flags, BType type, PackageID pkgId, Name name,
                                    VarScope scope, VarKind kind, String metaVarName) {
            super(pos, type, name, scope, kind, metaVarName);
            this.flags = flags;
            this.pkgId = pkgId;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A function parameter declaration.
     *
     * @since 0.995.0
     */
    public static class BIRFunctionParameter extends BIRVariableDcl {
        public final boolean hasDefaultExpr;

        public BIRFunctionParameter(DiagnosticPos pos, BType type, Name name,
                                    VarScope scope, VarKind kind, String metaVarName, boolean hasDefaultExpr) {
            super(pos, type, name, scope, kind, metaVarName);
            this.hasDefaultExpr = hasDefaultExpr;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A function definition.
     *
     * @since 0.980.0
     */
    public static class BIRFunction extends BIRDocumentableNode {

        /**
         * Name of the function.
         */
        public Name name;

        /**
         * Value represents flags.
         */
        public int flags;

        /**
         * Type of this function. e.g., (int, int) returns (int).
         */
        public BInvokableType type;

        /**
         * List of required parameters.
         */
        public List<BIRParameter> requiredParams;

        /**
         * Receiver. This is an optional field.
         */
        public BIRVariableDcl receiver;

        /**
         * Rest parameter.
         */
        public BIRParameter restParam;

        /**
         * Number of function arguments.
         */
        public int argsCount;

        /**
         * User defined local variables of this function.
         * <p>
         * First variable is reserved to store the return value of this function. The next 'argsCount'
         * entries are allocated for function arguments. The rest are for user-defined local variables and
         * temporary variables.
         */
        public List<BIRVariableDcl> localVars;

        public BIRVariableDcl returnVariable;

        /**
         * Variable used for parameters of this function.
         */
        public Map<BIRFunctionParameter, List<BIRBasicBlock>>  parameters;

        /**
         * List of basic blocks in this function.
         */
        public List<BIRBasicBlock> basicBlocks;

        /**
         * List of error entries in this function.
         */
        public List<BIRErrorEntry> errorTable;

        /**
         * Name of the current worker.
         */
        public Name workerName;

        /**
         * List of channels this worker interacts.
         */
        public ChannelDetails[] workerChannels;

        /**
         * Taint table for the function.
         */
        public TaintTable taintTable;

        public List<BIRAnnotationAttachment> annotAttachments;

        public BIRFunction(DiagnosticPos pos, Name name, int flags, BInvokableType type, Name workerName,
                int sendInsCount, TaintTable taintTable) {
            super(pos);
            this.name = name;
            this.flags = flags;
            this.type = type;
            this.localVars = new ArrayList<>();
            this.parameters = new LinkedHashMap<>();
            this.requiredParams = new ArrayList<>();
            this.basicBlocks = new ArrayList<>();
            this.errorTable = new ArrayList<>();
            this.workerName = workerName;
            this.workerChannels = new ChannelDetails[sendInsCount];
            this.taintTable = taintTable;
            this.annotAttachments = new ArrayList<>();
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A basic block definition.
     *
     * @since 0.980.0
     */
    public static class BIRBasicBlock extends BIRNode {
        public Name id;
        public List<BIRNonTerminator> instructions;
        public BIRTerminator terminator;

        public BIRBasicBlock(Name id) {
            super(null);
            this.id = id;
            this.instructions = new ArrayList<>();
            this.terminator = null;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public String toString() {
            return id.value;
        }
    }

    /**
     * Type definition node in BIR.
     *
     * @since 0.995.0
     */
    public static class BIRTypeDefinition extends BIRDocumentableNode {

        /**
         * Name of the type definition.
         */
        public Name name;

        public List<BIRFunction> attachedFuncs;

        public int flags;

        public BType type;

        public boolean isLabel;

        public List<BType> referencedTypes;

        /**
         * this is not serialized. it's used to keep the index of the def in the list.
         * otherwise the writer has to *find* it in the list.
         */
        public int index;

        public BIRTypeDefinition(DiagnosticPos pos, Name name, int flags, boolean isLabel,
                                 BType type, List<BIRFunction> attachedFuncs) {
            super(pos);
            this.name = name;
            this.flags = flags;
            this.isLabel = isLabel;
            this.type = type;
            this.attachedFuncs = attachedFuncs;
            this.referencedTypes = new ArrayList<>();
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public String toString() {
            return String.valueOf(type) + " " + String.valueOf(name);
        }
    }

    /**
     * An error entry in the error table.
     *
     * @since 0.995.0
     */
    public static class BIRErrorEntry extends BIRNode {

        public BIRBasicBlock trapBB;

        // this is inclusive
        public BIRBasicBlock endBB;

        public BIROperand errorOp;

        public BIRBasicBlock targetBB;

        public BIRErrorEntry(BIRBasicBlock trapBB, BIRBasicBlock endBB, BIROperand errorOp, BIRBasicBlock targetBB) {
            super(null);
            this.trapBB = trapBB;
            this.endBB = endBB;
            this.errorOp = errorOp;
            this.targetBB = targetBB;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * Channel details which has channel name and where it resides.
     *
     * @since 0.995.0
     */
    public static class ChannelDetails {
        public String name;
        public boolean channelInSameStrand;
        public boolean send;

        public ChannelDetails(String name, boolean channelInSameStrand, boolean send) {
            this.name = name;
            this.channelInSameStrand = channelInSameStrand;
            this.send = send;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Annotation definition node in BIR.
     *
     * @since 0.995.0
     */
    public static class BIRAnnotation extends BIRNode {
        /**
         * Name of the annotation definition.
         */
        public Name name;

        /**
         * Value represents flags.
         */
        public int flags;

        /**
         * Attach points, this is needed only in compiled symbol enter as it is.
         */
        public Set<AttachPoint> attachPoints;

        /**
         * Type of the annotation body.
         */
        public BType annotationType;

        public BIRAnnotation(DiagnosticPos pos, Name name, int flags,
                             Set<AttachPoint> points, BType annotationType) {
            super(pos);
            this.name = name;
            this.flags = flags;
            this.attachPoints = points;
            this.annotationType = annotationType;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }

    }

    /**
     * Constant definition node in BIR.
     *
     * @since 0.995.0
     */
    public static class BIRConstant extends BIRDocumentableNode {
        /**
         * Name of the constant.
         */
        public Name name;

        /**
         * Value for the Flags.
         */
        public int flags;

        /**
         * Type of the constant symbol.
         */
        public BType type;

        /**
         * Value of the constant.
         */
        public ConstValue constValue;

        public BIRConstant(DiagnosticPos pos, Name name, int flags,
                           BType type, ConstValue constValue) {
            super(pos);
            this.name = name;
            this.flags = flags;
            this.type = type;
            this.constValue = constValue;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }

    }

    /**
     * Represents an annotation attachment in BIR node tree.
     *
     * @since 1.0.0
     */
    public static class BIRAnnotationAttachment extends BIRNode {

        public PackageID packageID;
        public Name annotTagRef;

        // The length == 0 means that the value of this attachment is 'true'
        // The length > 1 means that there are one or more attachments of this annotation
        public List<BIRAnnotationValue> annotValues;

        public BIRAnnotationAttachment(DiagnosticPos pos, Name annotTagRef) {
            super(pos);
            this.annotTagRef = annotTagRef;
            this.annotValues = new ArrayList<>();
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * Represents one value in an annotation attachment.
     *
     * @since 1.0.0
     */
    public abstract static class BIRAnnotationValue {
        public BType type;

        public BIRAnnotationValue(BType type) {
            this.type = type;
        }
    }

    /**
     * Represent a literal value in an annotation attachment value.
     *
     * @since 1.0.0
     */
    public static class BIRAnnotationLiteralValue extends BIRAnnotationValue {
        public Object value;

        public BIRAnnotationLiteralValue(BType type, Object value) {
            super(type);
            this.value = value;
        }
    }

    /**
     * Represent a record value in an annotation attachment value.
     *
     * @since 1.0.0
     */
    public static class BIRAnnotationRecordValue extends BIRAnnotationValue {
        public Map<String, BIRAnnotationValue> annotValueEntryMap;

        public BIRAnnotationRecordValue(BType type, Map<String, BIRAnnotationValue> annotValueEntryMap) {
            super(type);
            this.annotValueEntryMap = annotValueEntryMap;
        }
    }

    /**
     * Represent a record value in an annotation attachment value.
     *
     * @since 1.0.0
     */
    public static class BIRAnnotationArrayValue extends BIRAnnotationValue {
        public BIRAnnotationValue[] annotArrayValue;

        public BIRAnnotationArrayValue(BType type, BIRAnnotationValue[] annotArrayValue) {
            super(type);
            this.annotArrayValue = annotArrayValue;
        }
    }

    /**
     * Constant value.
     *
     * @since 0.995.0
     */
    public static class ConstValue {
        public BType type;
        public Object value;

        public ConstValue(Object value, BType type) {
            this.value = value;
            this.type = type;
        }
    }

    /**
     * Taint table of the function.
     *
     * @since 0.995.0
     */
    public static class TaintTable {
        public int columnCount;
        public int rowCount;
        public Map<Integer, List<Byte>> taintTable;

        public TaintTable() {
            this.taintTable = new LinkedHashMap<>();
        }
    }

    /**
     * Documentable node which can have markdown documentations.
     *
     * @since 1.0.0
     */
    public abstract static class BIRDocumentableNode extends BIRNode {
        public MarkdownDocAttachment markdownDocAttachment;

        public BIRDocumentableNode(DiagnosticPos pos) {
            super(pos);
        }

        public void setMarkdownDocAttachment(MarkdownDocAttachment markdownDocAttachment) {
            this.markdownDocAttachment = markdownDocAttachment;
        }
    }

    /**
     * Stores the details of each level of locks.
     *
     * @since 1.0.0
     */
    public static class BIRLockDetailsHolder {
        public Set<BIRGlobalVariableDcl> globalLocks;
        public Map<BIROperand, Set<String>> fieldLocks;

        public BIRLockDetailsHolder(Set<BIRGlobalVariableDcl> globalLocks,
                                    Map<BIROperand, Set<String>> fieldLocks) {
            this.globalLocks = globalLocks;
            this.fieldLocks = fieldLocks;
        }

        public boolean isEmpty() {
            return globalLocks.isEmpty() && fieldLocks.isEmpty();
        }
    }
}
