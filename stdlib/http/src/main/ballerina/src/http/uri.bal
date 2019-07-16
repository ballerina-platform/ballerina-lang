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

# Encodes the given URL.
#
# + url - URL to be encoded
# + charset - Charactor set that URL to be encoded in
# + return - The `string` Value of the encoded url or an `ClientError` that occurred during encoding
public function encode(string url, string charset) returns string|ClientError = external;

# Decodes the given URL.
#
# + url - URL to be decoded
# + charset - Character set from which the URL is decoded
# + return - The `string` Value of the decoded url or an `ClientError` that occurred during decoding
public function decode(string url, string charset) returns string|ClientError = external;
