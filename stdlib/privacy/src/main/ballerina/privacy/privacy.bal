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

# Pseudonymize personally identifiable information (PII) and store PII and the pseudonymized identifier in the PII store
#
# + store - storage used to persist the PII and the identifier
# + pii - PII to be pseudonymized
# + return - 36 characters long UUID if storage operation was successful, error if storage operation failed
public function pseudonymize (PiiStore store, string pii) returns string|error {
    return store.pseudonymize(pii);
}

# Depseudonymize the identifier by retrieving the personally identifiable information (PII) from the PII store
#
# + store - storage used to persist the PII and the identifier
# + id - pseudonymized identifier to be depseudonymize
# + return - PII if retrieval was successful, error if retrieval failed
public function depseudonymize (PiiStore store, string id) returns string|error {
    return store.depseudonymize(id);
}

# Delete personally identifiable information (PII) from the PII store
#
# + store - storage used to persist the PII and the identifier
# + id - pseudonymized identifier to be deleted
# + return - nil if retrieval was successful, error if retrieval failed
public function delete (PiiStore store, string id) returns error? {
    return store.delete(id);
}
