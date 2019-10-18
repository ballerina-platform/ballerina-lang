const path = require('path');

module.exports = {
    entry: {
		"highlighter": './src/index.ts',
	},
    output: {
        filename: '[name].js',
        path: path.join(__dirname, 'lib'),
        library: 'ballerinaHighlighter',
        libraryTarget: 'umd'
    },
    module: {
        rules: [
            {
                test: /\.tsx?$/,
                loader: 'ts-loader',
                exclude: /node_modules/,
            },
            {
                test: /\.css$/,
                use: [ 'style-loader', 'css-loader' ]
            },
            {
                test: /\.wasm$/,
                type: 'javascript/auto',
                loaders: ['arraybuffer-loader'],
            },
            {
                test: /\.tmLanguage$/i,
                use: 'raw-loader',
            }
        ]
    },
    resolve: {
        extensions: [".tsx", ".ts", ".js", ".css"]
    },
    devtool: 'source-map'
};