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
//

# Creates and returns an integer range object, constrained to a given range.
#
# + begin - start index of the range
# + end - end index of the range
# + excludeEndValue - flag to indicate whther to exclude the last value of the range
# + return - An integer range object
function createIntRange(any begin, any end, boolean excludeEndValue) returns any|error {
    any|error result;
    if (begin is int && end is int) {
        if (excludeEndValue) {
            result = trap begin ..< end;
        } else {
            result = trap begin ... end;
        }
    } else {
        if (excludeEndValue) {
            result = error("operator '..<' not defined for '" + check getType(begin) + "' and '" + check getType(end) + "'.");
        } else {
            result = error("operator '...' not defined for '" + check getType(begin) + "' and '" + check getType(end) + "'.");
        }
    }
    return result;
}
