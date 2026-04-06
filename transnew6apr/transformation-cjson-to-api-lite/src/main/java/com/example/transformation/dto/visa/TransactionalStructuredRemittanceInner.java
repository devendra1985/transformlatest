package com.example.transformation.dto.visa;

public class TransactionalStructuredRemittanceInner {
  private Double amount;
  private String amountCurrencyCode;

  public Double getAmount() { return amount; }
  public void setAmount(Double amount) { this.amount = amount; }
  public String getAmountCurrencyCode() { return amountCurrencyCode; }
  public void setAmountCurrencyCode(String amountCurrencyCode) { this.amountCurrencyCode = amountCurrencyCode; }
}
