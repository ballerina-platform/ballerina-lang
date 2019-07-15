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

public type ConstructorData record {|
    string class;
    string[] paramTypes?;
|};

public type MethodData record {|
    string name?;
    string class;
    boolean isStatic = false;
    string[] paramTypes?;
|};

public type FieldData record {|
    string name;
    string class;
    boolean isStatic = false;
|};

#
public annotation ConstructorData Constructor on function;
public annotation MethodData Method on function;
public annotation FieldData FieldGet on function;
public annotation FieldData FieldSet on function;

