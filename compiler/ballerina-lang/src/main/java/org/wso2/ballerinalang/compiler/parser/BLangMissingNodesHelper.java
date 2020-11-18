/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.ballerinalang.compiler.parser;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.runtime.util.BLangConstants.UNDERSCORE;

/**
 * {@link BLangMissingNodesHelper} contains utility methods to handle missing nodes
 * in the syntax tree.
 *
 * @since 2.0.0
 */
public class BLangMissingNodesHelper {

    private Map<PackageID, Integer> missingIdentifierCount;

    private static final String MISSING_NODE_PREFIX = "$missingNode$";

    private static final CompilerContext.Key<BLangMissingNodesHelper> MISSING_NODES_HELPER_KEY =
            new CompilerContext.Key<>();

    private BLangMissingNodesHelper(CompilerContext context) {
        context.put(MISSING_NODES_HELPER_KEY, this);
        missingIdentifierCount = new HashMap<>();
    }

    public static BLangMissingNodesHelper getInstance(CompilerContext context) {
        BLangMissingNodesHelper helper = context.get(MISSING_NODES_HELPER_KEY);
        if (helper == null) {
            helper = new BLangMissingNodesHelper(context);
        }
        return helper;
    }

    public String getNextMissingNodeName(PackageID packageID) {
        Integer nextValue = Optional.ofNullable(missingIdentifierCount.get(packageID)).orElse(0);
        missingIdentifierCount.put(packageID, nextValue + 1);
        return MISSING_NODE_PREFIX + UNDERSCORE + nextValue;
    }
    
    public boolean isMissingNode(Name nodeName) {
        return isMissingNode(nodeName.value);
    }
    
    public boolean isMissingNode(String nodeName) {
        return nodeName.startsWith(MISSING_NODE_PREFIX);
    }
}
