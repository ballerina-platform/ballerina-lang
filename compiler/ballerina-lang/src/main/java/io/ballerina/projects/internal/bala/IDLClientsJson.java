/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package io.ballerina.projects.internal.bala;

import com.google.gson.annotations.SerializedName;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents the JSON model of IDL client entries.
 *
 * @since 2201.3.0
 */
public class IDLClientsJson {

    private static final String IDL_CLIENTS = "idlClients";
    @SerializedName(IDL_CLIENTS) private List<IDLClientEntry> idlClients;

    private IDLClientsJson(List<IDLClientEntry> idlClients) {
        this.idlClients = idlClients;
    }

    static class IDLClientEntry {
        PackageID sourcePkgId;
        List<SourceFileInfo> sourceFileInfo;

        IDLClientEntry(PackageID sourcePkgId, List<SourceFileInfo> sourceFileInfo) {
            this.sourcePkgId = sourcePkgId;
            this.sourceFileInfo = sourceFileInfo;
        }
    }

    static class PackageID {
        String org;
        String packageName;
        String moduleNamePart;
        String version;

        public PackageID(String org, String packageName, String moduleNamePart, String version) {
            this.org = org;
            this.packageName = packageName;
            this.moduleNamePart = moduleNamePart;
            this.version = version;
        }
    }

    static class SourceFileInfo {
        String srcFile;
        List<LineRangeInfo> lineRangeInfo;

        public SourceFileInfo(String srcFile, List<LineRangeInfo> lineRangeInfo) {
            this.srcFile = srcFile;
            this.lineRangeInfo = lineRangeInfo;
        }
    }

    static class LineRangeInfo {
        LineRange lineRange;
        PackageID clientId;

        public LineRangeInfo (LineRange lineRange, PackageID clientId) {
            this.lineRange = lineRange;
            this.clientId = clientId;
        }
    }

    static class LineRange {
        LinePosition startLine;
        LinePosition endLine;

        public LineRange(int startLine, int startOffset, int endLine, int endOffset) {
            this.startLine = new LinePosition(startLine, startOffset);
            this.endLine = new LinePosition(endLine, endOffset);
        }
    }

    static class LinePosition {
        int line;
        int offset;

        public LinePosition(int line, int offset) {
            this.line = line;
            this.offset = offset;
        }
    }

    public static IDLClientsJson from(Map<org.ballerinalang.model.elements.PackageID,
            Map<String, Map<io.ballerina.tools.text.LineRange,
                    Optional<org.ballerinalang.model.elements.PackageID>>>> idlClientMap) {
        List<IDLClientEntry> idlClientsJsonList = new ArrayList<>();
        for (Map.Entry<org.ballerinalang.model.elements.PackageID,
                Map<String, Map<io.ballerina.tools.text.LineRange,
                        Optional<org.ballerinalang.model.elements.PackageID>>>> sourceEntry : idlClientMap.entrySet()) {
            // Add source module
            PackageID sourcePkgJson = getPackageIDJson(sourceEntry.getKey());
            List<SourceFileInfo> sourceFileInfos = new ArrayList<>();
            IDLClientEntry idlClientEntry = new IDLClientEntry(sourcePkgJson, sourceFileInfos);
            idlClientsJsonList.add(idlClientEntry);

            // Add source files for added modules
            for (Map.Entry<String, Map<io.ballerina.tools.text.LineRange,
                    Optional<org.ballerinalang.model.elements.PackageID>>> sourceFileEntry :
                    sourceEntry.getValue().entrySet()) {
                List<LineRangeInfo> lineRangeInfos = new ArrayList<>();
                sourceFileInfos.add(new SourceFileInfo(sourceFileEntry.getKey(), lineRangeInfos));

                // Add line range and client module info
                for (Map.Entry<io.ballerina.tools.text.LineRange,
                        Optional<org.ballerinalang.model.elements.PackageID>> lineRangeEntry :
                        sourceFileEntry.getValue().entrySet()) {
                    PackageID clientPkg = getPackageIDJson(lineRangeEntry.getValue().orElseThrow());
                    LineRange lineRangeJson = new LineRange(lineRangeEntry.getKey().startLine().line(),
                            lineRangeEntry.getKey().startLine().offset(),
                            lineRangeEntry.getKey().endLine().line(),
                            lineRangeEntry.getKey().endLine().offset());
                    lineRangeInfos.add(new LineRangeInfo(lineRangeJson, clientPkg));
                }
            }
        }
        return new IDLClientsJson(idlClientsJsonList);
    }

    public static Map<org.ballerinalang.model.elements.PackageID, Map<String,
            Map<io.ballerina.tools.text.LineRange,
                    Optional<org.ballerinalang.model.elements.PackageID>>>> transformToMap (
                    IDLClientsJson idlClientsJson) {
        Map<org.ballerinalang.model.elements.PackageID, Map<String,
                Map<io.ballerina.tools.text.LineRange, Optional<org.ballerinalang.model.elements.PackageID>>>>
                idlClientMap = new HashMap<>();

        for (IDLClientEntry idlClientEntry : idlClientsJson.idlClients) {
            // Add source module to map
            PackageID sourcePkgId = idlClientEntry.sourcePkgId;
            org.ballerinalang.model.elements.PackageID pkgId = getPackageID(sourcePkgId);
            List<SourceFileInfo> sourceFileInfos = idlClientEntry.sourceFileInfo;
            Map<String, Map<io.ballerina.tools.text.LineRange,
                    Optional<org.ballerinalang.model.elements.PackageID>>> srcFileMap =
                    new HashMap<>();
            idlClientMap.put(pkgId, srcFileMap);

            // Add source file info
            for (SourceFileInfo sourceFileInfo : sourceFileInfos) {
                Map<io.ballerina.tools.text.LineRange, Optional<org.ballerinalang.model.elements.PackageID>>
                        lineRangeMap = new HashMap<>();
                srcFileMap.put(sourceFileInfo.srcFile, lineRangeMap);
                List<LineRangeInfo> lineRangeInfos = sourceFileInfo.lineRangeInfo;

                // Add line range info
                for (LineRangeInfo lineRangeInfo : lineRangeInfos) {
                    org.ballerinalang.model.elements.PackageID clientPkgId = getPackageID(lineRangeInfo.clientId);
                    lineRangeMap.put(
                            createLineRange(lineRangeInfo.lineRange, sourceFileInfo.srcFile), Optional.of(clientPkgId));
                }
            }
        }
        return idlClientMap;
    }

    private static PackageID getPackageIDJson(org.ballerinalang.model.elements.PackageID sourcePkg) {
        return new PackageID(sourcePkg.orgName.value, sourcePkg.pkgName.value, sourcePkg.name.value,
                sourcePkg.version.value);
    }

    private static org.ballerinalang.model.elements.PackageID getPackageID(PackageID sourcePkgId) {
        return new org.ballerinalang.model.elements.PackageID(new Name(sourcePkgId.org),
                new Name(sourcePkgId.packageName), new Name(sourcePkgId.moduleNamePart),
                new Name(sourcePkgId.version), null);
    }

    private static io.ballerina.tools.text.LineRange createLineRange(LineRange lineRange, String srcFileName) {
        io.ballerina.tools.text.LinePosition start = io.ballerina.tools.text.LinePosition.from(
                lineRange.startLine.line, lineRange.startLine.offset);
        io.ballerina.tools.text.LinePosition end = io.ballerina.tools.text.LinePosition.from(
                lineRange.endLine.line, lineRange.endLine.offset);
        return io.ballerina.tools.text.LineRange.from(srcFileName, start, end);
    }
}
