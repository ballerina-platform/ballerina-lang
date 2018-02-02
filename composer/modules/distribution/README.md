# Ballerina Composer standalone distribution

This is the [Electron](https://electron.atom.io/) based standalone distribution of [Ballerina Composer](https://github.com/ballerinalang/composer).

## How to run/build
Follow below steps to build standalone Electron based distribution of Ballerina Composer.

### Prerequisites
*   Make sure you have already built [web module](https://github.com/ballerinalang/composer/tree/master/modules/web) & [services module](https://github.com/ballerinalang/composer/tree/master/modules/services/workspace-service)
*   [NodeJS](https://nodejs.org/en/) (Version 6.x.x)

### To Run
inside distribution module directory, run `./node_modules/.bin/electron .` (in Windows use `\`)

### To Build for current platform
inside distribution module directory, run `npm run dist` & check dist folder

### To Build for all platforms
inside distribution module directory, run `npm run dist-all` & check dist folder
