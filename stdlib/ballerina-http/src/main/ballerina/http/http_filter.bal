// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package ballerina.http;

@Description {value:"Representation of a HTTP Request Filter. This filter will be applied before the request is
dispatched to the relevant resource. Any Filter implementation should be struct-wise similar to the Filter struct."}
@Field {value:"filterRequest: Request filter function"}
@Field {value:"filterResponse: Response filter function"}
public type Filter object {
    @Description {value:"filterRequest: Request filter function"}
    public function filterRequest (Request request, FilterContext context) returns FilterResult;
    @Description {value:"Initializes the filter"}
    public function init ();
};

@Description {value:"Representation of filter Context."}
@Field {value:"serviceType: Type of the service"}
@Field {value:"serviceName: Name of the service"}
@Field {value:"filterResponse: Name of the resource"}
public type FilterContext object {
    public {
        typedesc serviceType;
        string serviceName;
        string resourceName;
    }
    new (serviceType, serviceName, resourceName){}
};

@Description {value:"Represents a filter result. This should be populated and returned by each request and response
filter function"}
@Field {value:"canProceed: Flag to check if the execution of the request should proceed or stopped"}
@Field {value:"statusCode: Status code which will be returned to the request sender if the canProceed is set to false"}
@Field {value:"message: Message which will be returned to the request sender if the canProceed is set to false"}
public type FilterResult {
    boolean canProceed,
    int statusCode,
    string message,
};
