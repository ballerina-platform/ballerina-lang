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
Represent the JMS message used to send and receive content from the a JMS provider.

Most message-oriented middleware (MOM) products treat messages as lightweight entities that consist of a header
and a body. The header contains fields used for message routing and identification; the body contains the
application data being sent.
}
public type Message object {

    documentation { Gets text content of the JMS message
        R{{}} the string containing this message's data or an JMS error
    }
    public extern function getTextMessageContent() returns @tainted string|error;

    documentation { Gets map content of the JMS message
        R{{}} the string containing this message's data or an JMS error
    }
    public extern function getMapMessageContent() returns @tainted map|error;

    documentation { Sets a JMS transport string property from the message
        P{{key}} The string property name
        P{{value}} The string property value
        R{{}} nil or an JMS error
    }
    public extern function setStringProperty(@sensitive string key, string value) returns error?;

    documentation { Gets a JMS transport string property from the message
        P{{key}} The string property name
        R{{}} The string property value, JMS error or nil if there is no property by this name
    }
    public extern function getStringProperty(@sensitive string key) returns @tainted (string|error)?;

    documentation { Sets a JMS transport integer property from the message
        P{{key}} The integer property name
        P{{value}} The integer property value
        R{{}} nil or an JMS error
    }
    public extern function setIntProperty(@sensitive string key, int value) returns error?;

    documentation { Gets a JMS transport integer property from the message
        P{{key}} The integer property name
        R{{}} The integer property value or JMS error
    }
    public extern function getIntProperty(@sensitive string key) returns @tainted int|error;

    documentation { Sets a JMS transport boolean property from the message
        P{{key}} The boolean property name
        P{{value}} The boolean property value
        R{{}} nil or an JMS error
    }
    public extern function setBooleanProperty(@sensitive string key, boolean value) returns error?;

    documentation { Gets a JMS transport boolean property from the message
        P{{key}} The boolean property name
        R{{}} The boolean property value or JMS error
    }
    public extern function getBooleanProperty(@sensitive string key) returns @tainted boolean|error;

    documentation { Sets a JMS transport float property from the message
        P{{key}} The float property name
        P{{value}} The float property value
        R{{}} nil or an JMS error
    }
    public extern function setFloatProperty(@sensitive string key, float value) returns error?;

    documentation { Gets a JMS transport float property from the message
        P{{key}} The float property name
        R{{}} The float property value or JMS error
    }
    public extern function getFloatProperty(@sensitive string key) returns @tainted float|error;

    documentation { Gets JMS transport header MessageID from the message
        R{{}} The header value or JMS error
    }
    public extern function getMessageID() returns @tainted string|error;

    documentation { Gets JMS transport header Timestamp from the message
        R{{}} The timestamp header value or JMS error
    }
    public extern function getTimestamp() returns @tainted int|error;

    documentation {Sets DeliveryMode JMS transport header to the message
        P{{mode}} The header value
        R{{}} nil or an JMS error
    }
    public extern function setDeliveryMode(int mode) returns error?;

    documentation { Get JMS transport header DeliveryMode from the message
        R{{}} The delivery mode header value or JMS error
    }
    public extern function getDeliveryMode() returns @tainted int|error;

    documentation { Sets Expiration JMS transport header to the message
        P{{value}} The header value
        R{{}} nil or an JMS error
    }
    public extern function setExpiration(int value) returns error?;

    documentation { Gets JMS transport header Expiration from the message
        R{{}} The expiration header value or JMS error
    }
    public extern function getExpiration() returns @tainted int|error;

    documentation { Sets Type JMS transport header to the message
        P{{messageType}} The message type header value
        R{{}} nil or an JMS error if any JMS provider level internal error occur
    }
    public extern function setType(string messageType) returns error?;

    documentation { Gets JMS transport header Type from the message
        R{{}} The JMS message type header value or JMS error
    }
    public extern function getType() returns @tainted string|error;

    documentation { Clears JMS properties of the message
        R{{}} nil or error if any JMS provider level internal error occur
    }
    public extern function clearProperties() returns error?;

    documentation {Clears body of the JMS message
        R{{}} nil or an JMS error
    }
    public extern function clearBody() returns error?;

    documentation { Sets priority JMS transport header to the message
        P{{value}} The header value
        R{{}} nil or an JMS error
    }
    public extern function setPriority(int value) returns error?;

    documentation {Gets JMS transport header Priority from the message
        R{{}} The JMS priority header value or error
    }
    public extern function getPriority() returns @tainted int|error;

    documentation { Gets JMS transport header Redelivered from the message
        R{{}} The JMS redelivered header value or JMS error
    }
    public extern function getRedelivered() returns @tainted boolean|error;

    documentation { Sets CorrelationID JMS transport header to the message
        P{{value}} The header value
        R{{}} nil or an JMS error
    }
    public extern function setCorrelationID(string value) returns error?;

    documentation { Gets JMS transport header CorrelationID from the message
        R{{}} The JMS correlation ID header value or JMS error or nil if header is not set
    }
    public extern function getCorrelationID() returns @tainted (string|error)?;
};
