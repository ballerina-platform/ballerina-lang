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
 */

package org.ballerinalang.plugins.idea.codeinsight.recursivesearch;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import org.jetbrains.annotations.Nullable;

/**
 * Recursive reference search settings provider.
 */
@State(
        name = "BallerinaRecursiveReferenceSearch",
        storages = @Storage(file = "recursive.reference.search.xml")
)
public class BallerinaRecursiveReferenceSearchSettings implements
        PersistentStateComponent<BallerinaRecursiveReferenceSearchSettings> {

    @Attribute
    private boolean myUseRecursiveReferenceSearch = false;

    public static BallerinaRecursiveReferenceSearchSettings getInstance() {
        return ServiceManager.getService(BallerinaRecursiveReferenceSearchSettings.class);
    }

    @Nullable
    @Override
    public BallerinaRecursiveReferenceSearchSettings getState() {
        return this;
    }

    @Override
    public void loadState(BallerinaRecursiveReferenceSearchSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public boolean useRecursiveReferenceSearch() {
        return myUseRecursiveReferenceSearch;
    }

    public void setUseRecursiveReferenceSearch(boolean useRecursiveReferenceSearch) {
        myUseRecursiveReferenceSearch = useRecursiveReferenceSearch;
    }
}
