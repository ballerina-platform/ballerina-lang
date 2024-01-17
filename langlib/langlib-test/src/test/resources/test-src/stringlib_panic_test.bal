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

function testInvalidLengthForStringPadding(int i) returns error? {
    match i {
        1 => {
            string _ = check (trap "aaa".padStart(int:MAX_VALUE));
        }

        2 => {
            string _ = check (trap "aaa".padStart(2147483648, "$"));
        }

        3 => {
            string _ = check (trap "aaa".padEnd(int:MAX_VALUE));
        }

        4 => {
            string _ = check (trap "aaa".padEnd(2147483648, "."));
        }

        5 => {
            string _ = check (trap "aaa".padZero(int:MAX_VALUE));
        }

        6 => {
            string _ = check (trap "aaa".padZero(2147483648, " "));
        }

        _ => {
            return error("Wrong arg");
        }
    }
}
