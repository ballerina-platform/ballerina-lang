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

# Represents the Ballerina `AbstractEvictionPolicy` object and cache eviction related operations
# based on a linked list data structure.
# Any custom cache implementation should be object-wise similar.
public type AbstractEvictionPolicy abstract object {

    # Update the linked list based on the get operation.
    #
    # + list - Linked list data structure
    # + node - Node of the linked list, which is retrieved
    public function get(LinkedList list, Node node);

    # Update the linked list based on the put operation.
    #
    # + list - Linked list data structure
    # + node - Node of the linked list, which is added newly
    public function put(LinkedList list, Node node);

    # Update the linked list based on the remove operation.
    #
    # + list - Linked list data structure
    # + node - Node of the linked list, which is deleted
    public function remove(LinkedList list, Node node);

    # Update the linked list based on the replace operation.
    #
    # + list - Linked list data structure
    # + newNode - Node of the linked list, which is used for replacing
    # + oldNode - Node of the linked list, which will be replaced
    public function replace(LinkedList list, Node newNode, Node oldNode);

    # Update the linked list based on the clear operation.
    #
    # + list - Linked list data structure
    public function clear(LinkedList list);

    # Update the linked list based on the evict operation.
    #
    # + list - Linked list data structure
    # + return - The Node, which should evict from the linked list or `()` if nothing to be evicted
    public function evict(LinkedList list) returns Node?;

};
