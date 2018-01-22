package transactions.coordinator;
import ballerina.log;

public const string PROTOCOL_COMPLETION = "completion";
public const string PROTOCOL_VOLATILE = "volatile";
public const string PROTOCOL_DURABLE = "durable";

enum Protocols {
    COMPLETION, DURABLE, VOLATILE
}

enum TransactionState {
    ACTIVE, PREPARED, COMMITTED, ABORTED
}

struct TwoPhaseCommitTransaction {
    string transactionId;
    string coordinationType = "2pc";
    map participants;
    TransactionState state;
    boolean possibleMixedOutcome;
}

struct CommitRequest {
    string transactionId;
}

struct CommitResponse {
    string message;
}

struct PrepareRequest {
    string transactionId;
}

struct PrepareResponse {
    string message;
}

struct NotifyRequest {
    string transactionId;
    string message;
}

struct NotifyResponse {
    string message;
}

struct AbortRequest {
    string transactionId;
}

struct AbortResponse {
    string message;
}

function twoPhaseCommit (TwoPhaseCommitTransaction txn) returns (string message, error err) {
    log:printInfo("Running 2-phase commit for transaction: " + txn.transactionId);

    var volatileEndpoints, durableEndpoints = getVolatileAndDurableEndpoints(txn);

    // Prepare phase & commit phase
    // First call prepare on all volatile participants
    boolean prepareVolatilesSuccessful = prepare(txn, volatileEndpoints);
    if (prepareVolatilesSuccessful) {
        // if all volatile participants voted YES, Next call prepare on all durable participants
        boolean prepareDurablesSuccessful = prepare(txn, durableEndpoints);
        if (prepareDurablesSuccessful) {
            // If all durable participants voted YES (PREPARED or READONLY), next call notify(commit) on all
            // (durable & volatile) participants and return committed to the initiator
            boolean notifyDurablesSuccessful = notify(txn, durableEndpoints, "commit");
            boolean notifyVolatilesSuccessful = notify(txn, volatileEndpoints, "commit");
            if (notifyDurablesSuccessful && notifyVolatilesSuccessful) {
                message = "committed";
            } else {
                // return Hazard outcome if a participant cannot successfully end its branch of the transaction
                err = {msg:"Hazard-Outcome"};
            }
        } else {
            // If some durable participants voted NO, next call notify(abort) on all durable participants
            // and return aborted to the initiator
            boolean notifyDurablesSuccessful = notify(txn, durableEndpoints, "abort");
            boolean notifyVolatilesSuccessful = notify(txn, volatileEndpoints, "abort");
            if (notifyDurablesSuccessful && notifyVolatilesSuccessful) {
                if (txn.possibleMixedOutcome) {
                    message = "mixed";
                } else {
                    message = "aborted";
                }
            } else {
                // return Hazard outcome if a participant cannot successfully end its branch of the transaction
                err = {msg:"Hazard-Outcome"};
            }
        }
    } else {
        boolean notifySuccessful = notify(txn, volatileEndpoints, "abort");
        if (notifySuccessful) {
            if (txn.possibleMixedOutcome) {
                message = "mixed";
            } else {
                message = "aborted";
            }
        } else {
            // return Hazard outcome if a participant cannot successfully end its branch of the transaction
            err = {msg:"Hazard-Outcome"};
        }
    }
    return;
}

function notifyAbort (TwoPhaseCommitTransaction txn) returns (string message, error err) {
    map participants = txn.participants;
    string transactionId = txn.transactionId;
    any[] p = participants.values();
    int i = 0;
    message = "aborted";
    while (i < lengthof p) {
        var participant, _ = (Participant)p[i];
        Protocol[] protocols = participant.participantProtocols;
        if (protocols != null) {
            int j = 0;
            while (j < lengthof protocols) {
                Protocol proto = protocols[j];
                var status, e = notifyParticipant(transactionId, proto.url, "abort");
                if (e != null) {
                    err = {msg:"Hazard-Outcome"};
                    return;
                } else if(status == "committed") {
                    txn.possibleMixedOutcome = true;
                    message = "mixed";
                    return;
                }
                j = j + 1;
            }
        }
        i = i + 1;
    }
    return;
}

