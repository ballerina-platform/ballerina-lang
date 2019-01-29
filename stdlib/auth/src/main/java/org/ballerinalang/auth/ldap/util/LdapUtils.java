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
import org.ballerinalang.auth.ldap.CommonLdapConfiguration;
import org.ballerinalang.auth.ldap.UserStoreException;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
 *
 * @since 0.983.0
 */
public class LdapUtils {

    private static final Log LOG = LogFactory.getLog(LdapUtils.class);
    private static final Pattern systemVariableIdentifierPattern = Pattern.compile("\\$\\{([^}]*)}");
    private static final ThreadLocal<String> socketFactoryName = new ThreadLocal<>();

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
     * @param dirContext  Directory naming context
     * @return Associated name for the given username
     * @throws UserStoreException if there is any exception occurs during the process
     * @throws NamingException if there is any exception occurs during the process
     */
    public static String getNameInSpaceForUsernameFromLDAP(String userName, CommonLdapConfiguration ldapConfiguration,
                                  DirContext dirContext) throws UserStoreException, NamingException {
        String userSearchFilter = ldapConfiguration.getUserNameSearchFilter();
        userSearchFilter = userSearchFilter.replace("?", LdapUtils.escapeSpecialCharactersForFilter(userName));
        String searchBase = ldapConfiguration.getUserSearchBase();
        return LdapUtils.getNameInSpaceForUserName(userName, searchBase, userSearchFilter, dirContext);
    }

    /**
     * Searches the corresponding name for a given username from LDAP.
     *
     * @param userName         Given username
     * @param searchBase       LDAP search base
     * @param searchFilter     LDAP search filter
     * @param dirContext Directory naming context
     * @return Associated name for the given username
     * @throws UserStoreException if there is any exception occurs during the process
     * @throws NamingException if there is any exception occurs during the process
     */
    public static String getNameInSpaceForUserName(String userName, String searchBase,
                          String searchFilter, DirContext dirContext) throws UserStoreException, NamingException {

        if (userName == null) {
            throw new UserStoreException("userName value is null.");
        }
        String userDN = null;
        NamingEnumeration<SearchResult> answer = null;
        try {
            SearchControls searchCtls = new SearchControls();
            searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            String[] searchBases = searchBase.split("#");
            for (String base : searchBases) {
                answer = dirContext.search(escapeDNForSearch(base), searchFilter, searchCtls);
                if (!(answer.hasMore())) {
                    continue;
                }
                SearchResult userObj = answer.next();
                if (userObj != null) {
                    //no need to decode since , if decoded the whole string, can't be encoded again
                    //eg CN=Hello\,Ok=test\,test, OU=Industry
                    userDN = userObj.getNameInNamespace();
                    break;
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Name in space for " + userName + " is " + userDN);
            }
        } finally {
            LdapUtils.closeNamingEnumeration(answer);
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
     * @throws NamingException if a naming exception is encountered
     */
    public static void closeContext(DirContext dirContext) throws NamingException {
        if (dirContext != null) {
            dirContext.close();
        }
    }

    /**
     * Closes the used NamingEnumerations to free up resources.
     *
     * @param namingEnumeration enumeration needs to be closed
     * @throws NamingException if a naming exception is encountered
     */
    public static void closeNamingEnumeration(NamingEnumeration<?> namingEnumeration) throws NamingException {
        if (namingEnumeration != null) {
            namingEnumeration.close();
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

    /**
     * Check whether provided url is ldaps or not.
     *
     * @param url LDAP server url
     * @return {@code true} if the url is an ldaps: url.
     */
    public static boolean isLdapsUrl(String url) {
        return (null != url) && (url.length() > 7) && url.substring(0, 8).equalsIgnoreCase("ldaps://");
    }

    /**
     * Replace system property holders in the property values.
     * e.g. Replace ${ballerina.home} with value of the ballerina.home system property.
     *
     * @param value string value to substitute
     * @return String substituted string
     */
    public static String substituteVariables(String value) {
        Matcher matcher = systemVariableIdentifierPattern.matcher(value);
        boolean found = matcher.find();
        if (!found) {
            return value;
        }
        StringBuffer sb = new StringBuffer();
        do {
            String sysPropKey = matcher.group(1);
            String sysPropValue = getSystemVariableValue(sysPropKey, null);
            if (sysPropValue == null || sysPropValue.length() == 0) {
                throw new RuntimeException("System property " + sysPropKey + " is not specified");
            }
            // Due to reported bug under CARBON-14746
            sysPropValue = sysPropValue.replace("\\", "\\\\");
            matcher.appendReplacement(sb, sysPropValue);
        } while (matcher.find());
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * A utility which allows reading variables from the environment or System properties.
     * If the variable in available in the environment as well as a System property, the System property takes
     * precedence.
     *
     * @param variableName System/environment variable name
     * @param defaultValue default value to be returned if the specified system variable is not specified.
     * @return value of the system/environment variable
     */
    private static String getSystemVariableValue(String variableName, String defaultValue) {
        String value;
        if (System.getProperty(variableName) != null) {
            value = System.getProperty(variableName);
        } else if (System.getenv(variableName) != null) {
            value = System.getenv(variableName);
        } else {
            value = defaultValue;
        }
        return value;
    }

    /**
     * Takes instance id of the service from the thread local.
     *
     * @return service instance id.
     */
    public static String getInstanceIdFromThreadLocal() {
        String result = socketFactoryName.get();
        if (result == null) {
            throw new BallerinaException("Cannot infer the ssl context related to the service");
        }
        return result;
    }

    public static void setServiceName(String serviceName) {
        socketFactoryName.set(serviceName);
    }

    public static void removeServiceName() {
        socketFactoryName.remove();
    }
}
