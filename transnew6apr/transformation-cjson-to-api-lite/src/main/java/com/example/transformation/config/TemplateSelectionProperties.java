package com.example.transformation.config;

public class TemplateSelectionProperties {
  private String templateIdFormat;
  private String recipientTypeBusiness;
  private String recipientTypeIndividual;
  private String segmentBusiness;
  private String segmentIndividual;

  public String templateIdFormat() {
    return templateIdFormat;
  }

  public void setTemplateIdFormat(String templateIdFormat) {
    this.templateIdFormat = templateIdFormat;
  }

  public String recipientTypeBusiness() {
    return recipientTypeBusiness;
  }

  public void setRecipientTypeBusiness(String recipientTypeBusiness) {
    this.recipientTypeBusiness = recipientTypeBusiness;
  }

  public String recipientTypeIndividual() {
    return recipientTypeIndividual;
  }

  public void setRecipientTypeIndividual(String recipientTypeIndividual) {
    this.recipientTypeIndividual = recipientTypeIndividual;
  }

  public String segmentBusiness() {
    return segmentBusiness;
  }

  public void setSegmentBusiness(String segmentBusiness) {
    this.segmentBusiness = segmentBusiness;
  }

  public String segmentIndividual() {
    return segmentIndividual;
  }

  public void setSegmentIndividual(String segmentIndividual) {
    this.segmentIndividual = segmentIndividual;
  }
}
