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

documentation {
    Encodes the given URL.

    P{{url}} URL to be encoded
    P{{charset}} Charactor set that URL to be encoded in
    R{{}} The `string` Value of the encoded url or an `error` that occured during encoding
}
public extern function encode(string url, string charset) returns string|error;

documentation {
    Decodes the given URL.

    P{{url}} URL to be decoded
    P{{charset}} Charactor set that URL to be decoded from
    R{{}} The `string` Value of the decoded url or an `error` that occured during decoding
}
public extern function decode(string url, string charset) returns string|error;