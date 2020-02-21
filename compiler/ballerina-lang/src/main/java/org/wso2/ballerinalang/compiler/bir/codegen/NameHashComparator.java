package org.wso2.ballerinalang.compiler.bir.codegen;

import org.wso2.ballerinalang.compiler.semantics.model.types.NamedNode;

import java.util.Comparator;

public class NameHashComparator implements Comparator<NamedNode> {

    @Override
    public int compare(NamedNode o1, NamedNode o2) {
        return Integer.compare(o1.getName().value.hashCode(), o2.getName().value.hashCode());
    }
}
