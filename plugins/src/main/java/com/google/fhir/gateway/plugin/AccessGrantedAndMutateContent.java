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
      preProcess(bundleEntryComponent.getResource());
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

  private void removeDisplayFromReference(Reference reference) {
    reference.setDisplay(null);
  }

  private void preProcess(Resource resource) {
    ResourceType resourceType = resource.getResourceType();
    String encodedId = encodeString(resource.getIdElement().getIdPart());
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
    removeDisplayFromReference(allergyIntolerance.getPatient());
    removeDisplayFromReference(allergyIntolerance.getEncounter());
    removeDisplayFromReference(allergyIntolerance.getRecorder());
    removeDisplayFromReference(allergyIntolerance.getAsserter());
  }

  private void processAppointment(Appointment appointment) {
    for (Reference reference : appointment.getReasonReference()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : appointment.getSupportingInformation()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : appointment.getSlot()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : appointment.getBasedOn()) {
      removeDisplayFromReference(reference);
    }
    for (Appointment.AppointmentParticipantComponent participantComponent :
        appointment.getParticipant()) {
      removeDisplayFromReference(participantComponent.getActor());
    }
  }

  private void processCarePlan(CarePlan carePlan) {
    for (Reference reference : carePlan.getBasedOn()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : carePlan.getReplaces()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : carePlan.getPartOf()) {
      removeDisplayFromReference(reference);
    }
    removeDisplayFromReference(carePlan.getSubject());
    removeDisplayFromReference(carePlan.getEncounter());
    removeDisplayFromReference(carePlan.getAuthor());
    for (Reference reference : carePlan.getContributor()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : carePlan.getCareTeam()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : carePlan.getAddresses()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : carePlan.getSupportingInfo()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : carePlan.getGoal()) {
      removeDisplayFromReference(reference);
    }
    for (CarePlan.CarePlanActivityComponent activityComponent : carePlan.getActivity()) {
      removeDisplayFromReference(activityComponent.getReference());
      for (Reference reference : activityComponent.getOutcomeReference()) {
        removeDisplayFromReference(reference);
      }
    }
  }

  private void processClaim(Claim claim) {
    removeDisplayFromReference(claim.getPatient());
    removeDisplayFromReference(claim.getEnterer());
    removeDisplayFromReference(claim.getInsurer());
    removeDisplayFromReference(claim.getProvider());
    removeDisplayFromReference(claim.getReferral());
  }

  private void processComposition(Composition composition) {
    removeDisplayFromReference(composition.getSubject());
    removeDisplayFromReference(composition.getEncounter());
    for (Reference reference : composition.getAuthor()) {
      removeDisplayFromReference(reference);
    }
    removeDisplayFromReference(composition.getCustodian());
  }

  private void processCondition(Condition condition) {
    removeDisplayFromReference(condition.getSubject());
    removeDisplayFromReference(condition.getEncounter());
    removeDisplayFromReference(condition.getRecorder());
    removeDisplayFromReference(condition.getAsserter());
    for (Condition.ConditionStageComponent stageComponent : condition.getStage()) {
      for (Reference reference : stageComponent.getAssessment()) {
        removeDisplayFromReference(reference);
      }
    }
    for (Condition.ConditionEvidenceComponent evidenceComponent : condition.getEvidence()) {
      for (Reference reference : evidenceComponent.getDetail()) {
        removeDisplayFromReference(reference);
      }
    }
  }

  private void processDiagnosticReport(DiagnosticReport diagnosticReport) {
    for (Reference reference : diagnosticReport.getBasedOn()) {
      removeDisplayFromReference(reference);
    }
    removeDisplayFromReference(diagnosticReport.getSubject());
    removeDisplayFromReference(diagnosticReport.getEncounter());
    for (Reference reference : diagnosticReport.getPerformer()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : diagnosticReport.getResultsInterpreter()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : diagnosticReport.getSpecimen()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : diagnosticReport.getResult()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : diagnosticReport.getImagingStudy()) {
      removeDisplayFromReference(reference);
    }
  }

  private void processEncounter(Encounter encounter) {
    removeDisplayFromReference(encounter.getSubject());
    for (Reference reference : encounter.getEpisodeOfCare()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : encounter.getBasedOn()) {
      removeDisplayFromReference(reference);
    }
    for (Encounter.EncounterParticipantComponent participantComponent :
        encounter.getParticipant()) {
      removeDisplayFromReference(participantComponent.getIndividual());
    }
    for (Reference reference : encounter.getAppointment()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : encounter.getReasonReference()) {
      removeDisplayFromReference(reference);
    }
    for (Encounter.DiagnosisComponent diagnosisComponent : encounter.getDiagnosis()) {
      removeDisplayFromReference(diagnosisComponent.getCondition());
    }
    for (Reference reference : encounter.getAccount()) {
      removeDisplayFromReference(reference);
    }
    removeDisplayFromReference(encounter.getHospitalization().getOrigin());
    removeDisplayFromReference(encounter.getHospitalization().getDestination());
    for (Encounter.EncounterLocationComponent locationComponent : encounter.getLocation()) {
      removeDisplayFromReference(locationComponent.getLocation());
    }
    removeDisplayFromReference(encounter.getServiceProvider());
    removeDisplayFromReference(encounter.getPartOf());
  }

  private void processEpisodeOfCare(EpisodeOfCare episodeOfCare) {
    for (EpisodeOfCare.DiagnosisComponent diagnosisComponent : episodeOfCare.getDiagnosis()) {
      removeDisplayFromReference(diagnosisComponent.getCondition());
    }
    removeDisplayFromReference(episodeOfCare.getPatient());
    removeDisplayFromReference(episodeOfCare.getManagingOrganization());
    for (Reference reference : episodeOfCare.getReferralRequest()) {
      removeDisplayFromReference(reference);
    }
    removeDisplayFromReference(episodeOfCare.getCareManager());
    for (Reference reference : episodeOfCare.getTeam()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : episodeOfCare.getAccount()) {
      removeDisplayFromReference(reference);
    }
  }

  private void processHealthService(HealthcareService healthcareService) {
    removeDisplayFromReference(healthcareService.getProvidedBy());
    for (Reference reference : healthcareService.getLocation()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : healthcareService.getCoverageArea()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : healthcareService.getEndpoint()) {
      removeDisplayFromReference(reference);
    }
  }

  private void processImagingStudy(ImagingStudy imagingStudy) {
    removeDisplayFromReference(imagingStudy.getSubject());
    removeDisplayFromReference(imagingStudy.getEncounter());
    for (Reference reference : imagingStudy.getBasedOn()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : imagingStudy.getInterpreter()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : imagingStudy.getEndpoint()) {
      removeDisplayFromReference(reference);
    }
    removeDisplayFromReference(imagingStudy.getProcedureReference());
    removeDisplayFromReference(imagingStudy.getLocation());
    for (Reference reference : imagingStudy.getReasonReference()) {
      removeDisplayFromReference(reference);
    }
    for (ImagingStudy.ImagingStudySeriesComponent studySeriesComponent : imagingStudy.getSeries()) {
      for (Reference reference : studySeriesComponent.getEndpoint()) {
        removeDisplayFromReference(reference);
      }
      for (Reference reference : studySeriesComponent.getSpecimen()) {
        removeDisplayFromReference(reference);
      }
      for (ImagingStudy.ImagingStudySeriesPerformerComponent performerComponent :
          studySeriesComponent.getPerformer()) {
        removeDisplayFromReference(performerComponent.getActor());
      }
    }
  }

  private void processImmunization(Immunization immunization) {
    removeDisplayFromReference(immunization.getPatient());
    removeDisplayFromReference(immunization.getEncounter());
    removeDisplayFromReference(immunization.getLocation());
    removeDisplayFromReference(immunization.getManufacturer());
    for (Immunization.ImmunizationPerformerComponent performerComponent :
        immunization.getPerformer()) {
      removeDisplayFromReference(performerComponent.getActor());
    }
    for (Reference reference : immunization.getReasonReference()) {
      removeDisplayFromReference(reference);
    }
    for (Immunization.ImmunizationReactionComponent reactionComponent :
        immunization.getReaction()) {
      removeDisplayFromReference(reactionComponent.getDetail());
    }
    for (Immunization.ImmunizationProtocolAppliedComponent protocolAppliedComponent :
        immunization.getProtocolApplied()) {
      removeDisplayFromReference(protocolAppliedComponent.getAuthority());
    }
  }

  private void processList(ListResource list) {
    removeDisplayFromReference(list.getSubject());
    removeDisplayFromReference(list.getEncounter());
    removeDisplayFromReference(list.getSource());
  }

  private void processLocation(Location location) {
    removeDisplayFromReference(location.getManagingOrganization());
    removeDisplayFromReference(location.getPartOf());
    for (Reference reference : location.getEndpoint()) {
      removeDisplayFromReference(reference);
    }
  }

  private void processMedia(Media media) {
    for (Reference reference : media.getBasedOn()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : media.getPartOf()) {
      removeDisplayFromReference(reference);
    }
    removeDisplayFromReference(media.getSubject());
    removeDisplayFromReference(media.getEncounter());
    removeDisplayFromReference(media.getOperator());
    removeDisplayFromReference(media.getDevice());
  }

  private void processMedication(Medication medication) {
    removeDisplayFromReference(medication.getManufacturer());
  }

  private void processMedicationAdministration(MedicationAdministration medicationAdministration) {
    for (Reference reference : medicationAdministration.getPartOf()) {
      removeDisplayFromReference(reference);
    }
    removeDisplayFromReference(medicationAdministration.getSubject());
    removeDisplayFromReference(medicationAdministration.getContext());
    for (Reference reference : medicationAdministration.getSupportingInformation()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : medicationAdministration.getReasonReference()) {
      removeDisplayFromReference(reference);
    }
    removeDisplayFromReference(medicationAdministration.getRequest());
    for (Reference reference : medicationAdministration.getDevice()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : medicationAdministration.getEventHistory()) {
      removeDisplayFromReference(reference);
    }
  }

  private void processMedicationDispense(MedicationDispense medicationDispense) {
    for (Reference reference : medicationDispense.getPartOf()) {
      removeDisplayFromReference(reference);
    }
    removeDisplayFromReference(medicationDispense.getSubject());
    removeDisplayFromReference(medicationDispense.getContext());
    for (Reference reference : medicationDispense.getSupportingInformation()) {
      removeDisplayFromReference(reference);
    }
    removeDisplayFromReference(medicationDispense.getLocation());
    for (Reference reference : medicationDispense.getAuthorizingPrescription()) {
      removeDisplayFromReference(reference);
    }
    removeDisplayFromReference(medicationDispense.getDestination());
    for (Reference reference : medicationDispense.getReceiver()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : medicationDispense.getDetectedIssue()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : medicationDispense.getEventHistory()) {
      removeDisplayFromReference(reference);
    }
  }

  private void processMedicationRequest(MedicationRequest medicationRequest) {
    removeDisplayFromReference(medicationRequest.getSubject());
    removeDisplayFromReference(medicationRequest.getEncounter());
    for (Reference reference : medicationRequest.getSupportingInformation()) {
      removeDisplayFromReference(reference);
    }
    removeDisplayFromReference(medicationRequest.getRequester());
    removeDisplayFromReference(medicationRequest.getPerformer());
    removeDisplayFromReference(medicationRequest.getRecorder());
    for (Reference reference : medicationRequest.getReasonReference()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : medicationRequest.getBasedOn()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : medicationRequest.getInsurance()) {
      removeDisplayFromReference(reference);
    }
    removeDisplayFromReference(medicationRequest.getPriorPrescription());
  }

  private void processMedicationStatement(MedicationStatement medicationStatement) {
    for (Reference reference : medicationStatement.getBasedOn()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : medicationStatement.getPartOf()) {
      removeDisplayFromReference(reference);
    }
    removeDisplayFromReference(medicationStatement.getSubject());
    removeDisplayFromReference(medicationStatement.getContext());
    removeDisplayFromReference(medicationStatement.getContext());
    removeDisplayFromReference(medicationStatement.getInformationSource());
    for (Reference reference : medicationStatement.getDerivedFrom()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : medicationStatement.getReasonReference()) {
      removeDisplayFromReference(reference);
    }
  }

  private void processObservation(Observation observation) {
    for (Reference reference : observation.getBasedOn()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : observation.getPartOf()) {
      removeDisplayFromReference(reference);
    }
    removeDisplayFromReference(observation.getSubject());
    for (Reference reference : observation.getFocus()) {
      removeDisplayFromReference(reference);
    }
    removeDisplayFromReference(observation.getEncounter());
    for (Reference reference : observation.getPerformer()) {
      removeDisplayFromReference(reference);
    }
    removeDisplayFromReference(observation.getSpecimen());
    removeDisplayFromReference(observation.getDevice());
    for (Reference reference : observation.getHasMember()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : observation.getHasMember()) {
      removeDisplayFromReference(reference);
    }
  }

  private void processOperationOutcome(OperationOutcome operationOutcome) {}

  private void processOrganization(Organization organization) {
    removeDisplayFromReference(organization.getPartOf());
    for (Reference reference : organization.getEndpoint()) {
      removeDisplayFromReference(reference);
    }
  }

  private void processPatient(Patient patient) {
    patient.setIdentifier(new ArrayList<>());
    patient.setName(new ArrayList<>());
    patient.setTelecom(new ArrayList<>());
    patient.setAddress(new ArrayList<>());
    patient.setPhoto(new ArrayList<>());
    patient.setContact(new ArrayList<>());
    removeDisplayFromReference(patient.getManagingOrganization());
    for (Patient.PatientLinkComponent linkComponent : patient.getLink()) {
      removeDisplayFromReference(linkComponent.getOther());
    }
  }

  private void processPractitioner(Practitioner practitioner) {}

  private void processPractitionerRole(PractitionerRole practitionerRole) {}

  private void processProcedure(Procedure procedure) {
    for (Reference reference : procedure.getBasedOn()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : procedure.getPartOf()) {
      removeDisplayFromReference(reference);
    }
    removeDisplayFromReference(procedure.getSubject());
    removeDisplayFromReference(procedure.getEncounter());
    removeDisplayFromReference(procedure.getRecorder());
    removeDisplayFromReference(procedure.getAsserter());
    removeDisplayFromReference(procedure.getLocation());
    for (Reference reference : procedure.getReasonReference()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : procedure.getComplicationDetail()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : procedure.getUsedReference()) {
      removeDisplayFromReference(reference);
    }
  }

  private void processRelatedPerson(RelatedPerson relatedPerson) {
    removeDisplayFromReference(relatedPerson.getPatient());
  }

  private void processServiceRequest(ServiceRequest serviceRequest) {
    for (Reference reference : serviceRequest.getBasedOn()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : serviceRequest.getReplaces()) {
      removeDisplayFromReference(reference);
    }
    removeDisplayFromReference(serviceRequest.getSubject());
    removeDisplayFromReference(serviceRequest.getEncounter());
    removeDisplayFromReference(serviceRequest.getRequester());
    for (Reference reference : serviceRequest.getPerformer()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : serviceRequest.getLocationReference()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : serviceRequest.getReasonReference()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : serviceRequest.getInsurance()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : serviceRequest.getSupportingInfo()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : serviceRequest.getSpecimen()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : serviceRequest.getRelevantHistory()) {
      removeDisplayFromReference(reference);
    }
  }

  private void processSpecimen(Specimen specimen) {
    removeDisplayFromReference(specimen.getSubject());
    for (Reference reference : specimen.getParent()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : specimen.getRequest()) {
      removeDisplayFromReference(reference);
    }
  }

  private void processQuestionnaireResponse(QuestionnaireResponse questionnaireResponse) {
    for (Reference reference : questionnaireResponse.getBasedOn()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : questionnaireResponse.getPartOf()) {
      removeDisplayFromReference(reference);
    }
    removeDisplayFromReference(questionnaireResponse.getSubject());
    removeDisplayFromReference(questionnaireResponse.getEncounter());
    removeDisplayFromReference(questionnaireResponse.getAuthor());
    removeDisplayFromReference(questionnaireResponse.getSource());
  }

  private void processClinicalImpression(ClinicalImpression clinicalImpression) {
    removeDisplayFromReference(clinicalImpression.getSubject());
    removeDisplayFromReference(clinicalImpression.getEncounter());
    removeDisplayFromReference(clinicalImpression.getAssessor());
    removeDisplayFromReference(clinicalImpression.getPrevious());
    for (Reference reference : clinicalImpression.getProblem()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : clinicalImpression.getPrognosisReference()) {
      removeDisplayFromReference(reference);
    }
    for (Reference reference : clinicalImpression.getSupportingInfo()) {
      removeDisplayFromReference(reference);
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
