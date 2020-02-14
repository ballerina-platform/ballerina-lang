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

int i = 0;

stream<int> evenNumberStream = stream {
    i += 2;
    return { value: i };
};

stream<int> evenNumberStream1 = stream {
    i += 2;
    return i;
};

stream<int> evenNumberStream2 = stream {
    i += 2;
    return { val: i }; // undefined field 'val' in record '$anonType$2', since it get's type checked with an annon type
};

stream<int> evenNumberStream3 = stream {
    return { value: "string" };
};

stream<int> evenNumberStream4 = stream {
    return { value: true };
};

stream<int> evenNumberStream5 = stream {
    return ();
};
