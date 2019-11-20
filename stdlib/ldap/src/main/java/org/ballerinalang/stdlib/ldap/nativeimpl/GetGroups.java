/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.ldap.nativeimpl;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.stdlib.ldap.CommonLdapConfiguration;
import org.ballerinalang.stdlib.ldap.LdapConstants;
import org.ballerinalang.stdlib.ldap.util.LdapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

/**
 * Provides the groups of a given user.
 *
 * @since 0.983.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "ldap",
        functionName = "getGroups", isPublic = true)
public class GetGroups {

    private static final Logger LOG = LoggerFactory.getLogger(GetGroups.class);

    public static Object getGroups(Strand strand, MapValue<?, ?> ldapConnection, String userName) {
        try {
            LdapUtils.setServiceName((String) ldapConnection.getNativeData(LdapConstants.ENDPOINT_INSTANCE_ID));
            DirContext ldapConnectionContext = (DirContext) ldapConnection.getNativeData(
                    LdapConstants.LDAP_CONNECTION_CONTEXT);
            CommonLdapConfiguration ldapConfiguration = (CommonLdapConfiguration) ldapConnection.getNativeData(
                    LdapConstants.LDAP_CONFIGURATION);
            String[] externalRoles = doGetGroupsListOfUser(userName, ldapConfiguration, ldapConnectionContext);
            return new ArrayValueImpl(externalRoles);
        } catch (NamingException | ErrorValue e) {
            return LdapUtils.createError(e.getMessage());
        } finally {
            LdapUtils.removeServiceName();
        }
    }

    private static String[] doGetGroupsListOfUser(String userName, CommonLdapConfiguration ldapAuthConfig,
                                                  DirContext ldapConnectionContext) throws NamingException {
        // Get the effective search base
        List<String> searchBase = ldapAuthConfig.getGroupSearchBase();
        return getLDAPGroupsListOfUser(userName, searchBase, ldapAuthConfig, ldapConnectionContext);
    }

    private static String[] getLDAPGroupsListOfUser(String userName, List<String> searchBase,
                                                    CommonLdapConfiguration ldapAuthConfig,
                                                    DirContext ldapConnectionContext) throws NamingException {
        if (userName == null) {
            throw BallerinaErrors.createError("UserName value is null.");
        }

        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        // Load normal roles with the user
        String searchFilter = ldapAuthConfig.getGroupNameListFilter();
        String roleNameProperty = ldapAuthConfig.getGroupNameAttribute();
        String membershipProperty = ldapAuthConfig.getMembershipAttribute();
        String nameInSpace = getNameInSpaceForUserName(userName, ldapAuthConfig, ldapConnectionContext);

        if (membershipProperty == null || membershipProperty.length() < 1) {
            throw BallerinaErrors.createError("MembershipAttribute not set in configuration.");
        }

        String membershipValue;
        if (nameInSpace != null) {
            LdapName ldn = new LdapName(nameInSpace);
            if (LdapConstants.MEMBER_UID.equals(ldapAuthConfig.getMembershipAttribute())) {
                // membership value of posixGroup is not DN of the user
                List rdns = ldn.getRdns();
                membershipValue = ((Rdn) rdns.get(rdns.size() - 1)).getValue().toString();
            } else {
                membershipValue = escapeLdapNameForFilter(ldn);
            }
        } else {
            return new String[0];
        }

        searchFilter = "(&" + searchFilter + "(" + membershipProperty + "=" + membershipValue + "))";
        String[] returnedAttributes = {roleNameProperty};
        searchControls.setReturningAttributes(returnedAttributes);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Reading roles with the membershipProperty Property: " + membershipProperty);
        }

        List<String> list = getListOfNames(searchBase, searchFilter, searchControls, roleNameProperty,
                ldapConnectionContext);
        return list.toArray(new String[list.size()]);
    }

    private static List<String> getListOfNames(List<String> searchBases, String searchFilter,
                                               SearchControls searchControls, String property,
                                               DirContext ldapConnectionContext) throws NamingException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Result for searchBase: " + searchBases + " searchFilter: " + searchFilter +
                    " property:" + property + " appendDN: false");
        }

        List<String> names = new ArrayList<>();
        NamingEnumeration<SearchResult> answer = null;
        try {
            // handle multiple search bases
            for (String searchBase : searchBases) {
                answer = ldapConnectionContext.search(LdapUtils.escapeDNForSearch(searchBase),
                        searchFilter, searchControls);
                while (answer.hasMoreElements()) {
                    SearchResult searchResult = answer.next();
                    if (searchResult.getAttributes() == null) {
                        continue;
                    }
                    Attribute attr = searchResult.getAttributes().get(property);
                    if (attr == null) {
                        continue;
                    }
                    for (Enumeration vals = attr.getAll(); vals.hasMoreElements(); ) {
                        String name = (String) vals.nextElement();
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Found user: " + name);
                        }
                        names.add(name);
                    }
                }

                if (LOG.isDebugEnabled()) {
                    for (String name : names) {
                        LOG.debug("Result  :  " + name);
                    }
                }
            }
        } finally {
            LdapUtils.closeNamingEnumeration(answer);
        }
        return names;
    }

    /**
     * Takes the corresponding name for a given username from LDAP.
     *
     * @param userName              Given username
     * @param ldapConfiguration     LDAP user store configurations
     * @param ldapConnectionContext connection context
     * @return Associated name for the given username
     */
    private static String getNameInSpaceForUserName(String userName, CommonLdapConfiguration ldapConfiguration,
                                                    DirContext ldapConnectionContext) throws NamingException {
        return LdapUtils.getNameInSpaceForUsernameFromLDAP(userName, ldapConfiguration, ldapConnectionContext);
    }

    /**
     * This method escapes the special characters in a LdapName according to the ldap filter escaping standards.
     *
     * @param ldn LDAP name
     * @return A String which special characters are escaped
     */
    private static String escapeLdapNameForFilter(LdapName ldn) {
        if (ldn == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Received null value to escape special characters. Returning null");
            }
            return null;
        }

        StringBuilder escapedDN = new StringBuilder();
        for (int i = ldn.size() - 1; i > -1; i--) { //escaping the rdns separately and re-constructing the DN
            escapedDN = escapedDN.append(escapeSpecialCharactersForFilterWithStarAsRegex(ldn.get(i)));
            if (i != 0) {
                escapedDN = escapedDN.append(",");
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Escaped DN value for filter : " + escapedDN.toString());
        }
        return escapedDN.toString();
    }

    /**
     * Escaping ldap search filter special characters in a string.
     *
     * @param filter LDAP search filter
     * @return A String which special characters are escaped
     */
    private static String escapeSpecialCharactersForFilterWithStarAsRegex(String filter) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < filter.length(); i++) {
            char currentChar = filter.charAt(i);
            switch (currentChar) {
                case '\\':
                    if (filter.charAt(i + 1) == '*') {
                        sb.append("\\2a");
                        i++;
                        break;
                    }
                    sb.append("\\5c");
                    break;
                case '(':
                    sb.append("\\28");
                    break;
                case ')':
                    sb.append("\\29");
                    break;
                case '\u0000':
                    sb.append("\\00");
                    break;
                default:
                    sb.append(currentChar);
            }
        }
        return sb.toString();
    }
}
