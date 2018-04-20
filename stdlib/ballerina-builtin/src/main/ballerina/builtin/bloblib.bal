// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


@Description {value:"Converts blob to a string"}
@Param {value:"b: The blob value to be converted"}
@Param {value:"encoding: Encoding to used in blob conversion to string"}
@Return {value:"String representation of the given blob"}
public native function<blob b> toString(string encoding) returns string;

@Description {value:"Encode a given blob with Base64 encoding scheme."}
@Param {value:"b: Content that needs to be encoded"}
@Return {value:"Return an encoded blob"}
public native function<blob b> base64Encode() returns blob;

@Description {value:"Decode a given blob with Base64 encoding scheme."}
@Param {value:"b: Content that needs to be decoded"}
@Return {value:"Return a decoded blob"}
public native function<blob b> base64Decode() returns blob;
