# Copied from https://github.com/jwhitlark/clj-on-k8s-quickstart/blob/master/minimum-src/Dockerfile
FROM clojure:lein-alpine

RUN mkdir -p /usr/src/patients
WORKDIR /usr/src/patients
COPY . /usr/src/patients
RUN lein uberjar
# "postgres_host" and "postgres_user" below should be replaces with actual values.
ENV DB_URL="jdbc:postgresql://postgres_host/patients_db?user=postgres_user"
ENV PORT=8080
CMD ["java", "-jar", "target/patients-0.0.1-SNAPSHOT-standalone.jar"]
