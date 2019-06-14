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

public type anyStruct record {|
    any...;
|};

public type annotationData record {
    string name;
    string moduleName;
    string moduleVersion;
    record {| any...; |} value;
};

public function getServiceAnnotations(service serviceType) returns annotationData[] = external;

public function getResourceAnnotations(service serviceType, string resourceName) returns annotationData[] = external;

public function getFunctionAnnotations(any functionPointer) returns annotationData[] = external;

const COLON = ":";

public function getServiceAnnots(service serviceType, string? moduleName = (), string annotName) returns any {
    return getServiceAnnotationsExternal(serviceType, getAnnotQualifiedIdentifier(moduleName = moduleName, annotName));
}

public function getResourceAnnots(service serviceType, string resourceName, string? moduleName = (),
                                       string annotName) returns any {
    return getResourceAnnotationsExternal(serviceType, resourceName,
                                          getAnnotQualifiedIdentifier(moduleName = moduleName, annotName));
}

public function getFunctionAnnots(any functionPointer, string? moduleName = (), string annotName) returns any {
    return getFunctionAnnotationsExternal(functionPointer,
                                          getAnnotQualifiedIdentifier(moduleName = moduleName, annotName));
}

function getServiceAnnotationsExternal(service serviceType, string annot) returns any = external;

function getResourceAnnotationsExternal(service serviceType, string resourceName, string annot)
    returns any = external;

function getFunctionAnnotationsExternal(any functionPointer, string annot) returns any = external;

function getAnnotQualifiedIdentifier(string? moduleName = (), string annotName) returns string {
    if (moduleName is string) {
        return moduleName + COLON + annotName;
    }
    return annotName;
}
