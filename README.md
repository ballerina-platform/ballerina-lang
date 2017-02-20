# Ballerina Container Support

Container support for Ballerina provides the implementation for the following functionality.

1. Package Ballerina programs with Docker using `ballerina docker` command.

## Building From Source
> Docker is required to build and enable Ballerina Container Support. To install Docker, follow the instructions on the [Docker Engine Installation Guide](https://docs.docker.com/engine/installation/).

> The base Ballerina Docker image has to be built before building on running Ballerina Container Support. To do this follow the instructions in the [Ballerina Base Image README](ballerina-base-image/README.md) and build `ballerina-pkg:latest` Docker image.

Navigate to the source root and execute the following command.

```bash
mvn clean install
```

This will create a fat JAR file with the dependencies included, inside the `target` folder.

## Usage
Add the `ballerina-container-support-<VERSION>.jar` file to `bre/lib/` folder in the Ballerina distribution.

### `ballerina docker` Command Line Usage
```
create docker images for Ballerina program archives

Usage:

  ballerina docker <package-name> [--tag | -t <image-name>] [--host | -H <hostURL>] --yes | -y

Flags:
  --tag, -t      docker image tag to use
  --host, -H     remote docker daemon to use
  --yes, -y      assume yes for prompts


```
The `docker` command will detect the Ballerina archive type (main vs service) from the archive file extension provided. 

To create a Docker image from a Ballerina package, simply provide the package name as an argument.

```bash
$ ./ballerina docker helloWorld.bmz
ballerina: build docker image [helloworld:latest] in docker host [localhost]? (y/n): y

ballerina: docker image helloworld:latest successfully built.

Use the following command to start a container.
        docker run --name determined_aluminum -it helloworld:latest

```

Creating a Docker image for a Ballerina Service.

```bash
$ ./ballerina docker helloWorldService.bsz
ballerina: build docker image [helloworldservice:latest] in docker host [localhost]? (y/n): y
Building Docker image helloworldservice:latest...

ballerina: docker image helloworldservice:latest successfully built.

Use the following command to start a container.
        docker run -p 46325:9090 --name direct_actress -d helloworldservice:latest

Use the following command to inspect the logs.
        docker logs direct_actress

Use the following command to retrieve the IP address of the container
        docker inspect direct_actress | grep IPAddress

Ballerina service will be running on the following ports.
        http://localhost:46325
        http://<container-ip>:9090

Make requests using the format [curl -X GET http://localhost:46325/<service-name>]

```

You can additionally provide a customized image name.

```bash
./ballerina docker helloWorld.bmz -t myhelloworld:0.1
ballerina: build docker image [myhelloworld:0.1] in docker host [localhost]? (y/n): y

ballerina: docker image myhelloworld:0.1 successfully built.

Use the following command to start a container.
        docker run --name burning_aids -it myhelloworld:0.1

```
If a remote Docker daemon is available to be used, it can also be specified so the Docker image is created at the remote end.

```bash
./ballerina docker helloWorld.bmz -H http://127.0.0.1:2375
ballerina: build docker image [myhelloworld:0.1] in docker host [http://127.0.0.1:2375]? (y/n): y

ballerina: docker image helloworld:latest successfully built.

Use the following command to start a container.
        docker run --name future_aquarium -it helloworld:latest

```
## License
Ballerina Container Support is licensed under [Apache License v2](LICENSE).

