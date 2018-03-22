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

package ballerina.net.http;

documentation {
    SimpleClientEndpoint provides an HTTP endpoint with minimal configurations.

    F{{epName}} - Name of the endpoint
    F{{simpleConfig}} - The configurations for the endpoint to connect to. This contains all the configurations as the
                        ClientEndpointConfiguration, except for the resiliency related configurations.
}
public struct SimpleClientEndpoint {
    string epName;
    SimpleClientEndpointConfiguration simpleConfig;

    private:
        ClientEndpoint httpEP;
}

documentation {
    The configurations possible with the SimpleClientEndpoint. This endpoint excludes the resiliency related configurations.

    F{{url}} - The URL of the HTTP endpoint to connect to
    F{{secureSocket}} - The SSL configurations for the endpoint
    F{{endpointTimeout}} - The maximum time to wait (in milli seconds) for a response before closing the connection
    F{{httpVersion}} - The HTTP version to be used to communicate with the endpoint
    F{{forwarded}} - The choice of setting forwarded/x-forwarded header
    F{{keepAlive}} - Specifies whether to keep the connection alive (or not) for multiple request/response pairs
    F{{transferEncoding}} - The types of encoding applied to the request
    F{{chunking}} - The chunking behaviour of the request
    F{{followRedirects}} - Redirect related options
    F{{retry}} - Retry related options
    F{{proxy}} - Proxy related options
    F{{connectionThrottling}} - The configurations for controlling the number of connections allowed concurrently
}
public struct SimpleClientEndpointConfiguration {
    string url;
    SecureSocket|null secureSocket;
    int endpointTimeout;
    string httpVersion;
    string forwarded;
    boolean keepAlive;
    TransferEncoding transferEncoding;
    Chunking chunking;
    FollowRedirects|null followRedirects;
    Retry|null retry;
    Proxy|null proxy;
    ConnectionThrottling|null connectionThrottling;
}

documentation {
    The initializer for the SimpleClientEndpointConfiguration for initializing the configs to their default values.

    T{{config}} - The SimpleClientEndpoint struct to be initialized
}
public function <SimpleClientEndpointConfiguration config> SimpleClientEndpointConfiguration() {
    config.chunking = Chunking.AUTO;
    config.transferEncoding = TransferEncoding.CHUNKING;
    config.httpVersion = "1.1";
    config.forwarded = "disable";
    config.endpointTimeout = 60000;
    config.keepAlive = true;
}

documentation {
    The initialization function for the SimpleClientEndpoint.

    P{{simpleConfig}} - The user provided configurations for the endpoint
    T{{ep}} - The endpoint to be initialized
}
public function <SimpleClientEndpoint ep> init(SimpleClientEndpointConfiguration simpleConfig) {
    string url = simpleConfig.url;
    if (url.hasSuffix("/")) {
        int lastIndex = url.length() - 1;
        url = url.subString(0, lastIndex);
    }
    ep.httpEP = {};
    ep.httpEP.config = {};
    ep.httpEP.config.targets = [];

    ep.httpEP.config.targets[0] = {uri: simpleConfig.url, secureSocket: simpleConfig.secureSocket};
    ep.httpEP.config.endpointTimeout = simpleConfig.endpointTimeout;
    ep.httpEP.config.httpVersion = simpleConfig.httpVersion;
    ep.httpEP.config.forwarded = simpleConfig.forwarded;
    ep.httpEP.config.keepAlive = simpleConfig.keepAlive;
    ep.httpEP.config.transferEncoding = simpleConfig.transferEncoding;
    ep.httpEP.config.chunking = simpleConfig.chunking;
    ep.httpEP.config.followRedirects = simpleConfig.followRedirects;
    ep.httpEP.config.retry = simpleConfig.retry;
    ep.httpEP.config.proxy = simpleConfig.proxy;
    ep.httpEP.config.connectionThrottling = simpleConfig.connectionThrottling;

    ep.httpEP.httpClient = createHttpClient(url, ep.httpEP.config);
}

documentation {
    The register() function is not implemented for the SimpleClientEndpoint.
}
public function <SimpleClientEndpoint ep> register(typedesc serviceType) {

}

documentation {
    The start() function is not implemented for the SimpleClientEndpoint.
}
public function <SimpleClientEndpoint ep> start() {

}

documentation {
    Returns the backing HTTP client used by the endpoint.

    T{{ep}} - The endpoint of which the backing HTTP client needs to be retrieved
}
public function <SimpleClientEndpoint ep> getClient() returns HttpClient {
    return ep.httpEP.httpClient;
}

documentation {
    The stop() function is not implemented for the SimpleClientEndpoint.
}
public function <SimpleClientEndpoint ep> stop() {

}
