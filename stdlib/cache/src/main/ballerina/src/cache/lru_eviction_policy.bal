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

# Represents the `LruEvictionPolicy` object, which has LRU eviction algorithm related operations
# based on a linked list data structure.
public type LruEvictionPolicy object {

    *AbstractEvictionPolicy;

    # Update the linked list based on the get operation related to the LRU eviction algorithm.
    #
    # + list - Linked list data structure
    # + node - Node of the linked list, which is retrieved
    public function get(LinkedList list, Node node) {
        remove(list, node);
        addFirst(list, node);
    }

    # Update the linked list based on the put operation related to the LRU eviction algorithm.
    #
    # + list - Linked list data structure
    # + node - Node of the linked list, which is added newly
    public function put(LinkedList list, Node node) {
        addFirst(list, node);
    }

    # Update the linked list based on the remove operation related to the LRU eviction algorithm.
    #
    # + list - Linked list data structure
    # + node - Node of the linked list, which is deleted
    public function remove(LinkedList list, Node node) {
        remove(list, node);
    }

    # Update the linked list based on the replace operation related to the LRU eviction algorithm.
    #
    # + list - Linked list data structure
    # + newNode - Node of the linked list, which is used for replacing
    # + oldNode - Node of the linked list, which will be replaced
    public function replace(LinkedList list, Node newNode, Node oldNode) {
        remove(list, oldNode);
        addFirst(list, newNode);
    }

    # Update the linked list based on the clear operation related to the LRU eviction algorithm.
    #
    # + list - Linked list data structure
    public function clear(LinkedList list) {
        clear(list);
    }

    # Update the linked list based on the evict operation related to the LRU eviction algorithm.
    #
    # + list - Linked list data structure
    # + return - The Node, which should evict from the linked list or `()` if nothing to be evicted
    public function evict(LinkedList list) returns Node? {
        return removeLast(list);
    }

};
