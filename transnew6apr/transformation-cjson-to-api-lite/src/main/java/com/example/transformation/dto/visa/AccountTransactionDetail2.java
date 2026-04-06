package com.example.transformation.dto.visa;

import java.util.List;

public class AccountTransactionDetail2 {
  public enum PaymentRailEnum {
    SWIFT
  }

  private String businessApplicationId;
  private String clientReferenceId;
  private Long initiatingPartyId;
  private Double transactionAmount;
  private String transactionCurrencyCode;
  private String settlementCurrencyCode;
  private String endToEndId;
  private String purposeOfPayment;
  private Long quoteId;
  private String statementNarrative;
  private List<TransactionalStructuredRemittanceInner> structuredRemittance;
  private FundingModel1 fundingModel;
  private PaymentRailEnum paymentRail;
  private PayoutSpeed payoutSpeed;

  public String getBusinessApplicationId() { return businessApplicationId; }
  public void setBusinessApplicationId(String businessApplicationId) { this.businessApplicationId = businessApplicationId; }
  public String getClientReferenceId() { return clientReferenceId; }
  public void setClientReferenceId(String clientReferenceId) { this.clientReferenceId = clientReferenceId; }
  public Long getInitiatingPartyId() { return initiatingPartyId; }
  public void setInitiatingPartyId(Long initiatingPartyId) { this.initiatingPartyId = initiatingPartyId; }
  public Double getTransactionAmount() { return transactionAmount; }
  public void setTransactionAmount(Double transactionAmount) { this.transactionAmount = transactionAmount; }
  public String getTransactionCurrencyCode() { return transactionCurrencyCode; }
  public void setTransactionCurrencyCode(String transactionCurrencyCode) { this.transactionCurrencyCode = transactionCurrencyCode; }
  public String getSettlementCurrencyCode() { return settlementCurrencyCode; }
  public void setSettlementCurrencyCode(String settlementCurrencyCode) { this.settlementCurrencyCode = settlementCurrencyCode; }
  public String getEndToEndId() { return endToEndId; }
  public void setEndToEndId(String endToEndId) { this.endToEndId = endToEndId; }
  public String getPurposeOfPayment() { return purposeOfPayment; }
  public void setPurposeOfPayment(String purposeOfPayment) { this.purposeOfPayment = purposeOfPayment; }
  public Long getQuoteId() { return quoteId; }
  public void setQuoteId(Long quoteId) { this.quoteId = quoteId; }
  public String getStatementNarrative() { return statementNarrative; }
  public void setStatementNarrative(String statementNarrative) { this.statementNarrative = statementNarrative; }
  public List<TransactionalStructuredRemittanceInner> getStructuredRemittance() { return structuredRemittance; }
  public void setStructuredRemittance(List<TransactionalStructuredRemittanceInner> structuredRemittance) { this.structuredRemittance = structuredRemittance; }
  public FundingModel1 getFundingModel() { return fundingModel; }
  public void setFundingModel(FundingModel1 fundingModel) { this.fundingModel = fundingModel; }
  public PaymentRailEnum getPaymentRail() { return paymentRail; }
  public void setPaymentRail(PaymentRailEnum paymentRail) { this.paymentRail = paymentRail; }
  public PayoutSpeed getPayoutSpeed() { return payoutSpeed; }
  public void setPayoutSpeed(PayoutSpeed payoutSpeed) { this.payoutSpeed = payoutSpeed; }
}
