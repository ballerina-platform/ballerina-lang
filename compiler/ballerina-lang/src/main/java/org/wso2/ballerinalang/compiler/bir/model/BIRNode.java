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

import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.elements.AttachPoint;
import org.ballerinalang.model.elements.MarkdownDocAttachment;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.NamedNode;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Root class of Ballerina intermediate representation-BIR.
 *
 * @since 0.980.0
 */
public abstract class BIRNode {
    public final Location pos;

    protected BIRNode(Location pos) {
        this.pos = pos;
    }

    public abstract void accept(BIRVisitor visitor);

    /**
     * A package definition.
     *
     * @since 0.980.0
     */
    public static class BIRPackage extends BIRNode {
        public final PackageID packageID;
        public final List<BIRImportModule> importModules;
        public final List<BIRTypeDefinition> typeDefs;
        public final List<BIRGlobalVariableDcl> globalVars;
        public final Set<BIRGlobalVariableDcl> importedGlobalVarsDummyVarDcls;
        public final List<BIRFunction> functions;
        public final List<BIRAnnotation> annotations;
        public final List<BIRConstant> constants;
        public final List<BIRServiceDeclaration> serviceDecls;
        public boolean isListenerAvailable;

        public BIRPackage(Location pos, Name org, Name pkgName, Name name, Name version,
                          Name sourceFileName) {
            this(pos, org, pkgName, name, version, sourceFileName, false);
        }

