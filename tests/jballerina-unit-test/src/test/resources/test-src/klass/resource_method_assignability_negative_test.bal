// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

service class Foo {
    resource function get greeting() returns string => "hello";

    remote function hello() {

    }

    function foo() returns int => 42;
}

service class Bar {
    resource function dot hello() returns string => "hello";

    remote function hello() {

    }

    function foo() returns int => 32;
}

function testServiceObjectValue() {
    Foo f = new Bar();

    Bar bar = service object {
        remote function hello() {

        }

        function foo() returns int => 52;
    };
}

type MyClientObjectType client object {
    resource function get foo/[int]();
};

function testResourcePathParamAssignabilityNegative() {
    client object {
        resource function get .();
    } _ = client object {
        resource function post .() {
        }
    };

    client object {
        resource function get [int a]();
    } _ = client object {
        resource function get [string a]() {
        }
    };

    client object {
        resource function get foo/[int a]();
    } _ = client object {
        resource function get foo/[string a]() {
        }
    };

    client object {
        resource function get [int]();

    } _ = client object {
        resource function get [string]() {
        }
    };

    client object {
        resource function get foo/[int]();
    } _ = client object {
        resource function get foo/[string]() {
        }
    };
    
    client object {
        resource function get [int]();

    } _ = client object {
        resource function get [byte]() {
        }
    };

    client object {
        resource function get [int a]();
    } _ = client object {
        resource function get [string a]() {
        }
        resource function post [int a]() {
        }
    };

    client object {
        resource function get [int a]();
    } _ = client object {
        resource function get [string]() {
        }
        resource function post [int]() {
        }
    };

    client object {
        resource function get bar/[int... a]();
    } _ = client object {
        resource function get bar/[string... a]() {
        }
    };

    client object {
        resource function get bar/[int... a]();
    } _ = client object {
        resource function get bar/[byte... a]() {
        }
    };

    client object {
        resource function get bar/[int... a]();
    } _ = client object {
        resource function get bar/[int a]() {
        }
    };

    client object {
        *MyClientObjectType;
    } _ = client object {
        resource function get foo2/[int]() {
        }
    };

    client object {
        *MyClientObjectType;
    } _ = client object {
        resource function get foo/[string]() {
        }
    };

    client object {
        resource function get .(int a);
    } _ = client object {
        resource function get .() {
        }
    };

    client object {
        resource function get .();
    } _ = client object {
        resource function get .(int a) {
        }
    };

    client object {
        resource function get .(int... a);
    } _ = client object {
        resource function get .(int a) {
        }
    };

    client object {
        resource function get foo();
    } _ = client object {
        resource function get foo(int a) {
        }
    };

    client object {
        resource function get [int a]();
    } _ = client object {
        resource function get .(int a) {
        }
    };

    client object {
        resource function get [int b](int a);
    } _ = client object {
        resource function get .(int a) {
        }
    };

    client object {
        resource function get .(int a);
    } _ = client object {
        resource function get [int a]() {
        }
    };

    client object {
        resource function get .(int a);
    } _ = client object {
        resource function get [int]() {
        }
    };

    service object {
        resource function get .();
    } _ = service object {
        resource function post .() {
        }
    };

    service object {
        resource function get [int a]();
    } _ = service object {
        resource function get [string a]() {
        }
    };

    service object {
        resource function get foo/[int a]();
    } _ = service object {
        resource function get foo/[string a]() {
        }
    };

    service object {
        resource function get [int]();
    } _ = service object {
        resource function get [string]() {
        }
    };

    service object {
        resource function get [int]();
    } _ = client object {
        resource function get [int]() {
        }
    };
    
    client object {
        resource function get foo/[string...]();
    } _ = client object {
        resource function get foo/[int...]() {
        }
    };
}
