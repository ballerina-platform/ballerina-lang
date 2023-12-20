// Copyright (c) 2023 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

# + url - Target service url
public client class Client {
    public string url;

    public function init(string url) {
        self.url = url;
    }
    
    # Sample resource method.
    #
    # + id1 - Path parameter
    # + ids - Rest path parameter
    # + str - Argument
    # + ids2 - Rest argument
    # + return - The response for the request
    resource function post path1/[string id1]/path2/[string... ids](string str, string... ids2) returns Response {
        return new Response();
    }
}

public class Response {
    public int statusCode = 200;
    public string reasonPhrase = "Test Reason phrase";
}
