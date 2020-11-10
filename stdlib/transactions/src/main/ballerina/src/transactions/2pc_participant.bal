// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/log;

# This represents the protocol associated with the coordination type.
#
# + name - protocol name
type LocalProtocol record {
    string name = "";
};

# This represents the protocol associated with the coordination type.

# + name - protocol name
# + url - protocol URL. This URL will have a value only if the participant is remote. If the participant is local,
#         the `protocolFn` will be called
type RemoteProtocol record {
    string name = "";
    string url = "";
};

type UProtocol LocalProtocol|RemoteProtocol;

type Participant object {

    string participantId;

    function prepare(string protocol) returns [(PrepareResult|error)?, Participant];

    function notify(string action, string? protocolName) returns (NotifyResult|error)?;
};

class RemoteParticipant {

    string participantId;
    private string transactionId;
    private RemoteProtocol[] participantProtocols;

    function init(string participantId, string transactionId, RemoteProtocol[] participantProtocols) {
        self.participantId = participantId;
        self.transactionId = transactionId;
        self.participantProtocols = participantProtocols;
    }

    function prepare(string protocol) returns @tainted [(PrepareResult|error)?, Participant] {
        foreach var remoteProto in self.participantProtocols {
            if (remoteProto.name == protocol) {
                // We are assuming a participant will have only one instance of a protocol
                return [self.prepareMe(remoteProto.url), self];
            }
        }
        return [(), self]; // No matching protocol
    }

    function notify(string action, string? protocolName) returns @tainted NotifyResult|error? {
        if (protocolName is string) {
            foreach var remoteProtocol in self.participantProtocols {
                if (protocolName == remoteProtocol.name) {
                    // We are assuming a participant will have only one instance of a protocol
                    return self.notifyMe(remoteProtocol.url, action);
                }
            }
        } else {
            NotifyResult|error notifyResult = (action == COMMAND_COMMIT) ? NOTIFY_RESULT_COMMITTED
                                                                         : NOTIFY_RESULT_ABORTED;
            foreach var remoteProtocol in self.participantProtocols {
                var result = self.notifyMe(remoteProtocol.url, action);
                if (result is error) {
                    notifyResult = result;
                }
                // Else, nothing to do since we have set the notifyResult already
            }
            return notifyResult;
        }
        return (); // No matching protocol
    }

    function prepareMe(string protocolUrl) returns @tainted PrepareResult|error {
        Participant2pcClientEP participantEP  = getParticipant2pcClient(protocolUrl);

        // Let's set this to true and change it to false only if a participant aborted or an error occurred while trying
        // to prepare a participant
        boolean successful = true;

        log:printInfo("Preparing remote participant: " + self.participantId);
        // If a participant voted NO or failed then abort
        var result = participantEP->prepare(self.transactionId);
        if (result is error) {
            log:printError("Remote participant: " + self.participantId + " failed", result);
            return result;
        } else {
            if (result == "aborted") {
                log:printInfo("Remote participant: " + self.participantId + " aborted.");
                return PREPARE_RESULT_ABORTED;
            } else if (result == "committed") {
                log:printInfo("Remote participant: " + self.participantId + " committed");
                return PREPARE_RESULT_COMMITTED;
            } else if (result == "read-only") {
                log:printInfo("Remote participant: " + self.participantId + " read-only");
                return PREPARE_RESULT_READ_ONLY;
            } else if (result == "prepared") {
                log:printInfo("Remote participant: " + self.participantId + " prepared");
                return PREPARE_RESULT_PREPARED;
            } else {
                log:printInfo("Remote participant: " + self.participantId + ", outcome: " + result);
            }
        }
        panic TransactionError("Remote participant:" + self.participantId + " replied with invalid outcome");
    }

    function notifyMe(string protocolUrl, string action) returns @tainted NotifyResult|error {
        Participant2pcClientEP participantEP;

        log:printInfo("Notify(" + action + ") remote participant: " + protocolUrl);
        participantEP = getParticipant2pcClient(protocolUrl);
        var result = participantEP->notify(self.transactionId, action);
        if (result is error) {
            log:printError("Remote participant: " + self.participantId + " replied with an error", result);
            return result;
        } else {
            if (result == NOTIFY_RESULT_ABORTED_STR) {
                log:printInfo("Remote participant: " + self.participantId + " aborted");
                return NOTIFY_RESULT_ABORTED;
            } else if (result == NOTIFY_RESULT_COMMITTED_STR) {
                log:printInfo("Remote participant: " + self.participantId + " committed");
                return NOTIFY_RESULT_COMMITTED;
            }
        }
        panic TransactionError("Unknown status on notify remote participant");
    }
}

