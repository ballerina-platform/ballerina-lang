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
        self.first = ();
        self.last = ();
        self.curr = ();
        self.size = 0;
        self.ascend = true;
    }

    // Returns whether the list is empty.
    public function isEmpty() returns boolean {
        match self.first {
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
        self.curr = ();
        self.ascend = true;
    }

    // Reset the iterator head to the rear of the list.
    public function resetToRear() {
        self.curr = ();
        self.ascend = false;
    }

    // When iterating, indicates whethers there's a node next to the self.current node.
    public function hasNext() returns boolean {
        match self.curr {
            Node c => {
                Node? next = c.next;
                return next !== ();
            }
            () => {
                return (self.ascend && self.first !== ());
            }
        }
    }

    // When iterating, indicates whethers there's a node prior to the self.current node.
    public function hasPrevious() returns boolean {
        match self.curr {
            Node c => {
                Node? prev = c.prev;
                return prev !== ();
            }
            () => {
                return (!self.ascend && self.last !== ());
            }
        }
    }

    // Iterates to the next node and returns it's data.
    public function next() returns any? {
        if (self.hasNext()) {
            match self.curr {
                Node c => {
                    Node? next = c.next;
                    self.curr = next;
                }
                () => {
                    Node? next = self.first;
                    self.curr = next;
                }
            }
            return self.curr.data;
        } else {
            error e = error("couldn't iterate to next node.");
            // TODO : Fix this.
            panic e;
        }
    }

    // Iterates to the previous node and returns it's data.
    public function previous() returns any? {
        if (self.hasPrevious()) {
            match self.curr {
                Node c => {
                    Node? prev = c.prev;
                    self.curr = prev;
                }
                () => {
                    Node? prev = self.last;
                    self.curr = prev;
                }
            }
            return self.curr.data;
        } else {
            error e = error("couldn't iterate to previous node.");
            // TODO : Fix this.
            panic e;
        }
    }

    // Removes the self.current node.
    public function removeCurrent() {
        match self.curr {
            Node c => {
                _ = self.unlink(c);
            }
            () => {}
        }
    }

    // Returns the number of elements in this list.
    public function getSize() returns int {
        return self.size;
    }

    // Removes all of the elements from this list.
    public function clear() {
        // TODO: unlink every node and clean up properly.
        //match self.first {
        //    Node f => {
        //        Node x = f;
        //        while (x != ()) {
        //            Node nxt = x.next;
        //            x.data = ();
        //            x.next = ();
        //            x.prev = ();
        //            x = nxt;
        //        }
        //        self.first = ();
        //        self.last = ();
        //        self.size = 0;
        //    }
        //    () => {}
        //}
        self.first = ();
        self.last = ();
        self.size = 0;
    }

    // Removes the first occurrence of the specified element from this list.
    public function removeFirstOccurrence(any? elem) returns boolean {
        return self.remove(elem);
    }

    // Removes the first occurrence of the specified element from this list.
    public function remove(any? elem) returns boolean {
        // TODO: find a way to loop, and implement properly
        //match elem {
        //    () => {
        //        Node x = self.first;
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
        //        Node x = self.first;
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

    // Returns the self.first element in this list.
    public function getFirst() returns any? {
        match self.first {
            Node n => {
                return n.data;
            }
            () => {
                return ();
            }
        }
    }

    // Returns the self.last element in this list.
    public function getLast() returns any? {
        match self.last {
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
        self.linkFirst(data);
    }

    // Inserts the specified element at the end of this list.
    public function addLast(any data) {
        self.linkLast(data);
    }

    // Removes and returns the self.first element from this list.
    public function removeFirst() returns any? {
        match self.first {
            Node f => {
                return self.unlinkFirst(f);
            }
            () => {
                return ();
            }
        }
    }

    // Removes and returns the last element from this list.
    public function removeLast() returns any? {
        match self.last {
            Node l => {
                return self.unlinkLast(l);
            }
            () => {
                return ();
            }
        }
    }

    public function insertBeforeCurrent(any data) {
        match self.curr {
            Node c => {
                self.linkBefore(data, c);
            }
            () => {}
        }
    }

    // Links data as first element.
    function linkFirst(any data) {
        match self.first {
            Node f => {
                Node newNode = new((), data, f);
                self.first = newNode;
                f.prev = newNode;
            }
            () => {
                Node newNode = new((), data, ());
                self.first = newNode;
                self.last = newNode;
            }
        }
        self.size += 1;
    }

    // Links data as last element.
    function linkLast(any data) {
        match self.last {
            Node l => {
                Node newNode = new(l, data, ());
                self.last = newNode;
                l.next = newNode;
            }
            () => {
                Node newNode = new((), data, ());
                self.first = newNode;
                self.last = newNode;
            }
        }
        self.size += 1;
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
                self.first = newNode;
            }
        }
        self.size += 1;
    }

    // Unlinks non-null first Node node.
    function unlinkFirst(Node node) returns any? {
        any data = node.data;
        Node? next = node.next;
        self.first = next;
        node.data = ();
        node.next = ();
        match next {
            Node n => {
                n.prev = ();
            }
            () => {
                self.last = ();
            }
        }
        self.size -= 1;
        return data;
    }

    // Unlinks non-null last Node node.
    function unlinkLast(Node node) returns any? {
        any data = node.data;
        Node? prev = node.prev;
        self.last = prev;
        node.data = ();
        node.prev = ();
        match prev {
            Node n => {
                n.next = ();
            }
            () => {
                self.first = ();
            }
        }
        self.size -= 1;
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
                self.first = next;
            }
        }
        match next {
            Node n => {
                n.prev = prev;
                x.next = ();
            }
            () => {
                self.last = prev;
            }
        }
        if (reflect:equals(x, self.curr)) {
            if (self.ascend) {
                self.curr = prev;
            } else {
                self.curr = next;
            }
        }
        x.data = ();
        self.size -= 1;
        return data;
    }

    public function dequeue() returns any? {
        match self.first {
            Node value => {
                self.first = value.next;
                match self.first {
                    Node nextValue => {
                        // do nothing
                    }
                    () => {
                        self.last = ();
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
        any[] arr = [self.size - 1];
        Node? temp;
        int i = 0;
        if (!self.isEmpty()) {
            match self.first {
                Node value => {
                    temp = value;
                    while (temp !== ()) {
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