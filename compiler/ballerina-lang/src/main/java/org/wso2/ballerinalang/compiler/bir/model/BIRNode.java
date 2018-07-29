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

import java.util.ArrayList;
import java.util.List;

/**
 * Root class of Ballerina intermediate representation-BIR.
 *
 * @since 0.980.0
 */
public abstract class BIRNode {

    /**
     * A package definition.
     *
     * @since 0.980.0
     */
    public static class BIRPackage extends BIRNode {
        public Name name;
        public Name version;
        public List<BIRFunction> functions;
        public List<BType> types;

        public BIRPackage(Name name, Name version) {
            this.name = name;
            this.version = version;
            this.functions = new ArrayList<>();
            this.types = new ArrayList<>();
        }

        // SymbolTable - declaration
        //      function signatures
        //      type signatures

        // TODO Consider objects (functions..)

        // definition
        //      function body...
    }

    /**
     * A variable declaration.
     *
     * @since 0.980.0
     */
    public static class BIRVariableDcl extends BIRNode {
        public BType type;
        public Name name; // _1
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
         * Visibility of this function.
         * 0 - package_private
         * 1 - private
         * 2 - public
         */
        public Visibility visibility;

        /**
         * Type of this function. e.g., (int, int) -> (int).
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

        // TODO taint table storage

        public BIRFunction(Name name) {
            this.name = name;
        }
    }

    /**
     * A basic block definition.
     *
     * @since 0.980.0
     */
    public static class BIRBasicBlock extends BIRNode {
        public List<BIRInstruction> instructions;
        public BIRTerminator terminator;
    }
}

// TODO string table..
