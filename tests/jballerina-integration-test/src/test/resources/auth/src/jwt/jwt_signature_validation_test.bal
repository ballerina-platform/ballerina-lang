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

import ballerina/config;
import ballerina/http;
import ballerina/jwt;

// Only `JwtTrustStoreConfig`
jwt:InboundJwtAuthProvider jwtAuthProvider22_1 = new({
    issuer: "ballerina",
    audience: "vEwzbcasJVQm1jVYHUHCjhxZ4tYa",
    trustStoreConfig: {
        certificateAlias: "ballerina",
        trustStore: {
            path: config:getAsString("truststore"),
            password: "ballerina"
        }
    }
});

http:BearerAuthHandler jwtAuthHandler22_1 = new(jwtAuthProvider22_1);

listener http:Listener listener22_1 = new(20114, {
    auth: {
        authHandlers: [jwtAuthHandler22_1]
    },
    secureSocket: {
        keyStore: {
            path: config:getAsString("keystore"),
            password: "ballerina"
        }
    }
});

@http:ServiceConfig {
    basePath: "/echo"
}
service echo22_1 on listener22_1 {

    resource function test(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }
}

// Only `JwksConfig`
jwt:InboundJwtAuthProvider jwtAuthProvider22_2 = new({
    issuer: "ballerina",
    audience: "vEwzbcasJVQm1jVYHUHCjhxZ4tYa",
    jwksConfig: {
        url: "https://localhost:20199/oauth2/jwks",
        clientConfig: {
            secureSocket: {
                trustStore: {
                    path: config:getAsString("truststore"),
                    password: "ballerina"
                }
            }
        }
    }
});

http:BearerAuthHandler jwtAuthHandler22_2 = new(jwtAuthProvider22_2);

listener http:Listener listener22_2 = new(20115, {
    auth: {
        authHandlers: [jwtAuthHandler22_2]
    },
    secureSocket: {
        keyStore: {
            path: config:getAsString("keystore"),
            password: "ballerina"
        }
    }
});

@http:ServiceConfig {
    basePath: "/echo"
}
service echo22_2 on listener22_2 {

    resource function test(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }
}

// Both `JwtTrustStoreConfig` and `JwksConfig`
jwt:InboundJwtAuthProvider jwtAuthProvider22_3 = new({
    issuer: "ballerina",
    audience: "vEwzbcasJVQm1jVYHUHCjhxZ4tYa",
    jwksConfig: {
        url: "https://localhost:20199/oauth2/jwks",
        clientConfig: {
            secureSocket: {
                trustStore: {
                    path: config:getAsString("truststore"),
                    password: "ballerina"
                }
            }
        }
    },
    trustStoreConfig: {
        certificateAlias: "ballerina",
        trustStore: {
            path: config:getAsString("truststore"),
            password: "ballerina"
        }
    }
});

http:BearerAuthHandler jwtAuthHandler22_3 = new(jwtAuthProvider22_3);

listener http:Listener listener22_3 = new(20116, {
    auth: {
        authHandlers: [jwtAuthHandler22_3]
    },
    secureSocket: {
        keyStore: {
            path: config:getAsString("keystore"),
            password: "ballerina"
        }
    }
});

@http:ServiceConfig {
    basePath: "/echo"
}
service echo22_3 on listener22_3 {

    resource function test(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }
}

// No `JwtTrustStoreConfig` or `JwksConfig`
jwt:InboundJwtAuthProvider jwtAuthProvider22_4 = new({
    issuer: "ballerina",
    audience: "vEwzbcasJVQm1jVYHUHCjhxZ4tYa"
});

http:BearerAuthHandler jwtAuthHandler22_4 = new(jwtAuthProvider22_4);

listener http:Listener listener22_4 = new(20117, {
    auth: {
        authHandlers: [jwtAuthHandler22_4]
    },
    secureSocket: {
        keyStore: {
            path: config:getAsString("keystore"),
            password: "ballerina"
        }
    }
});

@http:ServiceConfig {
    basePath: "/echo"
}
service echo22_4 on listener22_4 {

    resource function test(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }
}