        public BIRPackage(Location pos, Name org, Name pkgName, Name name, Name version, Name sourceFileName,
                          boolean isTestPkg) {
            super(pos);
            packageID = new PackageID(org, pkgName, name, version, sourceFileName, isTestPkg);
            this.importModules = new ArrayList<>();
            this.typeDefs = new ArrayList<>();
            this.globalVars = new ArrayList<>();
            this.importedGlobalVarsDummyVarDcls = new HashSet<>();
            this.functions = new ArrayList<>();
            this.annotations = new ArrayList<>();
            this.constants = new ArrayList<>();
            this.serviceDecls = new ArrayList<>();
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * An import package definition.
     *
     * @since 0.990.0
     */
    public static class BIRImportModule extends BIRNode {
        public final PackageID packageID;

        public BIRImportModule(Location pos, Name org, Name name, Name version) {
            super(pos);
            packageID = new PackageID(org, name, version);
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
        public Name originalName;
        public String metaVarName;
        public String jvmVarName;
        public VarKind kind;
        public VarScope scope;
        public boolean ignoreVariable;
        public BIRBasicBlock endBB;
        public BIRBasicBlock startBB;
        public int insOffset;
        public boolean onlyUsedInSingleBB;

        // Stores the scope of the current instruction with respect to local variables.
        public BirScope insScope;

        public BIRVariableDcl(Location pos, BType type, Name name, Name originalName, VarScope scope,
                              VarKind kind, String metaVarName) {
            super(pos);
            this.type = type;
            this.name = name;
            this.originalName = originalName;
            this.scope = scope;
            this.kind = kind;
            this.metaVarName = metaVarName;
            this.jvmVarName = name.value.replace("%", "_");
        }

        public BIRVariableDcl(Location pos, BType type, Name name, VarScope scope,
                              VarKind kind, String metaVarName) {
            this(pos, type, name, name, scope, kind, metaVarName);
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
        public long flags;
        public List<BIRAnnotationAttachment> annotAttachments;

        public BIRParameter(Location pos, Name name, long flags) {
            super(pos);
            this.name = name;
            this.flags = flags;
            this.annotAttachments = new ArrayList<>();
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
        public long flags;
        public PackageID pkgId;
        public SymbolOrigin origin;
        public List<BIRAnnotationAttachment> annotAttachments;

        public BIRGlobalVariableDcl(Location pos, long flags, BType type, PackageID pkgId, Name name, Name originalName,
                                    VarScope scope, VarKind kind, String metaVarName, SymbolOrigin origin) {
            super(pos, type, name, originalName, scope, kind, metaVarName);
            this.flags = flags;
            this.pkgId = pkgId;
            this.origin = origin;
            this.annotAttachments = new ArrayList<>();
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

        public BIRFunctionParameter(Location pos, BType type, Name name,
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
    public static class BIRFunction extends BIRDocumentableNode implements NamedNode {

        /**
         * Name of the function.
         */
        public Name name;

        /**
         * Original name of the function.
         * e.g. function `comeHere() {}
         */
        public Name originalName;

        /**
         * Value represents flags.
         */
        public long flags;

        /**
         * The origin of the function.
         */
        public SymbolOrigin origin;

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
        public List<BIRFunctionParameter>  parameters;

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

        public List<BIRAnnotationAttachment> annotAttachments;

        public List<BIRAnnotationAttachment> returnTypeAnnots;

        public Set<BIRGlobalVariableDcl> dependentGlobalVars = new TreeSet<>();

        // Below fields will only be available on resource functions
        // TODO: consider creating a sub class for resource functions issue: #36964
        public List<BIRVariableDcl> pathParams;
        
        public BIRVariableDcl restPathParam;
        
        public List<Name> resourcePath;
        
        public List<Location> resourcePathSegmentPosList;
        
        public Name accessor;
        
        public List<BType> pathSegmentTypeList;

        public BIRFunction(Location pos, Name name, Name originalName, long flags, SymbolOrigin origin,
                           BInvokableType type, List<BIRParameter> requiredParams, BIRVariableDcl receiver,
                           BIRParameter restParam, int argsCount, List<BIRVariableDcl> localVars,
                           BIRVariableDcl returnVariable, List<BIRFunctionParameter> parameters,
                           List<BIRBasicBlock> basicBlocks, List<BIRErrorEntry> errorTable, Name workerName,
                           ChannelDetails[] workerChannels,
                           List<BIRAnnotationAttachment> annotAttachments,
                           List<BIRAnnotationAttachment> returnTypeAnnots,
                           Set<BIRGlobalVariableDcl> dependentGlobalVars) {
            super(pos);
            this.name = name;
            this.originalName = originalName;
            this.flags = flags;
            this.origin = origin;
            this.type = type;
            this.requiredParams = requiredParams;
            this.receiver = receiver;
            this.restParam = restParam;
            this.argsCount = argsCount;
            this.localVars = localVars;
            this.returnVariable = returnVariable;
            this.parameters = parameters;
            this.basicBlocks = basicBlocks;
            this.errorTable = errorTable;
            this.workerName = workerName;
            this.workerChannels = workerChannels;
            this.annotAttachments = annotAttachments;
            this.returnTypeAnnots = returnTypeAnnots;
            this.dependentGlobalVars = dependentGlobalVars;
        }

        public BIRFunction(Location pos, Name name, Name originalName, long flags, BInvokableType type, Name workerName,
                           int sendInsCount, SymbolOrigin origin) {
            super(pos);
            this.name = name;
            this.originalName = originalName;
            this.flags = flags;
            this.type = type;
            this.localVars = new ArrayList<>();
            this.parameters = new ArrayList<>();
            this.requiredParams = new ArrayList<>();
            this.basicBlocks = new ArrayList<>();
            this.errorTable = new ArrayList<>();
            this.workerName = workerName;
            this.workerChannels = new ChannelDetails[sendInsCount];
            this.annotAttachments = new ArrayList<>();
            this.returnTypeAnnots = new ArrayList<>();
            this.origin = origin;
        }

        public BIRFunction(Location pos, Name name, long flags, BInvokableType type, Name workerName,
                           int sendInsCount, SymbolOrigin origin) {
            this(pos, name, name, flags, type, workerName, sendInsCount, origin);
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }

        public BIRFunction duplicate() {
            BIRFunction f = new BIRFunction(pos, name, originalName, flags, type, workerName, 0, origin);
            f.localVars = localVars;
            f.parameters = parameters;
            f.requiredParams = requiredParams;
            f.basicBlocks = basicBlocks;
            f.errorTable = errorTable;
            f.workerChannels = workerChannels;
            f.annotAttachments = annotAttachments;
            f.returnTypeAnnots = returnTypeAnnots;
            return f;

        }

        @Override
        public Name getName() {
            return name;
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
    public static class BIRTypeDefinition extends BIRDocumentableNode implements NamedNode {

        public Name name;

        public Name originalName;

        /**
         * internal name of the type definition.
         * for anonTypes this will be something like $anonType2 while name will reflect the structure
         */
        public Name internalName;

        public List<BIRFunction> attachedFuncs;

        public long flags;

        public BType type;

        public boolean isBuiltin;

        public List<BType> referencedTypes;

        public BType referenceType;

        public SymbolOrigin origin;

        public List<BIRAnnotationAttachment> annotAttachments;

        /**
         * this is not serialized. it's used to keep the index of the def in the list.
         * otherwise the writer has to *find* it in the list.
         */
        public int index;

        public BIRTypeDefinition(Location pos, Name internalName, long flags, boolean isBuiltin,
                                 BType type, List<BIRFunction> attachedFuncs, SymbolOrigin origin, Name name,
                                 Name originalName) {
            super(pos);
            this.internalName = internalName;
            this.flags = flags;
            this.isBuiltin = isBuiltin;
            this.type = type;
            this.attachedFuncs = attachedFuncs;
            this.referencedTypes = new ArrayList<>();
            this.origin = origin;
            this.name = name;
            this.originalName = originalName;
            this.annotAttachments = new ArrayList<>();
        }

        public BIRTypeDefinition(Location pos, Name name, Name originalName, long flags, boolean isBuiltin,
                                 BType type, List<BIRFunction> attachedFuncs, SymbolOrigin origin) {
            this(pos, name, flags, isBuiltin, type, attachedFuncs, origin, name, originalName);
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public String toString() {
            return type + " " + internalName;
        }

        @Override
        public Name getName() {
            return name;
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
    public static class BIRAnnotation extends BIRDocumentableNode {
        /**
         * Name of the annotation definition.
         */
        public Name name;

        /**
         * Original name of the annotation definition.
         */
        public Name originalName;

        /**
         * Value represents flags.
         */
        public long flags;

        /**
         * The origin of the annotation.
         */
        public SymbolOrigin origin;

        /**
         * Attach points, this is needed only in compiled symbol enter as it is.
         */
        public Set<AttachPoint> attachPoints;

        /**
         * Type of the annotation body.
         */
        public BType annotationType;

        /**
         * Temporary packageID field for when BIRAnnotation is used to identify attachments.
         */
        public PackageID packageID;

        /**
         * Annotation attachments on the annotation.
         */
        public List<BIRAnnotationAttachment> annotAttachments;

        public BIRAnnotation(Location pos, Name name, Name originalName, long flags,
                             Set<AttachPoint> points, BType annotationType, SymbolOrigin origin) {
            super(pos);
            this.name = name;
            this.originalName = originalName;
            this.flags = flags;
            this.attachPoints = points;
            this.annotationType = annotationType;
            this.origin = origin;
            this.annotAttachments = new ArrayList<>();
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
         * Original name of the constant.
         */
        public Name originalName;

        /**
         * Value for the Flags.
         */
        public long flags;

        /**
         * Type of the constant symbol.
         */
        public BType type;

        /**
         * Value of the constant.
         */
        public ConstValue constValue;

        /**
         * The origin of the symbol for the constant.
         */
        public SymbolOrigin origin;

        /**
         * Annotation attachments on the constant.
         */
        public List<BIRAnnotationAttachment> annotAttachments;

        public BIRConstant(Location pos, Name name, Name originalName, long flags,
                           BType type, ConstValue constValue, SymbolOrigin origin) {
            super(pos);
            this.name = name;
            this.originalName = originalName;
            this.flags = flags;
            this.type = type;
            this.constValue = constValue;
            this.origin = origin;
            this.annotAttachments = new ArrayList<>();
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

        public PackageID annotPkgId;
        public Name annotTagRef;

        public BIRAnnotationAttachment(Location pos, PackageID annotPkgId, Name annotTagRef) {
            super(pos);
            this.annotPkgId = annotPkgId;
            this.annotTagRef = annotTagRef;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * Represents a const annotation attachment in BIR node tree.
     *
     * @since 2.1.0
     */
    public static class BIRConstAnnotationAttachment extends BIRAnnotationAttachment {

        public ConstValue annotValue;

        public BIRConstAnnotationAttachment(Location pos, PackageID annotPkgId, Name annotTagRef,
                                            ConstValue annotValue) {
            super(pos, annotPkgId, annotTagRef);
            this.annotValue = annotValue;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
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
     * Documentable node which can have markdown documentations.
     *
     * @since 1.0.0
     */
    public abstract static class BIRDocumentableNode extends BIRNode {
        public MarkdownDocAttachment markdownDocAttachment;

        protected BIRDocumentableNode(Location pos) {
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

        //This is the list of recursive locks in the current scope.
        private List<BIRTerminator.Lock> locks = new ArrayList<>();

        public boolean isEmpty() {
            return locks.isEmpty();
        }

        public void removeLastLock() {
            locks.remove(size() - 1);
        }

        public BIRTerminator.Lock getLock(int index) {
            return locks.get(index);
        }

        public void addLock(BIRTerminator.Lock lock) {
            locks.add(lock);
        }

        public int size() {
            return locks.size();
        }
    }

    /**
     * Represents the entries in a mapping constructor expression.
     *
     * @since 1.3.0
     */
    public abstract static class BIRMappingConstructorEntry {

        public boolean isKeyValuePair() {
            return true;
        }
    }

    /**
     * Represents a key-value entry in a mapping constructor expression.
     *
     * @since 1.3.0
     */
    public static class BIRMappingConstructorKeyValueEntry extends BIRMappingConstructorEntry {

        public BIROperand keyOp;
        public BIROperand valueOp;

        public BIRMappingConstructorKeyValueEntry(BIROperand keyOp, BIROperand valueOp) {
            this.keyOp = keyOp;
            this.valueOp = valueOp;
        }
    }

    /**
     * Represents a spread-field entry in a mapping constructor expression.
     *
     * @since 1.3.0
     */
    public static class BIRMappingConstructorSpreadFieldEntry extends BIRMappingConstructorEntry {

        public BIROperand exprOp;

        public BIRMappingConstructorSpreadFieldEntry(BIROperand exprOp) {
            this.exprOp = exprOp;
        }

        @Override
        public boolean isKeyValuePair() {
            return false;
        }
    }

    /**
     * Represents a list member entry in a list constructor expression.
     *
     * @since 2201.1.0
     */
    public abstract static class BIRListConstructorEntry {
        public BIROperand exprOp;
    }

    /**
     * Represents a spread member entry in a list constructor expression.
     *
     * @since 2201.1.0
     */
    public static class BIRListConstructorSpreadMemberEntry extends BIRListConstructorEntry {

        public BIRListConstructorSpreadMemberEntry(BIROperand exprOp) {
            this.exprOp = exprOp;
        }
    }

    /**
     * Represents an expression member entry in a list constructor expression.
     *
     * @since 2201.1.0
     */
    public static class BIRListConstructorExprEntry extends BIRListConstructorEntry {

        public BIRListConstructorExprEntry(BIROperand exprOp) {
            this.exprOp = exprOp;
        }
    }

    /**
     * Represents a service declaration.
     *
     * @since 2.0.0
     */
    public static class BIRServiceDeclaration extends BIRDocumentableNode {

        public List<String> attachPoint;
        public String attachPointLiteral;
        public List<BType> listenerTypes;
        public Name generatedName;
        public Name associatedClassName;
        public BType type;
        public SymbolOrigin origin;
        public long flags;

        public BIRServiceDeclaration(List<String> attachPoint, String attachPointLiteral, List<BType> listenerTypes,
                                     Name generatedName, Name associatedClassName, BType type, SymbolOrigin origin,
                                     long flags, Location location) {
            super(location);
            this.attachPoint = attachPoint;
            this.attachPointLiteral = attachPointLiteral;
            this.listenerTypes = listenerTypes;
            this.generatedName = generatedName;
            this.associatedClassName = associatedClassName;
            this.type = type;
            this.origin = origin;
            this.flags = flags;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }
}
