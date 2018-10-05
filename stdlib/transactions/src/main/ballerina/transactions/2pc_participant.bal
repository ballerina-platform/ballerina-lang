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
type Protocol record {
    @readonly string name;
};

# This represents the protocol associated with the coordination type.
#
# + name - protocol name
type LocalProtocol record {
    @readonly string name;
};

# This represents the protocol associated with the coordination type.

# + name - protocol name
# + url - protocol URL. This URL will have a value only if the participant is remote. If the participant is local,
#         the `protocolFn` will be called
public type RemoteProtocol record {
    @readonly string name;
    @readonly string url;
};

type Participant abstract object {

    string participantId;

    function prepare(string protocol) returns ((PrepareResult|error)?, Participant);

    function notify(string action, string? protocolName) returns (NotifyResult|error)?;
};

type RemoteParticipant object {

    private string participantId;
    private string transactionId;
    private RemoteProtocol[] participantProtocols;

    new(participantId, transactionId, participantProtocols) {}

    function prepare(string protocol) returns ((PrepareResult|error)?, Participant) {
        foreach remoteProto in participantProtocols {
            if (remoteProto.name == protocol) {
                // We are assuming a participant will have only one instance of a protocol
                return (self.prepareMe(remoteProto.url), self);
            }
        }
        return ((), self); // No matching protocol
    }

    function notify(string action, string? protocolName) returns (NotifyResult|error)? {
        match protocolName {
            string proto => {
                foreach remoteProtocol in participantProtocols {
                    if (proto == remoteProtocol.name) {
                        // We are assuming a participant will have only one instance of a protocol
                        return self.notifyMe(remoteProtocol.url, action);
                    }
                }
            }
            () => {
                NotifyResult|error notifyResult = (action == COMMAND_COMMIT) ? NOTIFY_RESULT_COMMITTED
                                                                             : NOTIFY_RESULT_ABORTED;
                foreach remoteProtocol in participantProtocols {
                    var result = self.notifyMe(remoteProtocol.url, action);
                    match result {
                        error err => notifyResult = err;
                        NotifyResult => {} // Nothing to do since we have set the notifyResult already
                    }
                }
                return notifyResult;
            }
        }
        return (); // No matching protocol
    }

    function prepareMe(string protocolUrl) returns PrepareResult|error {
        endpoint Participant2pcClientEP participantEP;
        participantEP = getParticipant2pcClient(protocolUrl);

        // Let's set this to true and change it to false only if a participant aborted or an error occurred while trying
        // to prepare a participant
        boolean successful = true;

        log:printInfo("Preparing remote participant: " + self.participantId);
        // If a participant voted NO or failed then abort
        var result = participantEP->prepare(self.transactionId);
        match result {
            error err => {
                log:printError("Remote participant: " + self.participantId + " failed", err = err);
                return err;
            }
            string status => {
                if (status == "aborted") {
                    log:printInfo("Remote participant: " + self.participantId + " aborted.");
                    return PREPARE_RESULT_ABORTED;
                } else if (status == "committed") {
                    log:printInfo("Remote participant: " + self.participantId + " committed");
                    return PREPARE_RESULT_COMMITTED;
                } else if (status == "read-only") {
                    log:printInfo("Remote participant: " + self.participantId + " read-only");
                    return PREPARE_RESULT_READ_ONLY;
                } else if (status == "prepared") {
                    log:printInfo("Remote participant: " + self.participantId + " prepared");
                    return PREPARE_RESULT_PREPARED;
                } else {
                    log:printInfo("Remote participant: " + self.participantId + ", outcome: " + status);
                }
            }
        }
        error err = {message:"Remote participant:" + self.participantId + " replied with invalid outcome"};
        throw err;
    }

    function notifyMe(string protocolUrl, string action) returns NotifyResult|error {
        endpoint Participant2pcClientEP participantEP;

        log:printInfo("Notify(" + action + ") remote participant: " + protocolUrl);
        participantEP = getParticipant2pcClient(protocolUrl);
        var result = participantEP->notify(self.transactionId, action);
        match result {
            error err => {
                log:printError("Remote participant: " + self.participantId + " replied with an error", err = err);
                return err;
            }
            string notificationStatus => {
                if (notificationStatus == NOTIFY_RESULT_ABORTED_STR) {
                    log:printInfo("Remote participant: " + self.participantId + " aborted");
                    return NOTIFY_RESULT_ABORTED;
                } else if (notificationStatus == NOTIFY_RESULT_COMMITTED_STR) {
                    log:printInfo("Remote participant: " + self.participantId + " committed");
                    return NOTIFY_RESULT_COMMITTED;
                }
            }
        }
        error err = {message:"Unknown status on notify remote participant"};
        throw err;
    }
};

