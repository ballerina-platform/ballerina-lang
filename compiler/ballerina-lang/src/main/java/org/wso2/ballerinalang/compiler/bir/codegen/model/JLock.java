/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerinalang.compiler.bir.codegen.model;

import org.objectweb.asm.Label;

/**
 * This class holds JVM constructs required for handle Ballerina locks.
 *
 * @since 2201.11.0
 */
public class JLock {

    public final Label startLabel;
    public final Label endLabel;
    public final Label handlerLabel;

    public JLock() {
        this.startLabel = new Label();
        this.endLabel = new Label();
        this.handlerLabel = new Label();
    }
}
