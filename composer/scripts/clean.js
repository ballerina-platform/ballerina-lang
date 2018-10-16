const { forEachSubModule, runNpmCommandSync, modulesRoot } = require('./common');
runNpmCommandSync(["i"], modulesRoot);
const rimraf = require('rimraf');
const join = require('path').join;


forEachSubModule((modPath) => {
    rimraf.sync(join(modPath, 'node_modules'));
    rimraf.sync(join(modPath, 'lib'));
    rimraf.sync(join(modPath, 'build'));
});