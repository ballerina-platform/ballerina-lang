/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.ballerinalang.compiler.bir.codegen;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IF_ICMPGE;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V9;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CLASS_FILE_SUFFIX;

/**
 * Generates the launcher agent class of the executable jar. When the jar is run with {@code java -jar}, the JVM runs
 * this agent before loading the main class, so an unsupported Java runtime can be reported with a clear error
 * instead of an {@link UnsupportedClassVersionError} thrown while linking the generated classes.
 * <p>
 * The class is compiled to Java 9 bytecode, the first version that supports the {@code Launcher-Agent-Class}
 * manifest attribute, so that it can be loaded by older Java runtimes.
 *
 * @since 2201.14.2
 */
public final class JavaVersionCheckGen {

    public static final String LAUNCHER_AGENT_CLASS_NAME = "io.ballerina.launcher.JavaVersionCheck";
    public static final String LAUNCHER_AGENT_CLASS_FILE =
            LAUNCHER_AGENT_CLASS_NAME.replace('.', '/') + CLASS_FILE_SUFFIX;
    private static final int MIN_JAVA_VERSION = 25;

    private JavaVersionCheckGen() {
    }

    public static byte[] generateLauncherAgentClass() {
        String className = LAUNCHER_AGENT_CLASS_NAME.replace('.', '/');
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        cw.visit(V9, ACC_PUBLIC | ACC_FINAL | ACC_SUPER, className, null, "java/lang/Object", null);
        genAgentMainMethod(cw);
        cw.visitEnd();
        return cw.toByteArray();
    }

    private static void genAgentMainMethod(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "agentmain", "(Ljava/lang/String;)V", null, null);
        mv.visitCode();
        mv.visitLdcInsn("java.specification.version");
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "getProperty", "(Ljava/lang/String;)Ljava/lang/String;",
                false);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
        mv.visitIntInsn(BIPUSH, MIN_JAVA_VERSION);
        Label supported = new Label();
        mv.visitJumpInsn(IF_ICMPGE, supported);

        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
        mv.visitLdcInsn("error: this Ballerina program requires Java " + MIN_JAVA_VERSION + " or later to run.");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        mv.visitInsn(ICONST_1);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "exit", "(I)V", false);

        mv.visitLabel(supported);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
}
