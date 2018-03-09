/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.siddhi.core.util.collection.operator;

import org.ballerinalang.siddhi.core.event.ComplexEventChunk;
import org.ballerinalang.siddhi.core.event.state.StateEvent;
import org.ballerinalang.siddhi.core.event.stream.StreamEvent;
import org.ballerinalang.siddhi.core.table.InMemoryCompiledUpdateSet;
import org.ballerinalang.siddhi.core.table.holder.IndexedEventHolder;
import org.ballerinalang.siddhi.core.util.collection.AddingStreamEventExtractor;
import org.ballerinalang.siddhi.core.util.collection.executor.CollectionExecutor;

/**
 * Operator which is related to non-indexed In-memory table operations.
 */
public class OverwriteTableIndexOperator extends IndexOperator {


    public OverwriteTableIndexOperator(CollectionExecutor collectionExecutor, String queryName) {
        super(collectionExecutor, queryName);
    }

    @Override
    public ComplexEventChunk<StreamEvent> tryUpdate(ComplexEventChunk<StateEvent> updatingOrAddingEventChunk,
                                                    Object storeEvents,
                                                    InMemoryCompiledUpdateSet compiledUpdateSet,
                                                    AddingStreamEventExtractor addingStreamEventExtractor) {

        updatingOrAddingEventChunk.reset();
        while (updatingOrAddingEventChunk.hasNext()) {
            StateEvent overwritingOrAddingEvent = updatingOrAddingEventChunk.next();
            ((IndexedEventHolder) storeEvents).overwrite(addingStreamEventExtractor.getAddingStreamEvent
                    (overwritingOrAddingEvent));

        }
        return null;
    }

}
