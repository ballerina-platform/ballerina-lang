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

import ballerinax/java;

const COLON = ":";

# Function to retrieve the service's annotation.
#
# + serviceType - The service that the annotation belongs to
# + annotName - Name of the annotation
# + moduleName - Name of the module
# + return - Returns the service annotation data
public function getServiceAnnotations(service serviceType, string annotName, string? moduleName = ()) returns any {
    var result = externGetServiceAnnotations(serviceType,
                                            java:fromString(getAnnotQualifiedIdentifier(annotName, moduleName)));
    if (result is handle) {
        return java:toString(result);
    } else {
        return result;
    }
}

# Function to retrieve the resource's annotation.
#
# + serviceType - The service that the resource belongs to
# + resourceName - Name of the resource
# + annotName - Name of the annotation
# + moduleName - Name of the module
# + return - Returns the resource annotation data
public function getResourceAnnotations(service serviceType, string resourceName, string annotName,
                                        string? moduleName = ()) returns any {
    var result = externGetResourceAnnotations(serviceType, java:fromString(resourceName),
                                          java:fromString(getAnnotQualifiedIdentifier(annotName, moduleName)));
    if (result is handle) {
      return java:toString(result);
    } else {
      return result;
    }
}

function externGetServiceAnnotations(service serviceType, handle annot) returns any =
@java:Method {
    class: "org.ballerinalang.stdlib.reflect.AnnotationUtils"
} external;

function externGetResourceAnnotations(service serviceType, handle resourceName, handle annot) returns any =
@java:Method {
    class: "org.ballerinalang.stdlib.reflect.AnnotationUtils"
} external;

function getAnnotQualifiedIdentifier(string annotName, string? moduleName = ()) returns string {
    if (moduleName is string) {
        return moduleName + COLON + annotName;
    }
    return annotName;
}
