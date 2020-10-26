package org.wso2.ballerinalang.compiler.bir.codegen;

import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.AsyncDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.ScheduleFunctionInfo;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_FUNCTION_POINTER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUTURE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SCHEDULER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SCHEDULE_FUNCTION_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_CLASS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_METADATA;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE;

/**
 * Util methods for the method gen classes.
 */
public class JvmMethodGenUtils {
    static final String FRAMES = "frames";
    static final String INIT_FUNCTION_SUFFIX = "<init>";

    static boolean hasInitFunction(BIRNode.BIRPackage pkg) {
        for (BIRNode.BIRFunction func : pkg.functions) {
            if (func != null && isModuleInitFunction(pkg, func)) {
                return true;
            }
        }
        return false;
    }

    static boolean isModuleInitFunction(BIRNode.BIRPackage module, BIRNode.BIRFunction func) {
        return func.name.value.equals(calculateModuleInitFuncName(packageToModuleId(module)));
    }

    private static String calculateModuleInitFuncName(PackageID id) {
        return JvmMethodGen.calculateModuleSpecialFuncName(id, INIT_FUNCTION_SUFFIX);
    }

    static PackageID packageToModuleId(BIRNode.BIRPackage mod) {
        return new PackageID(mod.org, mod.name, mod.version);
    }

    static void genArgs(MethodVisitor mv, int schedulerVarIndex) {
        mv.visitVarInsn(ALOAD, schedulerVarIndex);
        mv.visitIntInsn(BIPUSH, 1);
        mv.visitTypeInsn(ANEWARRAY, OBJECT);
    }

    static void submitToScheduler(MethodVisitor mv, String moduleClassName,
                                   String workerName, AsyncDataCollector asyncDataCollector) {
        String metaDataVarName = JvmCodeGenUtil.getStrandMetadataVarName("main");
        asyncDataCollector.getStrandMetadata().putIfAbsent(metaDataVarName, new ScheduleFunctionInfo("main"));
        mv.visitLdcInsn(workerName);
        mv.visitFieldInsn(GETSTATIC, moduleClassName, metaDataVarName, String.format("L%s;", STRAND_METADATA));
        mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, SCHEDULE_FUNCTION_METHOD,
                           String.format("([L%s;L%s;L%s;L%s;L%s;L%s;)L%s;", OBJECT, B_FUNCTION_POINTER, STRAND_CLASS,
                                         TYPE, STRING_VALUE, STRAND_METADATA, FUTURE_VALUE), false);
    }

    static void visitReturn(MethodVisitor mv) {
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private JvmMethodGenUtils() {
    }
}
