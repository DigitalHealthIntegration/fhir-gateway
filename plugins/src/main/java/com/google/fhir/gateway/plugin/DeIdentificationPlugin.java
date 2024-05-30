/*
 * Copyright 2021-2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.fhir.gateway.plugin;

import ca.uhn.fhir.context.FhirContext;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.fhir.gateway.FhirUtil;
import com.google.fhir.gateway.HttpFhirClient;
import com.google.fhir.gateway.JwtUtil;
import com.google.fhir.gateway.interfaces.*;
import javax.inject.Named;

public class DeIdentificationPlugin implements AccessChecker {

  private final FhirContext fhirContext;
  private final HttpFhirClient httpFhirClient;
  private final String claim;
  private final PatientFinder patientFinder;

  // We're not using any of the parameters here, but real access checkers would likely use some/all.
  private DeIdentificationPlugin(
      HttpFhirClient httpFhirClient,
      String claim,
      FhirContext fhirContext,
      PatientFinder patientFinder) {
    this.fhirContext = fhirContext;
    this.claim = claim;
    this.httpFhirClient = httpFhirClient;
    this.patientFinder = patientFinder;
  }

  @Override
  public AccessDecision checkAccess(RequestDetailsReader requestDetails) {
    return AccessGrantedAndMutateContent.accessGranted(fhirContext);
    //        return NoOpAccessDecision.accessGranted();
  }

  // The factory must be thread-safe.
  @Named(value = "de-identify")
  public static class Factory implements AccessCheckerFactory {

    static final String CLAIM = "de-identify";

    private String getClaim(DecodedJWT jwt) {
      return FhirUtil.checkIdOrFail(JwtUtil.getClaimOrDie(jwt, CLAIM));
    }

    @Override
    public AccessChecker create(
        DecodedJWT jwt,
        HttpFhirClient httpFhirClient,
        FhirContext fhirContext,
        PatientFinder patientFinder) {
      String claim = getClaim(jwt);
      return new DeIdentificationPlugin(httpFhirClient, claim, fhirContext, patientFinder);
    }
  }
}
