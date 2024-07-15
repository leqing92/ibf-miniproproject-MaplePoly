# Build Angular
# https://hub.docker.com/_/node
FROM node:22 AS ngbuild

# Create a workdir named as frontend in stage ngbuild
WORKDIR /frontend

# Install Angular globally
RUN npm i -g @angular/cli@17.3.8 

# docker file is outside frontend
COPY frontend/angular.json .
COPY frontend/package*.json .
COPY frontend/tsconfig*.json .
COPY frontend/src src
COPY frontend/ngsw-config.json .

# Install Modules
# ci will use lock ver. but not the newest ver.
# by put && it only build if npm ci is successful
RUN npm install --legacy-peer-deps
RUN npm run build --prod

# Build Spring Boot
# https://hub.docker.com/_/openjdk
FROM openjdk:21 AS javabuild

# Create a workdir named as backend in stage javabuild
WORKDIR /backend

COPY backend/mvnw .
COPY backend/pom.xml .
COPY backend/.mvn .mvn
COPY backend/src src

# Copy ng build angular file from stage=ngbuild at <frontend> workdir to static in /giphy SB
# reminder to delete the static file copied from angular before build
COPY --from=ngbuild /frontend/dist/monopoly-angular-material/browser/ src/main/resources/static

# Generate target/giphy-0.0.1-SNAPSHOT.jar
RUN chmod a+x mvnw 
RUN ./mvnw package -Dmaven.test.skip=true

# Run container
FROM openjdk:21

WORKDIR /app

# remember to change the project name
COPY --from=javabuild /backend/target/monopoly-0.0.1-SNAPSHOT.jar app.jar

ENV PORT=8080

EXPOSE ${PORT}
ENTRYPOINT SERVER_PORT=${PORT} java -jar app.jar