class LocalParticipant {

    string participantId;
    private TwoPhaseCommitTransaction participatedTxn;
    private LocalProtocol[] participantProtocols;

    function init(string participantId, TwoPhaseCommitTransaction participatedTxn, LocalProtocol[]
        participantProtocols) {
        self.participantId = participantId;
        self.participatedTxn = participatedTxn;
        self.participantProtocols = participantProtocols;
    }

    function prepare(string protocol) returns [(PrepareResult|error)?, Participant] {
        foreach var localProto in self.participantProtocols {
            if (localProto.name == protocol) {
                log:printInfo("Preparing local participant: " + self.participantId);
                return [self.prepareMe(self.participatedTxn.transactionId, self.participatedTxn.transactionBlockId),
                self];
            }
        }
        return [(), self];
    }

    function prepareMe(string transactionId, string transactionBlockId) returns PrepareResult|error {
        string participatedTxnId = getParticipatedTransactionId(transactionId, transactionBlockId);
        if (!participatedTransactions.hasKey(participatedTxnId)) {
            return TransactionError(TRANSACTION_UNKNOWN);
        }
        if (self.participatedTxn.state == TXN_STATE_ABORTED) {
            removeParticipatedTransaction(participatedTxnId);
            log:printInfo("Local participant: " + self.participantId + " aborted");
            return PREPARE_RESULT_ABORTED;
        } else if (self.participatedTxn.state == TXN_STATE_COMMITTED) {
            removeParticipatedTransaction(participatedTxnId);
            return PREPARE_RESULT_COMMITTED;
        } else {
            boolean successful = prepareResourceManagers(transactionId, transactionBlockId);
            if (successful) {
                self.participatedTxn.state = TXN_STATE_PREPARED;
                log:printInfo("Local participant: " + self.participantId + " prepared");
                return PREPARE_RESULT_PREPARED;
            } else {
                log:printInfo("Local participant: " + self.participantId + " aborted");
                return PREPARE_RESULT_ABORTED;
            }
        }
    }

    function notify(string action, string? protocolName) returns (NotifyResult|error)? {
        if (protocolName is string) {
            foreach var localProto in self.participantProtocols {
                if (protocolName == localProto.name) {
                    log:printInfo("Notify(" + action + ") local participant: " + self.participantId);
                    return self.notifyMe(action, self.participatedTxn.transactionBlockId);
                }
            }
        } else {
            NotifyResult|error notifyResult = (action == COMMAND_COMMIT) ? NOTIFY_RESULT_COMMITTED
                                                                         : NOTIFY_RESULT_ABORTED;
            foreach var localProto in self.participantProtocols {
                var result = self.notifyMe(action, self.participatedTxn.transactionBlockId);
                if (result is error) {
                    notifyResult = result;
                }
                // Else, nothing to do since we have set the notifyResult already
            }
            return notifyResult;
        }
        return (); // No matching protocol
    }

    function notifyMe(string action, string participatedTxnBlockId) returns NotifyResult|error {
        string participatedTxnId = getParticipatedTransactionId(self.participatedTxn.transactionId, participatedTxnBlockId);
        if (action == COMMAND_COMMIT) {
            if (self.participatedTxn.state == TXN_STATE_PREPARED) {
                boolean successful = commitResourceManagers(self.participatedTxn.transactionId, participatedTxnBlockId);
                removeParticipatedTransaction(participatedTxnId);
                if (successful) {
                    return NOTIFY_RESULT_COMMITTED;
                } else {
                    return TransactionError(NOTIFY_RESULT_FAILED_EOT_STR);
                }
            } else {
                return TransactionError(NOTIFY_RESULT_NOT_PREPARED_STR);
            }
        } else if (action == COMMAND_ABORT) {
            boolean successful = abortResourceManagers(self.participatedTxn.transactionId, participatedTxnBlockId);
            removeParticipatedTransaction(participatedTxnId);
            if (successful) {
                return NOTIFY_RESULT_ABORTED;
            } else {
               return TransactionError(NOTIFY_RESULT_FAILED_EOT_STR);
            }
        } else {
            panic TransactionError("Invalid protocol action:" + action);
        }
    }
}