function getVolatileAndDurableEndpoints(TwoPhaseCommitTransaction txn) returns
                                                                       (string[] volatileEndpoints,
                                                                        string[] durableEndpoints) {
    volatileEndpoints = [];
    durableEndpoints = [];
    map participants = txn.participants;
    any[] p = participants.values();
    int i = 0;
    while (i < lengthof p) {
        var participant, _ = (Participant)p[i];
        Protocol[] protocols = participant.participantProtocols;
        if (protocols != null) {
            int j = 0;
            while (j < lengthof protocols) {
                Protocol proto = protocols[j];
                if (proto.name == PROTOCOL_VOLATILE) {
                    volatileEndpoints[lengthof volatileEndpoints] = proto.url;
                } else if (proto.name == PROTOCOL_DURABLE) {
                    durableEndpoints[lengthof durableEndpoints] = proto.url;
                }
                j = j + 1;
            }
        }
        i = i + 1;
    }
    return;
}

function prepare (TwoPhaseCommitTransaction txn, string[] participantURLs) returns (boolean successful) {
    endpoint<ParticipantClient> participantEP {
    }
    string transactionId = txn.transactionId;
    // Let's set this to true and change it to false only if a participant aborted or an error occurred while trying
    // to prepare a participant
    successful = true;
    int i = 0;
    while (i < lengthof participantURLs) {
        ParticipantClient participantClient = create ParticipantClient();
        bind participantClient with participantEP;

        log:printInfo("Preparing participant: " + participantURLs[i]);
        // If a participant voted NO then abort
        var status, e = participantEP.prepare(transactionId, participantURLs[i]);
        if (e != null || status == "aborted") {
            log:printInfo("Participant: " + participantURLs[i] + " failed or aborted");
            successful = false;
            break;
        } else if (status == "committed") {
            log:printInfo("Participant: " + participantURLs[i] + " committed");
            // If one or more participants returns "committed" and the overall prepare fails, we have to
            // report a mixed-outcome to the initiator
            txn.possibleMixedOutcome = true;
            // Don't send notify to this participant because it is has already committed. We can forget about this participant.
            participantURLs[i] = null; //TODO: Nulling this out because there is no way to remove an element from an array
        } else if (status == "read-only") {
            log:printInfo("Participant: " + participantURLs[i] + " read-only");
            // Don't send notify to this participant because it is read-only. We can forget about this participant.
            participantURLs[i] = null; //TODO: Nulling this out because there is no way to remove an element from an array
        } else {
            log:printInfo("Participant: " + participantURLs[i] + ", status: " + status);
        }
        i = i + 1;
    }
    return;
}

function notify (TwoPhaseCommitTransaction txn, string[] participantURLs, string message) returns (boolean successful) {
    string transactionId = txn.transactionId;
    int i = 0;
    successful = true;
    while (i < lengthof participantURLs) {
        string participantURL = participantURLs[i];
        if (participantURL != null) {
            var _, err = notifyParticipant(transactionId, participantURL, message);
            if (err != null) {
                successful = false;
                return;
            }
        }
        i = i + 1;
    }
    return;
}

function notifyParticipant (string transactionId, string url, string message) returns (string, error) {
    endpoint<ParticipantClient> participantEP {
    }
    ParticipantClient participantClient = create ParticipantClient();
    bind participantClient with participantEP;

    log:printInfo("Notify(" + message + ") participant: " + url);
    var status, e = participantEP.notify(transactionId, url, message);

    error err;
    if (e != null) { // participant may return "Transaction-Unknown", "Not-Prepared" or "Failed-EOT"
        err = e;
    } else if (status == "aborted") {
        log:printInfo("Participant: " + url + " aborted");
    } else if (status == "committed") {
        log:printInfo("Participant: " + url + " committed");
    }
    return status, err;
}
