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

# Tests whether passed values are equal .
#
# + expected - expected value
# + actual - actual value
public function assertValueEqual(anydata expected, anydata actual) = external;

# Tests whether passed value has error type.
#
# + value - the passed value to be asserted
public function assertError(anydata|error value) = external;

# Tests whether passed value has not error type.
#
# + value - the passed value to be asserted
public function assertNotError(anydata|error value) = external;

# Tests whether passed value is True.
#
# + value - the passed value to be asserted
public function assertTrue(boolean value) = external;

# Tests whether passed value is False.
#
# + value - the passed value to be asserted
public function assertFalse(boolean value) = external;
