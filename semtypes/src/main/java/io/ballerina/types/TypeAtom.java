/*
 *  Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.types;

/**
 * Represent a TypeAtom.
 *
 * @param index      index of the type atom. This is unique within a given {@code Env}. {@code RecAtom}'s that refer to
 *                   this type atom will also have the same index.
 * @param atomicType atomic type representing the actual type represented by this atom.
 * @since 2201.8.0
 */
public record TypeAtom(int index, AtomicType atomicType) implements Atom {

    // Note: Whenever creating a 'TypeAtom', its 'atomicType' needs to be added to the 'Env.atomTable'
    public static TypeAtom createTypeAtom(int index, AtomicType atomicType) {
        assert index >= 0;
        return new TypeAtom(index, atomicType);
    }

    @Override
    public int hashCode() {
        return this.getIdentifier().hashCode();
    }

    @Override
    public Kind kind() {
        return atomicType.atomKind();
    }
}
