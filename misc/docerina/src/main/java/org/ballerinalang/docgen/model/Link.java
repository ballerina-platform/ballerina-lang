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

/**
 * Links of the packages available.
 */
public class Link {
    public final Caption caption;
    public final String href;
    public final boolean active;

    /**
     * Constructor.
     *
     * @param caption package name.
     * @param href    url.
     * @param active  if package is active/inactive.
     */
    public Link(Caption caption, String href, boolean active) {
        this.caption = caption;
        this.href = href;
        this.active = active;
    }
}
