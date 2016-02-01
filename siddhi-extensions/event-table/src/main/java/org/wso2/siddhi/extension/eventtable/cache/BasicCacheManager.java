/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.extension.eventtable.cache;

import org.wso2.siddhi.core.event.stream.StreamEvent;

import java.util.LinkedList;

public class BasicCacheManager implements CacheManager {

    private final LinkedList<StreamEvent> cacheList;
    private long limit;

    public BasicCacheManager(LinkedList<StreamEvent> cacheList, long limit) {
        this.cacheList = cacheList;
        this.limit = limit;
    }

    @Override
    public void add(StreamEvent item) {
        if (cacheList.size() >= limit) {
            cacheList.remove(0);
        }
    }

    @Override
    public void delete(StreamEvent item) {
        //No implementation Required
    }

    @Override
    public void read(StreamEvent item) {
        //No implementation Required
    }

    @Override
    public void update(StreamEvent item) {
        //No implementation Required
    }

    @Override
    public void invalidateCache() {
        cacheList.clear();
    }

    @Override
    public boolean isContains(StreamEvent item) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
