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
import ballerina/http;

@final string TWO_PHASE_COMMIT = "2pc";

string[] coordinationTypes = [TWO_PHASE_COMMIT];

map<ProtocolName[]> coordinationTypeToProtocolsMap = getCoordinationTypeToProtocolsMap();
function getCoordinationTypeToProtocolsMap () returns map<ProtocolName[]> {
    ProtocolName[] twoPhaseCommitProtocols = [PROTOCOL_COMPLETION, PROTOCOL_VOLATILE, PROTOCOL_DURABLE];
    map<ProtocolName[]> m;
    m[TWO_PHASE_COMMIT] = twoPhaseCommitProtocols;
    return m;
}

@http:ServiceConfig {
    basePath:initiatorCoordinatorBasePath
}
//documentation {
//    Service on the initiator which is independent from the coordination type and handles registration of remote participants.
//}
service InitiatorService bind coordinatorListener {

    @http:ResourceConfig {
        methods:["POST"],
        path:registrationPathPattern,
        body:"regReq",
        consumes:["application/json"]
    }
    documentation {
        register(in: Micro-Transaction-Registration,
        out: Micro-Transaction-Coordination?,
        fault: ( Invalid-Protocol |
        Already-Registered |
        Cannot-Register |
        Micro-Transaction-Unknown )? )

        If the registering participant specified a protocol name not matching the coordination type of the micro-transaction,
        the following fault is returned:

        Invalid-Protocol

                If the registering participant is already registered to the micro-transaction,
        the following fault is returned:

        Already-Registered

                If the coordinator already started the end-of-transaction processing for participants of the Durable
         protocol (see section 3.1.2) of the micro-transaction, the following fault is returned. Note explicitly,
         that registration for the Durable protocol is allowed while the coordinator is running the end-of-transaction
         processing for participants of the Volatile protocol (see section 3.1.3).

         Cannot-Register
        If the registering participant specified an unknown micro-transaction identifier, the following fault is returned:

        Micro-Transaction-Unknown
    }
    register (endpoint conn, http:Request req, int transactionBlockId, RegistrationRequest regReq) {
        string participantId = regReq.participantId;
        string txnId = regReq.transactionId;

        if (!initiatedTransactions.hasKey(txnId)) {
            respondToBadRequest(conn, "Transaction-Unknown. Invalid TID:" + txnId);
        } else {
            TwoPhaseCommitTransaction txn = initiatedTransactions[txnId];
            if (isRegisteredParticipant(participantId, txn.participants)) { // Already-Registered
                respondToBadRequest(conn, "Already-Registered. TID:" + txnId + ",participant ID:" + participantId);
            } else if (!protocolCompatible(txn.coordinationType,
                                           regReq.participantProtocols)) { // Invalid-Protocol
                respondToBadRequest(conn, "Invalid-Protocol. TID:" + txnId + ",participant ID:" + participantId);
            } else {
                Participant participant = {participantId:participantId,
                                              participantProtocols:regReq.participantProtocols};
                txn.participants[participantId] = participant;
                Protocol[] participantProtocols = regReq.participantProtocols;
                Protocol[] coordinatorProtocols = [];
                int i = 0;
                foreach participantProtocol in participantProtocols {
                    Protocol coordinatorProtocol = {name:participantProtocol.name,
                                                       url:getCoordinatorProtocolAt(participantProtocol.name,
                                                                                    transactionBlockId)};
                    coordinatorProtocols[i] = coordinatorProtocol;
                    i = i + 1;
                }

                RegistrationResponse regRes = {transactionId:txnId,
                                                  coordinatorProtocols:coordinatorProtocols};
                json resPayload = regResponseToJson(regRes);
                http:Response res = new; res.statusCode = http:OK_200;
                res.setJsonPayload(resPayload);
                var resResult = conn -> respond(res);
                match resResult {
                    error err => log:printErrorCause("Sending response for register request for transaction " + txnId +
                                                     " failed", err);
                    () => log:printInfo("Registered remote participant: " + participantId + " for transaction: " +
                                          txnId);
                }
            }
        }
        //TODO: Need to handle the  Cannot-Register error case    
    }
}
