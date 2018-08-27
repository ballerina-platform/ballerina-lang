FROM timbru31/java-node:latest
RUN useradd --create-home theia && apt-get update && apt-get install -y build-essential
WORKDIR /home/theia
USER theia
COPY --chown=theia:theia . composer
RUN cd composer && \
    yarn && \
    rm -rf /home/theia/composer/resources/lib && \
    rm /home/theia/composer/resources/ballerina.tmLanguage
EXPOSE 3000
WORKDIR /home/theia/composer/browser-app
ENV SHELL /bin/bash
CMD yarn run start --hostname 0.0.0.0