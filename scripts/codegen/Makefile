CUR_DIR = $(shell pwd)
ROOT_DIR=$(shell cd ../.. && pwd)

gen:
	rm -rf ./client
	java -jar openapi-generator-cli.jar generate -i http://localhost:2000/dto.json -g kotlin -o client --library jvm-retrofit2
	cd $(ROOT_DIR)/app/src/main/java && rm -rf org
	./GenIndex
	mv $(CUR_DIR)/client/src/main/kotlin/org $(ROOT_DIR)/app/src/main/java
	cd $(ROOT_DIR)/app/src/main/java/org/openapitools/client/infrastructure && rm -f ApiClient.kt
