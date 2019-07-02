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

import ballerina/oauth2;
import ballerina/http;

oauth2:IntrospectionServerConfig introspectionServerConfig = {
    url: "https://localhost:9196/oauth2/token/introspect",
    clientConfig: {
        auth: {
            scheme: http:BASIC_AUTH,
            config: {
                username: "3MVG9YDQS5WtC11paU2WcQjBB3L5w4gz52uriT8ksZ3nUVjKvrfQMrU4uvZohTftxStwNEW4cfStBEGRxRL68",
                password: "9205371918321623741"
            }
        }
    }
};

oauth2:OAuth2Provider oauth2Provider21 = new(introspectionServerConfig);
http:BearerAuthHeaderAuthnHandler oauth2AuthnHandler21 = new(oauth2Provider21);

listener http:Listener listener21 = new(9116, config = {
    auth: {
        authnHandlers: [oauth2AuthnHandler21]
    },
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
});

@http:ServiceConfig {
    basePath: "/echo"
}
service echo21 on listener21 {

    @http:ResourceConfig {
        methods: ["GET"]
    }
    resource function test(http:Caller caller, http:Request req) {
        checkpanic caller->respond(());
    }
}
