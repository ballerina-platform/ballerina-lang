const path = require("path");
const TSDocgenPlugin = require("react-docgen-typescript-webpack-plugin");
module.exports = (baseConfig, env, config) => {
    baseConfig.module.rules.push({
        test: /\.(ts|tsx)$/,
        loader: require.resolve("awesome-typescript-loader")
    });
    baseConfig.module.rules.push({
        test: /\.(png|jpg|svg|cur|gif|eot|svg|ttf|woff|woff2)$/,
        loader: require.resolve("url-loader")
    });
    baseConfig.module.rules.push({
        test: /\.(css|scss)$/,
        loaders: ["style-loader", "css-loader", "sass-loader"],
        include: path.resolve(__dirname, "../")
    });
    baseConfig.plugins.push(new TSDocgenPlugin()); // optional
    baseConfig.resolve.extensions.push(".ts", ".tsx");
    return baseConfig;
};