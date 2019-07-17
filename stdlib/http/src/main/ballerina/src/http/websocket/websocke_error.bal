// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public const CONNECTION_CLOSURE_ERROR = "{ballerina/http}WsConnectionClosureError";
public type WsConnectionClosureError error<CONNECTION_CLOSURE_ERROR, Detail>;

public const INVALID_HANDSHAKE_ERROR = "{ballerina/http}WsInvalidHandshakeError";
public type WsInvalidHandshakeError error<INVALID_HANDSHAKE_ERROR, Detail>;

public const PAYLOAD_TOO_BIG_ERROR = "{ballerina/http}WsPayloadTooBigError";
public type WsPayloadTooBigError error<PAYLOAD_TOO_BIG_ERROR, Detail>;

public const PROTOCOL_ERROR = "{ballerina/http}WsProtocolError";
public type WsProtocolError error<PROTOCOL_ERROR, Detail>;

public const CONNECTION_ERROR = "{ballerina/http}WsConnectionError";
public type WsConnectionError error<CONNECTION_ERROR, Detail>;

public const INVALID_CONTINUATION_FRAME_ERROR = "{ballerina/http}WsInvalidContinuationFrameError";
public type WsInvalidContinuationFrameError error<INVALID_CONTINUATION_FRAME_ERROR, Detail>;

public const GENERIC_ERROR = "{ballerina/http}WsGenericError";
public type WsGenericError error<GENERIC_ERROR, Detail>;

public type WebSocketError WsConnectionClosureError|WsInvalidHandshakeError|WsPayloadTooBigError|
WsProtocolError|WsConnectionError|WsInvalidContinuationFrameError|WsGenericError;
