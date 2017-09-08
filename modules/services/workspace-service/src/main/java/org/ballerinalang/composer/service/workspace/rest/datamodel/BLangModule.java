package org.ballerinalang.composer.service.workspace.rest.datamodel;

import com.fasterxml.jackson.core.util.VersionUtil;
import com.fasterxml.jackson.databind.module.SimpleModule;
//import org.ballerinalang.model.tree.FunctionNode;

/**
 * BLangModule
 */
public class BLangModule extends SimpleModule {

    private static final long serialVersionUID = 1L;

    private static final String NAME = "CustomIntervalModule";
    private static final VersionUtil VERSION_UTIL = new VersionUtil() {
    };

    public BLangModule() {
        super(NAME, VERSION_UTIL.version());
        //addSerializer(FunctionNode.class , new BLangSerializer());
        //addSerializer(Interval.class, new BLangSerializer());
        //addSerializer(Address.class, new AddSerializer());
    }
}
