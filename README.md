# AivenKafkaMultiCloudPOC


To build & run in GCP :

Switch to JAVA 17.
Upload keystore, truststore of aiven kafka to secret manager with name kafka-keystore and kafka-truststore , create other secrets as required.

1.gcloud auth application-default login  ->  Login with an account that has secretManager secret Accessor role in the GCP project

2.mvn clean install -D spring.profiles.active=gcp

3.mvn spring-boot:run  -D spring.profiles.active=gcp


