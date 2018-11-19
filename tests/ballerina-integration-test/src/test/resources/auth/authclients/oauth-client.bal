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

import ballerina/http;
import ballerina/io;
import ballerina/mime;

endpoint http:Client clientEP1 {
    url:"https://localhost:9095/foo",
    auth: {
        scheme: http:OAUTH2,
        refreshToken: "5Aep861..zRMyCurAUgnwQaEjnCVqxK2utna7Mm4nb9UamD7BW50R2huecjSaLlv5mT1z_TViZ",
        clientId: "3MVG9YDQS5WtC11paU2WcQjBB3L5w4gz52uriT8ksZ3nUVjKvrfQMrU4uvZohTftxStwNEW4cfStBEGRxRL68",
        clientSecret: "9205371918321623741",
        refreshUrl: "https://localhost:9095/foo/token",
        credentialBearer: http:POST_BODY_BEARER
    }
};

endpoint http:Client clientEP2 {
    url:"https://localhost:9095/foo",
    auth: {
        scheme: http:OAUTH2,
        refreshToken: "5Aep861..zRMyCurAUgnwQaEjnCVqxK2utna7Mm4nb9UamD7BW50R2huecjSaLlv5mT1z_TViZ",
        clientId: "3MVG9YDQS5WtC11paU2WcQjBB3L5w4gz52uriT8ksZ3nUVjKvrfQMrU4uvZohTftxStwNEW4cfStBEGRxRL68",
        clientSecret: "9205371918321623741",
        refreshUrl: "https://localhost:9095/foo/token",
        credentialBearer: http:AUTH_HEADER_BEARER
    }
};

endpoint http:Client clientEP3 {
    url:"https://localhost:9095/foo",
    auth: {
        scheme: http:OAUTH2,
        refreshToken: "5Aep861..zRMyCurAUgnwQaEjnCVqxK2utna7Mm4nb9UamD7BW50R2huecjSaLlv5mT1z_TViZ",
        clientId: "3MVG9YDQS5WtC11paU2WcQjBB3L5w4gz52uriT8ksZ3nUVjKvrfQMrU4uvZohTftxStwNEW4cfStBEGRxRL68",
        clientSecret: "9205371918321623741",
        refreshUrl: "https://localhost:9095/foo/token"
    }
};

endpoint http:Client clientEP4 {
    url:"https://localhost:9095/foo",
    auth: {
        scheme: http:OAUTH2,
        refreshToken: "5Aep861..zRMyCurAUgnwQaEjnCVqxK2utna7Mm4nb9UamD7BW50R2huecjSaLlv5mT1z_TViZ",
        clientId: "3MVG9YDQS5WtC11paU2WcQjBB3L5w4gz52uriT8ksZ3nUVjKvrfQMrU4uvZohTftxStwNEW4cfStBEGRxRL68",
        clientSecret: "9205371918321623741",
        refreshUrl: "https://localhost:9095/foo/token",
        scopes: ["token-scope1", "token-scope2"]
    }
};

endpoint http:Client clientEP5 {
    url:"https://localhost:9095/foo",
    auth: {
        scheme: http:OAUTH2,
        refreshToken: "5Aep861..zRMyCurAUgnwQaEjnCVqxK2utna7Mm4nb9UamD7BW50R2huecjSaLlv5mT1z_TViZ",
        clientId: "3MVG9YDQS5WtC11paU2WcQjBB3L5w4gz52uriT8ksZ3nUVjKvrfQMrU4uvZohTftxStwNEW4cfStBEGRxRL68",
        clientSecret: "9205371918321623741",
        refreshUrl: "https://localhost:9095/foo/token",
        credentialBearer: http:POST_BODY_BEARER,
        scopes: ["token-scope1", "token-scope2"]

    }
};

public function main (string arg) {
    if (arg == "POST_BODY_BEARER") {
        var resp = clientEP1->get("/bar");
    } else if (arg == "AUTH_HEADER_BEARER") {
        var resp = clientEP2->get("/bar");
    } else if (arg == "NO_CONFIG") {
        var resp = clientEP3->get("/bar");
    } else if (arg == "SCOPE") {
        var resp = clientEP4->get("/bar");
    } else if (arg == "SCOPE_POST_BODY_BEARER") {
        var resp = clientEP5->get("/bar");
    }
}
