/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.util.codegen.attributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains metadata relevant to taint-analysis.
 */
public class TaintTableAttributeInfo implements AttributeInfo {

    // Index to a UTF8CPEntry
    private int attributeNameIndex;

    public int columnCount;
    public int rowCount;
    public Map<Integer, List<Boolean>> taintTable = new LinkedHashMap<>();

    public TaintTableAttributeInfo(int attributeNameIndex) {
        this.attributeNameIndex = attributeNameIndex;
    }

    @Override
    public Kind getKind() {
        return Kind.TAINT_TABLE;
    }

    @Override
    public int getAttributeNameIndex() {
        return attributeNameIndex;
    }
}
