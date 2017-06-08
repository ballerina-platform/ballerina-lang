package org.ballerinalang.bre.bvm;

import org.ballerinalang.bre.Context;
import org.ballerinalang.util.codegen.ProgramFile;

/**
 * Created by rajith on 6/8/17.
 */
public class DebuggerExecutor implements Runnable {
    private ProgramFile programFile;
    private Context bContext;

    public DebuggerExecutor(ProgramFile programFile, Context bContext) {
        this.programFile = programFile;
        this.bContext = bContext;
    }

    @Override
    public void run() {
        BLangVM bLangVM = new BLangVM(programFile);
        bContext.getDebugInfoHolder().waitTillDebuggeeResponds();
        bLangVM.run(bContext);
    }
}
