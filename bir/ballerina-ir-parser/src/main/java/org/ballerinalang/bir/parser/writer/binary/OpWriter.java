package org.ballerinalang.bir.parser.writer.binary;

import org.ballerinalang.bir.model.OpVisitor;
import org.ballerinalang.bir.model.op.OpGoTo;
import org.ballerinalang.bir.model.op.OpReturn;

import java.nio.ByteBuffer;

/**
 * Writes statements to binary file.
 * Uses visitor patten.
 * returns each opcodes length.
 */
public class OpWriter implements OpVisitor {
    private ByteBuffer out;

    public OpWriter(ByteBuffer out) {
        this.out = out;
    }

    @Override
    public void accept(OpGoTo opGoTo) {
        out.put((byte) 4);
        out.put((byte) opGoTo.destination.index);
    }

    @Override
    public void accept(OpReturn opReturn) {
        out.put((byte) 5);
    }
}
