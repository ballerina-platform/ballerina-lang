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
package grpc;

public type MessageContext object {

    documentation {
        Check whether the requested header exists

        P{{headerName}} - The header name.
    }
    public native function hasHeader (string headerName) returns (boolean);

    documentation {
        Returns the header value with the specified header name. If there are more than one header value for the specified header name, the first value is returned.

        P{{headerName}} - headerName: The header name.
    }
    public native function getHeader (string headerName) returns (string);

    documentation {
        Sets the value of a transport header.

        P{{headerName}} - The header name.
        P{{headerValue}} - The header value.
    }
    public native function setHeader (string headerName, string headerValue);

    documentation {
        Removes a transport header from the request.

        P{{headerName}} - The header name.
    }
    public native function removeHeader (string headerName);
};


