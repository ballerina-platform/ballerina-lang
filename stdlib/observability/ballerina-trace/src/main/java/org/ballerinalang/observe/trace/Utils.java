/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
*
*/
package org.ballerinalang.observe.trace;

import org.ballerinalang.model.values.BMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This provides the util functions to tracing related functions.
 */
public class Utils {

    private Utils() {
        //Do nothing
    }

    public static Map<String, String> toStringMap(BMap map) {
        Map<String, String> returnMap = new HashMap<>();
        Set bIterator = map.keySet();
        for (Object aKey : bIterator) {
            returnMap.put(aKey.toString(), map.get(aKey).stringValue());
        }
        return returnMap;
    }
}
