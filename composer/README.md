# Ballerina Composer Library

## Development Guide

### Building

Execute `npm run build` in the composer folder. You can find the final distribution at target/composer-$version.zip

### Setting up watch mode

Execute `npm run watch` on the submodule (eg: bbe, tracing, documentation, etc.) you want to develop. Then, execute same command (`npm run watch`) in the root of `distribution` folder.

When a change happens to the submodule, it will re-compile and then trigger a distribution build. 

Inside distribution/build folder will be latest composer-lib with your changes.

### Run tests
Execute `npm run test` on submodule.  If you want to execute tests for every submodule execute `npm run test` in this directory.

### Generate coverage reports
Execute `npm run test-coverage` on submodule.  If you want to one coverag report for all submodules `npm run test-coverage` in this directory.