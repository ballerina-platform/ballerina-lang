const { forEachSubModule, runNpmCommandSync, runGitCommand, modulesRoot, getProjectVersion } = require('./common');
runNpmCommandSync(["i"], modulesRoot);

const fs = require('fs');
const join = require('path').join;
const glob = require('glob');

const args = process.argv.slice(2);

function updateVersion(newVersion, modulePath) {
    const packageJsonPath = join(modulePath, 'package.json');
    const packageJson = require(packageJsonPath);
    if (newVersion && packageJson.version !== newVersion) {
        packageJson.version = newVersion;
        fs.writeFileSync(packageJsonPath, JSON.stringify(packageJson, null, 2));
    }
}

const projectVersion = getProjectVersion();
forEachSubModule((modPath) => {
    process.stdout.write("==============================================================================\n");
    process.stdout.write(" Building - " + modPath + "\n");
    process.stdout.write("==============================================================================\n");

    updateVersion(projectVersion, modPath);
    runNpmCommandSync(["i"], modPath);
    runNpmCommandSync(["run", "build"], modPath);
    if (args[0] !== "--skipTest=true") {
        runNpmCommandSync(["run", "test"], modPath);
    }
    // Undo changes to package-lock files. These changes mostly happen
    // because of differences between npm versions
    runGitCommand(["checkout", "--", "package-lock.json"], modPath);
});

const extendedLangClientModPath =  join(modulesRoot, 'extended-language-client');
runNpmCommandSync(["pack", "."], extendedLangClientModPath);
glob(join(extendedLangClientModPath, '*.tgz'), (er, files) => {
    files.forEach(f => {
        fs.renameSync(f, join(extendedLangClientModPath, 'pack.tgz'));
    });
})