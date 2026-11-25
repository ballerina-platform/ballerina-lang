// Copyright (c) 2026 WSO2 LLC. (http://www.wso2.com).
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

public type KeyStore record {|
    string path;
    string password;
|};

public type SecureSocket record {|
    KeyStore keyStore;
    boolean enable = true;
|};

// The compiler generates immutable types for `SecureSocket` and `KeyStore`.
public type ImmutableSecureSocket readonly & SecureSocket;
