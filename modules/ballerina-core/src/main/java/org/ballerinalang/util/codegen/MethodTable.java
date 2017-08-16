package org.ballerinalang.util.codegen;

import org.ballerinalang.util.codegen.cpentries.ConstantPool;
import org.ballerinalang.util.codegen.cpentries.ConstantPoolEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chanaka on 8/16/17.
 */
public class MethodTable implements ConstantPool {

    protected Map<Integer, Integer> methodTableIndices = new HashMap<>();
    private List<ConstantPoolEntry> constPool = new ArrayList<>();

    @Override
    public int addCPEntry(ConstantPoolEntry cpEntry) {
        if (constPool.contains(cpEntry)) {
            return constPool.indexOf(cpEntry);
        }

        constPool.add(cpEntry);
        return constPool.size() - 1;
    }

    @Override
    public ConstantPoolEntry getCPEntry(int index) {
        return constPool.get(index);
    }

    @Override
    public int getCPEntryIndex(ConstantPoolEntry cpEntry) {
        return constPool.indexOf(cpEntry);
    }

    @Override
    public ConstantPoolEntry[] getConstPoolEntries() {
        return constPool.toArray(new ConstantPoolEntry[0]);
    }

    public Map<Integer, Integer> getMethodTableIndices() {
        return methodTableIndices;
    }

    public void setMethodTableIndices(Map<Integer, Integer> methodTable) {
        this.methodTableIndices = methodTable;
    }

    public void addMethodIndex(int methodNameCPIndex, int ip) {
        methodTableIndices.put(methodNameCPIndex, new Integer(ip));
    }
}
