/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package io.ballerina.runtime.api.values;

/**
 * <p>
 * Represents an XML qualified name in ballerina.
 * </p>
 * 
 * @since 1.1.0
 */
public interface BXMLQName extends BValue {

    /**
     * Create attribute map with an XML.
     *
     * @param localName Local part of the qualified name
     * @param uri Namespace URI
     * @param prefix Namespace prefix
     */

    /**
     * Returns local part of the qualified name.
     *
     * @return local part string of the qualified name
     */
    String getLocalName();

    /**
     * Set the local part of the qualified name.
     *
     * @param localName local part to be set.
     */
    void setLocalName(String localName);

    /**
     * Returns the namespace URI.
     *
     * @return namespace URI
     */
    String getUri();

    /**
     * Set the namespace URI of the qualified name.
     *
     * @param uri namespace URI to be set
     */
    public void setUri(String uri);

    /**
     * Returns the namespace prefix of the qualified name.
     *
     * @return namespace prefix
     */
    public String getPrefix();

    /**
     * Set the namespace prefix of the qualified name.
     *
     * @param prefix namespace prefix
     */
    public void setPrefix(String prefix);
}
