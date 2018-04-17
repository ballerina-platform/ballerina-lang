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

package ballerina.transactions;

import ballerina/log;

documentation {
    This represents the protocol associated with the coordination type.

    F{{name}} - protocol name
}
public type Protocol {
    @readonly string name;
};

documentation {
    This represents the protocol associated with the coordination type.

    F{{name}} - protocol name
    F{{transactionBlockId}} - ID of the transaction block corresponding to this local participant
}
public type LocalProtocol {
    @readonly string name;
    @readonly int transactionBlockId;
};

documentation {
    This represents the protocol associated with the coordination type.

    F{{name}} - protocol name
    F{{url}}  - protocol URL. This URL will have a value only if the participant is remote. If the participant is local,
                the `protocolFn` will be called
}
public type RemoteProtocol {
    @readonly string name;
    @readonly string url;
};

type Participant object {
    private {
        string participantId;
    }

    function prepare(string protocol) returns PrepareResult | () | error   {

    }

    function notify(string action, string? protocolName) returns NotifyResult | error {

    }
};

type RemoteParticipant object {
    private {
        string participantId;
        TwoPhaseCommitTransaction txn;
        RemoteProtocol[] participantProtocols;
    }

    new(participantId, txn, participantProtocols){}

    function prepare(string protocol) returns PrepareResult | () | error  {
        boolean successful = true;
        foreach remoteProto in participantProtocols {
            if(remoteProto.name == protocol) {
                // We are assuming a participant will have only one instance of a protocol
                var result = self.prepareMe(remoteProto.url);
                io:print("NEW BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB: ");
                io:println(result);
                return result;
            }
        }
        return (); // No matching protocol
    }

    function notify(string action, string? protocolName) returns NotifyResult | error {
        match protocolName {
            string proto => {
                foreach remoteProtocol in participantProtocols {
                    if(proto == remoteProtocol.name) {
                        // We are assuming a participant will have only one instance of a protocol
                        return self.notifyMe(remoteProtocol.url, action);
                    }
                }
            }
            () => {
                NotifyResult|error notifyResult =
                                (action == COMMAND_COMMIT) ? NOTIFY_RESULT_COMMITTED : NOTIFY_RESULT_ABORTED;
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
    }

    function prepareMe(string protocolUrl) returns PrepareResult|error {
        endpoint Participant2pcClientEP participantEP;
        participantEP = getParticipant2pcClientEP(protocolUrl);

        // Let's set this to true and change it to false only if a participant aborted or an error occurred while trying
        // to prepare a participant
        boolean successful = true;
        string transactionId = txn.transactionId;

        log:printInfo("Preparing remote participant: " + participantId);
        // If a participant voted NO or failed then abort
        var result = participantEP -> prepare(transactionId);
        match result {
            error err => {
                log:printErrorCause("Remote participant: " + participantId + " failed", err);
                txn.removeParticipant(participantId, "Could not remove failed participant: " + participantId +
                                                        " from transaction: " + transactionId);
                return err;
            }
            string status => {
                if (status == "aborted") {
                    log:printInfo("Remote participant: " + participantId + " aborted.");
                    return PREPARE_RESULT_ABORTED;
                } else if (status == "committed") {
                    log:printInfo("Remote participant: " + participantId + " committed");
                    return PREPARE_RESULT_COMMITTED;
                } else if (status == "read-only") {
                    log:printInfo("Remote participant: " + participantId + " read-only");
                    return PREPARE_RESULT_READ_ONLY;
                } else if (status == "prepared") {
                    log:printInfo("Remote participant: " + participantId + " prepared");
                    return PREPARE_RESULT_PREPARED;
                } else {
                    log:printInfo("Remote participant: " + participantId + ", outcome: " + status);
                }
            }
        }
        error err = {message: "Remote participant:" + participantId + " replied with invalid outcome"};
        throw err;
    }

    function notifyMe(string protocolUrl, string action) returns NotifyResult | error {
        endpoint Participant2pcClientEP participantEP;

        string participantId = self.participantId;
        log:printInfo("Notify(" + action + ") remote participant: " + protocolUrl);
        participantEP = getParticipant2pcClientEP(protocolUrl);
        var result = participantEP -> notify(txn.transactionId, action);
        match result {
            error err => {
                log:printErrorCause("Remote participant: " + participantId + " replied with an error", err);
                return err;
            }
            string notificationStatus => {
                if (notificationStatus == NOTIFY_RESULT_ABORTED_STR) {
                    log:printInfo("Remote participant: " + participantId + " aborted");
                    return NOTIFY_RESULT_ABORTED;
                } else if (notificationStatus == NOTIFY_RESULT_COMMITTED_STR) {
                    log:printInfo("Remote participant: " + participantId + " committed");
                    return NOTIFY_RESULT_COMMITTED;
                }
            }
        }
        error err = {message: "Unknown status on notify remote participant"};
        throw err;
    }
};

type LocalParticipant object {
    private {
        string participantId;
        TwoPhaseCommitTransaction txn;
        LocalProtocol[] participantProtocols;
    }

    new(participantId, txn, participantProtocols){}

    function prepare(string protocol) returns PrepareResult|()|error  {
        boolean successful = true;
        foreach localProto in participantProtocols {
            if(localProto.name == protocol) {
                log:printInfo("Preparing local participant: " + self.participantId);
                return prepareMe(txn.transactionId, localProto.transactionBlockId, COMMAND_PREPARE);
            }
        }
        return ();
    }

    function prepareMe (string transactionId, int transactionBlockId,
                                         string protocolAction) returns PrepareResult|error  {
        string participatedTxnId = getParticipatedTransactionId(transactionId, transactionBlockId);
        if (!participatedTransactions.hasKey(participatedTxnId)) {
            error err = {message:"Transaction-Unknown"};
            return err;
        }
        TwoPhaseCommitTransaction participatedTxn = participatedTransactions[participatedTxnId];
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

        error err = {message: "Local participant:" + participantId + " outcome invalid"};
        throw err;
    }

    function notify(string action, string? protocolName) returns NotifyResult | error {
        match protocolName {
            string proto => {
                foreach localProto in participantProtocols {
                    if(proto == localProto.name) {
                        log:printInfo("Notify(" + action + ") local participant: " + self.participantId);
                        return notifyMe(action);
                    }
                }
            }
            () => {
                NotifyResult|error notifyResult =
                                (action == COMMAND_COMMIT) ? NOTIFY_RESULT_COMMITTED : NOTIFY_RESULT_ABORTED;
                foreach remoteProtocol in participantProtocols {
                    var result = self.notifyMe(action);
                    match result {
                        error err => notifyResult = err;
                        NotifyResult notifyRes => {} // Nothing to do since we have set the notifyResult already
                    }
                }
                return notifyResult;
            }
        }    
    }

    function notifyMe(string action) returns NotifyResult | error {
        string participatedTxnId = getParticipatedTransactionId(txn.transactionId, txn.transactionBlockId);
        if(action == "commit") {
            if (txn.state == TXN_STATE_PREPARED) {
                boolean successful = commitResourceManagers(txn.transactionId, txn.transactionBlockId);
                removeParticipatedTransaction(participatedTxnId);
                if(successful) {
                    return NOTIFY_RESULT_COMMITTED;
                } else {
                    error err = {message: "Failed-EOT"};
                    return err;
                }
            } else {
                error err = {message: "Not prepared"};
                return err;
            }
        } else if (action == "abort") {
            boolean successful = abortResourceManagers(txn.transactionId, txn.transactionBlockId);
            removeParticipatedTransaction(participatedTxnId);
            if(successful) {
                return NOTIFY_RESULT_ABORTED;
            } else {
                error err = {message: "Failed-EOT"};
                return err;
            }
        } else {
            error err = {message: "Invalid protocol action:" + action};
            throw err;
        }
    }
};

