/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.natives;

import java.util.HashMap;
import java.util.Map;

/**
 * This represents a native element repository, that would be populated by any
 * native element provider.
 */
public class NativeElementRepository {

    private Map<NativeElementKey, String> entries = new HashMap<>();
    
    private Map<NativeElementKey, NativeElement> nativeElementCache = new HashMap<>();
    
    public void addEntry(NativeElementKey key, String className) {
        this.entries.put(key, className);
    }
    
    public String lookupEntry(NativeElementKey key) {
        return this.entries.get(key);
    }
    
    public NativeElement loadNativeElement(NativeElementKey key) {
        NativeElement result = this.nativeElementCache.get(key);
        if (result == null) {
            String className = this.lookupEntry(key);
            if (className != null) {
                try {
                    result = (NativeElement) Class.forName(className).newInstance();
                    this.nativeElementCache.put(key, result);
                } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                    throw new RuntimeException("Error in loading native element: " + e.getMessage(), e);
                }
            }
        }
        return result;
    }
    
}
