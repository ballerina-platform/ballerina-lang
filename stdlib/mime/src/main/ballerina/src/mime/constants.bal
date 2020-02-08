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

# Defines the position of the headers in the request/response.
#
# `LEADING`: Header is placed before the payload of the request/response
# `TRAILING`: Header is placed after the payload of the request/response
public type HeaderPosition LEADING|TRAILING;

# Header is placed before the payload of the request/response.
public const LEADING = "leading";

# Header is placed after the payload of the request/response.
public const TRAILING = "trailing";
