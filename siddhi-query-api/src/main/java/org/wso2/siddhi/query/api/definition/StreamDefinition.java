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
package org.wso2.siddhi.query.api.definition;

import org.wso2.siddhi.query.api.annotation.Annotation;

/**
 * Siddhi Stream Definition
 */
public class StreamDefinition extends AbstractDefinition {

    private static final long serialVersionUID = 1L;

    public StreamDefinition() {
    }

    protected StreamDefinition(String streamId) {
        super(streamId);
    }

    public static StreamDefinition id(String streamId) {
        return new StreamDefinition(streamId);
    }

    public StreamDefinition attribute(String attributeName, Attribute.Type type) {
        checkAttribute(attributeName);
        this.attributeList.add(new Attribute(attributeName, type));
        return this;
    }

    public StreamDefinition annotation(Annotation annotation) {
        annotations.add(annotation);
        return this;
    }


    @Override
    public String toString() {
        return "StreamDefinition{" +
                "id='" + id + '\'' +
                ", attributeList=" + attributeList +
                ", annotations=" + annotations +
                '}';
    }

}
