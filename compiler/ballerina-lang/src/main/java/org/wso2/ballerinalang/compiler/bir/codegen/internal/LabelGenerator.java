/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerinalang.compiler.bir.codegen.internal;

import org.objectweb.asm.Label;

import java.util.HashMap;
import java.util.Map;

/**
 * JVM bytecode label generation class.
 *
 * @since 1.2.0
 */
public class LabelGenerator {

    private Map<String, Label> bbLabels = new HashMap<>();

    public Label getLabel(String labelKey) {

        Label result = this.bbLabels.get(labelKey);
        if (result != null) {
            return result;
        } else {
            Label label = new Label();
            this.bbLabels.put(labelKey, label);
            return label;
        }
    }

    public void putLabel(String labelKey, Label label) {
        this.bbLabels.put(labelKey, label);
    }
}
