/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.core.util.collection.map;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class SiddhiMap<T> {
    private ConcurrentHashMap<String, T> map = new ConcurrentHashMap<String, T>();

    public T put(String key, T t) {
        return map.put(key, t);
    }

    public T putIfAbsent(String key, T t) {
        return map.putIfAbsent(key, t);
    }

    public T get(String key) {
        return map.get(key);
    }

    public T remove(String key) {
        return map.remove(key);
    }

    public void clear() {
        map.clear();
    }

    public Iterator<T> iterator() {
        return map.values().iterator();
    }

    public Object[] currentState() {
        return new Object[]{map};
    }

    public void restoreState(Object[] objects) {
        map = (ConcurrentHashMap<String, T>) objects[0];
    }

    public int size() {
        return map.size();
    }

}
