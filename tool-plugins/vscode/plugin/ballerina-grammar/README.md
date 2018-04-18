# ballerina-grammar

This repository contains the `.tmlanguage` file describbing the ballerina language grammar. Currently its consumed by the ballerina [vscode plugin](https://github.com/ballerina-platform/ballerina-lang/tree/master/tool-plugins/vscode) to provide syntax highlighting for ballerina.

# Contributing

As `.tmLanguage` files which are of `plist` format are rather hard to read to the human eye `ballerina.tmLanguage` file is generated from the `ballerina.YAML-tmLaguage` YAML file.

Any modifications by hand should be done only to this YAML file.

To generate the tmLanguage file,

~~~
npm install
npm run build
~~~
