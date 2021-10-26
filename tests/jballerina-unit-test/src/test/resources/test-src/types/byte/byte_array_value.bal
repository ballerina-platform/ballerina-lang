// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function base16ByteArrayLiteralWithWS() {
    byte[] byteArray = base16 `a
                        d`;
    assertEquality(byteArray, base16 `ad`);
    
    byte[] byteArray2 = base16 `a	d`;
    assertEquality(byteArray2, base16 `ad`);
}

function base64ByteArrayLiteralWithWS() {
    byte[] byteArray1 = base64 `aGVsbG8gYmFsbGVyaW5hICEhIQ==`;
   // In following two cases, we have tabs. not spaces.
    byte[] byteArray2 = base64 `aGVs	bG8g	YmFs	bGVy	aW5h	ICEh	IQ==`;
    byte[] byteArray3 = base64 `	aG	Vs	b	G	8g	Y	mFs	bGVy	aW5h	ICEh	IQ=		=	`;
    
    assertEquality(byteArray1, byteArray2);
    assertEquality(byteArray1, byteArray3);
}

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }
    
    panic error("AssertionError", message = "expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
