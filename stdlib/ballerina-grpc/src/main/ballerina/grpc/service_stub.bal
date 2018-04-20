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
package ballerina.grpc;

documentation {
    gRPC Service Stub for outbound gRPC requests
}
public type Stub object {
    public {
        Client client;
    }

    documentation {
        Init native function for initialize the Stub.

        P{{clientEndpoint}} - client endpoint struct.
        P{{stubType}} - Service Stub type. possible values: blocking, nonblocking.
        P{{descriptorKey}} - Proto descriptor key. Key of proto descriptor.
        P{{descriptorMap}} - Proto descriptor map. descriptor map with all dependent descriptors.
    }
    public native function initStub(any clientEndpoint, string stubType, string descriptorKey, map descriptorMap);

    documentation {
        Blocking execute function implementation of the gRPC client stub.

        P{{methodID}} - remote procedure call id.
        P{{payload}} - Any type of request payload.
    }
    public native function blockingExecute(string methodID, any payload, Headers... headers) returns ((any, Headers)|
            error);

    documentation {
        Non Blocking execute function implementation of the gRPC client stub.

        P{{methodID}} - remote procedure call id.
        P{{payload}} - Any type of request payload.
        P{{listenerService}} - call back listener service.
    }
    public native function nonBlockingExecute(string methodID, any payload, typedesc listenerService, Headers... headers
    ) returns error?;


    documentation {
        Blocking execute function implementation of the gRPC client stub.

        P{{methodID}} - remote procedure call id.
        P{{listenerService}} - call back listener service.
    }
    public native function streamingExecute(string methodID, typedesc listenerService, Headers... headers) returns (
            Client|error);

};
