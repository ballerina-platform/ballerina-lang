/*
*  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.core.util.generator;

import com.hazelcast.core.IAtomicLong;
import org.wso2.siddhi.core.config.SiddhiContext;

public class GlobalIndexGenerator {


    private static IAtomicLong one;
    private static IAtomicLong two;

    public GlobalIndexGenerator(SiddhiContext siddhiContext) {
        one = siddhiContext.getHazelcastInstance().getAtomicLong("GlobalIndexGenerator-one");
        two = siddhiContext.getHazelcastInstance().getAtomicLong("GlobalIndexGenerator-two");
    }

    public synchronized String getNewIndex() {
        long localOne;
        long localTwo = two.incrementAndGet();
        if (localTwo == Long.MAX_VALUE) {
            two.set(Long.MIN_VALUE);
            localOne = one.incrementAndGet();
            if (localOne == Long.MAX_VALUE) {
                one.set(Long.MIN_VALUE);
            }
        } else {
            localOne = one.get();
        }
        return localOne + "_" + localTwo;
    }
}
