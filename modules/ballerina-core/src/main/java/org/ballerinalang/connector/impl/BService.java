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
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.ballerinalang.connector.impl;

import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rajith on 9/4/17.
 */
public class BService extends AbstractServiceResource implements Service {
    String name;

    //key - resourceName, value - resource
    private Map<String, Resource> resourceMap = new HashMap<>();

    public BService(String name) {
        this.name = name;
    }

    public void addResource(String name, Resource resource) {
        resourceMap.put(name, resource);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Annotation getAnnotation(String pkgPath, String name) {
        String key = pkgPath + ":" + name;
        return annotationMap.get(key);
    }

    @Override
    public Resource[] getResources() {
        return resourceMap.values().toArray(new Resource[0]);
    }
}
