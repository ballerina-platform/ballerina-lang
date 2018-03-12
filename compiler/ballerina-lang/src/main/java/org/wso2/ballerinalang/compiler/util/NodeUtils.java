/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.util;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @since 0.94
 */
public class NodeUtils {

    /**
     * Return the {@code Name} from the give package name components.
     *
     * @param names        utility class which manages all the names used
     *                     in the Ballerina compiler.
     * @param pkgNameComps components of the given package name.
     * @return returns the name from the given package name components.
     */
    public static Name getName(Names names, List<BLangIdentifier> pkgNameComps) {
        String pkgName = String.join(".", pkgNameComps.stream()
                .map(id -> id.value)
                .collect(Collectors.toList()));
        return names.fromString(pkgName);
    }

    public static Name getName(String localname, String namespaceURI) {
        String qname = (namespaceURI == null ? "" : "{" + namespaceURI + "}") + localname;
        return new Name(qname);
    }

    public static PackageID getPackageID(Names names, BLangIdentifier orgNameNode,
                                         List<BLangIdentifier> pkgNameComps, BLangIdentifier versionNode) {
        List<Name> nameList = pkgNameComps.stream().map(names::fromIdNode).collect(Collectors.toList());
        Name orgName = null;
        if (orgNameNode != null) {
            orgName = names.fromIdNode(orgNameNode);
        }
        Name version = names.fromIdNode(versionNode);
        if (version == Names.EMPTY) {
            version = Names.DEFAULT_VERSION;
        }
        return new PackageID(orgName, nameList, version);
    }
}
