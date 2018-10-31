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

public type TransactionContext record {
    @readonly string contextVersion = "1.0";
    @readonly string transactionId;
    @readonly int transactionBlockId;
    @readonly string coordinationType;
    @readonly string registerAtURL;
};

type RegistrationRequest record {
    string transactionId;
    string participantId;
    RemoteProtocol[] participantProtocols;
};

type RegistrationResponse record {
    string transactionId;
    RemoteProtocol[] coordinatorProtocols;
};

function toProtocolArray(RemoteProtocol[] remoteProtocols) returns Protocol[] {
    Protocol[] protocols;
    foreach remoteProtocol in remoteProtocols {
        Protocol proto = {name:remoteProtocol.name};
        protocols[lengthof protocols] = proto;
    }
    return protocols;
}

public type RequestError record {
    string errorMessage;
};

public type PrepareRequest record {
    string transactionId;
};

public type PrepareResponse record {
    string message;
};

public type NotifyRequest record {
    string transactionId;
    string message;
};

public type NotifyResponse record {
    string message;
};
