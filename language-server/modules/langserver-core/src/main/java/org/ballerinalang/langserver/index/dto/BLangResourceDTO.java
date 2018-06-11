package org.ballerinalang.langserver.index.dto;

/**
 * Created by nadeeshaan on 6/9/18.
 */
public class BLangResourceDTO {
    
    private int serviceId;
    
    private String name;

    public BLangResourceDTO(int serviceId, String name) {
        this.serviceId = serviceId;
        this.name = name;
    }

    public int getServiceId() {
        return serviceId;
    }

    public String getName() {
        return name;
    }
}
