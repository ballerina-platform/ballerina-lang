// Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

type ClientAuthHandler ClientBasicAuthHandler|ClientOAuth2Handler;

isolated function enrichRequest(ClientAuthHandler clientAuthHandler, Request req) returns Request|error {
    if clientAuthHandler is ClientBasicAuthHandler {
        return clientAuthHandler.enrich(req);
    } else if clientAuthHandler is ClientOAuth2Handler {
        return clientAuthHandler->enrich(req);
    } else {
        panic error("invalid client auth-handler found");
    }
}

public class Request {
    int id = 0;
}

public type CredentialsConfig record {|
    string username;
    string password;
|};

public isolated class ClientBasicAuthProvider {

    private final CredentialsConfig & readonly credentialsConfig;

    public isolated function init(CredentialsConfig credentialsConfig) {
        self.credentialsConfig = credentialsConfig.cloneReadOnly();
    }

    public isolated function generateToken() returns string|error {
        return "";
    }
}

public readonly isolated class ClientBasicAuthHandler {

    private final ClientBasicAuthProvider provider;

    public isolated function enrich(Request req) returns Request|error {
        return req;
    }
}

public type GrantConfig record {|int a;|}|record {|string b;|};

isolated class TokenCache {

    private string accessToken;
    private string refreshToken;
    private int expTime;
}

public isolated class ClientOAuth2Provider {

    private final GrantConfig & readonly grantConfig;
    private final TokenCache tokenCache;

    public isolated function generateToken() returns string|error {
        return "";
    }
}

public readonly isolated client class ClientOAuth2Handler {

    private final ClientOAuth2Provider provider;

    remote isolated function enrich(Request req) returns Request|error {
        return req;
    }
}
