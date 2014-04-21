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

import com.hazelcast.core.IList;
import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.exception.NoIdentifierException;

import java.util.*;

public class SiddhiListGrid<T> extends SiddhiList<T> {

    static final Logger log = Logger.getLogger(SiddhiListGrid.class);

    protected IList<T> list;
    protected String elementId;
    protected SiddhiContext siddhiContext;


    public SiddhiListGrid(String elementId, SiddhiContext siddhiContext) {
        this.siddhiContext = siddhiContext;
        if (elementId == null) {
            throw new NoIdentifierException(this.getClass().getSimpleName() + " elementId cannot be null");
        }
        this.elementId = elementId + "-" + this.getClass().getSimpleName();
        list = siddhiContext.getHazelcastInstance().getList(this.elementId);
    }

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
        java.util.List<T> tempList = new LinkedList<T>();
        for (T t : list) {
            tempList.add(t);
        }
        if (log.isDebugEnabled()) {
            log.debug("list with size " + list.size() + " bring persisted ");
        }
        //todo may be we throw an error saying we don't support for distributed cases
        return new Object[]{tempList};
    }

    public void restoreState(Object[] objects) {
        list.addAll((java.util.List<T>) objects[0]);
    }

    public int size() {
        return list.size();
    }

    public void clear() {
        list.clear();
    }
}
