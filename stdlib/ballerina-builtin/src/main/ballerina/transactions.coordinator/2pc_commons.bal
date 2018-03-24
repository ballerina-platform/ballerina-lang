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

import ballerina/log;

const string PROTOCOL_COMPLETION = "completion";
const string PROTOCOL_VOLATILE = "volatile";
const string PROTOCOL_DURABLE = "durable";

enum Protocols {
    COMPLETION, DURABLE, VOLATILE
}

public enum TransactionState {
    ACTIVE, PREPARED, COMMITTED, ABORTED
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

function getParticipant2pcClientEP (string participantURL) returns Participant2pcClientEP {
    if (httpClientCache.hasKey(participantURL)) {
        Participant2pcClientEP participantEP =? <Participant2pcClientEP>httpClientCache.get(participantURL);
        return participantEP;
    } else {
        Participant2pcClientEP participantEP = {};
        Participant2pcClientConfig config = {participantURL:participantURL,
                                                endpointTimeout:120000, retryConfig:{count:5, interval:5000}};
        participantEP.init(config);
        httpClientCache.put(participantURL, participantEP);
        return participantEP;
    }
}
