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

public type Product record {
    string id?;
    string name;
    string description?;
};

client class ProductClient {

    private string url;

    public function init(string url) returns error? {
        self.url = url;
    }

    remote function findByName(string name) returns Product {
        return {
            id: "123",
            name: "Test"
        };
    }

    remote function listAll() returns Product[] {
        return [];
    }

    remote function countByName(string name) returns int {
        return 1;
    }

    remote function getVersion() returns string {
        return "v1.0.0";
    }
}

public function main() returns error? {
    ProductClient prodClient = check new("http://example,com");
    string v = prodClient->listAll();
}
