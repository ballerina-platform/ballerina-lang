package org.ballerinalang.mime.util;

import org.ballerinalang.nativeimpl.io.channels.FileIOChannel;

public class EntityBodyReader {
    private boolean isStream;
    private EntityBodyStream entityBodyChannel;
    private FileIOChannel fileIOChannel;

    EntityBodyReader(EntityBodyStream entityBodyChannel, boolean isStream) {
        this.entityBodyChannel = entityBodyChannel;
        this.isStream = isStream;
    }

    EntityBodyReader(FileIOChannel fileIOChannel, boolean isStream) {
        this.fileIOChannel = fileIOChannel;
        this.isStream = isStream;
    }

    public boolean isStream() {
        return isStream;
    }

    public EntityBodyStream getEntityBodyStream() {
        return entityBodyChannel;
    }

    public FileIOChannel getFileIOChannel() {
        return fileIOChannel;
    }
}
