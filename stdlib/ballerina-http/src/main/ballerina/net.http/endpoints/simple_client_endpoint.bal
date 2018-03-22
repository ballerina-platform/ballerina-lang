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
    F{{target}} - The configurations for the endpoint to connect to. Users need to provide a URL and
                  (optionally) a SecureSocket struct.
}
public struct SimpleClientEndpoint {
    string epName;
    TargetService target;

    private:
        ClientEndpoint httpEP;
}

documentation {
    The initializer for the SimpleClientEndpoint. This initializes the enclosed ClientEndpoint struct
    to its default values.

    T{{ep}} - The SimpleClientEndpoint struct to be initialized
}
public function <SimpleClientEndpoint ep> SimpleClientEndpoint() {
    ep.httpEP = {};
    ep.httpEP.config = {};
}

documentation {
    The initialization function for the SimpleClientEndpoint.

    P{{target}} - The user provided configurations for the endpoint
    T{{ep}} - The endpoint to be initialized
}
public function <SimpleClientEndpoint ep> init(TargetService target) {
    string url = target.url;
    if (url.hasSuffix("/")) {
        int lastIndex = url.length() - 1;
        url = url.subString(0, lastIndex);
    }
    ep.httpEP.config.targets = [];
    ep.httpEP.config.targets[0] = target;
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
