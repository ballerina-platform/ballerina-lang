package io.ballerina.projects;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.util.HashSet;

/**
 * Remove unused functions and replace with null.
 *
 */
public class DeadFunctionVisitor extends ClassVisitor {
    private final HashSet<String> deadFunctionNames;

    public DeadFunctionVisitor(int api, ClassWriter cw,HashSet<String> deadFunctionNames) {
        super(api, cw);
        this.deadFunctionNames = deadFunctionNames;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor;
        if (!name.contains("init") && !deadFunctionNames.contains(name)) {
            methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        } else {
            methodVisitor = null;
        }
        return methodVisitor;
    }
}