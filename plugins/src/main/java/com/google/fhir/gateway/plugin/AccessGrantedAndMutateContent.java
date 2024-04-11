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
import com.google.fhir.gateway.FhirUtil;
import com.google.fhir.gateway.interfaces.AccessDecision;
import com.google.fhir.gateway.interfaces.RequestDetailsReader;
import com.google.fhir.gateway.interfaces.RequestMutation;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import org.apache.http.HttpResponse;
import org.hl7.fhir.r4.model.*;
import org.jetbrains.annotations.Nullable;

public class AccessGrantedAndMutateContent implements AccessDecision {
  private final FhirContext fhirContext;

  private AccessGrantedAndMutateContent(FhirContext fhirContext) {
    this.fhirContext = fhirContext;
  }

  @Override
  public boolean canAccess() {
    return true;
  }

  @Nullable
  @Override
  public RequestMutation getRequestMutation(RequestDetailsReader requestDetailsReader) {
    String requestPath = requestDetailsReader.getRequestPath();
    if (!"".equals(requestPath) && !ResourceType.Bundle.name().equals(requestPath)) {
      return null;
    }
    Bundle requestBundle = FhirUtil.parseRequestToBundle(fhirContext, requestDetailsReader);
    for (Bundle.BundleEntryComponent bundleEntryComponent : requestBundle.getEntry()) {
      preProcess(bundleEntryComponent);
    }
    String bundleString = fhirContext.newJsonParser().encodeResourceToString(requestBundle);
    return RequestMutation.builder().requestContent(bundleString.getBytes()).build();
  }

  @Override
  public String postProcess(RequestDetailsReader request, HttpResponse response)
      throws IOException {
    return null;
  }

  public static AccessDecision accessGranted(FhirContext fhirContext) {
    return new AccessGrantedAndMutateContent(fhirContext);
  }

  private void deIdentifyReferenceElement(Reference reference) {
    String referenceString = reference.getReference();
    if (referenceString == null) {
      return;
    }
    String encodedReferenceId = encodeString(getIdPartFromElement(referenceString));
    String referenceResourceType = reference.getResource().fhirType();
    reference.setReference(referenceResourceType + "/" + encodedReferenceId);
    reference.setDisplay(null);
  }

  private void preProcess(Bundle.BundleEntryComponent bundleEntryComponent) {
    String fullUrl = bundleEntryComponent.getFullUrl();
    Resource resource = bundleEntryComponent.getResource();
    ResourceType resourceType = resource.getResourceType();
    String encodedId;

    if (fullUrl != null) {
      encodedId = encodeString(getIdPartFromElement(bundleEntryComponent.getFullUrl()));
      bundleEntryComponent.setFullUrl(resourceType.name() + "/" + encodedId);
    } else {
      encodedId = encodeString(getIdPartFromElement(resource.getId()));
    }
    resource.setId(resourceType.name() + "/" + encodedId);

    switch (resourceType) {
      case AllergyIntolerance:
        processAllergyIntolerance((AllergyIntolerance) resource);
        break;
      case Appointment:
        processAppointment((Appointment) resource);
        break;
      case Claim:
        processClaim((Claim) resource);
        break;
      case Composition:
        processComposition((Composition) resource);
        break;
      case Condition:
        processCondition((Condition) resource);
        break;
      case DiagnosticReport:
        processDiagnosticReport((DiagnosticReport) resource);
        break;
      case Encounter:
        processEncounter((Encounter) resource);
        break;
      case EpisodeOfCare:
        processEpisodeOfCare((EpisodeOfCare) resource);
        break;
      case HealthcareService:
        processHealthService((HealthcareService) resource);
        break;
      case ImagingStudy:
        processImagingStudy((ImagingStudy) resource);
        break;
      case Immunization:
        processImmunization((Immunization) resource);
        break;
      case List:
        processList((ListResource) resource);
        break;
      case Location:
        processLocation((Location) resource);
        break;
      case Media:
        processMedia((Media) resource);
        break;
      case Medication:
        processMedication((Medication) resource);
        break;
      case MedicationAdministration:
        processMedicationAdministration((MedicationAdministration) resource);
        break;
      case MedicationDispense:
        processMedicationDispense((MedicationDispense) resource);
        break;
      case MedicationRequest:
        processMedicationRequest((MedicationRequest) resource);
        break;
      case MedicationStatement:
        processMedicationStatement((MedicationStatement) resource);
        break;
      case Observation:
        processObservation((Observation) resource);
        break;
      case OperationOutcome:
        processOperationOutcome((OperationOutcome) resource);
        break;
      case Organization:
        processOrganization((Organization) resource);
        break;
      case Patient:
        processPatient((Patient) resource);
        break;
      case Practitioner:
        processPractitioner((Practitioner) resource);
        break;
      case PractitionerRole:
        processPractitionerRole((PractitionerRole) resource);
        break;
      case Procedure:
        processProcedure((Procedure) resource);
        break;
      case RelatedPerson:
        processRelatedPerson((RelatedPerson) resource);
        break;
      case ServiceRequest:
        processServiceRequest((ServiceRequest) resource);
        break;
      case Specimen:
        processSpecimen((Specimen) resource);
        break;
      case QuestionnaireResponse:
        processQuestionnaireResponse((QuestionnaireResponse) resource);
        break;
      case ClinicalImpression:
        processClinicalImpression((ClinicalImpression) resource);
        break;
      default:
        break;
    }
  }

