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

import ballerina/java;

// This is a linked list data structure implementation, which is used for the eviction algorithm of the cache.

# Represents a structure to keep data and references to the adjacent nodes of the linked list.
#
# + value - Value to be stored in the linked list node
# + prev - Previous node of the linked list
# + next - Next node of the linked list
public type Node record {|
    any value;
    Node? prev = ();
    Node? next = ();
|};

# The `cache:LinkedList` object consists operations of `LinkedList` data structure which are related
# to LRU eviction algorithm
#
# + head - The first node of the linked list
# + tail - The last node of the linked list
public type Cache object {

    // This flag is used to avoid concurrency issues occurring during the removing nodes from the linked-list.
    // Ballerina locks cannot be used for this since it may lead to unexpected results.
    boolean removeInProgress = false;
    Node? head = ();
    Node? tail = ();

    # Adds a node to the end of the provided linked list.
    #
    # + node - The node, which should be added to the provided linked list
    public function addLast(Node node) {
        if (self.tail is ()) {
            self.head = node;
            self.tail = self.head;
            return;
        }
        Node tempNode = node;
        Node tailNode = <Node>self.tail;
        tempNode.prev = tailNode;
        tailNode.next = tempNode;
        self.tail = tempNode;
    }

    # Adds a node to the start of the provided linked list.
    #
    # + node - The node, which should be added to the provided linked list
    public function addFirst(Node node) {
        if (self.head is ()) {
            self.head = node;
            self.tail = self.head;
            return;
        }
        Node tempNode = node;
        Node headNode = <Node>self.head;
        tempNode.next = headNode;
        headNode.prev = tempNode;
        self.head = tempNode;
    }

    # Removes a node from the provided linked list.
    #
    # + node - The node, which should be removed from the provided linked list
    public function remove(Node node) {
        // Using this flag, we prevent the concurrency issues, but this will avoid removing some nodes from the linked-list.
        // Due to that, when the eviction happens, there can be situations where a node which is used recently is get
        // removed from the cache.
        if (!self.removeInProgress) {
            self.removeInProgress = true;
            if (node.prev is ()) {
                self.head = node.next;
            } else {
                Node prev = <Node>node.prev;
                prev.next = node.next;
            }
            if (node.next is ()) {
                self.tail = node.prev;
            } else {
                Node next = <Node>node.next;
                next.prev = node.prev;
            }
            node.next = ();
            node.prev = ();
            self.removeInProgress = false;
        }
    }

    # Removes the last node from the provided linked list.
    #
    # + return - Last node of the provided linked list or `()` if the last node is empty
    public function removeLast() returns Node? {
        if (self.tail is ()) {
            return ();
        }
        Node tail = <Node>self.tail;
        Node predecessorOfTail = <Node>tail.prev;
        self.tail = predecessorOfTail;
        predecessorOfTail.next = ();
        tail.prev = ();

        return tail;
    }

    # Clears the provided linked list.
    public function clear() {
        self.head = ();
        self.tail = ();
    }
};

function externLockInit(LinkedList linkedList) = @java:Method {
    name: "init",
    class: "org.ballerinalang.stdlib.cache.nativeimpl.Lock"
} external;

function externCheckState(LinkedList linkedList) returns boolean = @java:Method {
    name: "lock",
    class: "org.ballerinalang.stdlib.cache.nativeimpl.Lock"
} external;
