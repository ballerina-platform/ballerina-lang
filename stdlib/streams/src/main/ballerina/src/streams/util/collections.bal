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

# The `Node` object represents a node in the linkedlist data structure.
#
# + data - description
# + next - description
# + prev - description
public type Node object {
    public any? data;
    public Node? next;
    public Node? prev;

    public function __init(Node? prev, any? data, Node? next) {
        self.prev = prev;
        self.data = data;
        self.next = next;
    }
};

# The `LinkedList` object which represents the linked list data structure.
#
# + first - description
# + last - description
# + curr - description
# + size - description
# + ascend - description
public type LinkedList object {
    public Node? first;
    public Node? last;
    public Node? curr;
    public int size = 0;
    public boolean ascend = true;

    public function __init() {
        self.first = ();
        self.last = ();
        self.curr = ();
        self.size = 0;
        self.ascend = true;
    }

    # Checks if the linked list is empty.
    #
    # + return - Returns `true` if the linked list is empty, otherwise returns `false`.
    public function isEmpty() returns boolean {
        var value = self.first;
        if (value is Node) {
            return false;
        } else {
            return true;
        }
    }

    # Moves the cursoer to the front/head of the linked list.
    public function resetToFront() {
        self.curr = ();
        self.ascend = true;
    }

    # Moves the cursor to the end of the linked list if the cursor is not already at the last element of the linked
    # list.
    public function resetToRear() {
        self.curr = ();
        self.ascend = false;
    }

    # Returns true if the linked list has more elements starting from the current cursor location.
    #
    # + return - Returns `true` if there are more elements onwards from the current cursor location, otherwise `false`.
    public function hasNext() returns boolean {
        var c = self.curr;
        if (c is Node) {
            Node? next = c.next;
            return !(next is ());
        } else {
            return (self.ascend && !(self.first is ()));
        }
    }

    # Returns `true` if there are prior elements to the current element/cursor location, otherwise false.
    #
    # + return - Returns true, if there are elements prior to the current cursor location, otherwise `false`.
    public function hasPrevious() returns boolean {
        var c = self.curr;
        if (c is Node) {
            Node? prev = c.prev;
            return !(prev is ());
        } else {
            return (!self.ascend && !(self.last is ()));
        }
    }

    # Returns the next element of the linked list and moves the cursor to the next element.
    #
    # + return - The next element from the current cursor location.
    public function next() returns any? {
        if (self.hasNext()) {
            var c = self.curr;
            if (c is Node) {
                Node? next = c.next;
                self.curr = next;
            } else {
                Node? next = self.first;
                self.curr = next;
            }
            Node? val = self.curr;
            if (val is Node) {
                return val.data;
            }
            return ();
        } else {
            error e = error("couldn't iterate to next node.");
            // TODO : Fix this.
            panic e;
        }
    }

    # Returns the previous element of the linked list and moves the cursor to the previous element.
    #
    # + return - The previous element from the current cursor location.
    public function previous() returns any? {
        if (self.hasPrevious()) {
            var c = self.curr;
            if (c is Node) {
                Node? prev = c.prev;
                self.curr = prev;
            } else {
                Node? prev = self.last;
                self.curr = prev;
            }
            Node? val = self.curr;
            if (val is Node) {
                return val.data;
            }
            return ();
        } else {
            error e = error("couldn't iterate to previous node.");
            // TODO : Fix this.
            panic e;
        }
    }

    # Removes the element at the current cursor location.
    public function removeCurrent() {
        var c = self.curr;
        if (c is Node) {
            _ = self.unlink(c);
        } else {

        }
    }

    # Returns the current number of elements in the linked list.
    #
    # + return - The number of elements in the linked list.
    public function getSize() returns int {
        return self.size;
    }

    # Empties the linked list.
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

    # Removes the first occurence of the element pass as `elem` and return `true` of the removal is successful.
    #
    # + return - Return `true` if removal is successful otherwise `false`.
    public function removeFirstOccurrence(any? elem) returns boolean {
        return self.remove(elem);
    }

    # Removes the first occurence of the element pass as `elem` and return `true` of the removal is successful.
    #
    # + return - Return `true` if removal is successful otherwise `false`.
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

    # Returns the first element of the linked list, without moving the cursor.
    #
    # + return - First element of the linked list.
    public function getFirst() returns any? {
        var n = self.first;
        if (n is Node) {
            return n.data;
        } else {
            return ();
        }
    }

    # Returns the last element of the linked list, without moving the cursor.
    #
    # + return - Last element of the linked list.
    public function getLast() returns any? {
        var n = self.last;
        if (n is Node) {
            return n.data;
        } else {
            return ();
        }
    }

    # Adds a new element to the front of the linked list without moving the cursor.
    #
    # + data - Data to be added to the front of the linked list.
    public function addFirst(any data) {
        self.linkFirst(data);
    }

    # Adds a new element to the end of the linked list without moving the cursor.
    #
    # + data - Data to be added to the end of the linked list.
    public function addLast(any data) {
        self.linkLast(data);
    }

    # Removes the first element in the linked list without moving the cursor.
    #
    # + return - Returns the removed element.
    public function removeFirst() returns any? {
        var f = self.first;
        if (f is Node) {
            return self.unlinkFirst(f);
        } else {
            return ();
        }
    }

    # Removes the last element in the linked list without moving the cursor.
    #
    # + return - Returns the removed element.
    public function removeLast() returns any? {
        var l = self.last;
        if (l is Node) {
            return self.unlinkLast(l);
        } else {
            return ();
        }
    }

    # Insert a new element before the current cursor location.
    #
    # + data - Data to be inserted.
    public function insertBeforeCurrent(any data) {
        var c = self.curr;
        if (c is Node) {
            self.linkBefore(data, c);
        } else {

        }
    }

    // Links data as first element.
    function linkFirst(any data) {
        var f = self.first;
        if (f is Node) {
            Node newNode = new((), data, f);
            self.first = newNode;
            f.prev = newNode;
        } else {
            Node newNode = new((), data, ());
            self.first = newNode;
            self.last = newNode;
        }
        self.size += 1;
    }

    // Links data as last element.
    function linkLast(any data) {
        var l = self.last;
        if (l is Node) {
            Node newNode = new(l, data, ());
            self.last = newNode;
            l.next = newNode;
        } else {
            Node newNode = new((), data, ());
            self.first = newNode;
            self.last = newNode;
        }
        self.size += 1;
    }

    // Inserts element 'data' before non-null Node succ.
    function linkBefore(any data, Node succ) {
        Node? pred = succ.prev;
        Node newNode = new(pred, data, succ);
        succ.prev = newNode;
        var n = pred;
        if (n is Node) {
            n.next = newNode;
        } else {
            self.first = newNode;
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
        var n = next;
        if (n is Node) {
            n.prev = ();
        } else {
            self.last = ();
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
        var n = prev;
        if (n is Node) {
            n.next = ();
        } else {
            self.first = ();
        }
        self.size -= 1;
        return data;
    }

    // Unlinks non-null node x.
    function unlink(Node x) returns any? {
        any data = x.data;
        Node? next = x.next;
        Node? prev = x.prev;

        var p = prev;
        if (p is Node) {
            p.next = next;
            x.prev = ();
        } else {
            self.first = next;
        }

        var n = next;
        if (n is Node) {
            n.prev = prev;
            x.next = ();
        } else {
            self.last = prev;
        }

        if (x === self.curr) {
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

    # Returns the first element which is added to the linked list.
    #
    # + return - The dequeued element.
    public function dequeue() returns any? {
        var value = self.first;
        if (value is Node) {
            self.first = value.next;
            var nextValue = self.first;
            if (nextValue is Node) {
                // do nothing
            } else {
                self.last = ();
            }
            return value.data;
        } else {
            return ();
        }
    }

    # Creates an array from the elements in the linked list and return it. The cursor will not be changed.
    #
    # + return - An array of elements in the linked list.
    public function asArray() returns any[] {
        any[] arr = [self.size - 1];
        Node? temp;
        int i = 0;
        if (!self.isEmpty()) {
            var value = self.first;
            if (value is Node) {
                temp = value;
                while (temp is Node) {
                    arr[i] = temp.data;
                    i = i + 1;
                    temp = temp.next;
                }
            } else {

            }
        }
        return arr;
    }

    # Adds elements of an array to the current cursor location and moves the cursor to the end of the list.
    #
    # + data - The array to be added to the linked list.
    public function addAll(any[] data) {
        foreach any d in data {
            self.addLast(d);
        }
    }
};

# This class implements a merge sort algorithm to sort timestamp values for state persistence.
public type IntSort object {

    # Sorts a given array of int values.
    #
    # + arr - The array of int values to be sorted.
    public function sort(int[] arr) {
        self.sortInternal(arr, 0, arr.length() - 1);
    }

    function sortInternal(int[] arr, int l, int r) {
        if (l < r) {
            // Find the middle point
            int m = (l + r) / 2;

            // Sort first and second halves
            self.sortInternal(arr, l, m);
            self.sortInternal(arr, m + 1, r);

            // Merge the sorted halves
            self.merge(arr, l, m, r);
        }
    }

    function merge(int[] arr, int l, int m, int r) {
        // Find sizes of two subarrays to be merged
        int n1 = m - l + 1;
        int n2 = r - m;

        // Create temp arrays
        int[] L = [];
        int[] R = [];

        // Copy data to temp arrays
        int i = 0;
        while (i < n1) {
            L[i] = arr[l + i];
            i = i + 1;
        }

        int j = 0;
        while (j < n2) {
            R[j] = arr[m + 1 + j];
            j = j + 1;
        }

        // Initial indexes of first and second subarrays
        i = 0;
        j = 0;

        // Initial index of merged subarry array
        int k = l;
        while (i < n1 && j < n2) {
            if (L[i] <= R[j]) {
                arr[k] = L[i];
                i += 1;
            } else {
                arr[k] = R[j];
                j += 1;
            }
            k += 1;
        }

        // Copy remaining elements of L[] if any
        while (i < n1) {
            arr[k] = L[i];
            i += 1;
            k += 1;
        }

        // Copy remaining elements of R[] if any
        while (j < n2) {
            arr[k] = R[j];
            j += 1;
            k += 1;
        }
    }
};
