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
package org.ballerinalang.auth.ldap.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ballerinalang.auth.ldap.CommonLDAPConfiguration;
import org.ballerinalang.auth.ldap.LDAPConnectionContext;
import org.ballerinalang.auth.ldap.UserStoreException;

import javax.naming.CompositeName;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

/**
 * Utility class for LDAP related common operations.
 */
public class LDAPUtils {

    private static final Log LOG = LogFactory.getLog(LDAPUtils.class);

    /**
     * Checks whether a given string is null or empty after the trim.
     *
     * @param str String which needs to be check for null or empty
     * @return true if the string is null or empty else false
     */
    public static boolean isNullOrEmptyAfterTrim(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * This is to search user and retrieve ldap name directly from ldap.
     *
     * @param userName          Given username
     * @param ldapConfiguration LDAP user store configurations
     * @param connectionSource  LDAPConnectionContext
     * @return Associated name for the given username
     * @throws UserStoreException if there is any exception occurs during the process
     */
    public static String getNameInSpaceForUsernameFromLDAP(String userName, CommonLDAPConfiguration ldapConfiguration,
                                  LDAPConnectionContext connectionSource) throws UserStoreException {
        String userSearchFilter = ldapConfiguration.getUserNameSearchFilter();
        userSearchFilter = userSearchFilter.replace("?", LDAPUtils.escapeSpecialCharactersForFilter(userName));
        String searchBase = ldapConfiguration.getUserSearchBase();
        return LDAPUtils.getNameInSpaceForUserName(userName, searchBase, userSearchFilter, connectionSource);
    }

    /**
     * Searches the corresponding name for a given username from LDAP.
     *
     * @param userName         Given username
     * @param searchBase       LDAP search base
     * @param searchFilter     LDAP search filter
     * @param connectionSource LDAPConnectionContext
     * @return Associated name for the given username
     * @throws UserStoreException if there is any exception occurs during the process
     */
    public static String getNameInSpaceForUserName(String userName, String searchBase,
                          String searchFilter, LDAPConnectionContext connectionSource) throws UserStoreException {

        boolean debug = LOG.isDebugEnabled();

        if (userName == null) {
            throw new UserStoreException("userName value is null.");
        }

        String userDN = null;
        DirContext dirContext = null;
        NamingEnumeration<SearchResult> answer = null;
        try {
            dirContext = connectionSource.getContext();
            SearchControls searchCtls = new SearchControls();
            searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);

            if (LOG.isDebugEnabled()) {
                try {
                    LOG.debug("Searching for user with SearchFilter: " + searchFilter + " in SearchBase: "
                            + dirContext.getNameInNamespace());
                } catch (NamingException e) {
                    LOG.debug("Error while getting DN of search base", e);
                }
            }
            String[] searchBases = searchBase.split("#");
            for (String base : searchBases) {
                answer = dirContext.search(escapeDNForSearch(base), searchFilter, searchCtls);
                if (!(answer.hasMore())) {
                    continue;
                }
                SearchResult userObj = (SearchResult) answer.next();
                if (userObj != null) {
                    //no need to decode since , if decoded the whole string, can't be encoded again
                    //eg CN=Hello\,Ok=test\,test, OU=Industry
                    userDN = userObj.getNameInNamespace();
                    break;
                }
            }
            if (debug) {
                LOG.debug("Name in space for " + userName + " is " + userDN);
            }
        } catch (InvalidNameException e) {
            throw new UserStoreException(e);
        } catch (NamingException e) {
            throw new UserStoreException(e);
        } finally {
            LDAPUtils.closeNamingEnumeration(answer);
            LDAPUtils.closeContext(dirContext);
        }
        return userDN;
    }

    /**
     * This method performs the additional level escaping for ldap search. In ldap search / and " characters
     * have to be escaped again.
     *
     * @param dn DN
     * @return composite name
     * @throws InvalidNameException failed to build composite name
     */
    public static Name escapeDNForSearch(String dn) throws InvalidNameException {
        return new CompositeName().add(dn);
    }

    /**
     * Closes the directory context.
     *
     * @param dirContext directory context needs be closed
     * @throws UserStoreException if a naming exception is encountered
     */
    public static void closeContext(DirContext dirContext) throws UserStoreException {
        try {
            if (dirContext != null) {
                dirContext.close();
            }
        } catch (NamingException e) {
            String errorMessage = "Error in closing connection context.";
            LOG.error(errorMessage, e);
            throw new UserStoreException(e);
        }
    }

    /**
     * Closes the used NamingEnumerations to free up resources.
     *
     * @param namingEnumeration enumeration needs to be closed
     */
    public static void closeNamingEnumeration(NamingEnumeration<?> namingEnumeration) {
        if (namingEnumeration != null) {
            try {
                namingEnumeration.close();
            } catch (NamingException e) {
                String errorMessage = "Error in closing NamingEnumeration.";
                LOG.error(errorMessage, e);
            }
        }
    }

    /**
     * Escaping ldap search filter special characters in a string.
     *
     * @param dnPartial String to replace special characters of
     * @return String by replacing the special characters
     */
    public static String escapeSpecialCharactersForFilter(String dnPartial) {

        dnPartial = dnPartial.replace("\\*", "*");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dnPartial.length(); i++) {
            char currentChar = dnPartial.charAt(i);
            switch (currentChar) {
                case '\\':
                    sb.append("\\5c");
                    break;
                case '*':
                    sb.append("\\2a");
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
