// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import immutable_def_test/defns;
import samjs/package_comp_plugin_code_modify_add_function as _;

public type Config record {|
    int maxQueryDepth?;
    readonly string schemaString = "";
    readonly ConfigInterceptor[] interceptors = [];
|};

public function main() {
    defns:Record & readonly x = {arr: [1, 2]};

    defns:ServerError y = defns:fn();
    json _ = y.detail().toJson();

    defns:Tuple & readonly z = 1;
    defns:Tuple & readonly _ = ["foo", z];

    error a = error("oops!");
    string _ = a.detail().toString();

    defns:RecordTwo & readonly b = {
        ob: object {
            int i = 1;
        }
    };
    map<defns:RecordTwo> & readonly c = {b};
    defns:TupleTwo & readonly _ = c;

    defns:Foo & readonly d;
}
