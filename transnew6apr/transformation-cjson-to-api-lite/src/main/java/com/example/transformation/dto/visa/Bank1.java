package com.example.transformation.dto.visa;

public class Bank1 {
  public enum AccountNumberTypeEnum {
    DEFAULT("DEFAULT");

    private final String value;

    AccountNumberTypeEnum(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    public static AccountNumberTypeEnum fromValue(String value) {
      for (AccountNumberTypeEnum v : values()) {
        if (v.value.equals(value)) {
          return v;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  private String bankCode;
  private String bankName;
  private String accountName;
  private String accountNumber;
  private AccountNumberTypeEnum accountNumberType;
  private String currencyCode;
  private String countryCode;

  public String getBankCode() { return bankCode; }
  public void setBankCode(String bankCode) { this.bankCode = bankCode; }
  public String getBankName() { return bankName; }
  public void setBankName(String bankName) { this.bankName = bankName; }
  public String getAccountName() { return accountName; }
  public void setAccountName(String accountName) { this.accountName = accountName; }
  public String getAccountNumber() { return accountNumber; }
  public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
  public AccountNumberTypeEnum getAccountNumberType() { return accountNumberType; }
  public void setAccountNumberType(AccountNumberTypeEnum accountNumberType) { this.accountNumberType = accountNumberType; }
  public String getCurrencyCode() { return currencyCode; }
  public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
  public String getCountryCode() { return countryCode; }
  public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
}
