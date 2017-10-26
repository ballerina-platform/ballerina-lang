/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.doc.gen.commons.metadata;

import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

/**
 * POJO for holding extension meta data
 */
public class NamespaceMetaData implements Comparable<NamespaceMetaData> {
    private String name;
    private TreeMap<String, List<ExtensionMetaData>> extensionMap;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TreeMap<String, List<ExtensionMetaData>> getExtensionMap() {
        return extensionMap;
    }

    public void setExtensionMap(TreeMap<String, List<ExtensionMetaData>> extensionMap) {
        this.extensionMap = extensionMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NamespaceMetaData that = (NamespaceMetaData) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        return extensionMap != null ? extensionMap.equals(that.extensionMap) : that.extensionMap == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (extensionMap != null ? extensionMap.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(NamespaceMetaData o) {
        if (o == null) {
            return -1;
        }
        if (this.equals(o)) {
            return 0;
        }
        int result = 0;
        if (name != null && o.name != null) {
            result = name.compareTo(o.name);
        } else if (name == null && o.name == null) {
            result = 0;
        } else if (name == null) {
            return -1;
        } else {
            return 1;
        }
        Iterator<String> thisIterable = extensionMap.keySet().iterator();
        Iterator<String> oIterable = o.extensionMap.keySet().iterator();
        while (result == 0) {
            if (thisIterable.hasNext() && oIterable.hasNext()) {
                result = thisIterable.next().compareTo(oIterable.next());
            } else if (!thisIterable.hasNext() && !oIterable.hasNext()) {
                result = 0;
            } else if (!thisIterable.hasNext()) {
                return -1;
            } else {
                return 1;
            }
        }
        return result;
    }
}
