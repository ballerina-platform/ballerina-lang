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

import io.netty.buffer.ByteBuf;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

/**
 * Common functions used in BIR writers.
 *
 * @since 2.0.0
 */
public class BIRWriterUtils {

    public static void writePosition(DiagnosticPos pos, ByteBuf buf, ConstantPool cp) {
        int sLine = Integer.MIN_VALUE;
        int eLine = Integer.MIN_VALUE;
        int sCol = Integer.MIN_VALUE;
        int eCol = Integer.MIN_VALUE;
        String sourceFileName = "";
        if (pos != null) {
            sLine = pos.sLine;
            eLine = pos.eLine;
            sCol = pos.sCol;
            eCol = pos.eCol;
            if (pos.src != null) {
                sourceFileName = pos.src.cUnitName;
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

    public static int addPkgCPEntry(String orgName, String name, String version, ConstantPool cp) {
        int orgCPIndex = addStringCPEntry(orgName, cp);
        int nameCPIndex = addStringCPEntry(name, cp);
        int versionCPIndex = addStringCPEntry(version, cp);
        return cp.addCPEntry(new CPEntry.PackageCPEntry(orgCPIndex, nameCPIndex, versionCPIndex));
    }

    public static int addPkgCPEntry(BIRNode.BIRPackage birPackage, ConstantPool cp) {
        return addPkgCPEntry(birPackage.org.value, birPackage.name.value, birPackage.version.value, cp);
    }

    public static int addPkgCPEntry(PackageID packageID, ConstantPool cp) {
        return addPkgCPEntry(packageID.orgName.value, packageID.name.value, packageID.version.value, cp);
    }
}
