const { forEachSubModule, runNpmCommandSync, runGitCommand } = require('./common');

const args = process.argv.slice(2);

forEachSubModule((modPath) => {
    process.stdout.write("==============================================================================\n");
    process.stdout.write(" Building - " + modPath +"\n");
    process.stdout.write("==============================================================================\n");

    runNpmCommandSync(["i"], modPath);
    runNpmCommandSync(["run", "build"], modPath);
    if (args[0] !== "--skipTest=true") {
        runNpmCommandSync(["run", "test"], modPath);
    }
    // Undo changes to package-lock files. These changes mostly happen
    // because of differences between npm versions
    runGitCommand(["checkout", "--", "package-lock.json"], modPath);
});
