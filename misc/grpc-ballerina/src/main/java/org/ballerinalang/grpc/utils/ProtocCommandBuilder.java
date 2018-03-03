package org.ballerinalang.grpc.utils;

import static org.ballerinalang.grpc.utils.BalGenerationConstants.DESC_PATH_PLACEHOLDER;
import static org.ballerinalang.grpc.utils.BalGenerationConstants.EXE_PATH_PLACEHOLDER;
import static org.ballerinalang.grpc.utils.BalGenerationConstants.NEW_LINE_CHARATER;
import static org.ballerinalang.grpc.utils.BalGenerationConstants.PROTO_FOLDER_PLACEHOLDER;
import static org.ballerinalang.grpc.utils.BalGenerationConstants.PROTO_PATH_PLACEHOLDER;
import static org.ballerinalang.grpc.utils.BalGenerationConstants.SPACE_CHARATER;

/**
 * This class is used to build the protoc compiler command to generate descriptor.
 */
public class ProtocCommandBuilder {
    private static final String COMMAND_PLACEHOLDER = EXE_PATH_PLACEHOLDER + SPACE_CHARATER + NEW_LINE_CHARATER +
            "--proto_path=" + PROTO_FOLDER_PLACEHOLDER + SPACE_CHARATER + NEW_LINE_CHARATER +
            PROTO_PATH_PLACEHOLDER + SPACE_CHARATER + NEW_LINE_CHARATER +
            "--descriptor_set_out=" + DESC_PATH_PLACEHOLDER;
    private String exePath;
    private String protoPath;
    private String protoFolderPath;
    private String descriptorSetOutPath;
    
    public ProtocCommandBuilder(String exePath, String protoPath, String protofolderPath, String descriptorSetOutPath) {
        this.exePath = exePath;
        this.protoPath = protoPath;
        this.descriptorSetOutPath = descriptorSetOutPath;
        this.protoFolderPath = protofolderPath;
    }
    
    public String build() {
        return COMMAND_PLACEHOLDER.replace(EXE_PATH_PLACEHOLDER, this.exePath)
                .replace(PROTO_PATH_PLACEHOLDER, this.protoPath)
                .replace(DESC_PATH_PLACEHOLDER, descriptorSetOutPath)
                .replace(PROTO_FOLDER_PLACEHOLDER, protoFolderPath);
    }
}
