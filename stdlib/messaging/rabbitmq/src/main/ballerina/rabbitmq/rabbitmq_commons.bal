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

# Types of exchanges supported by the Ballerina RabbitMQ Connector.
public type ExchangeType "direct"|"fanout"|"topic"|"headers";

# Constant for the RabbitMQ Direct Exchange type.
public const DIRECT_EXCHANGE = "direct";

# Constant for the RabbitMQ Fanout Exchange type.
public const FANOUT_EXCHANGE = "fanout";

# Constant for the RabbitMQ Topic Exchange type.
public const TOPIC_EXCHANGE = "topic";

# Types of acknowledgement modes supported by the Ballerina RabbitMQ Connector.
public type AcknowledgementMode AUTO_ACK | CLIENT_ACK;

# Constant for the RabbitMQ auto acknowledgement mode.
public const AUTO_ACK = "auto";

# Constant for the RabbitMQ client acknowledgement mode.
public const CLIENT_ACK = "client";

# RabbitMQ Error code.
public const string RABBITMQ_ERROR_CODE = "{ballerina/RabbitMQ}RabbitMQError";

# RabbitMQ Error record.
#
# + message - Error message.
type RabbitMQError record {|
    string message;
|};
