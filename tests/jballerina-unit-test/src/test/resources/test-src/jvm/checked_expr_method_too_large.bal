// Copyright (c) 2022, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

service / on new Listener() {
    resource function get TestRequest (Caller caller) returns error? {
        do {
            {
                var x = check foo(0);
                if x is false {
                    check caller->respond(error("internal error"));
                } else if x is int {
                    check caller->respond(x);
                } else if x is Response {
                    check caller->respond(error("error in response"));
                }
            }

            {
                var x = check foo(5);
                if x is false {
                    check caller->respond(error("internal error"));
                } else if x is int {
                    check caller->respond(x);
                } else if x is Response {
                    check caller->respond(error("error in response"));
                }
            }

            {
                var x = check foo(200);
                if x is Response {
                    check caller->respond(error("error in response"));
                } else if x is int {
                    check caller->respond(x);
                } else if x is false {
                    check caller->respond(error("internal error"));
                }
            }

        } on fail {
            var v = check foo(0);
            if v is false|error {
                check caller->respond();
                return;
            }
            else if v is Response {
                check caller->respond(error("error"));
                return;
            }

            check caller->respond(101);
        }
    }
}

function foo(int i) returns Response|false|int|error|() {
    if (i == 0) {
        return false;
    } else if (i == 5) {
        return 5;
    } else if (i == 101) {
        return error("error!");
    } else if (i == 200) {
        return new Response(200, "OK");
    }
    return;
}

public client class Caller {
    remote function respond(int|string|error? message = ()) returns error? {
        if (message is int|string) {
            return;
        } else if (message is error) {
            return error("error while responding");
        }
    }
}

public class Response {
    int statusCode;
    string status;

    function init(int statusCode, string status) {
        self.statusCode = statusCode;
        self.status = status;
    }
}

public class Listener {

    public function 'start() returns error? {
    }

    public function gracefulStop() returns error? {
    }

    public function immediateStop() returns error? {
    }

    public function detach(service object {} s) returns error? {
    }

    public function attach(service object {} s, string[]? name = ()) returns error? {
    }
}
