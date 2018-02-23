package org.ballerinalang.net.grpc.builder;

import java.util.ArrayList;
import java.util.List;

/**
 * todo.
 */
public class ProtoDescriptor {

    private String name;
    private List protoDependencies;

    public ProtoDescriptor(String name) {

        this.name = name;
    }

    /**
     * todo.
     * @param dependency
     */
    public void addDependency(Object dependency) {

        if (protoDependencies == null) {
            protoDependencies = new ArrayList();
        }
        protoDependencies.add(dependency);
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public List getProtoDependencies() {

        return protoDependencies;
    }

    public void setProtoDependencies(ArrayList dependencySet) {

        this.protoDependencies = dependencySet;
    }
}
