package com.example.transformation.dto.visa;

public class PayoutToAccountRequest1RecipientDetail {
  public enum ContactNumberTypeEnum {
    MOBILE, WORK, HOME
  }

  private Bank1 bank;
  private String contactEmail;
  private String contactNumber;
  private ContactNumberTypeEnum contactNumberType;
  private String name;
  private Type type;
  private RecipientAccountPayoutAddress1 address;

  public Bank1 getBank() { return bank; }
  public void setBank(Bank1 bank) { this.bank = bank; }
  public String getContactEmail() { return contactEmail; }
  public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
  public String getContactNumber() { return contactNumber; }
  public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
  public ContactNumberTypeEnum getContactNumberType() { return contactNumberType; }
  public void setContactNumberType(ContactNumberTypeEnum contactNumberType) { this.contactNumberType = contactNumberType; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public Type getType() { return type; }
  public void setType(Type type) { this.type = type; }
  public RecipientAccountPayoutAddress1 getAddress() { return address; }
  public void setAddress(RecipientAccountPayoutAddress1 address) { this.address = address; }
}
