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

import ballerina/http;
import ballerina/kafka;
import ballerina/lang.'array;
import ballerina/lang.'string;
import ballerina/lang.'int;
import ballerina/runtime;

public const TOPIC = "add-person";

public type Person record {|
    string name;
    int age;
|};

public type PersonSerializer object {
    *kafka:Serializer;

    public function serialize(any data) returns byte[] {
        Person person = <Person> data;
        byte[] result = person.age.toString().toBytes();
        byte[] nameBytes = person.name.toBytes();
        result.push(...nameBytes);

        return result;
    }

    public function close() {
        // Do nothing
    }
};

public type PersonDeserializer object {
    *kafka:Deserializer;

    public function deserialize(byte[] data) returns any {
        int age = checkpanic getIntFromString(checkpanic 'string:fromBytes('array:slice(data, 0, 2)));
        string name = checkpanic 'string:fromBytes('array:slice(data, 2));

        Person result = { name: name, age: age };
        return result;
    }

    public function close() {
        // Do nothing
    }
};

Person resultPerson = {
    name: "John Doe",
    age: 0
};

PersonSerializer personSerializer = new;
PersonDeserializer personDeserializer = new;

kafka:ProducerConfig producerConfigs = {
    bootstrapServers: "localhost:9092",
    clientId: "basic-producer",
    acks: kafka:ACKS_ALL,
    requestTimeoutInMillis: 1000,
    retryCount: 3,
    valueSerializerType: kafka:SER_CUSTOM,
    valueSerializer: personSerializer
};
kafka:Producer producer = new(producerConfigs);

kafka:ConsumerConfig consumerConfigs = {
    bootstrapServers: "localhost:9092",
    groupId: "test-group",
    clientId: "person-consumer",
    offsetReset: "earliest",
    autoCommit: true,
    topics: [TOPIC],
    valueDeserializerType: kafka:DES_CUSTOM,
    valueDeserializer: personDeserializer
};
listener kafka:Consumer consumer = new(consumerConfigs);

public function sendData(string name, int age) returns error? {
    Person person = {
        name: name,
        age: age
    };
    var result =  producer->send(person, TOPIC);
    return result;
}

service ListenerService on consumer {
    resource function onMessage(kafka:Consumer consumer, kafka:ConsumerRecord[] records) {
        foreach var kafkaRecord in records {
            var value = <@untainted> kafkaRecord.value;
            if (value is Person) {
                resultPerson = value;
            }
        }
    }
}

listener http:Listener httpListener = new(14001);

@http:ServiceConfig {
    basePath: "/"
}
service SendData on httpListener {
    resource function sendData(http:Caller caller, http:Request request) {
        http:Response response = new;
        var payload = request.getJsonPayload();
        int age = 0;
        string name = "";
        boolean failed = false;
        if (payload is json) {
            var nameValue = payload.name.toString();
            var ageValue = getIntFromString(payload.age.toString());
            if (ageValue is int) {
                age = ageValue;
                name = nameValue;
                var result = sendData(name, age);
                if (result is error) {
                    failed = true;
                    response.statusCode = 501;
                    response.setPayload("Sending data to Kafka server failed: " + result.toString());
                }
            } else {
                failed = true;
                response.statusCode = 400;
                response.setPayload("Invalid request: Data conversion failed.");
            }
        } else {
            response.statusCode = 400;
            failed = true;
            response.setPayload("Invalid request: Payload is not JSON.");
        }
        int i = 0;

        while (i < 10) {
            i += 1;
            runtime:sleep(1000);
            if (resultPerson.name == name && resultPerson.age == age) {
                response.setPayload("Successfully received");
                failed = false;
                break;
            }
        }
        if (failed && response.statusCode == 200) {
            response.statusCode = 501;
            response.setPayload("Not received the data");
        }
        var responseResult = caller->respond(response);
    }
}

function getIntFromString(string value) returns int|error {
    int result = check 'int:fromString(value);
    return result;
}
