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

public type Span object {

    private {
        string spanId,
        boolean isFinished,
    }

    // todo Timer within startSpan and finish natives

    public native function addTag(string tagKey, string tagValue) returns error?;

    public native function finish();

};

public native function startSpan(string serviceName, string spanName, map? tags = ()) returns Span {}

// Native implementation to avoid reading configuration file
//public native function isTraceEnabled() returns boolean {}

//public native function isMetricsEnabled() returns boolean {}