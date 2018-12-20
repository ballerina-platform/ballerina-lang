FROM timbru31/java-node:latest
RUN useradd --create-home theia && apt-get update && apt-get install -y build-essential
WORKDIR /home/theia
USER theia
COPY --chown=theia:theia . composer
RUN cd composer && yarn
EXPOSE 3000
WORKDIR /home/theia/composer/browser-app
ENV SHELL /bin/bash
CMD yarn start --hostname 0.0.0.0