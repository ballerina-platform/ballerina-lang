var combine = require('istanbul-combine');


const { forEachSubModule, runNpmCommandSync, runGitCommand } = require('./common');

const args = process.argv.slice(2);

forEachSubModule((modPath) => {
    process.stdout.write("==============================================================================\n");
    process.stdout.write(" Generating coverage report for - " + modPath +"\n");
    process.stdout.write("==============================================================================\n");

    runNpmCommandSync(["i"], modPath);
    runNpmCommandSync(["run", "build"], modPath);
    process.chdir(modPath);
    runNpmCommandSync(["run", "test-coverage"], modPath);

    // Undo changes to package-lock files. These changes mostly happen
    // because of differences between npm versions
    runGitCommand(["checkout", "--", "package-lock.json"], modPath);
});


var opts = {
  dir: 'coverage',                       // output directory for combined report(s)
  pattern: '**/coverage/coverage-**.json',   // json reports to be combined 
  print: 'summary',                      // print to the console (summary, detail, both, none) 
  base:'sources',                        // base directory for resolving absolute paths, see karma bug
  reporters: {
    html: { /* html reporter options */ },
    cobertura: { /* etc. */ }
  }
};

combine.sync(opts);    
