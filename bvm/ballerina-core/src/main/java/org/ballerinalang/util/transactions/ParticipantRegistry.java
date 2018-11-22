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
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


/**
 * Registry for Participated transaction related information.
 *
 * @since 0.985.0
 */
class ParticipantRegistry {
    Map<String, Deque<List<Participant>>> resourceParticipantReg;
    Map<String, Map<String, Participant>> localFunctionParticipants;

    ParticipantRegistry() {
        resourceParticipantReg = new HashMap<>();
        localFunctionParticipants = new HashMap<>();
    }

    void register(String gTransactionId, BFunctionPointer committed, BFunctionPointer aborted) {
        Participant participant = new Participant(gTransactionId, committed, aborted);
        Deque<List<Participant>> participantStack = resourceParticipantReg.computeIfAbsent(
                gTransactionId, id -> new ArrayDeque<>());
        if (participantStack.isEmpty()) {
            participantStack.push(new ArrayList<>());
        }
        participantStack.peek().add(participant);
    }

    public void register(String gTransactionId, String participantName, BFunctionPointer committed, BFunctionPointer aborted) {
        Participant participant = new Participant(gTransactionId, committed, aborted);
        localFunctionParticipants.computeIfAbsent(gTransactionId, gid -> new HashMap<>())
                .put(participantName, participant);
    }

    void participantFailed(String transactionId) {
        Deque<List<Participant>> participants = resourceParticipantReg.get(transactionId);
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

    void participantFailed(String gTransactionId, String uniqueName) {
        Map<String, Participant> participants = localFunctionParticipants.get(gTransactionId);
        if (participants == null) {
            return;
        }

        Participant participant = participants.get(uniqueName);
        if (participant == null) {
            return;
        }
        participant.localTxStatus = LocalTxStatus.FAILED;
    }

    public void purge(String transactionId) {
        Deque<List<Participant>> participants = resourceParticipantReg.get(transactionId);
        if (participants == null) {
            return;
        }
        participants.clear();
    }

    List<BFunctionPointer> getCommittedFuncs(String transactionId) {
        Deque<List<Participant>> participants = resourceParticipantReg.get(transactionId);
        Stream<BFunctionPointer> resourceCommittedFuncs;
        if (participants == null || participants.isEmpty()) {
            resourceCommittedFuncs = Stream.empty();
        } else {
            resourceCommittedFuncs = participants.peek().stream()
                    .filter(p -> p.committed != null)
                    .map(p -> p.committed);
        }

        Stream<BFunctionPointer> localFuncCommitedFuncStream;
        Map<String, Participant> localFuncParticipants = localFunctionParticipants.get(transactionId);
        if (localFuncParticipants == null) {
            localFuncCommitedFuncStream = Stream.empty();
        } else {
            localFuncCommitedFuncStream = localFuncParticipants.entrySet().stream()
                    .map(e -> e.getValue())
                    .filter(v -> v.committed != null)
                    .map(v -> v.committed);
        }

        return Stream.concat(localFuncCommitedFuncStream, resourceCommittedFuncs).collect(Collectors.toList());
    }

    List<BFunctionPointer> getAbortedFuncs(String transactionId) {
        Deque<List<Participant>> participantStack = resourceParticipantReg.get(transactionId);
        Stream<BFunctionPointer> resourceAbortedFunctions;
        if (participantStack == null || participantStack.isEmpty()) {
            resourceAbortedFunctions = Stream.empty();
        } else {
            List<Participant> participants = participantStack.peek();
            if (participants != null && participants.isEmpty()) { // topmost participant list only get empty when
                // last attempt was failed.
                participantStack.pop();
                participants = participantStack.peek();
            }
            if (participants == null) {
                return new ArrayList<>();
            }
            resourceAbortedFunctions = participants.stream()
                    .filter(p -> p.aborted != null)
                    .map(p -> p.aborted);
        }
        Stream<BFunctionPointer> localFuncParticipantAborted;
        Map<String, Participant> localFuncParticipants = localFunctionParticipants.get(transactionId);
        if (localFuncParticipants == null) {
            localFuncParticipantAborted = Stream.empty();
        } else {
            localFuncParticipantAborted = localFuncParticipants.entrySet().stream()
                    .map(e -> e.getValue())
                    .filter(v -> v.aborted != null)
                    .map(v -> v.aborted);
        }
        return Stream.concat(localFuncParticipantAborted, resourceAbortedFunctions).collect(Collectors.toList());
    }

    boolean prepareCommit(String transactionId) {
        boolean resourceCommitSuccess = prepareAndCommitResources(transactionId);
        boolean functionCommitSuccess = prepareAndCommitLocalParticipantFunctions(transactionId);
        return resourceCommitSuccess && functionCommitSuccess;
    }

    private boolean prepareAndCommitLocalParticipantFunctions(String transactionId) {
        Map<String, Participant> participants = localFunctionParticipants.get(transactionId);
        if (participants == null) {
            return true;
        }
        boolean participantFailed = participants.entrySet().stream()
                .anyMatch(e -> e.getValue().localTxStatus == LocalTxStatus.FAILED);
        return !participantFailed;
    }

    private boolean prepareAndCommitResources(String transactionId) {
        Deque<List<Participant>> participants = resourceParticipantReg.get(transactionId);
        // Since all the participant (with matching transaction id) are marked failed
        // when single participant in this ResourceManager fails,
        // we can infer that having at least on non failed participant means this transaction attempt was a success.

        if (participants != null) {
            // participants stack is not null and top of the stack is a empty list means
            // local transaction failed and participant got a new stack frame with a empty list.
            if (participants.isEmpty() || participants.peek().isEmpty()) {
                return false;
            }
            participants.peek().forEach(p -> p.localTxStatus = LocalTxStatus.SUCCESS);
        }

        return true;
    }

    public boolean hasParticipants(String transactionId) {
        return resourceParticipantReg.containsKey(transactionId) ||
                localFunctionParticipants.containsKey(transactionId);
    }

    /**
     * Remove participant information related to given global transaction.
     * @param gTransactionId global transaction id
     *
     * @since 0.985.0
     */
    public void remove(String gTransactionId) {
        resourceParticipantReg.remove(gTransactionId);
        localFunctionParticipants.remove(gTransactionId);
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
