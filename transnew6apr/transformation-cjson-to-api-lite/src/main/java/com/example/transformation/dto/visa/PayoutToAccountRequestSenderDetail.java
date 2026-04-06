package com.example.transformation.dto.visa;

public class PayoutToAccountRequestSenderDetail {
  public enum ContactNumberTypeEnum {
    MOBILE, WORK, HOME
  }

  private SenderAccountPayoutAddress1 address;
  private String contactEmail;
  private String contactNumber;
  private ContactNumberTypeEnum contactNumberType;
  private String senderAccountNumber;
  private String senderReferenceNumber;
  private String name;
  private Type type;

  public SenderAccountPayoutAddress1 getAddress() { return address; }
  public void setAddress(SenderAccountPayoutAddress1 address) { this.address = address; }
  public String getContactEmail() { return contactEmail; }
  public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
  public String getContactNumber() { return contactNumber; }
  public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
  public ContactNumberTypeEnum getContactNumberType() { return contactNumberType; }
  public void setContactNumberType(ContactNumberTypeEnum contactNumberType) { this.contactNumberType = contactNumberType; }
  public String getSenderAccountNumber() { return senderAccountNumber; }
  public void setSenderAccountNumber(String senderAccountNumber) { this.senderAccountNumber = senderAccountNumber; }
  public String getSenderReferenceNumber() { return senderReferenceNumber; }
  public void setSenderReferenceNumber(String senderReferenceNumber) { this.senderReferenceNumber = senderReferenceNumber; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public Type getType() { return type; }
  public void setType(Type type) { this.type = type; }
}
