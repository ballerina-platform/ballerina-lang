# Ballerina Composer Library

## Development Guide

### Building

Execute `npm run build` in the composer folder. You can find the final distribution at target/composer-$version.zip

### Setting up watch mode

Execute `npm run watch` on the submodule (eg: bbe, tracing, documentation, etc.) you want to develop. Then, execute same command (`npm run watch`) in the root of `distribution` folder.

When a change happens to the submodule, it will re-compile and then trigger a distribution build. 

Inside distribution/build folder will be latest composer-lib with your changes.