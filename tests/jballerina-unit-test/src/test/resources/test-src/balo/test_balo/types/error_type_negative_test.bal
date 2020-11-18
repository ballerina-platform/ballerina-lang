// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import testorg/errors as er;

function getDistinctError() returns error {
    er:OrderCreationError e = er:OrderCreationError("order creation failed", message = "Client has been stopped");
    er:OrderCreationError2 k = er:OrderCreationError2("failed", message = "Client has been stopped");
    er:OrderCreationError f = k;
    k = f; // expected 'testorg/errors:1.0.0:OrderCreationError2', found 'testorg/errors:1.0.0:OrderCreationError'
    return f;
}

function testErrorDetailDefinedAfterErrorDef() returns er:PostDefinedError {
    er:NewPostDefinedError e = er:PostDefinedError("New error", code = "ABCD");
    return e;
}
