/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.composer.service.workspace.langserver.util.filters;

import org.ballerinalang.composer.service.workspace.langserver.SymbolInfo;
import org.ballerinalang.composer.service.workspace.suggetions.SuggestionsFilterDataModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Interface for filtering the symbols
 */
public interface SymbolFilter {
    /**
     * Filters the symbolInfo from the list based on a particular filter criteria
     * @param dataModel - Suggestion filter data model
     * @param symbols - Symbol info list
     * @param properties - Additional Parameters Map
     * @return {@link ArrayList}
     */
    List filterItems(SuggestionsFilterDataModel dataModel,
                                        ArrayList<SymbolInfo> symbols, HashMap<String, Object> properties);
}
