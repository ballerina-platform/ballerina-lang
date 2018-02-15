//
// Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
//

package ballerina.builtin;

@Description {value:"Converts int to a hex string"}
@Param {value:"decimalValue: Int(decimal) value to be converted"}
@Return {value:"The hex(string) of the decimal value"}
public native function <int decimalValue> toHexString () (string);

@Description {value:"Converts int to a octal string"}
@Param {value:"decimalValue: Int(decimal) value to be converted"}
@Return {value:"The octal(string) of the decimal value"}
public native function <int decimalValue> toOctalString () (string);

@Description {value:"Converts int to a binary string"}
@Param {value:"decimalValue: Int(decimal) value to be converted"}
@Return {value:"The binary(string) of the decimal value"}
public native function <int decimalValue> toBinaryString () (string);
