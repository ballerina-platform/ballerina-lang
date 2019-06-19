/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.codelenses.providers;

import org.ballerinalang.langserver.codelenses.LSCodeLensesProvider;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownDocumentationLine;

import java.util.List;

/**
 * Abstract code lenses provider containing all utility functions.
 *
 * @since 0.990.3
 */
public abstract class AbstractCodeLensesProvider implements LSCodeLensesProvider {
    /**
     * To track codelens provider is enabled or not.
     */
    protected boolean isEnabled = true;

    /**
     * A uniquely identifiable name for the codelens provider.
     */
    protected final String name;

    public AbstractCodeLensesProvider(String name) {
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    /**
     * Calculate and returns topmost position of the annotations.
     *
     * @param annotationAttachments a list of {@link BLangAnnotationAttachment}
     * @param initialValue          initial position
     * @return calculated topmost position for the node
     */
    protected static int getTopMostLocOfAnnotations(List<BLangAnnotationAttachment> annotationAttachments,
                                                    int initialValue) {
        int topMost = initialValue;
        if (annotationAttachments != null) {
            for (BLangAnnotationAttachment attachment : annotationAttachments) {
                topMost = Math.min(attachment.pos.sLine - 1, topMost);
            }
        }
        return topMost;
    }

    /**
     * Calculate and returns topmost position of the documentations.
     *
     * @param docs         {@link BLangMarkdownDocumentation} markdown docs
     * @param initialValue initial position
     * @return calculated topmost position for the node
     */
    protected static int getTopMostLocOfDocs(BLangMarkdownDocumentation docs, int initialValue) {
        int topMost = initialValue;
        if (docs != null) {
            for (BLangMarkdownDocumentationLine line : docs.documentationLines) {
                topMost = Math.min(line.pos.sLine - 1, topMost);
            }
        }
        return topMost;
    }
}
