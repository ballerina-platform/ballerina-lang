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
import ballerina/io;

type ProtocolName "completion" | "volatile" | "durable";
@final ProtocolName PROTOCOL_COMPLETION = "completion";
@final ProtocolName PROTOCOL_VOLATILE = "volatile";
@final ProtocolName PROTOCOL_DURABLE = "durable";

type TransactionState "active" | "prepared" | "committed" | "aborted";
@final TransactionState TXN_STATE_ACTIVE = "active";
@final TransactionState TXN_STATE_PREPARED = "prepared";
@final TransactionState TXN_STATE_COMMITTED = "committed";
@final TransactionState TXN_STATE_ABORTED = "aborted";

@final string TRANSACTION_CONTEXT_VERSION = "1.0";

@final public string COMMAND_PREPARE = "prepare";
@final public string COMMAND_COMMIT = "commit";
@final public string COMMAND_ABORT = "abort";

@final public string OUTCOME_PREPARED = "prepared";
@final public string OUTCOME_NOT_PREPARED = "Not-Prepared";
@final public string OUTCOME_MIXED = "mixed";
@final public string OUTCOME_ABORTED = "aborted";
@final public string OUTCOME_COMMITTED = "committed";
@final public string OUTCOME_HAZARD = "Hazard-Outcome";
@final public string OUTCOME_FAILED_EOT = "Failed-EOT";
@final public string OUTCOME_READ_ONLY = "read-only";

public type TransactionContext {
    @readonly string contextVersion = "1.0";
    @readonly string transactionId;
    @readonly int transactionBlockId;
    @readonly string coordinationType;
    @readonly string registerAtURL;
};

type Participant {
    string participantId;
    Protocol[] participantProtocols;
};

documentation {
    This represents the protocol associated with the coordination type.

    F{{name}} - protocol name
    F{{url}}  - protocol URL. This URL will have a value only if the participant is remote. If the participant is local,
                the `protocolFn` will be called
    F{{protocolFn}} - This function will be called only if the participant is local. This avoid calls over the network.
}
public type Protocol {
    @readonly ProtocolName name;
    @readonly string url;
    @readonly int transactionBlockId;
    @readonly (function (string transactionId,
                           int transactionBlockId,
                           string protocolAction) returns boolean)? protocolFn;
};

public type RegistrationRequest {
    string transactionId;
    string participantId;
    Protocol[] participantProtocols;
};

public function regRequestToJson (RegistrationRequest req) returns json {
    json j = {};
    j.transactionId = req.transactionId;
    j.participantId = req.participantId;
    json[] protocols = [];
    foreach proto in req.participantProtocols {
        json j2 = {name: protocolNameToString(proto.name), url:proto.url};
        protocols[lengthof protocols] = j2;
    }
    j.participantProtocols = protocols;
    return j;
}

public type RegistrationResponse {
    string transactionId;
    Protocol[] coordinatorProtocols;
};

public function regResponseToJson (RegistrationResponse res) returns json {
    json j = {};
    j.transactionId = res.transactionId;
    json[] protocols;
    foreach proto in res.coordinatorProtocols {
        json j2 = {name: protocolNameToString(proto.name), url:proto.url};
        protocols[lengthof protocols] = j2;
    }
    j.coordinatorProtocols = protocols;
    return j;
}

public function jsonToRegResponse (json j) returns RegistrationResponse {
    string transactionId = <string>jsonToAny(j.transactionId);
    RegistrationResponse res = {transactionId:transactionId};
    Protocol[] protocols;
    foreach proto in j.coordinatorProtocols {
        string name = <string>jsonToAny(proto.name);
        string url = <string>jsonToAny(proto.url);
        Protocol p = {name:stringToProtocolName(name), url:url};
        protocols[lengthof protocols] = p;
    }
    res.coordinatorProtocols = protocols;
    return res;
}

//TODO: This function is a workaround for type system limitations
function protocolNameToString(ProtocolName protoName) returns string {
    if(protoName == PROTOCOL_COMPLETION) {
        return "completion";
    }
    if(protoName == PROTOCOL_DURABLE) {
        return "durable";
    }
    if(protoName == PROTOCOL_VOLATILE) {
        return "volatile";
    }
    error err = {message: "Invalid protocol name"};
    throw err;
}

//TODO: This function is a workaround for type system limitations
function stringToProtocolName(string strName) returns ProtocolName {
    if (strName == "completion") {
        return PROTOCOL_COMPLETION;
    }
    if(strName == "durable"){
        return PROTOCOL_DURABLE;
    }
    if(strName == "volatile") {
        return PROTOCOL_VOLATILE;
    }
    error err = {message: "Invalid protocol name " + strName};
    throw err;
}

// TODO: temp function. Remove when =? is fixed for json
function jsonToAny(json j) returns any {
    match j {
        int i => return i;
        string s => return s;
        boolean b => return b;
        () => return null;
        json j2 => return j2;
    }
}

public type RequestError {
    string errorMessage;
};

public type PrepareRequest {
    string transactionId;
};

public type PrepareResponse {
    string message;
};

public type NotifyRequest {
    string transactionId;
    string message;
};

public type NotifyResponse {
    string message;
};
