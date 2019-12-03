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

type TransactionContext record {
    string contextVersion = "1.0";
    string transactionId = "";
    string transactionBlockId = "";
    string coordinationType = "";
    string registerAtURL = "";
};

type RegistrationRequest record {
    string transactionId = "";
    string participantId = "";
    RemoteProtocol[] participantProtocols = [];
};

type RegistrationResponse record {
    string transactionId = "";
    RemoteProtocol[] coordinatorProtocols = [];
};

function toProtocolArray(RemoteProtocol[] remoteProtocols) returns UProtocol?[] {
    UProtocol?[] protocols = [];
    foreach var remoteProtocol in remoteProtocols {
        LocalProtocol proto = {name:remoteProtocol.name};
        protocols[protocols.length()] = proto;
    }
    return protocols;
}

type RequestError record {
    string errorMessage = "";
};

type PrepareRequest record {
    string transactionId = "";
};

type PrepareResponse record {
    string message = "";
};

type NotifyRequest record {
    string transactionId = "";
    string message = "";
};

type NotifyResponse record {
    string message = "";
};

type ParticipantFunctionResult record {|
    any|error data;
|};
