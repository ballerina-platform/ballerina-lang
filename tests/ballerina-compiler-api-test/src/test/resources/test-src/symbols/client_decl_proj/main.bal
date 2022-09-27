// Copyright (c) 2022 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

@ClientAnnot {
    i: 1
}
client "https://postman-echo.com/get?name=projectapiclientplugin" as myapi;

public function main() {
    myapi:client x;
}

function testClientDeclStmt() {
    @ClientAnnot {
        i: 2
    }
    client "https://postman-echo.com/get?name=simpleclienttest" as bar;
    bar:ClientConfiguration config = {'limit: 5};
    bar:client cl = new (config);
    int 'limit = cl->getLimit();
    cl1:Config config2 = {url: "http://www.example.com"};
}

public const annotation record { int i; }[] ClientAnnot on source client;