  private void processAllergyIntolerance(AllergyIntolerance allergyIntolerance) {
    deIdentifyReferenceElement(allergyIntolerance.getPatient());
    deIdentifyReferenceElement(allergyIntolerance.getEncounter());
    deIdentifyReferenceElement(allergyIntolerance.getRecorder());
    deIdentifyReferenceElement(allergyIntolerance.getAsserter());
  }

  private void processAppointment(Appointment appointment) {
    for (Reference reference : appointment.getReasonReference()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : appointment.getSupportingInformation()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : appointment.getSlot()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : appointment.getBasedOn()) {
      deIdentifyReferenceElement(reference);
    }
    for (Appointment.AppointmentParticipantComponent participantComponent :
        appointment.getParticipant()) {
      deIdentifyReferenceElement(participantComponent.getActor());
    }
  }

  private void processCarePlan(CarePlan carePlan) {
    for (Reference reference : carePlan.getBasedOn()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : carePlan.getReplaces()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : carePlan.getPartOf()) {
      deIdentifyReferenceElement(reference);
    }
    deIdentifyReferenceElement(carePlan.getSubject());
    deIdentifyReferenceElement(carePlan.getEncounter());
    deIdentifyReferenceElement(carePlan.getAuthor());
    for (Reference reference : carePlan.getContributor()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : carePlan.getCareTeam()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : carePlan.getAddresses()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : carePlan.getSupportingInfo()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : carePlan.getGoal()) {
      deIdentifyReferenceElement(reference);
    }
    for (CarePlan.CarePlanActivityComponent activityComponent : carePlan.getActivity()) {
      deIdentifyReferenceElement(activityComponent.getReference());
      for (Reference reference : activityComponent.getOutcomeReference()) {
        deIdentifyReferenceElement(reference);
      }
    }
  }

  private void processClaim(Claim claim) {
    deIdentifyReferenceElement(claim.getPatient());
    deIdentifyReferenceElement(claim.getEnterer());
    deIdentifyReferenceElement(claim.getInsurer());
    deIdentifyReferenceElement(claim.getProvider());
    deIdentifyReferenceElement(claim.getReferral());
  }

  private void processComposition(Composition composition) {
    deIdentifyReferenceElement(composition.getSubject());
    deIdentifyReferenceElement(composition.getEncounter());
    for (Reference reference : composition.getAuthor()) {
      deIdentifyReferenceElement(reference);
    }
    deIdentifyReferenceElement(composition.getCustodian());
  }

