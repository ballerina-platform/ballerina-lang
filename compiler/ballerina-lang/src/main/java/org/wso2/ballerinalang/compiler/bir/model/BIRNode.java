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

import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.List;

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
    public static class BIRVariableDcl extends BIRNode {
        public BType type;
        public Name name;
        public VarKind kind;
        public VarScope scope;

        public BIRVariableDcl(DiagnosticPos pos, BType type, Name name, VarScope scope, VarKind kind) {
            super(pos);
            this.type = type;
            this.name = name;
            this.scope = scope;
            this.kind = kind;
        }

        public BIRVariableDcl(BType type, Name name, VarScope scope, VarKind kind) {
            this(null, type, name, scope, kind);
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
         * Visibility of this variable.
         * 0 - package_private
         * 1 - private
         * 2 - public
         */
        public Visibility visibility;

        public BIRGlobalVariableDcl(DiagnosticPos pos, Visibility visibility, BType type,
                                    Name name, VarScope scope, VarKind kind) {
            super(pos, type, name, scope, kind);
            this.visibility = visibility;
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
    public static class BIRFunction extends BIRNode {

        /**
         * Name of the function.
         */
        public Name name;

        /**
         * Indicate whether this is a function definition or a declaration.
         */
        public boolean isDeclaration;

        /**
         * Indicate whether this is a function definition or an interface.
         */
        public boolean isInterface;

        /**
         * Visibility of this function.
         * 0 - package_private
         * 1 - private
         * 2 - public
         */
        public Visibility visibility;

        /**
         * Type of this function. e.g., (int, int) returns (int).
         */
        public BInvokableType type;

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

        /**
         * List of basic blocks in this function.
         */
        public List<BIRBasicBlock> basicBlocks;

        /**
         * List of error entries in this function.
         */
        public List<BIRErrorEntry> errorTable;

        public BIRFunction(DiagnosticPos pos, Name name, Visibility visibility, BInvokableType type) {
            super(pos);
            this.name = name;
            this.visibility = visibility;
            this.type = type;
            this.localVars = new ArrayList<>();
            this.basicBlocks = new ArrayList<>();
            this.errorTable = new ArrayList<>();
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
        public List<BIRInstruction> instructions;
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
    }

    /**
     * Type definition node in BIR.
     *
     * @since 0.995.0
     */
    public static class BIRTypeDefinition extends BIRNode {

        /**
         * Name of the type definition.
         */
        public Name name;


        public List<BIRFunction> attachedFuncs;

        /**
         * Visibility of this type definition.
         * 0 - package_private
         * 1 - private
         * 2 - public
         */
        public Visibility visibility;

        public BType type;

        /**
         * this is not serialized. it's used to keep the index of the def in the list.
         * otherwise the writer has to *find* it in the list.
         */
        public int index;

        public BIRTypeDefinition(DiagnosticPos pos, Name name, Visibility visibility,
                                 BType type, List<BIRFunction> attachedFuncs) {
            super(pos);
            this.name = name;
            this.visibility = visibility;
            this.type = type;
            this.attachedFuncs = attachedFuncs;
        }

        @Override
        public void accept(BIRVisitor visitor) {

        }
    }

    /**
     * An error entry in the error table.
     *
     * @since 0.995.0
     */
    public static class BIRErrorEntry extends BIRNode {

        public BIRBasicBlock trapBB;

        public BIROperand errorOp;

        public BIRErrorEntry(BIRBasicBlock trapBB, BIROperand errorOp) {
            super(null);
            this.trapBB = trapBB;
            this.errorOp = errorOp;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }
}
