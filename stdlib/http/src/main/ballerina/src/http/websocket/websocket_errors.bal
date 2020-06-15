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

# Raised during failures in connection closure
public type WsConnectionClosureError distinct error;

# Raised during the handshake when the WebSocket upgrade fails
public type WsInvalidHandshakeError distinct error;

# Raised when receiving a frame with a payload exceeding the maximum size
public type WsPayloadTooBigError distinct error;

# Raised when the other side breaks the protocol
public type WsProtocolError distinct error;

# Raised during connection failures
public type WsConnectionError distinct error;

# Raised when an out of order/invalid continuation frame is received
public type WsInvalidContinuationFrameError distinct error;

# Raised for errors not captured by the specific errors
public type WsGenericError distinct error;

# The union of all the WebSocket related errors
public type WebSocketError WsConnectionClosureError|WsInvalidHandshakeError|WsPayloadTooBigError|
WsProtocolError|WsConnectionError|WsInvalidContinuationFrameError|WsGenericError;
