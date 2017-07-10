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
    List<SymbolInfo> filterItems(SuggestionsFilterDataModel dataModel,
                                        ArrayList<SymbolInfo> symbols, HashMap<String, Object> properties);
}
