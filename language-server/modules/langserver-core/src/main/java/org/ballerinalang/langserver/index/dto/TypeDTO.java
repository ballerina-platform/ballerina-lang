package org.ballerinalang.langserver.index.dto;

import java.util.List;

/**
 * Created by nadeeshaan on 6/9/18.
 */
public class TypeDTO {
    
    private PackageIDDTO packageIDDTO;
    
    private String name;
    
    private List<TypeDTO> memberTypes;

    public TypeDTO(PackageIDDTO packageIDDTO, String name, List<TypeDTO> memberTypes) {
        this.packageIDDTO = packageIDDTO;
        this.name = name;
        this.memberTypes = memberTypes;
    }

    public String getName() {
        return name;
    }

    public List<TypeDTO> getMemberTypes() {
        return memberTypes;
    }

    public PackageIDDTO getPackageIDDTO() {
        return packageIDDTO;
    }
}
