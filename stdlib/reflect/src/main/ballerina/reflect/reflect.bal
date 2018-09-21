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

# Check whether 2 values are deeply equal. Supports string, int, float, boolean, type, structs, maps, arrays, any,
# JSON. Any other type returns FALSE.
#
# + value1 - The first value for equality.
# + value2 - The second value for equality.
# + return - TRUE if values are deeply equal, else FALSE.
public extern function equals(any value1, any value2) returns (boolean);

public type anyStruct record {};

public type annotationData record {
    string name;
    string pkgName;
    string pkgVersion;
    anyStruct value;
};

public extern function getServiceAnnotations(typedesc serviceType) returns (annotationData[]);

public extern function getResourceAnnotations(typedesc serviceType, string resourceName) returns (annotationData[]);

public extern function getStructAnnotations(typedesc structType) returns (annotationData[]);

public extern function getStructFieldAnnotations(typedesc structType, string fieldName) returns (annotationData[]);

public extern function getFunctionAnnotations(any functionPointer) returns (annotationData[]);
