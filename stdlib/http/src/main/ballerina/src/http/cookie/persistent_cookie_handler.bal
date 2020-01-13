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

# The representation of a persistent cookie handler for managing persistent cookies.
public type PersistentCookieHandler abstract object {

    # Adds a persistent cookie to the cookie store.
    #
    # + cookie - Cookie to be added
    public function storeCookie(Cookie cookie);

    # Gets all persistent cookies.
    #
    # + return - Array of persistent cookies stored in the cookie store
    public function getCookies() returns Cookie[];

    # Removes a specific persistent cookie.
    #
    # + name - Name of the persistent cookie to be removed
    # + domain - Domain of the persistent cookie to be removed
    # + path - Path of the persistent cookie to be removed
    # + return - Return true if the relevant persistent cookie is removed, false otherwise
    public function removeCookie(string name, string domain, string path) returns boolean;

    # Removes all persistent cookies.
    public function clearAllCookies();
};
