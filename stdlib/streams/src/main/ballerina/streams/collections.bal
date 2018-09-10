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

import ballerina/reflect;

public type Node object {
    public any? data;
    public Node? next;
    public Node? prev;

    new(prev, data, next) {

    }
};

public type LinkedList object {
    public Node? first;
    public Node? last;
    public Node? curr;
    public int size = 0;
    public boolean ascend = true;

    new() {
        first = ();
        last = ();
        curr = ();
        size = 0;
        ascend = true;
    }

    // Returns whether the list is empty.
    public function isEmpty() returns boolean {
        match first {
            Node value => {
                return false;
            }
            () => {
                return true;
            }
        }
    }

    // Reset the iterator head to the front of the list.
    public function resetToFront() {
        curr = ();
        ascend = true;
    }

    // Reset the iterator head to the rear of the list.
    public function resetToRear() {
        curr = ();
        ascend = false;
    }

    // When iterating, indicates whethers there's a node next to the current node.
    public function hasNext() returns boolean {
        match curr {
            Node c => {
                Node? next = c.next;
                return next != ();
            }
            () => {
                return (ascend && first != ());
            }
        }
    }

    // When iterating, indicates whethers there's a node prior to the current node.
    public function hasPrevious() returns boolean {
        match curr {
            Node c => {
                Node? prev = c.prev;
                return prev != ();
            }
            () => {
                return (!ascend && last != ());
            }
        }
    }

    // Iterates to the next node and returns it's data.
    public function next() returns any? {
        if (hasNext()) {
            match curr {
                Node c => {
                    Node? next = c.next;
                    curr = next;
                }
                () => {
                    Node? next = first;
                    curr = next;
                }
            }
            return curr.data;
        } else {
            error e = { message: "couldn't iterate to next node." };
            return e;
        }
    }

    // Iterates to the previos node and returns it's data.
    public function previous() returns any? {
        if (hasPrevious()) {
            match curr {
                Node c => {
                    Node? prev = c.prev;
                    curr = prev;
                }
                () => {
                    Node? prev = last;
                    curr = prev;
                }
            }
            return curr.data;
        } else {
            error e = { message: "couldn't iterate to previous node." };
            return e;
        }
    }

    // Removes the current node.
    public function removeCurrent() {
        match curr {
            Node c => {
                _ = unlink(c);
            }
            () => {}
        }
    }

    // Returns the number of elements in this list.
    public function getSize() returns int {
        return size;
    }

    // Removes all of the elements from this list.
    public function clear() {
        // TODO: unlink every node and clean up properly.
        //match first {
        //    Node f => {
        //        Node x = f;
        //        while (x != ()) {
        //            Node nxt = x.next;
        //            x.data = ();
        //            x.next = ();
        //            x.prev = ();
        //            x = nxt;
        //        }
        //        first = ();
        //        last = ();
        //        size = 0;
        //    }
        //    () => {}
        //}
        first = ();
        last = ();
        size = 0;
    }

    // Removes the first occurrence of the specified element from this list.
    public function removeFirstOccurrence(any? elem) returns boolean {
        return remove(elem);
    }

    // Removes the first occurrence of the specified element from this list.
    public function remove(any? elem) returns boolean {
        // TODO: find a way to loop, and implement properly
        //match elem {
        //    () => {
        //        Node x = first;
        //        while (x != ()) {
        //            any? data = x.data;
        //            match data {
        //                () => {
        //                    _ = unlink(x);
        //                    return true;
        //                }
        //                any d => {}
        //            }
        //            x = x.next;
        //        }
        //    }
        //    any a => {
        //        Node x = first;
        //        while (x != ()) {
        //            any? data = x.data;
        //            match data {
        //                () => {}
        //                any d => {
        //                    if (reflect:equals(a, d)) {
        //                        _ = unlink(x);
        //                        return true;
        //                    }
        //                }
        //            }
        //            x = x.next;
        //        }
        //    }
        //}
        return false;
    }

    // Returns the first element in this list.
    public function getFirst() returns any? {
        match first {
            Node n => {
                return n.data;
            }
            () => {
                return ();
            }
        }
    }

    // Returns the last element in this list.
    public function getLast() returns any? {
        match last {
            Node n => {
                return n.data;
            }
            () => {
                return ();
            }
        }
    }

    // Inserts the specified element at the beginning of this list.
    public function addFirst(any data) {
        linkFirst(data);
    }

    // Inserts the specified element at the end of this list.
    public function addLast(any data) {
        linkLast(data);
    }

    // Removes and returns the first element from this list.
    public function removeFirst() returns any? {
        match first {
            Node f => {
                return unlinkFirst(f);
            }
            () => {
                return ();
            }
        }
    }

    // Removes and returns the last element from this list.
    public function removeLast() returns any? {
        match last {
            Node l => {
                return unlinkLast(l);
            }
            () => {
                return ();
            }
        }
    }

    public function insertBeforeCurrent(any data) {
        match curr {
            Node c => {
                linkBefore(data, c);
            }
            () => {}
        }
    }

    // Links data as first element.
    function linkFirst(any data) {
        match first {
            Node f => {
                Node newNode = new((), data, f);
                first = newNode;
                f.prev = newNode;
            }
            () => {
                Node newNode = new((), data, ());
                first = newNode;
                last = newNode;
            }
        }
        size++;
    }

    // Links data as last element.
    function linkLast(any data) {
        match last {
            Node l => {
                Node newNode = new(l, data, ());
                last = newNode;
                l.next = newNode;
            }
            () => {
                Node newNode = new((), data, ());
                first = newNode;
                last = newNode;
            }
        }
        size++;
    }

    // Inserts element 'data' before non-null Node succ.
    function linkBefore(any data, Node succ) {
        Node? pred = succ.prev;
        Node newNode = new(pred, data, succ);
        succ.prev = newNode;
        match pred {
            Node n => {
                n.next = newNode;
            }
            () => {
                first = newNode;
            }
        }
        size++;
    }

    // Unlinks non-null first Node node.
    function unlinkFirst(Node node) returns any? {
        any data = node.data;
        Node? next = node.next;
        first = next;
        node.data = ();
        node.next = ();
        match next {
            Node n => {
                n.prev = ();
            }
            () => {
                last = ();
            }
        }
        size--;
        return data;
    }

    // Unlinks non-null last Node node.
    function unlinkLast(Node node) returns any? {
        any data = node.data;
        Node? prev = node.prev;
        last = prev;
        node.data = ();
        node.prev = ();
        match prev {
            Node n => {
                n.next = ();
            }
            () => {
                first = ();
            }
        }
        size--;
        return data;
    }

    // Unlinks non-null node x.
    function unlink(Node x) returns any? {
        any data = x.data;
        Node? next = x.next;
        Node? prev = x.prev;
        match prev {
            Node p => {
                p.next = next;
                x.prev = ();
            }
            () => {
                first = next;
            }
        }
        match next {
            Node n => {
                n.prev = prev;
                x.next = ();
            }
            () => {
                last = prev;
            }
        }
        if (reflect:equals(x, curr)) {
            if (ascend) {
                curr = prev;
            } else {
                curr = next;
            }
        }
        x.data = ();
        size--;
        return data;
    }

    public function dequeue() returns any? {
        match first {
            Node value => {
                first = value.next;
                match first {
                    Node nextValue => {
                        // do nothing
                    }
                    () => {
                        last = ();
                    }
                }
                return value.data;
            }
            () => {
                return ();
            }
        }
    }

    public function asArray() returns any[] {
        any[] arr = [size - 1];
        Node? temp;
        int i;
        if (!isEmpty()) {
            match first {
                Node value => {
                    temp = value;
                    while (temp != ()) {
                        arr[i] = temp.data;
                        i = i + 1;
                        temp = temp.next;
                    }
                }
                () => {}
            }
        }
        return arr;
    }
};