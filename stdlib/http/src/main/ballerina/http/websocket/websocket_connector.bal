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

import ballerina/internal;

documentation {
    Represents a WebSocket connector in ballerina. This include all connector oriented operations.
}
public type WebSocketConnector object {
    private boolean isReady = false;

    documentation {
        Push text to the connection.

        P{{data}} Data to be sent, if byte[] it is converted to a UTF-8 string for sending
        P{{final}} True if this is a final frame of a (long) message
        R{{}} `error` if an error occurs when sending
    }
    public function pushText(string|json|xml|boolean|int|float|byte|byte[] data, boolean final = true) returns error? {
        string text;
        match data {
            byte byteContent => {
                text = <string>(<int>byteContent);
            }
            int integerContent => {
                text = <string>integerContent;
            }
            float floatContent => {
                text = <string>floatContent;
            }
            byte[] byteArrayContent => {
                text = internal:byteArrayToString(byteArrayContent, "UTF-8");
            }
            string textContent => {
                text = textContent;
            }
            xml xmlContent => {
                text = <string>xmlContent;
            }
            json jsonContent => {
                text = jsonContent.toString();
            }
        }
        return externPushText(text, final);
    }

    extern function externPushText(string text, boolean final) returns error?;
    documentation {
        Push binary data to the connection.

        P{{data}} Binary data to be sent
        P{{final}} True if this is a final frame of a (long) message
        R{{}} `error` if an error occurs when sending
    }
    public extern function pushBinary(byte[] data, boolean final = true) returns error?;

    documentation {
        Ping the connection.

        P{{data}} Binary data to be sent.
        R{{}} `error` if an error occurs when sending
    }
    public extern function ping(byte[] data) returns error?;

    documentation {
        Send pong message to the connection.

        P{{data}} Binary data to be sent
        R{{}} `error` if an error occurs when sending
    }
    public extern function pong(byte[] data) returns error?;

    documentation {
        Close the connection.

        P{{statusCode}} Status code for closing the connection
        P{{reason}} Reason for closing the connection
        P{{timeoutInSecs}} Time to waits for the close frame from the remote endpoint before closing the connection.
                           If the timeout exceeds then the connection is terminated even though a close frame
                           is not received from the remote endpoint. If the value < 0 (eg: -1) the connection waits
                           until a close frame is received. If WebSocket frame is received from the remote endpoint
                           within waiting period the connection is terminated immediately.
        R{{}} `error` if an error occurs when sending
    }
    public extern function close(int statusCode, string reason, int timeoutInSecs = 60) returns error?;

    documentation {
        Called when the endpoint is ready to receive messages. Can be called only once per endpoint. For the
         WebSocketListener can be called only in upgrade or onOpen resources.

        R{{}} `error` if an error occurs when sending
    }
    public extern function ready() returns error?;

};

