/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.docgen.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Page created by traversing the bal package.
 */
public class Page {

    public final String description;
    public final Caption heading;
    public final List<Documentable> constructs;
    public final List<Link> links;
    public final List<Link> primitives;

    /**
     *
     * @param heading name of the bal package.
     * @param constructs constructs in the package.
     * @param links links to the other packages.
     */
    public Page(Caption heading, ArrayList<Documentable> constructs, List<Link> links) {
        this.heading = heading;
        this.constructs = constructs;
        this.links = links;
        this.description = null;
        this.primitives = new ArrayList<>();
    }

    /**
     * @param description description of the package.
     * @param heading    name of the bal package.
     * @param constructs constructs in the package.
     * @param links      links to the other packages.
     * @param primitives links to the primitives.
     */
    public Page(String description, Caption heading, ArrayList<Documentable> constructs, List<Link> links, List<Link>
            primitives) {
        this.description = description;
        this.heading = heading;
        this.constructs = constructs;
        this.links = links;
        this.primitives = primitives;
    }
}
