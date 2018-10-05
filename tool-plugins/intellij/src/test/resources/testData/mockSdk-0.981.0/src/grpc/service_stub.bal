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
    gRPC Service Stub for outbound gRPC requests. gRPC client code not directly calls these functions.
    Generated client endpoint actions uses these functions to interact with gRPC service.
}
public type Stub object {
    public Client client;

    documentation {
        Calls when initializing client endpoint with service descriptor data extracted from proto file.

        P{{clientEndpoint}} - client endpoint struct.
        P{{stubType}} - Service Stub type. possible values: blocking, nonblocking.
        P{{descriptorKey}} - Proto descriptor key. Key of proto descriptor.
        P{{descriptorMap}} - Proto descriptor map. descriptor map with all dependent descriptors.
    }
    public extern function initStub(any clientEndpoint, string stubType, string descriptorKey, map descriptorMap);

    documentation {
        Calls when executing blocking call with gRPC service.

        P{{methodID}} - Remote service method id.
        P{{payload}} - Request message. Message type varies with remote service method parameter.
        P{{headers}} - Optional headers parameter. Passes header value if needed. Default sets to nil.
        R{{}} - Returns response message and headers if executes successfully, error otherwise.
    }
    public extern function blockingExecute(string methodID, any payload, Headers? headers = ())
        returns ((any, Headers)|error);

    documentation {
        Calls when executing non-blocking call with gRPC service.

        P{{methodID}} - Remote service method id.
        P{{payload}} - Request message. Message type varies with remote service method parameter..
        P{{listenerService}} - Call back listener service. This service listens the response message from service.
        P{{headers}} - Optional headers parameter. Passes header value if needed. Default sets to nil.
        R{{}} - Returns an error if encounters an error while sending the request, returns nil otherwise.
    }
    public extern function nonBlockingExecute(string methodID, any payload, typedesc listenerService, Headers?
    headers = ()) returns error?;


    documentation {
        Calls when executing streaming call with gRPC service.

        P{{methodID}} - Remote service method id.
        P{{listenerService}} - Call back listener service. This service listens the response message from service.
        P{{headers}} - Optional headers parameter. Passes header value if needed. Default sets to nil.
        R{{}} - Returns client connection if executes successfully, error otherwise.
    }
    public extern function streamingExecute(string methodID, typedesc listenerService, Headers? headers = ())
        returns Client|error;
};