  private void processCondition(Condition condition) {
    deIdentifyReferenceElement(condition.getSubject());
    deIdentifyReferenceElement(condition.getEncounter());
    deIdentifyReferenceElement(condition.getRecorder());
    deIdentifyReferenceElement(condition.getAsserter());
    for (Condition.ConditionStageComponent stageComponent : condition.getStage()) {
      for (Reference reference : stageComponent.getAssessment()) {
        deIdentifyReferenceElement(reference);
      }
    }
    for (Condition.ConditionEvidenceComponent evidenceComponent : condition.getEvidence()) {
      for (Reference reference : evidenceComponent.getDetail()) {
        deIdentifyReferenceElement(reference);
      }
    }
  }

  private void processDiagnosticReport(DiagnosticReport diagnosticReport) {
    for (Reference reference : diagnosticReport.getBasedOn()) {
      deIdentifyReferenceElement(reference);
    }
    deIdentifyReferenceElement(diagnosticReport.getSubject());
    deIdentifyReferenceElement(diagnosticReport.getEncounter());
    for (Reference reference : diagnosticReport.getPerformer()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : diagnosticReport.getResultsInterpreter()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : diagnosticReport.getSpecimen()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : diagnosticReport.getResult()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : diagnosticReport.getImagingStudy()) {
      deIdentifyReferenceElement(reference);
    }
  }

  private void processEncounter(Encounter encounter) {
    deIdentifyReferenceElement(encounter.getSubject());
    for (Reference reference : encounter.getEpisodeOfCare()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : encounter.getBasedOn()) {
      deIdentifyReferenceElement(reference);
    }
    for (Encounter.EncounterParticipantComponent participantComponent :
        encounter.getParticipant()) {
      deIdentifyReferenceElement(participantComponent.getIndividual());
    }
    for (Reference reference : encounter.getAppointment()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : encounter.getReasonReference()) {
      deIdentifyReferenceElement(reference);
    }
    for (Encounter.DiagnosisComponent diagnosisComponent : encounter.getDiagnosis()) {
      deIdentifyReferenceElement(diagnosisComponent.getCondition());
    }
    for (Reference reference : encounter.getAccount()) {
      deIdentifyReferenceElement(reference);
    }
    deIdentifyReferenceElement(encounter.getHospitalization().getOrigin());
    deIdentifyReferenceElement(encounter.getHospitalization().getDestination());
    for (Encounter.EncounterLocationComponent locationComponent : encounter.getLocation()) {
      deIdentifyReferenceElement(locationComponent.getLocation());
    }
    deIdentifyReferenceElement(encounter.getServiceProvider());
    deIdentifyReferenceElement(encounter.getPartOf());
  }

  private void processEpisodeOfCare(EpisodeOfCare episodeOfCare) {
    for (EpisodeOfCare.DiagnosisComponent diagnosisComponent : episodeOfCare.getDiagnosis()) {
      deIdentifyReferenceElement(diagnosisComponent.getCondition());
    }
    deIdentifyReferenceElement(episodeOfCare.getPatient());
    deIdentifyReferenceElement(episodeOfCare.getManagingOrganization());
    for (Reference reference : episodeOfCare.getReferralRequest()) {
      deIdentifyReferenceElement(reference);
    }
    deIdentifyReferenceElement(episodeOfCare.getCareManager());
    for (Reference reference : episodeOfCare.getTeam()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : episodeOfCare.getAccount()) {
      deIdentifyReferenceElement(reference);
    }
  }

  private void processHealthService(HealthcareService healthcareService) {
    deIdentifyReferenceElement(healthcareService.getProvidedBy());
    for (Reference reference : healthcareService.getLocation()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : healthcareService.getCoverageArea()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : healthcareService.getEndpoint()) {
      deIdentifyReferenceElement(reference);
    }
  }

