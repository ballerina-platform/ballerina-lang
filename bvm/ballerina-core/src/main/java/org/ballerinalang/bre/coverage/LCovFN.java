package org.ballerinalang.bre.coverage;

public class LCovFN {

    private int funcStartLineNum;

    private String functionName;

    public LCovFN(int funcStartLineNum, String functionName) {
        this.funcStartLineNum = funcStartLineNum;
        this.functionName = functionName;
    }

    public int getFuncStartLineNum() {
        return funcStartLineNum;
    }

    public void setFuncStartLineNum(int funcStartLineNum) {
        this.funcStartLineNum = funcStartLineNum;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }
}
