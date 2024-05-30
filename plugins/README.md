# AccessChecker plugins

To implement an access-checker plugin, the
[AccessCheckerFactory interface](../server/src/main/java/com/google/fhir/gateway/interfaces/AccessCheckerFactory.java)
must be implemented, and it must be annotated by a `@Named(value = "KEY")`
annotation. `KEY` is the name of the access-checker that can be used when
running the proxy server (by setting `ACCESS_CHECKER` environment variable).

Example access-checker plugins in this module are
[ListAccessChecker](src/main/java/com/google/fhir/gateway/plugin/ListAccessChecker.java)
and
[PatientAccessChecker](src/main/java/com/google/fhir/gateway/plugin/PatientAccessChecker.java).

Beside doing basic validation of the access-token, the server also provides some
query parameters and resource parsing functionality which are wrapped inside
[PatientFinder](../server/src/main/java/com/google/fhir/gateway/interfaces/PatientFinder.java).

<!--- Add some documentation about how each access-checker works. --->


## De-identification plugin.

This plugin is designed to modify the response from the FHIR server before sending it to the client.
- Remove sensitive information from the patient resource.
- Encode all resource IDs.
- Remove display values from all reference elements.

### Follow these steps to use the de-identification plugin:
- Add the following attribute in the keycloak user.
  -  Key: `de-identify`
  -  value: `de-identify`
  -  Create the mapper in your keycloak client to include this attribute in the access token.
-  Open terminal with the root directory of this repo.
-  Build the jar by running following command in the terminal. (Note: If you are working on windows os, then use the poweshell instead of command prompt.)
    ```
    mvn clean package
    ```
    -  If the above command fails, then try running the command with skip tests.
        - `mvn clean package -DskipTeste=true`
-  Provide the configuation parameters through environment variables.
  -  `PROXY_TO`: The base URL of the FHIR server. (eg: `http://localhost:8080/fhir`)
  -  `TOKEN_ISSUER`: The URL of the access token issuer. (eg: `http://localhost:8082/auth/realms/fhir-hapi`)
  -  `BACKEND_TYPE`: "HAPI"
  -  `ACCESS_CHECKER`: "de-identify"
  -  `LOADER_PATH`: Path of the plugin jar (`eg.plugins/target/plugins-0.3.2.jar` Note: If it fails to load the plugin, then try with the absolute path.)

-  Execute the jar
  ```
  java -jar exec/target/fhir-gateway-exec.jar --server.port=9002
  ```
- Once the proxy is running, fetch the access token from the TOKEN_ISSUER and then you can query the proxy server with the access token.

  ```
  $ curl -X POST -d 'client_id=CLIENT_ID' -d 'username=testuser' \
    -d 'password=testpass' -d 'grant_type=password' \
    "http://localhost:9091/auth/realms/test/protocol/openid-connect/token"
  ```
  ```
  $ curl -X GET -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H "Content-Type: application/json; charset=utf-8" \
  'http://localhost:9002/Patient/'
  ```
  