  private void processImagingStudy(ImagingStudy imagingStudy) {
    deIdentifyReferenceElement(imagingStudy.getSubject());
    deIdentifyReferenceElement(imagingStudy.getEncounter());
    for (Reference reference : imagingStudy.getBasedOn()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : imagingStudy.getInterpreter()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : imagingStudy.getEndpoint()) {
      deIdentifyReferenceElement(reference);
    }
    deIdentifyReferenceElement(imagingStudy.getProcedureReference());
    deIdentifyReferenceElement(imagingStudy.getLocation());
    for (Reference reference : imagingStudy.getReasonReference()) {
      deIdentifyReferenceElement(reference);
    }
    for (ImagingStudy.ImagingStudySeriesComponent studySeriesComponent : imagingStudy.getSeries()) {
      for (Reference reference : studySeriesComponent.getEndpoint()) {
        deIdentifyReferenceElement(reference);
      }
      for (Reference reference : studySeriesComponent.getSpecimen()) {
        deIdentifyReferenceElement(reference);
      }
      for (ImagingStudy.ImagingStudySeriesPerformerComponent performerComponent :
          studySeriesComponent.getPerformer()) {
        deIdentifyReferenceElement(performerComponent.getActor());
      }
    }
  }

  private void processImmunization(Immunization immunization) {
    deIdentifyReferenceElement(immunization.getPatient());
    deIdentifyReferenceElement(immunization.getEncounter());
    deIdentifyReferenceElement(immunization.getLocation());
    deIdentifyReferenceElement(immunization.getManufacturer());
    for (Immunization.ImmunizationPerformerComponent performerComponent :
        immunization.getPerformer()) {
      deIdentifyReferenceElement(performerComponent.getActor());
    }
    for (Reference reference : immunization.getReasonReference()) {
      deIdentifyReferenceElement(reference);
    }
    for (Immunization.ImmunizationReactionComponent reactionComponent :
        immunization.getReaction()) {
      deIdentifyReferenceElement(reactionComponent.getDetail());
    }
    for (Immunization.ImmunizationProtocolAppliedComponent protocolAppliedComponent :
        immunization.getProtocolApplied()) {
      deIdentifyReferenceElement(protocolAppliedComponent.getAuthority());
    }
  }

  private void processList(ListResource list) {
    deIdentifyReferenceElement(list.getSubject());
    deIdentifyReferenceElement(list.getEncounter());
    deIdentifyReferenceElement(list.getSource());
  }

  private void processLocation(Location location) {
    deIdentifyReferenceElement(location.getManagingOrganization());
    deIdentifyReferenceElement(location.getPartOf());
    for (Reference reference : location.getEndpoint()) {
      deIdentifyReferenceElement(reference);
    }
  }

  private void processMedia(Media media) {
    for (Reference reference : media.getBasedOn()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : media.getPartOf()) {
      deIdentifyReferenceElement(reference);
    }
    deIdentifyReferenceElement(media.getSubject());
    deIdentifyReferenceElement(media.getEncounter());
    deIdentifyReferenceElement(media.getOperator());
    deIdentifyReferenceElement(media.getDevice());
  }

  private void processMedication(Medication medication) {
    deIdentifyReferenceElement(medication.getManufacturer());
  }

  private void processMedicationAdministration(MedicationAdministration medicationAdministration) {
    for (Reference reference : medicationAdministration.getPartOf()) {
      deIdentifyReferenceElement(reference);
    }
    deIdentifyReferenceElement(medicationAdministration.getSubject());
    deIdentifyReferenceElement(medicationAdministration.getContext());
    for (Reference reference : medicationAdministration.getSupportingInformation()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : medicationAdministration.getReasonReference()) {
      deIdentifyReferenceElement(reference);
    }
    deIdentifyReferenceElement(medicationAdministration.getRequest());
    for (Reference reference : medicationAdministration.getDevice()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : medicationAdministration.getEventHistory()) {
      deIdentifyReferenceElement(reference);
    }
  }

