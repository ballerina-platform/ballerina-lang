/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.model.symbols;

import java.util.List;

/**
 * Provides APIs for adding and getting annotations from an annotatable symbol.
 *
 * @since 2.0.0
 */
public interface Annotatable {

    /**
     * Adds the specified annotation attachment symbol to the annotatable symbol.
     *
     * @param symbol The symbol of the annotation attached
     */
    void addAnnotation(AnnotationAttachmentSymbol symbol);

    /**
     * Returns a list of the annotations attached to this symbol.
     *
     * @return A list of annotation attachment symbols
     */
    List<? extends AnnotationAttachmentSymbol> getAnnotations();
}
