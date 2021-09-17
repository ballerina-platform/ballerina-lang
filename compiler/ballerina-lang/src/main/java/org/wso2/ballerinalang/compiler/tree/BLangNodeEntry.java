/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.tree;

import org.ballerinalang.model.tree.NodeEntry;

/**
 * Represents AST node entries, which are not part of the AST.
 *
 * @since 2.0.0
 */
public abstract class BLangNodeEntry implements NodeEntry {

    public abstract <T> void accept(BLangNodeAnalyzer<T> analyzer, T props);

    public abstract <T, R> R apply(BLangNodeTransformer<T, R> modifier, T props);
}
