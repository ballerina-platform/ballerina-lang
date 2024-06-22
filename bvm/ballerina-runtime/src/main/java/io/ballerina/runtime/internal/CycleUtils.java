/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.internal;

import io.ballerina.runtime.api.values.BLink;

/**
 * This class contains the utility methods required for identifying cycles.
 *
 * @since Swan Lake
 */
public class CycleUtils {

    /**
     * Internal class which represents the link between the members of ballerina values.
     * This is used to detect the cycles in a map or array
     */
    public static class Node implements BLink {
        Object obj;
        BLink parent;
        int index = 0;

        public Node(Object obj, BLink parent) {
            this.obj = obj;
            this.parent = parent;
        }

        @Override
        public boolean hasCyclesSoFar() {
            Node parent = (Node) this.parent;
            while (parent != null) {
                if (parent.obj == obj) {
                    return true;
                }
                parent = (Node) parent.parent;
                index++;
            }
            return false;
        }

        public int getIndex() {
            return index;
        }
    }
}
