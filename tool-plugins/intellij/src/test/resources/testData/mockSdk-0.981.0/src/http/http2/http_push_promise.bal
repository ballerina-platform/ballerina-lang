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
    Represents an HTTP/2 `PUSH_PROMISE` frame.

    F{{path}} The resource path
    F{{method}} The HTTP method
}
public type PushPromise object {

    public string path;
    public string method;

    documentation {
        Constructs a `PushPromise` from a given path and a method.

        P{{path}} The resource path
        P{{method}} The HTTP method
    }
    public new (path = "/", method = "GET") {
    }

    documentation {
        Checks whether the requested header exists.

        P{{headerName}} The header name
        R{{}} A `boolean` representing the existence of a given header
    }
    public extern function hasHeader (string headerName) returns (boolean);

    documentation {
        Returns the header value with the specified header name.
        If there are more than one header value for the specified header name, the first value is returned.

        P{{headerName}} The header name
        R{{}} The header value, or null if there is no such header
    }
    public extern function getHeader (string headerName) returns (string);

    documentation {
        Gets transport headers from the `PushPromise`.

        P{{headerName}} The header name
        R{{}} The array of header values
    }
    public extern function getHeaders (string headerName) returns (string[]);

    documentation {
        Adds the specified key/value pair as an HTTP header to the `PushPromise`.

        P{{headerName}} The header name
        P{{headerValue}} The header value
    }
    public extern function addHeader (string headerName, string headerValue);

    documentation {
        Sets the value of a transport header in `PushPromise`.

        P{{headerName}} The header name
        P{{headerValue}} The header value
    }
    public extern function setHeader (string headerName, string headerValue);

    documentation {
        Removes a transport header from the `PushPromise`.

        P{{headerName}} The header name
    }
    public extern function removeHeader (string headerName);

    documentation {
        Removes all transport headers from the `PushPromise`.
    }
    public extern function removeAllHeaders ();

    documentation {
        Gets all transport header names from the `PushPromise`.

        R{{}} An array of all transport header names
    }
    public extern function getHeaderNames () returns (string[]);
};
