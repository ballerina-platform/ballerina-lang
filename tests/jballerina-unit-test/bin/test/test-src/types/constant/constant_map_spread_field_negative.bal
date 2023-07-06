// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

const map<string> CMS1 = {a : "C", b : "S"};
const map<string> CMS2 = {b : "C", c : "S"};

const map<string> CMS1_CLONE = {...CMS1};
const map<string> CMS12_CLONE = {...CMS1, ...CMS2};
const map<string> A = {b : "B", ...CMS1};
const map<string> B = {...CMS2, b : "B"};
const map<string> C = {...CMS2, ...CMS2};
