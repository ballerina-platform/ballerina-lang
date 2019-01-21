// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public function encodeBase32(byte[] input) returns string;

public function decodeBase32(string input) returns byte[];

public function encodeBase64(byte[] input) returns string;

public function decodeBase64(string input) returns byte[];

public function encodeUtf8(byte[] input) returns string;

public function decodeUtf8(string input) returns byte[];

public function encodeHex(byte[] input) returns string;

public function decodeHex(string input) returns byte[];
