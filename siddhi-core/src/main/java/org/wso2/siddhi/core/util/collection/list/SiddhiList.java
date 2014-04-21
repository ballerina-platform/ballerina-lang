/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.siddhi.core.util.collection.list;

import java.util.Iterator;
import java.util.LinkedList;

public class SiddhiList<T> {
    private LinkedList<T> list = new LinkedList<T>();

    public synchronized void add(T t) {
        list.add(t);
    }

    public synchronized T get(int index) {
        return list.get(index);
    }

    public synchronized T remove(int index) {
        return list.remove(index);
    }

    public synchronized boolean remove(T t) {
        return list.remove(t);
    }

    public Iterator<T> iterator() {
        return list.iterator();
    }

    public Object[] currentState() {
        return new Object[]{list};
    }

    public void restoreState(Object[] objects) {
        list = (LinkedList) objects[0];
    }

    public int size() {
        return list.size();
    }

    public void clear() {
        list.clear();
    }
}
