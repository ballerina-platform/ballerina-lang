/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerinalang.compiler.bir.optimizer;

import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Optimize Locks.
 *
 * @since 1.2.1
 */
public class BIRLockOptimizer extends BIRVisitor {

    private final List<BIRTerminator.Lock> lockList = new ArrayList<>();
    private final Map<BIRTerminator.Lock, Integer> lockToSetMap = new HashMap<>();
    private final Map<Integer, List<BIRTerminator.Lock>> setToLockMap = new HashMap<>();
    private int setId = -1;

    public void optimizeNode(BIRNode node) {
        // Collect lock nodes
        node.accept(this);

        // Identify disjoint locks
        optimizeLocks();
    }

    private void optimizeLocks() {
        analyzeLocks();
        propagateLocks();
    }

    private void analyzeLocks() {
        for (int lockListIndex = 0; lockListIndex < lockList.size(); lockListIndex++) {
            if (!lockToSetMap.containsKey(lockList.get(lockListIndex))) {
                analyzeUnvisitedLock(lockListIndex);
            } else {
                analyzeVisitedLock(lockListIndex);
            }
        }
    }

    private void propagateLocks() {
        for (Map.Entry<Integer, List<BIRTerminator.Lock>> entry : setToLockMap.entrySet()) {
            Integer lockId = entry.getKey();
            for (BIRTerminator.Lock lock : entry.getValue()) {
                lock.lockId = lockId;
            }
        }
    }

    private void analyzeVisitedLock(int lockListIndex) {
        BIRTerminator.Lock currentLock = lockList.get(lockListIndex);
        int previousSetId = setId;
        Integer currentSetIdLocal = lockToSetMap.get(currentLock);
        List<BIRTerminator.Lock> currentSet = setToLockMap.get(currentSetIdLocal);

        // Use the setId of the current one for the other shared locks
        setId = currentSetIdLocal;
        populateLockSet(currentSet, currentLock, lockListIndex);
        setId = previousSetId;
    }

    private void analyzeUnvisitedLock(int lockListIndex) {
        BIRTerminator.Lock currentLock = lockList.get(lockListIndex);
        List<BIRTerminator.Lock> currentSet = new LinkedList<>();

        // Add to the maps for the current unvisited lock.
        lockToSetMap.put(currentLock, ++setId);

        // Compare with the rest of the locks.
        currentSet.add(currentLock);

        populateLockSet(currentSet, currentLock, lockListIndex);

        setToLockMap.put(setId, currentSet);
    }

    private void populateLockSet(List<BIRTerminator.Lock> currentSet, BIRTerminator.Lock currentLock,
            int lockListIndex) {
        for (int i = (lockListIndex + 1); i < lockList.size(); i++) {
            BIRTerminator.Lock comparedLock = lockList.get(i);
            Set<BIRNode.BIRGlobalVariableDcl> globalVarSetOfComparedLock = comparedLock.lockVariables;

            if (isSharedLock(currentLock, globalVarSetOfComparedLock)
                && isNotInSameSet(currentLock, comparedLock)) {
                populateLockSet(currentSet, comparedLock);
            }
        }
    }

    private boolean isNotInSameSet(BIRTerminator.Lock currentLock, BIRTerminator.Lock comparedLock) {
        Integer currentLockSetId = lockToSetMap.get(currentLock);
        Integer comparedLockSetId = lockToSetMap.get(comparedLock);

        if (currentLockSetId == null ||
                comparedLockSetId == null) {
            return true;
        }

        return currentLockSetId.compareTo(comparedLockSetId) != 0;
    }

    private void populateLockSet(List<BIRTerminator.Lock> currentSet, BIRTerminator.Lock comparedLock) {
        // Merge if there is already a set for the compared lock.
        if (lockToSetMap.containsKey(comparedLock)) {
            Integer comparedLocksSetId = lockToSetMap.get(comparedLock);
            List<BIRTerminator.Lock> previousSet = setToLockMap.get(comparedLocksSetId);

            currentSet.addAll(previousSet);

            // Remove the unwanted set since the content has been added to the new set.
            setToLockMap.remove(comparedLocksSetId);

            for (BIRTerminator.Lock lock : previousSet) {
                lockToSetMap.put(lock, setId);
            }
        } else {
            currentSet.add(comparedLock);
        }

        // Update the compared lock to the related set.
        lockToSetMap.put(comparedLock, setId);
    }

    private boolean isSharedLock(BIRTerminator.Lock currentLock,
            Set<BIRNode.BIRGlobalVariableDcl> globalVarSetOfComparedLock) {
        for (BIRNode.BIRGlobalVariableDcl globalVarOfCurLock : currentLock.lockVariables) {
            if (globalVarSetOfComparedLock.contains(globalVarOfCurLock)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void visit(BIRNode.BIRPackage birPackage) {
        birPackage.typeDefs.forEach(tDef -> tDef.accept(this));
        birPackage.functions.forEach(func -> func.accept(this));
    }

    @Override
    public void visit(BIRNode.BIRTypeDefinition birTypeDefinition) {
        birTypeDefinition.attachedFuncs.forEach(func -> func.accept(this));
    }

    @Override
    public void visit(BIRNode.BIRFunction birFunction) {
        birFunction.basicBlocks.forEach(bb -> bb.accept(this));
    }

    @Override
    public void visit(BIRTerminator.GOTO birGoto) {
        // Do nothing
    }

    @Override
    public void visit(BIRTerminator.Call birCall) {
        // Do nothing
    }

    @Override
    public void visit(BIRTerminator.AsyncCall birCall) {
        // Do nothing
    }

    @Override
    public void visit(BIRTerminator.Return birReturn) {
        // Do nothing
    }

    @Override
    public void visit(BIRTerminator.Branch birBranch) {
        // Do nothing
    }

    @Override
    public void visit(BIRTerminator.FPCall fpCall) {
        // Do nothing
    }

    @Override
    public void visit(BIRTerminator.Lock lock) {
        lockList.add(lock);
    }

    @Override
    public void visit(BIRTerminator.FieldLock lock) {
        // Do nothing
    }

    @Override
    public void visit(BIRTerminator.Unlock unlock) {
        // Do nothing
    }

    @Override
    public void visit(BIRTerminator.Panic birPanic) {
        // Do nothing
    }

    @Override
    public void visit(BIRTerminator.Wait birWait) {
        // Do nothing
    }

    @Override
    public void visit(BIRTerminator.WaitAll waitAll) {
        // Do nothing
    }

    @Override
    public void visit(BIRTerminator.Flush birFlush) {
        // Do nothing
    }

    @Override
    public void visit(BIRTerminator.WorkerReceive workerReceive) {
        // Do nothing
    }

    @Override
    public void visit(BIRTerminator.WorkerSend workerSend) {
        // Do nothing
    }

    @Override
    public void visit(BIRNode.BIRBasicBlock birBasicBlock) {
        BIRTerminator terminator = birBasicBlock.terminator;

        terminator.accept(this);
    }
}
