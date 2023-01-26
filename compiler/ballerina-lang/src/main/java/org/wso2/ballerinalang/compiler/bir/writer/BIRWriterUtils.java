/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.ballerinalang.compiler.bir.writer;

import io.ballerina.tools.diagnostics.Location;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.AnnotationAttachmentSymbol;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationAttachmentSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangConstantValue;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Common functions used in BIR writers.
 *
 * @since 2.0.0
 */
public class BIRWriterUtils {

    public static void writePosition(Location pos, ByteBuf buf, ConstantPool cp) {
        int sLine = Integer.MIN_VALUE;
        int eLine = Integer.MIN_VALUE;
        int sCol = Integer.MIN_VALUE;
        int eCol = Integer.MIN_VALUE;
        String sourceFileName = "";
        if (pos != null) {
            sLine = pos.lineRange().startLine().line();
            eLine = pos.lineRange().endLine().line();
            sCol = pos.lineRange().startLine().offset();
            eCol = pos.lineRange().endLine().offset();
            if (pos.lineRange().filePath() != null) {
                sourceFileName = pos.lineRange().filePath();
            }
        }
        buf.writeInt(addStringCPEntry(sourceFileName, cp));
        buf.writeInt(sLine);
        buf.writeInt(sCol);
        buf.writeInt(eLine);
        buf.writeInt(eCol);
    }

    public static int addStringCPEntry(String value, ConstantPool cp) {
        return cp.addCPEntry(new CPEntry.StringCPEntry(value));
    }

    public static int addPkgCPEntry(PackageID packageID, ConstantPool cp) {
        int orgCPIndex = addStringCPEntry(packageID.orgName.getValue(), cp);
        int pkgNameCPIndex = addStringCPEntry(packageID.pkgName.getValue(), cp);
        int moduleNameCPIndex = addStringCPEntry(packageID.name.getValue(), cp);
        int versionCPIndex = addStringCPEntry(packageID.version.getValue(), cp);
        return cp.addCPEntry(new CPEntry.PackageCPEntry(orgCPIndex, pkgNameCPIndex, moduleNameCPIndex, versionCPIndex));
    }

    public static void writeConstValue(ConstantPool cp, ByteBuf buf, BIRNode.ConstValue constValue) {
        writeConstValue(cp, buf, constValue.value, constValue.type);
    }

    public static void writeConstValue(ConstantPool cp, ByteBuf buf, Object value, BType type) {
        switch (Types.getReferredType(type).tag) {
            case TypeTags.INT:
            case TypeTags.SIGNED32_INT:
            case TypeTags.SIGNED16_INT:
            case TypeTags.SIGNED8_INT:
            case TypeTags.UNSIGNED32_INT:
            case TypeTags.UNSIGNED16_INT:
            case TypeTags.UNSIGNED8_INT:
                buf.writeInt(addIntCPEntry((Long) value, cp));
                break;
            case TypeTags.BYTE:
                int byteValue = ((Number) value).intValue();
                buf.writeInt(addByteCPEntry(byteValue, cp));
                break;
            case TypeTags.FLOAT:
                // TODO:Remove the instanceof check by converting the float literal instance in Semantic analysis phase
                double doubleVal = value instanceof String ? Double.parseDouble((String) value)
                        : (Double) value;
                buf.writeInt(addFloatCPEntry(doubleVal, cp));
                break;
            case TypeTags.STRING:
            case TypeTags.CHAR_STRING:
            case TypeTags.DECIMAL:
                buf.writeInt(addStringCPEntry((String) value, cp));
                break;
            case TypeTags.BOOLEAN:
                buf.writeBoolean((Boolean) value);
                break;
            case TypeTags.NIL:
                break;
            case TypeTags.RECORD:
                Map<String, BIRNode.ConstValue> mapConstVal = (Map<String, BIRNode.ConstValue>) value;
                buf.writeInt(mapConstVal.size());
                mapConstVal.forEach((key, fieldValue) -> {
                    buf.writeInt(addStringCPEntry(key, cp));
                    writeType(cp, buf, fieldValue.type);
                    writeConstValue(cp, buf, fieldValue);
                });
                break;
            case TypeTags.TUPLE:
                BIRNode.ConstValue[] tupleConstVal = (BIRNode.ConstValue[]) value;
                buf.writeInt(tupleConstVal.length);
                for (BIRNode.ConstValue memValue : tupleConstVal) {
                    writeType(cp, buf, memValue.type);
                    writeConstValue(cp, buf, memValue);
                }
                break;
            case TypeTags.INTERSECTION:
                BType effectiveType = ((BIntersectionType) type).effectiveType;
                writeConstValue(cp, buf, new BIRNode.ConstValue(value, effectiveType));
                break;
            default:
                // TODO support for other types
                throw new UnsupportedOperationException(
                        "finite type value is not supported for type: " + type);

        }
    }

    public static int addIntCPEntry(long value, ConstantPool cp) {
        return cp.addCPEntry(new CPEntry.IntegerCPEntry(value));
    }

