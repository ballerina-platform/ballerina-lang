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
 * Represent the BDD atom.
 *
 * @since 2201.8.0
 */
public interface Atom {

    /**
     * Get the unique index of the atom.
     */
    int index();

    /**
     * Get the kind of the atom.
     */
    Kind kind();

    /**
     * This method returns a unique identifier for an Atom.
     * The identifier is a combination of the atom's index and kind.
     *
     * @return AtomIdentifier - a record containing the index and kind of the atom.
     */
    default AtomIdentifier getIdentifier() {
        return new AtomIdentifier(index(), kind());
    }

    record AtomIdentifier(int index, Kind kind) {
    }

    enum Kind {
        LIST_ATOM,
        FUNCTION_ATOM,
        MAPPING_ATOM,
        CELL_ATOM,
        XML_ATOM,
        DISTINCT_ATOM
    }
}
