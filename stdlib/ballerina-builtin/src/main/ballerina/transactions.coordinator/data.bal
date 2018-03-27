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

package ballerina.transactions.coordinator;
import ballerina/io;

struct Transaction {
    string transactionId;
    int transactionBlockId;
    string coordinationType = "2pc";
    boolean isInitiated; // Indicates whether this is a transaction that was initiated or is participated in
    map<Participant> participants;
    Protocol[] coordinatorProtocols;
}

public struct TransactionContext {
    string contextVersion = "1.0";
    string transactionId;
    int transactionBlockId;
    string coordinationType;
    string registerAtURL;
}

struct Participant {
    string participantId;
    Protocol[] participantProtocols;
}

documentation {
    This represents the protocol associated with the coordination type.

    F{{name}} - protocol name
    F{{url}}  - protocol URL. This URL will have a value only if the participant is remote. If the participant is local,
                the `protocolFn` will be called
    F{{protocolFn}} - This function will be called only if the participant is local. This avoid calls over the network.
}
public struct Protocol {
    string name;
    string url;
    int transactionBlockId;
    (function (string transactionId,
               int transactionBlockId,
               string protocolAction) returns boolean)|null protocolFn;
}

public struct RegistrationRequest {
    string transactionId;
    string participantId;
    Protocol[] participantProtocols;
}

public function regRequestToJson (RegistrationRequest req) returns json {
    json j;
    j.transactionId = req.transactionId;
    j.participantId = req.participantId;
    //json[] protocols = [];
    //foreach proto in req.participantProtocols {
    //    json j2 = {name:proto.name, url:proto.url};
    //    protocols[lengthof protocols - 1] = j2;
    //}
    //j.participantProtocols = protocols;
    j.participantProtocols = [{name:req.participantProtocols[0].name, url:req.participantProtocols[0].url}];
    return j;
}

public struct RegistrationResponse {
    string transactionId;
    Protocol[] coordinatorProtocols;
}

public function regResponseToJson (RegistrationResponse res) returns json {
    json j;
    j.transactionId = res.transactionId;
    json[] protocols;
    foreach proto in res.coordinatorProtocols {
        json j2 = {name:proto.name, url:proto.url};
        protocols[lengthof protocols] = j2;
    }
    j.coordinatorProtocols = protocols;
    return j;
}

public function jsonToRegResponse (json j) returns RegistrationResponse {
    io:println(j.transactionId);
    //string transactionId =? <string>j.transactionId; //TODO: Fix
    string transactionId = <string>jsonToAny(j.transactionId);
    RegistrationResponse res = {transactionId:transactionId};
    Protocol[] protocols;
    foreach proto in j.coordinatorProtocols {
        string name = <string>jsonToAny(proto.name);
        string url = <string>jsonToAny(proto.url);
        //string name =? <string>proto.name; //TODO: Fix
        //string url =? <string>proto.url; //TODO: Fix
        Protocol p = {name:name, url:url};
        protocols[lengthof protocols] = p;
    }
    res.coordinatorProtocols = protocols;
    return res;
}

// TODO: temp function. Remove when =? is fixed for json
function jsonToAny(json j) returns any {
    match j {
        int i => return i;
        string s => return s;
        boolean b => return b;
        null => return null;
        json j2 => return j2;
    }
}

public struct RequestError {
    string errorMessage;
}

public struct PrepareRequest {
    string transactionId;
}

public struct PrepareResponse {
    string message;
}

public struct NotifyRequest {
    string transactionId;
    string message;
}

public struct NotifyResponse {
    string message;
}
