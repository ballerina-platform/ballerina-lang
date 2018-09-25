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

package org.ballerinalang.auth.ldap.nativeimpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ballerinalang.auth.ldap.CommonLDAPConfiguration;
import org.ballerinalang.auth.ldap.LDAPConnectionContext;
import org.ballerinalang.auth.ldap.LDAPConstants;
import org.ballerinalang.auth.ldap.UserStoreException;
import org.ballerinalang.auth.ldap.util.LDAPUtils;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.naming.InvalidNameException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

/**
 * Provides the scopes of a given user.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "auth",
        functionName = "LDAPAuthStoreProvider.getScopesOfUser",
        args = {@Argument(name = "username", type = TypeKind.STRING)
        },
        returnType = {@ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.STRING)},
        isPublic = true)
public class GetLDAPScopesOfUser extends BlockingNativeCallableUnit {

    private static final Log LOG = LogFactory.getLog(GetLDAPScopesOfUser.class);
    private LDAPConnectionContext connectionSource;
    private CommonLDAPConfiguration ldapConfiguration;

    @Override
    public void execute(Context context) {

        try {
            BMap<String, BValue> authStore = ((BMap<String, BValue>) context.getRefArgument(0));
            connectionSource = (LDAPConnectionContext) authStore.getNativeData(LDAPConstants.LDAP_CONNECTION_SOURCE);
            ldapConfiguration = (CommonLDAPConfiguration) authStore.getNativeData(LDAPConstants.LDAP_CONFIGURATION);

            String userName = context.getStringArgument(0);
            String[] externalRoles = doGetGroupsListOfUser(userName, "*", ldapConfiguration);
            context.setReturnValues(new BStringArray(externalRoles));
        } catch (UserStoreException e) {
            context.setReturnValues(BLangVMErrors.createError(context, e.getMessage()));
        }

    }

    private String[] doGetGroupsListOfUser(String userName, String filter,
                                           CommonLDAPConfiguration ldapAuthConfig) throws UserStoreException {

        // Get the effective search base
        String searchBase = ldapAuthConfig.getGroupSearchBase();
        return getLDAPGroupsListOfUser(userName, searchBase, ldapAuthConfig);
    }

    private String[] getLDAPGroupsListOfUser(String userName, String searchBase,
                                             CommonLDAPConfiguration ldapAuthConfig) throws UserStoreException {

        if (userName == null) {
            throw new BallerinaException("userName value is null.");
        }

        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        // Load normal roles with the user
        String searchFilter = ldapAuthConfig.getGroupNameListFilter();
        String roleNameProperty = ldapAuthConfig.getGroupNameAttribute();
        String membershipProperty = ldapAuthConfig.getMembershipAttribute();
        String nameInSpace = this.getNameInSpaceForUserName(userName, ldapConfiguration);

        if (membershipProperty == null || membershipProperty.length() < 1) {
            throw new BallerinaException("Please set membershipAttribute in configuration");
        }

        String membershipValue;
        if (nameInSpace != null) {
            try {
                LdapName ldn = new LdapName(nameInSpace);
                if (LDAPConstants.MEMBER_UID.equals(ldapAuthConfig.getMembershipAttribute())) {
                    // membership value of posixGroup is not DN of the user
                    List rdns = ldn.getRdns();
                    membershipValue = ((Rdn) rdns.get(rdns.size() - 1)).getValue().toString();
                } else {
                    membershipValue = escapeLdapNameForFilter(ldn);
                }
            } catch (InvalidNameException e) {
                LOG.error("Error while creating LDAP name from: " + nameInSpace);
                throw new UserStoreException("Invalid naming exception for : " + nameInSpace, e);
            }
        } else {
            return new String[0];
        }

        searchFilter = "(&" + searchFilter + "(" + membershipProperty + "=" + membershipValue + "))";
        String returnedAtts[] = {roleNameProperty};
        searchCtls.setReturningAttributes(returnedAtts);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Reading roles with the membershipProperty Property: " + membershipProperty);
        }

        List<String> list = this.getListOfNames(searchBase, searchFilter, searchCtls,
                roleNameProperty, false);
        String[] result = list.toArray(new String[list.size()]);

        if (result != null) {
            for (String rolename : result) {
                LOG.debug("Found role: " + rolename);
            }
        }
        return result;
    }

    private List<String> getListOfNames(String searchBases, String searchFilter,
                                        SearchControls searchCtls, String property, boolean appendDn)
            throws UserStoreException {

        List<String> names = new ArrayList<String>();
        DirContext dirContext = null;
        NamingEnumeration<SearchResult> answer = null;

        if (LOG.isDebugEnabled()) {
            LOG.debug("Result for searchBase: " + searchBases + " searchFilter: " + searchFilter +
                    " property:" + property + " appendDN: " + appendDn);
        }

        try {
            dirContext = connectionSource.getContext();

            // handle multiple search bases
            String[] searchBaseArray = searchBases.split("#");
            for (String searchBase : searchBaseArray) {

                try {
                    answer = dirContext.search(LDAPUtils.escapeDNForSearch(searchBase), searchFilter, searchCtls);

                    while (answer.hasMoreElements()) {
                        SearchResult sr = answer.next();
                        if (sr.getAttributes() != null) {
                            Attribute attr = sr.getAttributes().get(property);
                            if (attr != null) {
                                for (Enumeration vals = attr.getAll(); vals.hasMoreElements(); ) {
                                    String name = (String) vals.nextElement();
                                    if (LOG.isDebugEnabled()) {
                                        LOG.debug("Found user: " + name);
                                    }
                                    names.add(name);
                                }
                            }
                        }
                    }
                } catch (NamingException e) {
                    // ignore
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(e);
                    }
                }

                if (LOG.isDebugEnabled()) {
                    for (String name : names) {
                        LOG.debug("Result  :  " + name);
                    }
                }

            }
        } finally {
            LDAPUtils.closeNamingEnumeration(answer);
            LDAPUtils.closeContext(dirContext);
        }
        return names;
    }

    /**
     * Takes the corresponding name for a given username from LDAP.
     *
     * @param userName Given username
     * @return Associated name for the given username
     * @throws UserStoreException if there is any exception occurs during the process
     */
    private String getNameInSpaceForUserName(String userName, CommonLDAPConfiguration ldapConfiguration)
            throws UserStoreException {
        return LDAPUtils.getNameInSpaceForUsernameFromLDAP(userName, ldapConfiguration, this.connectionSource);
    }

    /**
     * This method escapes the special characters in a LdapName according to the ldap filter escaping standards.
     *
     * @param ldn LDAP name
     * @return A String which special characters are escaped
     */
    private String escapeLdapNameForFilter(LdapName ldn) {

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
    private String escapeSpecialCharactersForFilterWithStarAsRegex(String filter) {

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
