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

import testorgthree/isolation;

isolated function testInvalidNonIsolatedFunctionCallInIsolatedFunction() {
    int _ = isolation:nonIsolatedFunction();

    isolation:NonIsolatedClass b = new; // OK
    int _ = b.getI();

    isolation:NonIsolatedClassWithNonIsolatedInit _ = new;

    isolation:IsolatedClassWithExplicitNonIsolatedInit _ = new (isolated object {
                                                                    public final int i = 1;
                                                                    public final boolean j = true;
                                                                });
}

isolated class InvalidIsolatedClassWithImportedIsolatedObjectFields {
    final isolation:NonIsolatedObjectType a;
    final isolation:NonIsolatedClass b = new;
    final isolation:NonIsolatedClassWithNonIsolatedInit c;
    private int d = 1;

    function init(isolation:NonIsolatedObjectType a, isolation:NonIsolatedClassWithNonIsolatedInit c) {
        self.a = a;
        self.c = c;
    }
}
