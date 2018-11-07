package org.ballerinalang.util.debugger;

import org.ballerinalang.util.codegen.LineNumberInfo;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Initializes and holds line number info for a module.
 * This includes line number info for a program file.
 *
 * @since 0.985
 *
 */
public class PackageLineNumberInfo {

    private LineNumberInfo[] ipLineNos;

    //key - fileName:ln, value - LineNumberInfo
    Map<String, LineNumberInfo> lineNumbers = new HashMap<>();

    public PackageLineNumberInfo(int instructionCount) {

        this.ipLineNos = new LineNumberInfo[instructionCount];
    }

    /**
     * Populates line number info for a module.
     *
     * @param beginIp beginning Ip for the line
     * @param endIp ending Ip for the line
     * @param lineNumberInfo line number info holder object
     */
    void populateLineNumbers(int beginIp, int endIp, LineNumberInfo lineNumberInfo) {

        addLineNumberInfo(beginIp, endIp, lineNumberInfo);
        String fileName = lineNumberInfo.getFileName();
        if (fileName.contains(File.separator)) {
            String[] pathArray = fileName.split(File.separatorChar == '\\' ? "\\\\" : File.separator);
            fileName = pathArray[pathArray.length - 1];
        }
        String fileNameAndNo = fileName + ":" + lineNumberInfo.getLineNumber();
        lineNumbers.put(fileNameAndNo, lineNumberInfo);
    }

    // populates array (index is same as Ip index in program file) of line number info
    private void addLineNumberInfo(int beginIp, int endIp, LineNumberInfo lineNumberInfo) {

        for (int i = beginIp; i < endIp; i++) {
            ipLineNos[i] = lineNumberInfo;
        }
        lineNumberInfo.setEndIp(endIp);
    }

    /**
     * Getter for the line number info for the given Ip.
     *
     * @param ip given Ip
     * @return line number info holder object
     */
    public LineNumberInfo getLineNumberInfo(int ip) {

        return ipLineNos[ip];
    }

    /**
     * Getter for the line number info map for the program file.
     *
     * @return line number info map for the program file
     */
    public Map<String, LineNumberInfo> getLineNumbers() {

        return lineNumbers;
    }
}