type LocalParticipant object {

    private string participantId;
    private TwoPhaseCommitTransaction participatedTxn;
    private LocalProtocol[] participantProtocols;

    new(participantId, participatedTxn, participantProtocols) {}

    function prepare(string protocol) returns ((PrepareResult|error)?, Participant) {
        foreach localProto in participantProtocols {
            if (localProto.name == protocol) {
                log:printInfo("Preparing local participant: " + self.participantId);
                return (prepareMe(participatedTxn.transactionId, participatedTxn.transactionBlockId), self);
            }
        }
        return ((), self);
    }

    function prepareMe(string transactionId, int transactionBlockId) returns PrepareResult|error {
        string participatedTxnId = getParticipatedTransactionId(transactionId, transactionBlockId);
        if (!participatedTransactions.hasKey(participatedTxnId)) {
            error err = {message:TRANSACTION_UNKNOWN};
            return err;
        }
        if (participatedTxn.state == TXN_STATE_ABORTED) {
            removeParticipatedTransaction(participatedTxnId);
            log:printInfo("Local participant: " + participantId + " aborted");
            return PREPARE_RESULT_ABORTED;
        } else if (participatedTxn.state == TXN_STATE_COMMITTED) {
            removeParticipatedTransaction(participatedTxnId);
            return PREPARE_RESULT_COMMITTED;
        } else {
            boolean successful = prepareResourceManagers(transactionId, transactionBlockId);
            if (successful) {
                participatedTxn.state = TXN_STATE_PREPARED;
                log:printInfo("Local participant: " + participantId + " prepared");
                return PREPARE_RESULT_PREPARED;
            } else {
                log:printInfo("Local participant: " + participantId + " aborted");
                return PREPARE_RESULT_ABORTED;
            }
        }
    }

    function notify(string action, string? protocolName) returns (NotifyResult|error)? {
        match protocolName {
            string proto => {
                foreach localProto in participantProtocols {
                    if (proto == localProto.name) {
                        log:printInfo("Notify(" + action + ") local participant: " + self.participantId);
                        return notifyMe(action, participatedTxn.transactionBlockId);
                    }
                }
            }
            () => {
                NotifyResult|error notifyResult = (action == COMMAND_COMMIT) ? NOTIFY_RESULT_COMMITTED
                                                                             : NOTIFY_RESULT_ABORTED;
                foreach localProto in participantProtocols {
                    var result = self.notifyMe(action, participatedTxn.transactionBlockId);
                    match result {
                        error err => notifyResult = err;
                        NotifyResult notifyRes => {} // Nothing to do since we have set the notifyResult already
                    }
                }
                return notifyResult;
            }
        }
        return (); // No matching protocol
    }

    function notifyMe(string action, int participatedTxnBlockId) returns NotifyResult|error {
        string participatedTxnId = getParticipatedTransactionId(participatedTxn.transactionId, participatedTxnBlockId);
        if (action == COMMAND_COMMIT) {
            if (participatedTxn.state == TXN_STATE_PREPARED) {
                boolean successful = commitResourceManagers(participatedTxn.transactionId, participatedTxnBlockId);
                removeParticipatedTransaction(participatedTxnId);
                if (successful) {
                    return NOTIFY_RESULT_COMMITTED;
                } else {
                    error err = {message:NOTIFY_RESULT_FAILED_EOT_STR};
                    return err;
                }
            } else {
                error err = {message:NOTIFY_RESULT_NOT_PREPARED_STR};
                return err;
            }
        } else if (action == COMMAND_ABORT) {
            boolean successful = abortResourceManagers(participatedTxn.transactionId, participatedTxnBlockId);
            removeParticipatedTransaction(participatedTxnId);
            if (successful) {
                return NOTIFY_RESULT_ABORTED;
            } else {
                error err = {message:NOTIFY_RESULT_FAILED_EOT_STR};
                return err;
            }
        } else {
            error err = {message:"Invalid protocol action:" + action};
            throw err;
        }
    }
};
