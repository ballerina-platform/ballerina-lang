/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.componentmodel.entitymodel.components;

/**
 * Represent the relationship between records.
 */
public class Association {
    private String associate;
    private Cardinality cardinality;

    public Association(String associate, Cardinality cardinality) {
        this.associate = associate;
        this.cardinality = cardinality;
    }

    public String getAssociate() {
        return associate;
    }

    public Cardinality getCardinality() {
        return cardinality;
    }

    public void setAssociate(String associate) {
        this.associate = associate;
    }

    public void setCardinality(Cardinality cardinality) {
        this.cardinality = cardinality;
    }

    /**
     * Represents the cardinality of the relationship.
     */
    public static class Cardinality {
        private String self;
        private String associate;

        public Cardinality(String self, String associate) {
            this.self = self;
            this.associate = associate;
        }
        public String getSelf() {
            return self;
        }
        public String getAssociate() {
            return associate;
        }
    }
}
