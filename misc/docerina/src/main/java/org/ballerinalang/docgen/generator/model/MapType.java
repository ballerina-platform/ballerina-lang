package org.ballerinalang.docgen.generator.model;

import com.google.gson.annotations.Expose;

public class MapType extends Construct {
    @Expose
    public Type mapParameterType;

    public MapType(String name, String description, boolean isDeprecated, Type mapParameterType) {
        super(name, description, isDeprecated);
        this.mapParameterType = mapParameterType;
    }
}
