/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.langserver.references.util;

import org.ballerinalang.langserver.LSServiceOperationContext;
import org.ballerinalang.langserver.common.constants.NodeContextKeys;
import org.ballerinalang.langserver.references.ReferencesTreeVisitor;
import org.eclipse.lsp4j.Location;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.util.List;

/**
 * Utility class for find all references functionality in language server.
 */
public class ReferenceUtil {
    /**
     * Get definition position for the given definition context.
     *
     * @param referencesContext   context of the references.
     * @param currentBLangPackage current package that is visiting.
     * @return position
     */
    public static List<Location> getReferences(LSServiceOperationContext referencesContext,
                                               BLangPackage currentBLangPackage) {
        currentBLangPackage.accept(new ReferencesTreeVisitor(referencesContext));
        return referencesContext.get(NodeContextKeys.REFERENCE_NODES_KEY);
    }
}
