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

@typeParam
type PureType3 any | error;

@typeParam
type ErrorType2 error;

# Takes in a stream and returns the iterator object of that stream.
#
# + strm - The stream
# + return - An abstract object which is iterable
public function getIteratorObj(stream<PureType3, ErrorType2> strm) returns abstract object { public function next() returns
    record {|PureType3 value;|}|ErrorType2?;} | abstract object {
        public function next() returns record {|PureType3 value;|}|ErrorType2?;
        public function close() returns ErrorType2?;
        } = external;
