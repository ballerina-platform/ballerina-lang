/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.ballerinalang.compiler.bir.codegen.bytecodeoptimizer;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;

import static org.objectweb.asm.Opcodes.ASM9;

/**
 * A visitor class used to visit fields and collect used class types.
 *
 * @since 2201.11.0
 */
public final class FieldNodeVisitor extends FieldVisitor {

    private final DependencyCollector collector;

    public FieldNodeVisitor(DependencyCollector collector) {
        super(ASM9);
        this.collector = collector;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        collector.addDesc(desc);
        return new AnnotationNodeVisitor(collector);
    }
}
