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


/**
 * An instruction in a basic block.
 * <p>
 * There can be two kinds of instructions in a basic block: instructions that
 * terminate a basic block and instructions that do not terminate a basic block.
 * They are modeled as {@link BIRNonTerminator} and {@link BIRTerminator} instructions.
 * <p>
 * A basic block contains one or more non-terminating instructions followed by
 * only one terminating instruction.
 *
 * @see BIRTerminator
 * @see BIRNonTerminator
 * @since 0.980.0
 */
public interface BIRInstruction {

}
