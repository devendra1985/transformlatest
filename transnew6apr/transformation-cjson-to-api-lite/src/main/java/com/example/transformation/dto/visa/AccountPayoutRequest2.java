package com.example.transformation.dto.visa;

public class AccountPayoutRequest2 {
  private AccountTransactionDetail2 transactionDetail;
  private PayoutMethod payoutMethod;
  private PayoutToAccountRequestSenderDetail senderDetail;
  private PayoutToAccountRequest1RecipientDetail recipientDetail;

  public AccountTransactionDetail2 getTransactionDetail() { return transactionDetail; }
  public void setTransactionDetail(AccountTransactionDetail2 transactionDetail) { this.transactionDetail = transactionDetail; }
  public PayoutMethod getPayoutMethod() { return payoutMethod; }
  public void setPayoutMethod(PayoutMethod payoutMethod) { this.payoutMethod = payoutMethod; }
  public PayoutToAccountRequestSenderDetail getSenderDetail() { return senderDetail; }
  public void setSenderDetail(PayoutToAccountRequestSenderDetail senderDetail) { this.senderDetail = senderDetail; }
  public PayoutToAccountRequest1RecipientDetail getRecipientDetail() { return recipientDetail; }
  public void setRecipientDetail(PayoutToAccountRequest1RecipientDetail recipientDetail) { this.recipientDetail = recipientDetail; }
}
