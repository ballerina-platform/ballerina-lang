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

documentation {
    Check whether 2 values are deeply equal. Supports string, int, float, boolean, type, structs, maps, arrays, any,
    JSON. Any other type returns FALSE.

    P{{value1}} The first value for equality.
    P{{value2}} The second value for equality.
    R{{}} TRUE if values are deeply equal, else FALSE.
}
public native function equals(any value1, any value2) returns (boolean);

//  Warning..!!!
//
//  ballerina.reflect package defines Internal APIs exposed by Ballerina Virtual Machine (BVM).
//  They are subject to change in a undocumented or unsupported way, use with caution.

public type anyStruct {};

public type annotationData {
    string name,
    string pkgName,
    anyStruct value,
};

public native function getServiceAnnotations(typedesc serviceType) returns (annotationData[]);

public native function getResourceAnnotations(typedesc serviceType, string resourceName) returns (annotationData[]);

public native function getStructAnnotations(typedesc structType) returns (annotationData[]);

public native function getStructFieldAnnotations(typedesc structType, string fieldName) returns (annotationData[]);

public native function getFunctionAnnotations(any functionPointer) returns (annotationData[]);
