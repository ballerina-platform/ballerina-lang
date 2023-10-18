/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerinalang.compiler.bir;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.UsedState;

import java.util.HashMap;
import java.util.HashSet;

import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.getModuleLevelClassName;

public abstract class InvocationGraphNode {

    public String nodeName;
    public PackageID pkgID;
    public HashSet<InvocationGraphNode> childrenInvocations = new HashSet<>();
    public HashSet<InvocationGraphNode> parentFunctions = new HashSet<>();
    public UsedState usedState = UsedState.UNUSED;
    public String sourceFileName;
    public String jarClassFilePath;

    public InvocationGraphNode(String nodeName, PackageID nodePkgID, String sourceFileName, String jarClassFilePath) {
        this.nodeName = nodeName;
        this.pkgID = nodePkgID;
        this.sourceFileName = sourceFileName;
        this.jarClassFilePath = jarClassFilePath;
    }

    public String getNodeID() {
        return pkgID.toString() + "/" + nodeName;
    }

    public String toString() {
        return nodeName;
    }

    private BIRDeadNodeAnalyzer.InvocationData getInvocationData(
            HashMap<PackageID, BIRDeadNodeAnalyzer.InvocationData> pkgWiseInvocationData) {
        return pkgWiseInvocationData.get(pkgID);
    }

    public abstract void markSelfAndChildrenAsUsed(
            HashMap<PackageID, BIRDeadNodeAnalyzer.InvocationData> pkgWiseInvocationData);

    public static class FunctionNode extends InvocationGraphNode {

        public boolean isAttachedFunction;

        public FunctionNode(String nodeName, PackageID nodePkgID, String sourceFileName) {

            super(nodeName, nodePkgID, sourceFileName, getSourceFunctionClassPath(nodePkgID, sourceFileName));
        }

        private static String getSourceFunctionClassPath(PackageID pkgId, String balFileName) {
            if (balFileName == null) {
                return "FAILED FUNCTIONS";
                // TODO find a way to get the jar class path of generated attached functions
//                getTypeValueClassName(pkgId, "int");
            }
            return getModuleLevelClassName(pkgId, balFileName).replace("$$$bal",".class");
        }

        @Override
        public void markSelfAndChildrenAsUsed(
                HashMap<PackageID, BIRDeadNodeAnalyzer.InvocationData> pkgWiseInvocationData) {
            if (this.usedState == UsedState.UNUSED) {
                BIRDeadNodeAnalyzer.InvocationData invocationData = pkgWiseInvocationData.get(this.pkgID);

                // TODO Remove the null check after partial graph persistence
                if (invocationData == null) {
                    return;
                }

                // TODO Remove deadFunctions and usedFunctions
                invocationData.deadFunctions.remove(this);
                invocationData.usedFunctions.add(this);

                invocationData.deadFunctionJarPathMap.get(
                        this.jarClassFilePath).remove(this.nodeName);
                this.usedState = UsedState.USED;

                this.childrenInvocations.forEach(child -> child.markSelfAndChildrenAsUsed(pkgWiseInvocationData));
            }
        }
    }

    public static class TypeDefNode extends InvocationGraphNode {

        public HashSet<InvocationGraphNode> childFunctions = new HashSet<>();
        public String typeDescJarPath;
        public String bTypeID;

        public TypeDefNode(String nodeName, PackageID nodePkgID, String sourceFileName, String jarClassFilePath,
                           String typeDescJarPath, String bTypeID) {
            super(nodeName, nodePkgID, sourceFileName, jarClassFilePath);
            this.bTypeID = bTypeID;
            this.typeDescJarPath = typeDescJarPath;
        }

        public TypeDefNode(String bTypeID) {
            this(null, null, null, null, null, bTypeID);
        }

        @Override
        public String getNodeID() {
            return bTypeID;
        }

        @Override
        public void markSelfAndChildrenAsUsed(
                HashMap<PackageID, BIRDeadNodeAnalyzer.InvocationData> pkgWiseInvocationData) {
            // TODO Remove the null check after partial graph persistence
            if (this.nodeName == null) {
                return;
            }

            if (this.usedState == UsedState.UNUSED) {
                BIRDeadNodeAnalyzer.InvocationData invocationData = pkgWiseInvocationData.get(this.pkgID);
                invocationData.deadTypeDefs.remove(this);
                invocationData.usedTypeDefs.add(this);
                invocationData.deadTypeDefJarPathMap.remove(this.jarClassFilePath);
                invocationData.deadTypeDefJarPathMap.remove(this.typeDescJarPath);
                this.usedState = UsedState.USED;
                this.childrenInvocations.forEach(child -> child.markSelfAndChildrenAsUsed(pkgWiseInvocationData));
            }
        }
    }
}
