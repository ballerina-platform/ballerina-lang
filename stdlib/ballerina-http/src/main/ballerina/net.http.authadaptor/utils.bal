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

package ballerina.net.http.authadaptor;

@Description {value:"Authentication header name"}
const string AUTH_HEADER = "Authorization";
@Description {value:"Basic authentication scheme"}
const string AUTH_SCHEME_BASIC = "Basic";
@Description {value:"Bearer authentication scheme"}
const string AUTH_SCHEME_BEARER = "Bearer";
@Description {value:"Auth annotation package"}
const string AUTH_ANN_PACKAGE = "ballerina.auth";
@Description {value:"Auth annotation name"}
const string AUTH_ANN_NAME = "Config";

@Description {value:"Extracts the basic authentication header value from the request"}
@Param {value:"req: Request instance"}
@Return {value:"string: value of the basic authentication header"}
@Return {value:"error: any error occurred while extracting the basic authentication header"}
public function extractBasicAuthHeaderValue (http:Request req) returns (string) {
    // extract authorization header
    string basicAuthHeader = req.getHeader(AUTH_HEADER);
    match basicAuthHeader {
        string basicAuthHeaderVal => {
            return basicAuthHeaderVal == null ? "" : basicAuthHeaderVal;
        }
    }
}

@Description {value:"Error handler"}
@Param {value:"message: error message"}
@Return {value:"error: error populated with the message"}
function handleError (string message) returns (error) {
    error e = {message:message};
    return e;
}
