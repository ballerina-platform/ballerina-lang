// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/io;
import ballerina/kafka;
import ballerina/lang.'array;
import ballerina/lang.'string;
import ballerina/lang.'int;

public const TOPIC = "add-person";

public type Person record {|
    string name;
    int age;
|};

public type PersonSerializer object {
    *kafka:Serializer;

    public function serialize(any data) returns byte[] {
        Person person = <Person>data;
        byte[] result = person.age.toString().toBytes();
        byte[] nameBytes = person.name.toBytes();
        result.push(...nameBytes);

        return result;
    }

    public function close() {
        io:println("closing");
    }
};

public type PersonDeserializer object {
    *kafka:Deserializer;

    public function deserialize(byte[] data) returns any {
        int age = checkpanic 'int:fromString(checkpanic 'string:fromBytes('array:slice(data, 0, 2)));
        string name = checkpanic 'string:fromBytes('array:slice(data, 2));

        Person result = {name: name, age: age};
        return result;
    }

    public function close() {
        io:println("closing");
    }
};

PersonSerializer personSerializer = new;
PersonDeserializer personDeserializer = new;

kafka:ProducerConfig producerConfigs = {
    bootstrapServers: "localhost:14113",
    clientId: "basic-producer",
    acks: kafka:ACKS_ALL,
    requestTimeoutInMillis: 1000,
    retryCount: 0,
    valueSerializerType: kafka:SER_CUSTOM,
    valueSerializer: personSerializer
};
kafka:Producer producer = new (producerConfigs);

kafka:ConsumerConfig consumerConfigs = {
    bootstrapServers: "localhost:14113",
    groupId: "test-group",
    clientId: "person-consumer",
    offsetReset: "earliest",
    autoCommit: true,
    topics: [TOPIC],
    valueDeserializerType: kafka:DES_CUSTOM,
    valueDeserializer: personDeserializer
};
kafka:Consumer consumer = new (consumerConfigs);

public function sendData() returns error? {
    Person person = {
        name: "Thisaru Guruge",
        age: 29
    };

    return producer->send(person, TOPIC);
}

public function retrieveData() returns any | error {
    var records = consumer->poll(1000);
    if (records is error) {
        return records;
    } else {
        if (records.length() == 1) {
            var result = records[0];
            var value = result.value;
            if (value is Person) {
                return value;
            } else {
                return error("{ballerina/kafka}Error", message = "Invalid type received");
            }
        } else {
            return error("{ballerina/kafka}Error", message = "More than one message received");
        }
    }
}
