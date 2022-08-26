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

class A {
    *B;
    int i = 0;
}

class B {
    *A;
    *C;
    int i = 0;
}

type C object {
    *A;
};

type ServiceObject service object {
    resource function get name() returns string;
};

service class ServiceClass {
    *ServiceObject;
}

type ClientObject client object {
    resource function get name() returns string;
};

client class ClientClass {
    *ClientObject;
}

type ClientObject2 client object {
    *ClientObject;
};

client class ClientClass2 {
    *ClientObject2;
}

client class ClientClass3 {
    *ClientClass2;
}
