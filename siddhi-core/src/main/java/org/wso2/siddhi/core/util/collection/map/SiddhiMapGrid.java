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

import com.hazelcast.core.IMap;
import com.hazelcast.query.SqlPredicate;
import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.exception.NoIdentifierException;

import java.util.HashMap;
import java.util.Iterator;

public class SiddhiMapGrid<T> extends SiddhiMap<T> {
    static final Logger log = Logger.getLogger(SiddhiMapGrid.class);

    protected IMap<String, T> map;
    protected String elementId;
    protected SiddhiContext siddhiContext;


    public SiddhiMapGrid(String elementId, SiddhiContext siddhiContext) {
        this.siddhiContext = siddhiContext;
        if (elementId == null) {
            throw new NoIdentifierException(this.getClass().getSimpleName() + " elementId cannot be null");
        }
        this.elementId = elementId + "-" + this.getClass().getSimpleName();
        map = siddhiContext.getHazelcastInstance().getMap(this.elementId);
    }

    public synchronized T put(String key, T t) {
        return map.put(key, t);
    }

    public synchronized T putIfAbsent(String key, T t) {
        return map.putIfAbsent(key, t);
    }

    public synchronized T get(String key) {
        return map.get(key);
    }

    public synchronized T remove(String key) {
        return map.remove(key);
    }

    public synchronized void clear() {
        map.clear();
    }

    public Iterator<T> iterator() {
        return map.values().iterator();
    }

    public Object[] currentState() {
        java.util.Map tempMap = new HashMap<String, T>();
        for (java.util.Map.Entry<String, T> entry : map.entrySet()) {
            tempMap.put(entry.getKey(), entry.getValue());
        }
        if (log.isDebugEnabled()) {
            log.debug("map size bring persisted " + map.size());
        }
        //todo may be we throw an error saying we don't support for distributed cases
        return new Object[]{tempMap};
    }

    public void restoreState(Object[] objects) {
        map.putAll((java.util.Map) objects[0]);
    }

    public synchronized Iterator<T> iterator(String condition) {

        if (condition.trim().equals("*")) {
            return map.values().iterator();
        }
        return map.values(new SqlPredicate(condition)).iterator();
    }

    public int size() {
        return map.size();
    }

}
