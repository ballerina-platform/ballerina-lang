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

# Represents Ballerina `AbstractEvictionPolicy` object and cache eviction related operations
# based on linked list data structure.
# Any custom cache implementation should be object-wise similar.
public type AbstractEvictionPolicy abstract object {

    public function get(LinkedList list, Node node);
    public function put(LinkedList list, Node node);
    public function remove(LinkedList list, Node node);
    public function replace(LinkedList list, Node newNode, Node oldNode);
    public function clear(LinkedList list);
    public function evict(LinkedList list) returns Node?;

};