  private void processMedicationDispense(MedicationDispense medicationDispense) {
    for (Reference reference : medicationDispense.getPartOf()) {
      deIdentifyReferenceElement(reference);
    }
    deIdentifyReferenceElement(medicationDispense.getSubject());
    deIdentifyReferenceElement(medicationDispense.getContext());
    for (Reference reference : medicationDispense.getSupportingInformation()) {
      deIdentifyReferenceElement(reference);
    }
    deIdentifyReferenceElement(medicationDispense.getLocation());
    for (Reference reference : medicationDispense.getAuthorizingPrescription()) {
      deIdentifyReferenceElement(reference);
    }
    deIdentifyReferenceElement(medicationDispense.getDestination());
    for (Reference reference : medicationDispense.getReceiver()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : medicationDispense.getDetectedIssue()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : medicationDispense.getEventHistory()) {
      deIdentifyReferenceElement(reference);
    }
  }

  private void processMedicationRequest(MedicationRequest medicationRequest) {
    deIdentifyReferenceElement(medicationRequest.getSubject());
    deIdentifyReferenceElement(medicationRequest.getEncounter());
    for (Reference reference : medicationRequest.getSupportingInformation()) {
      deIdentifyReferenceElement(reference);
    }
    deIdentifyReferenceElement(medicationRequest.getRequester());
    deIdentifyReferenceElement(medicationRequest.getPerformer());
    deIdentifyReferenceElement(medicationRequest.getRecorder());
    for (Reference reference : medicationRequest.getReasonReference()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : medicationRequest.getBasedOn()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : medicationRequest.getInsurance()) {
      deIdentifyReferenceElement(reference);
    }
    deIdentifyReferenceElement(medicationRequest.getPriorPrescription());
  }

  private void processMedicationStatement(MedicationStatement medicationStatement) {
    for (Reference reference : medicationStatement.getBasedOn()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : medicationStatement.getPartOf()) {
      deIdentifyReferenceElement(reference);
    }
    deIdentifyReferenceElement(medicationStatement.getSubject());
    deIdentifyReferenceElement(medicationStatement.getContext());
    deIdentifyReferenceElement(medicationStatement.getContext());
    deIdentifyReferenceElement(medicationStatement.getInformationSource());
    for (Reference reference : medicationStatement.getDerivedFrom()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : medicationStatement.getReasonReference()) {
      deIdentifyReferenceElement(reference);
    }
  }

  private void processObservation(Observation observation) {
    for (Reference reference : observation.getBasedOn()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : observation.getPartOf()) {
      deIdentifyReferenceElement(reference);
    }
    deIdentifyReferenceElement(observation.getSubject());
    for (Reference reference : observation.getFocus()) {
      deIdentifyReferenceElement(reference);
    }
    deIdentifyReferenceElement(observation.getEncounter());
    for (Reference reference : observation.getPerformer()) {
      deIdentifyReferenceElement(reference);
    }
    deIdentifyReferenceElement(observation.getSpecimen());
    deIdentifyReferenceElement(observation.getDevice());
    for (Reference reference : observation.getHasMember()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : observation.getHasMember()) {
      deIdentifyReferenceElement(reference);
    }
  }

  private void processOperationOutcome(OperationOutcome operationOutcome) {}

  private void processOrganization(Organization organization) {
    deIdentifyReferenceElement(organization.getPartOf());
    for (Reference reference : organization.getEndpoint()) {
      deIdentifyReferenceElement(reference);
    }
  }

  private void processPatient(Patient patient) {
    patient.setIdentifier(new ArrayList<>());
    patient.setExtension(new ArrayList<>());
    patient.setName(new ArrayList<>());
    patient.setTelecom(new ArrayList<>());
    patient.setAddress(new ArrayList<>());
    patient.setPhoto(new ArrayList<>());
    patient.setContact(new ArrayList<>());
    deIdentifyReferenceElement(patient.getManagingOrganization());
    for (Patient.PatientLinkComponent linkComponent : patient.getLink()) {
      deIdentifyReferenceElement(linkComponent.getOther());
    }
  }

  private void processPractitioner(Practitioner practitioner) {}

  private void processPractitionerRole(PractitionerRole practitionerRole) {}

