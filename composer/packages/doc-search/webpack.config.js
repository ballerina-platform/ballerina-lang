const path = require('path');

module.exports = {
  entry: {
    "doc-search": './src/app.tsx',
  },
  output: {
    filename: './search.js',
    path: path.join(__dirname, 'lib'),
    library: 'ballerinaDocSearch',
    libraryTarget: 'umd'
  },
  resolve: {
    extensions: [".ts", ".tsx", ".js", ".jsx"]
  },
  module: {
    rules: [
      { test: /\.(t|j)sx?$/, use: { loader: 'ts-loader' }, exclude: /node_modules/ },

      // addition - add source-map support
      { enforce: "pre", test: /\.js$/, exclude: /node_modules/, loader: "source-map-loader" }
    ]
  },
  devtool: "source-map"
}
