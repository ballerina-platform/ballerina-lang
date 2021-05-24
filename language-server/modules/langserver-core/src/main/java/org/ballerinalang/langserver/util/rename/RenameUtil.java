/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.util.rename;

import io.ballerina.projects.Module;
import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.ReferencesContext;
import org.ballerinalang.langserver.exception.UserErrorException;
import org.ballerinalang.langserver.util.references.ReferencesUtil;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A utility to handle renaming related operations.
 * 
 * @since 2.0.0
 */
public class RenameUtil {

    private RenameUtil() {
    }

    /**
     * Process a rename request and returns the text edits required to be made to complete the rename.
     *
     * @param context Context
     * @param newName Assigned identifier after renaming
     * @return Text edits for that rename operation
     */
    public static Map<String, List<TextEdit>> rename(ReferencesContext context, String newName) {
        if (!CommonUtil.isValidIdentifier(newName)) {
            throw new UserErrorException("Invalid identifier provided");
        }

        Map<Module, List<Location>> locationMap = ReferencesUtil.getReferences(context);

        Map<String, List<TextEdit>> changes = new HashMap<>();
        locationMap.forEach((module, locations) ->
                locations.forEach(location -> {
                    String uri = ReferencesUtil.getUriFromLocation(module, location);
                    List<TextEdit> textEdits = changes.computeIfAbsent(uri, k -> new ArrayList<>());
                    textEdits.add(new TextEdit(ReferencesUtil.getRange(location), newName));
                }));
        return changes;
    }
}
