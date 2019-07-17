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

const COLON = ":";

public function getServiceAnnotations(service serviceType, string annotName, string? moduleName = ()) returns any {
    return getServiceAnnotationsExternal(serviceType, getAnnotQualifiedIdentifier(annotName, moduleName));
}

public function getResourceAnnotations(service serviceType, string resourceName, string annotName,
                                        string? moduleName = ()) returns any {
    return getResourceAnnotationsExternal(serviceType, resourceName,
                                          getAnnotQualifiedIdentifier(annotName, moduleName));
}

function getServiceAnnotationsExternal(service serviceType, string annot) returns any = external;

function getResourceAnnotationsExternal(service serviceType, string resourceName, string annot)
    returns any = external;

function getAnnotQualifiedIdentifier(string annotName, string? moduleName = ()) returns string {
    if (moduleName is string) {
        return moduleName + COLON + annotName;
    }
    return annotName;
}
