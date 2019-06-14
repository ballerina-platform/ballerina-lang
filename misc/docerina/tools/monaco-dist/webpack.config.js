const path = require('path');

module.exports = {
    entry: {
		"monaco": './src/index.ts',
		// "editor.worker": 'monaco-editor/esm/vs/editor/editor.worker.js',
		// "json.worker": 'monaco-editor/esm/vs/language/json/json.worker',
		// "css.worker": 'monaco-editor/esm/vs/language/css/css.worker',
		// "html.worker": 'monaco-editor/esm/vs/language/html/html.worker',
		// "ts.worker": 'monaco-editor/esm/vs/language/typescript/ts.worker',
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
                test: /\.plist$/i,
                use: 'raw-loader',
            }
        ]
    },
    resolve: {
        extensions: [".tsx", ".ts", ".js", ".css"]
    },
    devtool: 'source-map'
};