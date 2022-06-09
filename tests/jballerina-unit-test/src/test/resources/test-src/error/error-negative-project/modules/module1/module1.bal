//  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
//  WSO2 Inc. licenses this file to you under the Apache License,
//  Version 2.0 (the "License"); you may not use this file except
//  in compliance with the License.
//  You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

public type E1 distinct error;

public type E2 distinct error;

type E3 distinct error;

type E4 distinct error;

type E5 E1 & E2;

type E6 E3 & E4;

type E7 E1 & E4;

type E8 E1|E2;

type E9 E3|E4;

type E10 E1|E4;

public type E5_Public E1 & E2;

public type E6_Public E3 & E4;

public type E7_Public E1 & E4;

public type E8_Public E1|E2;

public type E9_Public E3|E4;

public type E10_Public E1|E4;
