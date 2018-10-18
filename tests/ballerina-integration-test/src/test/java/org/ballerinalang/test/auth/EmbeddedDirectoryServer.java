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

package org.ballerinalang.test.auth;

import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.DirectoryService;
import org.apache.directory.server.core.entry.ServerEntry;
import org.apache.directory.server.core.partition.Partition;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmIndex;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.ldap.LdapServer;
import org.apache.directory.server.protocol.shared.store.LdifFileLoader;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;
import org.apache.directory.server.xdbm.Index;

import java.io.File;
import java.util.HashSet;

/**
 * Embedded Apache directory server implementation.
 */
public class EmbeddedDirectoryServer {

    // The directory service
    private DirectoryService service;
    private LdapServer ldapService;
    private File workDir;


    /**
     * Add a new partition to the server.
     *
     * @param partitionId The partition Id
     * @param partitionDn The partition DN
     * @return The newly added partition
     * @throws Exception If the partition can't be added
     */
    private Partition addPartition(String partitionId, String partitionDn) throws Exception {
        // Create a new partition named 'foo'.
        Partition partition = new JdbmPartition();
        partition.setId(partitionId);
        partition.setSuffix(partitionDn);
        service.addPartition(partition);

        return partition;
    }

    /**
     * Add a new set of index on the given attributes.
     *
     * @param partition The partition on which we want to add index
     * @param attrs The list of attributes to index
     */
    private void addIndex(Partition partition, String... attrs) {
        // Index some attributes on the apache partition
        HashSet<Index<?, ServerEntry>> indexedAttributes = new HashSet<Index<?, ServerEntry>>();

        for (String attribute : attrs) {
            indexedAttributes.add(new JdbmIndex<Object, ServerEntry>(attribute));
        }

        ((JdbmPartition) partition).setIndexedAttributes(indexedAttributes);
    }

    /**
     * Starts directory apache directory service.
     *
     * @param port directory service port
     * @throws Exception If server can't be started
     */
    public void startLdapServer(int port) throws Exception {

        workDir = new File(System.getProperty("java.io.tmpdir"), "TEMP_APACHEDS-" + System.currentTimeMillis());
        workDir.mkdirs();
        // Initialize the LDAP service
        ldapService = new LdapServer();
        ldapService.setTransports(new TcpTransport(port));

        service = new DefaultDirectoryService();
        ldapService.setDirectoryService(service);
        service.setWorkingDirectory(workDir);

        // Disable the ChangeLog system
        service.getChangeLog().setEnabled(false);
        service.setDenormalizeOpAttrsEnabled(true);

        // Create some new partitions named 'foo', 'bar' and 'apache'.
        Partition wso2Partition = addPartition("test", "dc=BALLERINA,dc=IO");

        // And start the service
        service.startup();

        //Load the LDIF file
        String ldif = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator +
                "auth"  + File.separator + "ldif").getAbsolutePath() + File.separator + "users-import.ldif";
        LdifFileLoader ldifLoader = new LdifFileLoader(service.getAdminSession(), ldif);
        ldifLoader.execute();

        ldapService.start();
    }

    /**
     * Stops directory apache directory service.
     */
    public void stopLdapService() {
        ldapService.stop();
        workDir.delete();
    }
}
