/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.util.transactions;

import org.ballerinalang.model.values.BFunctionPointer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Registry for Participated transaction related information.
 *
 * @since 0.985.0
 */
class ParticipantRegistry {
    Map<String, Deque<List<Participant>>> participantReg;

    ParticipantRegistry() {
        participantReg = new HashMap<>();
    }

    void register(String gTransactionId, BFunctionPointer committed, BFunctionPointer aborted) {
        Participant participant = new Participant(gTransactionId, committed, aborted);
        Deque<List<Participant>> participantStack = participantReg.computeIfAbsent(
                gTransactionId, id -> new ArrayDeque<>());
        if (participantStack.isEmpty()) {
            participantStack.push(new ArrayList<>());
        }
        participantStack.peek().add(participant);
    }

    void participantFailed(String transactionId) {
        Deque<List<Participant>> participants = participantReg.get(transactionId);
        if (participants == null || participants.isEmpty()) {
            return;
        }
        // Even single participant failed means all the participants will be retried (or the trx will be aborted).
        // Hence mark them as failed.

        List<Participant> lastAttemptsParticipants = participants.peek();
        lastAttemptsParticipants.forEach(p -> {
            if (p.localTxStatus == LocalTxStatus.STARTED) {
                p.localTxStatus = LocalTxStatus.FAILED;
            }
        });
        participants.push(new ArrayList<>());
    }

    public void purge(String transactionId) {
        Deque<List<Participant>> participants = participantReg.get(transactionId);
        if (participants == null) {
            return;
        }
        participants.clear();
    }

    List<BFunctionPointer> getCommittedFuncs(String transactionId) {
        Deque<List<Participant>> participants = participantReg.get(transactionId);
        if (participants == null || participants.isEmpty()) {
            return new ArrayList<>();
        }
        return participants.peek().stream()
                //.filter(p -> p.localTxStatus == LocalTxStatus.SUCCESS)
                .filter(p -> p.committed != null)
                .map(p -> p.committed)
                .collect(Collectors.toList());
    }

    List<BFunctionPointer> getAbortedFuncs(String transactionId) {
        Deque<List<Participant>> participantStack = participantReg.get(transactionId);
        if (participantStack == null || participantStack.isEmpty()) {
            return new ArrayList<>();
        }
        List<Participant> participants = participantStack.peek();
        if (participants != null && participants.isEmpty()) { // topmost participant list only get empty when
            // last attempt was failed.
            participantStack.pop();
            participants = participantStack.peek();
        }
        if (participants == null) {
            return new ArrayList<>();
        }
        return participants.stream()
                .filter(p -> p.aborted != null)
                .map(p -> p.aborted)
                .collect(Collectors.toList());
    }

    boolean prepareCommit(String transactionId) {
        Deque<List<Participant>> participants = participantReg.get(transactionId);
        // Since all the participant (with matching transaction id) are marked failed
        // when single participant in this ResourceManager fails,
        // we can infer that having at least on non failed participant means this transaction attempt was a success.

        if (participants != null) {
            if (participants.isEmpty() || participants.peek().isEmpty()) {
                return false;
            }
            participants.peek().forEach(p -> p.localTxStatus = LocalTxStatus.SUCCESS);
        }

        return true;
    }

    public boolean hasParticipants(String transactionId) {
        return participantReg.containsKey(transactionId);
    }

    static class Participant {
        final String gTransactionId;
        final BFunctionPointer committed;
        final BFunctionPointer aborted;
        private LocalTxStatus localTxStatus;

        public Participant(String gTransactionId, BFunctionPointer committed, BFunctionPointer aborted) {
            this.gTransactionId = gTransactionId;
            this.committed = committed;
            this.aborted = aborted;
            this.localTxStatus = LocalTxStatus.STARTED;
        }
    }

    enum LocalTxStatus {
        STARTED, SUCCESS, FAILED
    }
}
