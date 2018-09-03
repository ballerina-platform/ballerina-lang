package org.ballerinalang.bir.parser.writer.binary;

import org.ballerinalang.bir.model.IrBasicBlock;
import org.ballerinalang.bir.model.IrFunction;
import org.ballerinalang.bir.model.op.Op;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Writes IrPackage to bir binary reparations.
 */
public class BinaryWriter {

    private static final byte[] BIR_MAGIC = {(byte) 0xba, (byte) 0x10, (byte) 0xc0, (byte) 0xde};
    private static final byte BIR_VERSION = 1;


    private final ByteBuffer out;
    private final OpWriter opWriter;

    public BinaryWriter(ByteBuffer out) {
        this.out = out;
        this.opWriter = new OpWriter(out);
    }

    public void write(BIRNode.BIRPackage pkg) {
        out.put(BIR_MAGIC);
        out.put(BIR_VERSION);

        write(pkg.pakageId);

        //TODO: write non-func symbols

        // Func name table
        List<IrFunction> funcs = pkg.functions;
        int numFunc = funcs.size();
        out.putShort((short) numFunc);
        int[] offsetSlots = new int[numFunc];
        for (int i = 0; i < numFunc; i++) {
            IrFunction func = funcs.get(i);
            write(func.name);
            offsetSlots[i] = out.position();
            out.putInt((byte) 0);
        }

        // Func content
        int[] funcLens = new int[numFunc];
        for (int i = 0; i < numFunc; i++) {
            IrFunction func = funcs.get(i);
            funcLens[i] = write(func);
        }

        // Go back and fill the func table offsets
        for (int i = 0; i < numFunc; i++) {
            out.putInt(offsetSlots[i], funcLens[i]);
        }
    }

    private int write(IrFunction func) {
        int pos = out.position();
        List<IrBasicBlock> bbs = func.bbs;
        out.putShort((byte) bbs.size());
        for (IrBasicBlock bb : bbs) {
            write(bb);
        }
        return pos;
    }

    private void write(IrBasicBlock bb) {
        List<Op> ops = bb.ops;
        out.put((byte) ops.size());
        for (Op op : ops) {
            op.accept(opWriter);
        }
    }

    private void write(String name) {
        byte[] bytes = name.getBytes(StandardCharsets.UTF_8);
        out.putShort((short) bytes.length);
        out.put(bytes);
    }

}

