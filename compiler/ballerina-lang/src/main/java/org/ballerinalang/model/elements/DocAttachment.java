/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.model.elements;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 0.975.0
 */
public class DocAttachment {

    public String description;
    public List<DocAttribute> attributes;

    public DocAttachment() {
        this.attributes = new ArrayList<>();
    }

    public String getDescription() {
        return description;
    }

    /**
     * Represents Doc attachment attribute.
     */
    public static class DocAttribute {

        public String name;
        public String description;
        public DocTag docTag;

        public DocAttribute(String name,
                            String description,
                            DocTag docTag) {
            this.name = name;
            this.description = description;
            this.docTag = docTag;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public DocTag getDocTag() {
            return docTag;
        }

    }
}
