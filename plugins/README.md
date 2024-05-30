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

This plugin is designed to:
- Remove sensitive information from the patient resource.
- Encode all resource IDs.
- Remove display values from all reference elements.

### Follow these steps to use the de-identification plugin:
- Add the following attribute in the keycloak user.
  -  Key: `de-identify`
  -  value: `de-identify`
  -  Ensure this attribute is included in the access token.
-  Build the jar.
  -  `mvn package -Dspotless.apply.skip=true`
-  Provide the configuation parameters through environment variables.
  -  `PROXY_TO`: The base URL of the FHIR server.
  -  `TOKEN_ISSUER`: The URL of the access token issuer.
  -  `BACKEND_TYPE`: "HAPI"
  -  `ACCESS_CHECKER`: "de-identify"
  -  `LOADER_PATH`: Path of the plugin jar

-  Execute the jar
  - `java -jar exec/target/fhir-gateway-exec.jar --server.port=9002`
