# To run the program, put the code in "hello-world.bal"
# and use "ballerina run" command.
$ ballerina run hello-world.bal
Hello, World!

# To build a program archive, we can use the
# "ballerina build main" command followed by
# one or more packages to be included into the archive.
$ ballerina build main hello-world.bal
$ ls
hello-world.bmz	hello-world.bal

# Here is how we can specify a name of the archive file
$ ballerina build main hello-world.bal -o hello.bmz

# We can now run the archive directly.
$ ballerina run hello.bmz
Hello, World!

# Now that we are familiar with the hello world program,
# let's dig a little deeper.