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

public class ConfigField {
    private final service object {}? serviceObject;
    private final any fieldValue;
}

isolated function getConfigInterceptors(Config? config) returns readonly & (readonly & ConfigInterceptor)[] {
    if config is Config {
        return config.interceptors; // `readonly`ness should be propagated from the field and make this valid
    }
    return [];
}

public type ConfigErrorDetail record {|
    string message;
    map<anydata> extensions?;
|};

isolated class ConfigEngine {
    private final int? maxQueryDepth;
    private final readonly & (readonly & ConfigInterceptor)[] interceptors;
    private final readonly & boolean introspectionEnabled;
}

public isolated class ConfigContext {
    private final map<anydata> attributes;
    private final ConfigErrorDetail[] errors;
    private ConfigEngine? engine;
    private int nextInterceptor;

    public isolated function init() {
        self.attributes = {};
        self.engine = ();
        self.errors = [];
        self.nextInterceptor = 0;
    }
}
