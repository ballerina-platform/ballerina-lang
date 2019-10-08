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

# Error reason for failures during connection closure
public const CONNECTION_CLOSURE_ERROR = "{ballerina/http}WsConnectionClosureError";
# Raised during failures in connection closure
public type WsConnectionClosureError error<CONNECTION_CLOSURE_ERROR, Detail>;

# Error reason for WebSocket handshake failures
public const INVALID_HANDSHAKE_ERROR = "{ballerina/http}WsInvalidHandshakeError";
# Raised during the handshake when the WebSocket upgrade fails
public type WsInvalidHandshakeError error<INVALID_HANDSHAKE_ERROR, Detail>;

# Error reason for exceeding maximum frame size
public const PAYLOAD_TOO_BIG_ERROR = "{ballerina/http}WsPayloadTooBigError";
# Raised when receiving a frame with a payload exceeding the maximum size
public type WsPayloadTooBigError error<PAYLOAD_TOO_BIG_ERROR, Detail>;

# Error reason for other side breaking the protocol
public const PROTOCOL_ERROR = "{ballerina/http}WsProtocolError";
# Raised when the other side breaks the protocol
public type WsProtocolError error<PROTOCOL_ERROR, Detail>;

# Error reason for connection failures
public const CONNECTION_ERROR = "{ballerina/http}WsConnectionError";
# Raised during connection failures
public type WsConnectionError error<CONNECTION_ERROR, Detail>;

# Error reason for invalid continuation frame
public const INVALID_CONTINUATION_FRAME_ERROR = "{ballerina/http}WsInvalidContinuationFrameError";
# Raised when an out of order/invalid continuation frame is received
public type WsInvalidContinuationFrameError error<INVALID_CONTINUATION_FRAME_ERROR, Detail>;

# Error reason for errors not captured by the specific errors
public const GENERIC_ERROR = "{ballerina/http}WsGenericError";
# Raised for errors not captured by the specific errors
public type WsGenericError error<GENERIC_ERROR, Detail>;

# The union of all the WebSocket related errors
public type WebSocketError WsConnectionClosureError|WsInvalidHandshakeError|WsPayloadTooBigError|
WsProtocolError|WsConnectionError|WsInvalidContinuationFrameError|WsGenericError;
