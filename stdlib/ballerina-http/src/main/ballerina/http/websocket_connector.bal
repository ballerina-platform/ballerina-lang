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

documentation {
    Represents a WebSocket connector in ballerina. This include all connector oriented operations.
}
public type WebSocketConnector object {
    private {
        boolean isReady = false;
    }

    documentation {
        Push text to the connection.

        P{{text}} Text to be sent
        P{{final}} True if this is a final frame of a (long) message
        R{{}} `error` if an error occurs when sending
    }
    public native function pushText(string text, boolean final = true) returns error|();

    documentation {
        Push binary data to the connection.

        P{{data}} Binary data to be sent
        P{{final}} True if this is a final frame of a (long) message
        R{{}} `error` if an error occurs when sending
    }
    public native function pushBinary(blob data, boolean final = true) returns error|();

    documentation {
        Ping the connection.

        P{{data}} Binary data to be sent.
        R{{}} `error` if an error occurs when sending
    }
    public native function ping(blob data) returns error|();

    documentation {
        Send pong message to the connection.

        P{{data}} Binary data to be sent
        R{{}} `error` if an error occurs when sending
    }
    public native function pong(blob data) returns error|();

    documentation {
        Close the connection.

        P{{statusCode}} Status code for closing the connection
        P{{reason}} Reason for closing the connection
        P{{timeoutInSecs}} timeout which waits for the close frame from remote endpoint to close the connection.
                           If the timeout exceeds then the connection will be terminated even though a close frame
                           is not received from the remote backend. If the value is -1 the connection will wait until
                           a close frame is received.
                           Default value 0: After sending the close frame connection will be closed automatically
                           (Timeout is 0).
        R{{}} `error` if an error occurs when sending
    }
    public native function close(int statusCode, string reason, int timeoutInSecs = 0) returns error|();

    documentation {
        Called when the endpoint is ready to receive messages. Can be called only once per endpoint. For the
         WebSocketListener can be called only in upgrade or onOpen resources.

        R{{}} `error` if an error occurs when sending
    }
    public native function ready() returns error|();

};

