package org.ballerinalang.langserver.index;

import java.nio.file.Path;

/**
 * Index Interface for Ballerina Language Server.
 */
public interface LSIndex {
    /**
     * Load the index from a dump index database.
     * 
     * @param indexDumpPath     Path to the Index Dump
     * @return {@link Boolean}  Whether the index loading is successful or not
     */
    boolean initFromIndexDump(String indexDumpPath);

    /**
     * Load the index database schema from the disk.
     *
     * @return {@link Boolean}  Whether the index schema loading is successful or not
     */
    boolean loadIndexSchema();

    /**
     * Re-Index the Language server index.
     *
     * @return {@link Boolean}  Whether the re-indexing process is success or not
     */
    boolean reIndex();

    /**
     * Carryout the Language Server Index.
     *
     * @return {@link Boolean}  Whether the indexing process is success or not
     */
    boolean doIndex();

    /**
     * Save the current in-memory index to a given file location.
     *
     * @param path                  File path to save the index dump
     * @return {@link Boolean}      Whether the save process is success or not      
     */
    boolean saveIndexDump(Path path);
}