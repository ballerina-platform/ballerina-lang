/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.query.api.definition.partition;

import org.wso2.siddhi.query.api.condition.Condition;

public class RangePartitionType implements PartitionType {

    private Condition condition;
    private String label;

    public RangePartitionType(Condition condition, String label) {
        this.condition = condition;
        this.label = label;
    }

    public Condition getCondition() {
        return condition;
    }

    public String getLabel() {
        return label;
    }

    public String getPartitionKey() {
        return label;
    }

    @Override
    public String toString() {
        return "RangePartitionType{" +
               "condition=" + condition +
               ", label='" + label + '\'' +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RangePartitionType that = (RangePartitionType) o;

        if (condition != null ? !condition.equals(that.condition) : that.condition != null) {
            return false;
        }
        if (label != null ? !label.equals(that.label) : that.label != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = condition != null ? condition.hashCode() : 0;
        result = 31 * result + (label != null ? label.hashCode() : 0);
        return result;
    }
}
