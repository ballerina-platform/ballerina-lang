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

package org.wso2.siddhi.core.table.holder;

import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.query.api.expression.condition.Compare;

import java.util.Collection;
import java.util.Set;

/**
 * Interface for an EventHolder which keep events indexed for faster access.
 */
public interface IndexedEventHolder extends EventHolder {

    boolean isAttributeIndexed(String attribute);

    boolean isAttributeIndexed(int position);

    Collection<StreamEvent> getAllEvents();

    Collection<StreamEvent> findEvents(String attribute, Compare.Operator operator, Object value);

    void deleteAll();

    void deleteAll(Collection<StreamEvent> storeEventSet);

    void delete(String attribute, Compare.Operator operator, Object value);

    boolean containsEventSet(String attribute, Compare.Operator operator, Object value);

    void overwrite(StreamEvent streamEvent);

    Set<Object> getAllPrimaryKeyValues();

    PrimaryKeyReferenceHolder[] getPrimaryKeyReferenceHolders();

    boolean isMultiPrimaryKeyAttribute(String attributeName);
}