  private void processProcedure(Procedure procedure) {
    for (Reference reference : procedure.getBasedOn()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : procedure.getPartOf()) {
      deIdentifyReferenceElement(reference);
    }
    deIdentifyReferenceElement(procedure.getSubject());
    deIdentifyReferenceElement(procedure.getEncounter());
    deIdentifyReferenceElement(procedure.getRecorder());
    deIdentifyReferenceElement(procedure.getAsserter());
    deIdentifyReferenceElement(procedure.getLocation());
    for (Reference reference : procedure.getReasonReference()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : procedure.getComplicationDetail()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : procedure.getUsedReference()) {
      deIdentifyReferenceElement(reference);
    }
  }

  private void processRelatedPerson(RelatedPerson relatedPerson) {
    deIdentifyReferenceElement(relatedPerson.getPatient());
  }

  private void processServiceRequest(ServiceRequest serviceRequest) {
    for (Reference reference : serviceRequest.getBasedOn()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : serviceRequest.getReplaces()) {
      deIdentifyReferenceElement(reference);
    }
    deIdentifyReferenceElement(serviceRequest.getSubject());
    deIdentifyReferenceElement(serviceRequest.getEncounter());
    deIdentifyReferenceElement(serviceRequest.getRequester());
    for (Reference reference : serviceRequest.getPerformer()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : serviceRequest.getLocationReference()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : serviceRequest.getReasonReference()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : serviceRequest.getInsurance()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : serviceRequest.getSupportingInfo()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : serviceRequest.getSpecimen()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : serviceRequest.getRelevantHistory()) {
      deIdentifyReferenceElement(reference);
    }
  }

  private void processSpecimen(Specimen specimen) {
    deIdentifyReferenceElement(specimen.getSubject());
    for (Reference reference : specimen.getParent()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : specimen.getRequest()) {
      deIdentifyReferenceElement(reference);
    }
  }

  private void processQuestionnaireResponse(QuestionnaireResponse questionnaireResponse) {
    for (Reference reference : questionnaireResponse.getBasedOn()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : questionnaireResponse.getPartOf()) {
      deIdentifyReferenceElement(reference);
    }
    deIdentifyReferenceElement(questionnaireResponse.getSubject());
    deIdentifyReferenceElement(questionnaireResponse.getEncounter());
    deIdentifyReferenceElement(questionnaireResponse.getAuthor());
    deIdentifyReferenceElement(questionnaireResponse.getSource());
  }

  private void processClinicalImpression(ClinicalImpression clinicalImpression) {
    deIdentifyReferenceElement(clinicalImpression.getSubject());
    deIdentifyReferenceElement(clinicalImpression.getEncounter());
    deIdentifyReferenceElement(clinicalImpression.getAssessor());
    deIdentifyReferenceElement(clinicalImpression.getPrevious());
    for (Reference reference : clinicalImpression.getProblem()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : clinicalImpression.getPrognosisReference()) {
      deIdentifyReferenceElement(reference);
    }
    for (Reference reference : clinicalImpression.getSupportingInfo()) {
      deIdentifyReferenceElement(reference);
    }
  }

  private String getIdPartFromElement(String idElementString) {
    // Usually referenceElement and fullUrl, resource id exists in either of two formats mentioned
    // below.
    // "urn:uuid:<resource-id>"
    // "<resource-type>/<resource-id>"
    // This function will process the idElement string returns only the IdPart.
    String[] parts = idElementString.split("[:/]");
    if (parts.length > 1) {
      return parts[parts.length - 1];
    } else {
      return idElementString;
    }
  }

  private String encodeString(String str) {
    try {
      MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
      byte[] hashBytes = messageDigest.digest(str.getBytes());
      StringBuilder hexString = new StringBuilder();
      for (byte b : hashBytes) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1) hexString.append('0');
        hexString.append(hex);
      }
      return hexString.toString();
    } catch (NoSuchAlgorithmException ex) {
      // Do nothing
    }
    return null;
  }
}
