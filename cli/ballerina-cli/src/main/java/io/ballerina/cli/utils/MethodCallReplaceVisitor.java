package io.ballerina.cli.utils;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.lang.reflect.Method;

/**
 * Remove existing method call and replace it with a mock call.
 *
 * @since 2201.4.0
 */
public class MethodCallReplaceVisitor extends ClassVisitor {
    private final Method toFunc;
    public final Method fromFunc;

    private final int api;

    public MethodCallReplaceVisitor(int api, ClassWriter cw, Method toFunc, Method fromFunc) {
        super(api, cw);
        this.toFunc = toFunc;
        this.fromFunc = fromFunc;
        this.api = api;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return new MethodReplaceMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions),
                access, name, desc);
    }

    private final class MethodReplaceMethodVisitor extends GeneratorAdapter {

        public MethodReplaceMethodVisitor(
                MethodVisitor mv, int access, String name, String desc) {
            super(MethodCallReplaceVisitor.this.api , mv, access, name, desc);
        }

        @Override
        public void visitMethodInsn(
                int opcode, String owner, String name, String desc, boolean itf) {

            String fromFuncOwner = MethodCallReplaceVisitor.this
                    .fromFunc.getDeclaringClass().getName().replace(".", "/");
            String fromFuncName = MethodCallReplaceVisitor.this.fromFunc.getName();
            String fromFunDesc = Type.getMethodDescriptor(MethodCallReplaceVisitor.this.fromFunc);

            String toFuncOwner = MethodCallReplaceVisitor.this
                    .toFunc.getDeclaringClass().getName().replace(".", "/");
            String toFuncName = MethodCallReplaceVisitor.this.toFunc.getName();
            String toFunDesc = Type.getMethodDescriptor(MethodCallReplaceVisitor.this.toFunc);

            if (opcode == Opcodes.INVOKESTATIC && owner.equals(fromFuncOwner)
                    && name.equals(fromFuncName) && desc.equals(fromFunDesc)) {
                super.visitMethodInsn(Opcodes.INVOKESTATIC, toFuncOwner,
                        toFuncName, toFunDesc, false);
            } else {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
            }
        }
    }

}
