package org.ballerinalang.net.grpc.builder.components;

import static org.ballerinalang.net.grpc.builder.BalGeneratorConstants.NEW_LINE_CHARACTER;

/**
 * Class that responsible of generating struct data type at .bal stub
 */
public class StructBuilder {
    private String[] attributesNameArr;
    private String[] attributesTypeArr;
    private String name;
    private String attributeList;
    
    public StructBuilder(String[] attributesNameArr, String[] attributesTypeArr, String name) {
        this.attributesNameArr = attributesNameArr;
        this.attributesTypeArr = attributesTypeArr;
        this.name = name;
        
    }
    
    private void buildAttributes() {
        
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < attributesNameArr.length; i++) {
            str.append(String.format(NEW_LINE_CHARACTER + "%s %s;", attributesTypeArr[i], attributesNameArr[i]));
        }
        attributeList = str.toString();
    }
    
    public String buildStructs() {
        buildAttributes();
        return String.format(NEW_LINE_CHARACTER +
                "struct %s {" + NEW_LINE_CHARACTER +
                "%s" + NEW_LINE_CHARACTER +
                "}" + NEW_LINE_CHARACTER, name, attributeList);
    }
}
