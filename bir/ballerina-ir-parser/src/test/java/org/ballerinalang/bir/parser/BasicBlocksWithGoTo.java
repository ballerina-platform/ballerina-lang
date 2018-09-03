package org.ballerinalang.bir.parser;

import org.ballerinalang.bir.model.IrBasicBlock;
import org.ballerinalang.bir.model.IrPackage;
import org.ballerinalang.bir.model.op.OpGoTo;
import org.ballerinalang.bir.parser.writer.binary.BinaryWriter;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

public class BasicBlocksWithGoTo extends ParserTest {

    @Test(description = "parse empty package")
    public void testIrParse() throws Exception {
        IrPackage irPackage = parseBirWithSameName();
        List<IrBasicBlock> bbs = irPackage.functions.get(0).bbs;
        Assert.assertEquals(bbs.size(), 2);
        Assert.assertEquals(bbs.get(0).ops.size(), 1);
        Assert.assertEquals(bbs.get(0).ops.get(0).getClass(), OpGoTo.class);


        ByteBuffer out = ByteBuffer.allocate(1024);
        new BinaryWriter(out).write(irPackage);
        out.flip();
        FileChannel channel = new FileOutputStream(new File("/home/manu/sc/2017-07-24/myfunc.bir"), false).getChannel();
        channel.write(out);
    }

}
