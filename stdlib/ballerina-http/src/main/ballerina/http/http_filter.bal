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
@Field {value:"filterRequest: Request filter function pointer"}
@Field {value:"filterResponse: Response filter function pointer"}
public type Filter object {
    public {
        function (Request request, FilterContext context) returns (FilterResult) filterRequest;
        function (Response response, FilterContext context) returns (FilterResult) filterResponse;
    }

    public new (filterRequest, filterResponse) {
    }

    public function init ();
    public function terminate ();

};

@Description {value:"Representation of filter Context."}
@Field {value:"serviceType: Type of the service"}
@Field {value:"serviceName: Name of the service"}
@Field {value:"filterResponse: Name of the resource"}
public type FilterContext object {
    // TODO should have a map of properties
    public {
        typedesc serviceType;
        string serviceName;
        string resourceName;
    }
    new (serviceType){}
};

@Description {value:"Represents a filter result. This should be populated and returned by each request and response
filter function"}
@Field {value:"canProceed: Flag to check if the execution of the request should proceed or stopped"}
@Field {value:"statusCode: Status code which will be returned to the request sender if the canProceed is set to false"}
@Field {value:"message: Message which will be returned to the request sender if the canProceed is set to false"}
public type FilterResult {
    boolean canProceed;
    int statusCode;
    string message;
};

@Description {value:"Initializes the filter"}
public function Filter::init () {
    error e = {message:"Not implemented"};
    throw e;
}

@Description {value:"Stops the filter"}
public function Filter::terminate () {
    error e = {message:"Not implemented"};
    throw e;
}
