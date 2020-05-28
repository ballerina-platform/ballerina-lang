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

// The mock JWK server, which is to expose the JWK components according to the
// https://tools.ietf.org/html/rfc7517#section-5
listener http:Listener oauth2Server = new(20199, {
        secureSocket: {
            keyStore: {
                path: config:getAsString("keystore"),
                password: "ballerina"
            }
        }
    });

service oauth2 on oauth2Server {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/jwks"
    }
    // This JWKs endpoint respond with a JSON object that represents a set of JWKs.
    // https://tools.ietf.org/html/rfc7517#section-5
    resource function jwks(http:Caller caller, http:Request req) {
        http:Response res = new;
        json jwks = {
          "keys": [
            {
              "kty": "RSA",
              "e": "AQAB",
              "use": "sig",
              "kid": "NTAxZmMxNDMyZDg3MTU1ZGM0MzEzODJhZWI4NDNlZDU1OGFkNjFiMQ",
              "alg": "RS256",
              "n": "luZFdW1ynitztkWLC6xKegbRWxky-5P0p4ShYEOkHs30QI2VCuR6Qo4Bz5rTgLBrky03W1GAVrZxuvKRGj9V9-PmjdGtau4CTXu9pLLcqnruaczoSdvBYA3lS9a7zgFU0-s6kMl2EhB-rk7gXluEep7lIOenzfl2f6IoTKa2fVgVd3YKiSGsyL4tztS70vmmX121qm0sTJdKWP4HxXyqK9neolXI9fYyHOYILVNZ69z_73OOVhkh_mvTmWZLM7GM6sApmyLX6OXUp8z0pkY-vT_9-zRxxQs7GurC4_C1nK3rI_0ySUgGEafO1atNjYmlFN-M3tZX6nEcA6g94IavyQ"
            }
          ]
        };
        res.setJsonPayload(jwks);
        checkpanic caller->respond(res);
    }
}
