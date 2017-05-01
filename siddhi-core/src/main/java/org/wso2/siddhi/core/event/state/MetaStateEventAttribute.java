/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.event.state;

import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.Arrays;

/**
 * Holds the Attribute info for StateEvent data to StreamEvent data
 */
public class MetaStateEventAttribute {
    private Attribute attribute;
    private int[] position;

    public MetaStateEventAttribute(Attribute attribute, int[] position) {
        this.attribute = attribute;
        this.position = position;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public int[] getPosition() {
        return position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MetaStateEventAttribute)) {
            return false;
        }

        MetaStateEventAttribute attribute1 = (MetaStateEventAttribute) o;

        if (attribute != null ? !attribute.equals(attribute1.attribute) : attribute1.attribute != null) {
            return false;
        }
        return Arrays.equals(position, attribute1.position);

    }

    @Override
    public int hashCode() {
        int result = attribute != null ? attribute.hashCode() : 0;
        result = 31 * result + (position != null ? Arrays.hashCode(position) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MappingAttribute{" +
                "attribute=" + attribute +
                ", position=" + Arrays.toString(position) +
                '}';
    }
}
