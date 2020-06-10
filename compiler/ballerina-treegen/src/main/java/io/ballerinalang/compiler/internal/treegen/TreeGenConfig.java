/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerinalang.compiler.internal.treegen;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * The {@code TreeGenConfig} represents a set of config properties loaded from a config file.
 *
 * @since 1.3.0
 */
public class TreeGenConfig {

    public static final String SYNTAX_TREE_DESCRIPTOR_KEY = "syntax.tree.descriptor";

    public static final String INTERNAL_NODE_OUTPUT_DIR_KEY = "internal.node.output.dir";
    public static final String INTERNAL_NODE_PACKAGE_KEY = "internal.node.package";
    public static final String INTERNAL_NODE_TEMPLATE_KEY = "internal.node.template";
    public static final String INTERNAL_NODE_FACTORY_TEMPLATE_KEY = "internal.node.factory.template";
    public static final String INTERNAL_NODE_VISITOR_TEMPLATE_KEY = "internal.node.visitor.template";
    public static final String INTERNAL_NODE_TRANSFORMER_TEMPLATE_KEY = "internal.node.transformer.template";
    public static final String INTERNAL_TREE_MODIFIER_TEMPLATE_KEY = "internal.tree.modifier.template";

    public static final String EXTERNAL_NODE_OUTPUT_DIR_KEY = "external.node.output.dir";
    public static final String EXTERNAL_NODE_TEMPLATE_KEY = "external.node.template";
    public static final String EXTERNAL_NODE_VISITOR_TEMPLATE_KEY = "external.node.visitor.template";
    public static final String EXTERNAL_NODE_TRANSFORMER_TEMPLATE_KEY = "external.node.transformer.template";
    public static final String EXTERNAL_TREE_MODIFIER_TEMPLATE_KEY = "external.tree.modifier.template";
    public static final String EXTERNAL_NODE_FACTORY_TEMPLATE_KEY = "external.node.factory.template";
    public static final String EXTERNAL_NODE_PACKAGE_KEY = "external.node.package";

    private static final String TREE_GEN_CONFIG_PROPERTIES = "treegen_config.properties";

    private final Properties props;
    private static TreeGenConfig instance = new TreeGenConfig(loadConfig());

    static TreeGenConfig getInstance() {
        return instance;
    }

    private TreeGenConfig(Properties props) {
        this.props = props;
    }

    public String getOrThrow(String key) {
        if (Objects.isNull(key)) {
            throw new TreeGenException("The TreeGenConfig key must not be null");
        }

        String value = this.props.getProperty(key);
        if (Objects.isNull(value)) {
            throw new TreeGenException("The value of TreeGenConfig key '" + key + "' is null");
        }
        return value;
    }

    private static Properties loadConfig() {
        Properties props = new Properties();
        try (InputStream inputStream = TreeGenConfig.class.getClassLoader().getResourceAsStream(
                TREE_GEN_CONFIG_PROPERTIES)) {
            props.load(inputStream);
        } catch (IOException e) {
            throw new TreeGenException("Failed to load " + TREE_GEN_CONFIG_PROPERTIES + ". Reason: " +
                    e.getMessage(), e);
        }
        return props;
    }
}