    public static int addFloatCPEntry(double value, ConstantPool cp) {
        return cp.addCPEntry(new CPEntry.FloatCPEntry(value));
    }


    public static int addByteCPEntry(int value, ConstantPool cp) {
        return cp.addCPEntry(new CPEntry.ByteCPEntry(value));
    }

    public static void writeType(ConstantPool cp, ByteBuf buf, BType type) {
        buf.writeInt(cp.addShapeCPEntry(type));
    }

    public static void writeAnnotAttachments(ConstantPool cp, ByteBuf buff,
                                             List<BIRNode.BIRAnnotationAttachment> annotAttachments) {
        ByteBuf annotBuf = Unpooled.buffer();
        annotBuf.writeInt(annotAttachments.size());
        for (BIRNode.BIRAnnotationAttachment annotAttachment : annotAttachments) {
            writeAnnotAttachment(cp, annotBuf, annotAttachment);
        }
        int length = annotBuf.nioBuffer().limit();
        buff.writeLong(length);
        buff.writeBytes(annotBuf.nioBuffer().array(), 0, length);
    }

    public static void writeAnnotAttachment(ConstantPool cp, ByteBuf annotBuf,
                                            BIRNode.BIRAnnotationAttachment annotAttachment) {
        // Write module information of the annotation attachment
        annotBuf.writeInt(BIRWriterUtils.addPkgCPEntry(annotAttachment.annotPkgId, cp));
        // Write position
        writePosition(annotAttachment.pos, annotBuf, cp);
        annotBuf.writeInt(addStringCPEntry(annotAttachment.annotTagRef.value, cp));

        if (!(annotAttachment instanceof BIRNode.BIRConstAnnotationAttachment)) {
            annotBuf.writeBoolean(false);
            return;
        }

        annotBuf.writeBoolean(true);
        BIRNode.ConstValue constValue = ((BIRNode.BIRConstAnnotationAttachment) annotAttachment).annotValue;
        writeType(cp, annotBuf, constValue.type);
        writeConstValue(cp, annotBuf, constValue);
    }

    public static List<BIRNode.BIRAnnotationAttachment> getBIRAnnotAttachments(
            List<? extends AnnotationAttachmentSymbol> astAnnotAttachments) {
        List<BIRNode.BIRAnnotationAttachment> annotationAttachments = new ArrayList<>(astAnnotAttachments.size());
        for (AnnotationAttachmentSymbol annotationAttachmentSymbol : astAnnotAttachments) {
            annotationAttachments.add(createBIRAnnotationAttachment(
                    (BAnnotationAttachmentSymbol) annotationAttachmentSymbol));
        }
        return annotationAttachments;
    }

    public static BIRNode.BIRAnnotationAttachment createBIRAnnotationAttachment(
            BAnnotationAttachmentSymbol annotAttachmentSymbol) {
        Location pos = annotAttachmentSymbol.pos;
        PackageID annotPkgID = annotAttachmentSymbol.annotPkgID;
        Name annotTag = annotAttachmentSymbol.annotTag;

        if (!annotAttachmentSymbol.isConstAnnotation()) {
            return new BIRNode.BIRAnnotationAttachment(pos, annotPkgID, annotTag);
        }

        BLangConstantValue attachmentValue =
                ((BAnnotationAttachmentSymbol.BConstAnnotationAttachmentSymbol) annotAttachmentSymbol)
                        .attachmentValueSymbol.value;
        return new BIRNode.BIRConstAnnotationAttachment(pos, annotPkgID, annotTag, getBIRConstantVal(attachmentValue));
    }

    public static BIRNode.ConstValue getBIRConstantVal(BLangConstantValue constValue) {
        int tag = constValue.type.tag;
        if (tag == TypeTags.INTERSECTION) {
            constValue.type = ((BIntersectionType) constValue.type).effectiveType;
            tag = constValue.type.tag;
        }

        if (tag == TypeTags.RECORD) {
            Map<String, BIRNode.ConstValue> mapConstVal = new HashMap<>();
            ((Map<String, BLangConstantValue>) constValue.value)
                    .forEach((key, value) -> mapConstVal.put(key, getBIRConstantVal(value)));
            return new BIRNode.ConstValue(mapConstVal, ((BRecordType) constValue.type).getIntersectionType().get());
        }

        if (tag == TypeTags.TUPLE) {
            List<BLangConstantValue> constantValueList = (List<BLangConstantValue>) constValue.value;
            BIRNode.ConstValue[] tupleConstVal = new BIRNode.ConstValue[constantValueList.size()];
            for (int exprIndex = 0; exprIndex < constantValueList.size(); exprIndex++) {
                tupleConstVal[exprIndex] = getBIRConstantVal(constantValueList.get(exprIndex));
            }
            return new BIRNode.ConstValue(tupleConstVal, ((BTupleType) constValue.type).getIntersectionType().get());
        }

        return new BIRNode.ConstValue(constValue.value, constValue.type);
    }
}
