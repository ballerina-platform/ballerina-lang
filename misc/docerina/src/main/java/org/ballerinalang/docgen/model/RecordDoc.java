/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.docgen.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Documentable node for Structs.
 */
public class RecordDoc extends Documentable {
    public final List<Field> fields;
    public final boolean isRecord;
    /**
     * Constructor.
     * @param name struct name.
     * @param description description.
     * @param children children if any.
     * @param fields struct fields.
     */
    public RecordDoc(String name, String description, ArrayList<Documentable> children, List<Field> fields) {
        super(name, "fw-record", description, children);
        this.fields = fields;
        isRecord = true;
    }
}
