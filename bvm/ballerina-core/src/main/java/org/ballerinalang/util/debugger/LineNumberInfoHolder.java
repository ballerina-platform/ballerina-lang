package org.ballerinalang.util.debugger;

import org.ballerinalang.util.codegen.LineNumberInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfo;
import org.ballerinalang.util.codegen.attributes.LineNumberTableAttributeInfo;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class LineNumberInfoHolder {

    private Map<String, PackageLineNumberInfo> packageInfoMap = new HashMap<>();

    /**
     * Process and build information required for debugging the package.
     *
     * @param packageInfos   To extract relevant information.
     */
    public void processPkgInfo(PackageInfo[] packageInfos) {
        for(PackageInfo packageInfo : packageInfos) {
            PackageLineNumberInfo packageLineNumberInfo = new PackageLineNumberInfo(packageInfo.getInstructionCount());

            LineNumberTableAttributeInfo lineNumberTableAttributeInfo = (LineNumberTableAttributeInfo) packageInfo
                    .getAttributeInfo(AttributeInfo.Kind.LINE_NUMBER_TABLE_ATTRIBUTE);

            List<LineNumberInfo> lineNumberInfos = lineNumberTableAttributeInfo.getLineNumberInfoList().stream().sorted(
                    Comparator.comparing(LineNumberInfo::getIp)).collect(Collectors.toList());

            LineNumberInfo currentLineNoInfo = null;
            for (LineNumberInfo lineNoInfo : lineNumberInfos) {
                if (currentLineNoInfo == null) {
                    currentLineNoInfo = lineNoInfo;
                    continue;
                }
                packageLineNumberInfo.populateLineNumbers(currentLineNoInfo.getIp(), lineNoInfo.getIp(), currentLineNoInfo);
                currentLineNoInfo = lineNoInfo;
            }
            if (currentLineNoInfo != null) {
                packageLineNumberInfo.populateLineNumbers(currentLineNoInfo.getIp(),
                        packageInfo.getInstructionCount(), currentLineNoInfo);
            }
            packageInfoMap.put(packageInfo.getPkgPath(), packageLineNumberInfo);
        }
    }

    public Map<String, PackageLineNumberInfo> getPackageInfoMap() {
        return packageInfoMap;
    }
}
