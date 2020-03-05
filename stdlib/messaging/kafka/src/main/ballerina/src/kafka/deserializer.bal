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

# Represents a Kafka deserializer object. This object can be used to create custom deserializers for Ballerina Kafka
# consumers.
public type Deserializer abstract object {
    # Close the deserialization process. This function runs after the deserialization process is done.
    public function close();

    # Deserialize the provided data. Implement this to deserialize `byte[]` and return any data type.
    #
    # + data - Data which should be deserialized.
    # + return - Deserialized value.
    public function deserialize(byte[] data) returns any;
};
