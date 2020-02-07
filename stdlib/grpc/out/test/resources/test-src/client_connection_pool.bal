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

import ballerina/grpc;

grpc:PoolConfiguration sharedPoolConfig = {};

function testGlobalPoolConfig() returns grpc:Client?[] {
    grpc:Client client1 = new("http://localhost:8080");
    grpc:Client client2 = new("http://localhost:8080");
    grpc:Client client3 = new("http://localhost:8081");
    grpc:Client?[] clients = [client1, client2, client3];
    return clients;
}

function testSharedPoolConfig() returns grpc:Client?[] {
    grpc:Client client1 = new("http://localhost:8080", { poolConfig: sharedPoolConfig });
    grpc:Client client2 = new("http://localhost:8080", { poolConfig: sharedPoolConfig });
    grpc:Client?[] clients = [client1, client2];
    return clients;
}

function testPoolPerClient() returns grpc:Client?[] {
    grpc:Client client1 = new("http://localhost:8080", { poolConfig: { maxActiveConnections: 50 } });
    grpc:Client client2 = new("http://localhost:8080", { poolConfig: { maxActiveConnections: 25 } });
    grpc:Client?[] clients = [client1, client2];
    return clients;
}
