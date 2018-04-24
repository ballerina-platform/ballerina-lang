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

documentation {
    PushPromise represents a HTTP/2 PUSH_PROMISE frame.

    F{{path}} the resource path
    F{{method}} the http method
}
public type PushPromise object {
    public {
        string path;
        string method;
    }

    public new (path = "/", method = "GET") {
    }

    documentation {
        Checks whether the requested header exists.

        P{{headerName}} - The header name.
        R{{}} - A boolean representing the existence of a given header.
    }
    public native function hasHeader (string headerName) returns (boolean);


    documentation {
        Returns the header value with the specified header name.
        If there are more than one header value for the specified header name, the first value is returned.

        P{{headerName}} - The header name.
        R{{}} - The first header value for the provided header name. Returns null if the header does not exist.
    }
    public native function getHeader (string headerName) returns (string);

    documentation {
        Gets transport headers from the Push Promise.

        P{{headerName}} - The header name.
        R{{}} - The header values struct array for a given header name.
    }
    public native function getHeaders (string headerName) returns (string[]);

    documentation {
        Adds the specified key/value pair as an HTTP header to the Push Promise.

        P{{headerName}} - The header name.
        P{{headerValue}} - The header value.
    }
    public native function addHeader (string headerName, string headerValue);

    documentation {
        Sets the value of a transport header in Push Promise.

        P{{headerName}} - The header name.
        P{{headerValue}} - The header value.
    }
    public native function setHeader (string headerName, string headerValue);

    documentation {
        Removes a transport header from the Push Promise.

        P{{headerName}} - The header name.
    }
    public native function removeHeader (string headerName);

    documentation {
        Removes all transport headers from the Push Promise.
    }
    public native function removeAllHeaders ();

    documentation {
        Gets all transport header names from the Push Promise.

        R{{}} - An array of all transport header names.
    }
    public native function getHeaderNames () returns (string[]);
};

