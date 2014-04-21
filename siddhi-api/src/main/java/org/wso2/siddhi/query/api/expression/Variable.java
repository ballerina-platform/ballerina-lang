/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.query.api.expression;

import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.MalformedAttributeException;
import org.wso2.siddhi.query.api.query.QueryEventSource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Variable extends Expression {

    private String streamId;
    private int position = -1;
    private String attributeName;

    public Variable(String streamId, String attributeName) {
        this.streamId = streamId;
        this.attributeName = attributeName;
    }

    public Variable(String attributeName) {
        this.attributeName = attributeName;
    }

    public Variable(String streamId, int position, String attributeName) {
        this.streamId = streamId;
        this.position = position;
        this.attributeName = attributeName;
    }

    public String getStreamId() {
        return streamId;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public int getPosition() {
        return position;
    }

    @Override
    protected void validate(List<QueryEventSource> queryEventSourceList, String streamReferenceId,
                            boolean processInStreamDefinition) {
        if (streamId == null) {
            streamId = streamReferenceId;
        }
        if (streamId == null) {
            int occurrences = 0;
            String inputStreamName = "";
            for (QueryEventSource queryEvent : queryEventSourceList) {
                AbstractDefinition definition;
                if (processInStreamDefinition) {
                    definition = queryEvent.getInDefinition();
                } else {
                    definition = queryEvent.getOutDefinition();
                }
                for (Attribute attribute : definition.getAttributeList()) {
                    if (this.getAttributeName().equals(attribute.getName())) {
                        occurrences++;
                        inputStreamName = queryEvent.getSourceId();
                        break;
                    }
                }
            }
            if (occurrences == 1) {
                streamId = inputStreamName;
            } else if (occurrences > 1) {
                throw new MalformedAttributeException("Attribute \"" + getAttributeName() + "\" exists in more than one inputstream.Specify the input stream");
            } else if (occurrences == 0) {
                if (null != queryEventSourceList && queryEventSourceList.size() == 1) {
                    throw new MalformedAttributeException("Attribute \"" + getAttributeName() + "\" does not exist in " + queryEventSourceList.get(0).getSourceId());
                } else {
                    throw new MalformedAttributeException("Attribute \"" + getAttributeName() + "\" does not exist");
                }
            }

        }

        boolean isValid = false;
        for (QueryEventSource queryEvent : queryEventSourceList) {
            if (queryEvent.getSourceId().equals(streamId) || queryEvent.getReferenceSourceId().equals(streamId)) {
                AbstractDefinition definition;
                if (processInStreamDefinition) {
                    definition = queryEvent.getInDefinition();
                } else {
                    definition = queryEvent.getOutDefinition();
                }
                for (Attribute attribute : definition.getAttributeList()) {
                    if (this.getAttributeName().equals(attribute.getName())) {
                        isValid = true;
                        break;
                    }
                }
                break;
            }
        }
        if (!isValid) {
            throw new MalformedAttributeException("Attribute \"" + getAttributeName() + "\" does not exist in " + streamId);
        }
    }

    @Override
    public String toString() {
        return "Variable {" +
                "streamId='" + streamId + '\'' +
                ", position=" + position +
                ", attributeName=" + attributeName +
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

        Variable variable = (Variable) o;

        if (position != variable.position) {
            return false;
        }
        if (!attributeName.equals(variable.attributeName)) {
            return false;
        }
        if (streamId != null ? !streamId.equals(variable.streamId) : variable.streamId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = streamId != null ? streamId.hashCode() : 0;
        result = 31 * result + position;
        result = 31 * result + attributeName.hashCode();
        return result;
    }

    @Override
    public Set<String> getDependencySet() {
        Set<String> dependencySet = new HashSet<String>();
        if (streamId != null && !streamId.isEmpty()) {
            dependencySet.add(streamId);
        }
        return dependencySet;
    }
}
