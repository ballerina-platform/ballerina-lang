/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.ballerinalang.plugins.idea;

import com.intellij.core.CoreASTFactory;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.tree.IElementType;
import org.ballerinalang.plugins.idea.psi.BallerinaIdentifier;
import org.ballerinalang.plugins.idea.psi.BallerinaTypes;
import org.jetbrains.annotations.NotNull;

/**
 * Responsible for creating leaf elements in the PSI tree.
 */
public class BallerinaASTFactory extends CoreASTFactory {

    @NotNull
    @Override
    public CompositeElement createComposite(IElementType type) {
        return super.createComposite(type);
    }

    /**
     * Create a Leaf node for the given element type.
     */
    @NotNull
    @Override
    public LeafElement createLeaf(@NotNull IElementType type, @NotNull CharSequence text) {
        if (type == BallerinaTypes.IDENTIFIER) {
            return new BallerinaIdentifier(type, text);
        } else if (type == BallerinaTypes.NEW) {
            return new BallerinaIdentifier(type, text);
        }
        return super.createLeaf(type, text);
    }
}
