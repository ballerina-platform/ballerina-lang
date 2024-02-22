// Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
//
// WSO2 LLC. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied. See the License for the
// specific language governing permissions and limitations
// under the License.

// Simple types
type UserNil null;
type UserBoolean boolean;
type UserInt int;
type UserFloat float;
type UserDecimal decimal;

// Sequence types
type UserString string;
type UserXml_1 xml;
type UserXml_2 xml<xml:Element>;

// Other types
type UserAny any;
type UserAnydata anydata;
type UserNever never;
type UserReadonly readonly;
type UserJson json;
type UserByte byte;
type UserUnion_1 int|float|null;
type UserUnion_2 stream<int>|function(int x) returns int;
type UserIntersection_1 int & readonly;
type UserIntersection_2 UserRecord & readonly;

// Builtin object types
type UserIterable object:Iterable;
type UserRawTemplate object:RawTemplate;

// Structured types
type UserArr_1 int[];
type UserArr_2 stream<int>[];
type UserTuple [int, string,  xml<xml:Element>];
type UserMap_1 map<int>;
type UserMap_2 map<stream<int>[]>;
type UserTable_1 table<UserRecord> key(id);
type UserTable_2 table<UserRecord> key<int>;
type UserRecord record {|
    readonly int id;
    string firstName;
    string lastName;
    int salary;
|};

// Behavioral types
type UserError error;
type UserFunction function(int x) returns int;
type UserClient client object {
    int userInt;
};
type UserService service object {
    int userInt;
};
type UserFuture future<int>;
type UserTypedesc_1 typedesc<int>;
type UserTypedesc_2 typedesc<UserRecord>;
type UserStream stream<int>;
