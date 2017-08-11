/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.model.types;

import java.util.Objects;

/**
 * One edge in the Type Lattice.
 *
 * @since 0.8.0
 */
public class TypeEdge {
    private TypeVertex source, target;
    private boolean safe;
    private int opcode = -1;

    public TypeEdge(TypeVertex source, TypeVertex target, boolean safe, int opcode) {
        this.source = source;
        this.target = target;
        this.opcode = opcode;
        this.safe = safe;
    }

    public TypeVertex getSource() {
        return source;
    }

    public void setSource(TypeVertex source) {
        this.source = source;
    }

    public TypeVertex getTarget() {
        return target;
    }

    public void setTarget(TypeVertex target) {
        this.target = target;
    }

    public boolean isSafe() {
        return safe;
    }

    public int getOpcode() {
        return opcode;
    }

    /**
     * @return String A String representation of this Edge
     */
    public String toString() {
        return "(" + source.toString() + ", " + target.toString() + ")";
    }

    /**
     * @return int The hash code for this Edge
     */
    public int hashCode() {
        return Objects.hash(source.toString(), target.toString());
    }

    /**
     * @param other The Object to compare against this
     * @return ture iff other is an Edge with the same Vertices as this
     */
    public boolean equals(Object other) {
        if (!(other instanceof TypeEdge)) {
            return false;
        }

        TypeEdge e = (TypeEdge) other;
        return e.source.equals(this.source) && e.target.equals(this.target);
    }
}
