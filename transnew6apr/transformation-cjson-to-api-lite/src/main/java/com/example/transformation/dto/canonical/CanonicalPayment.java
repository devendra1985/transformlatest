package com.example.transformation.dto.canonical;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class CanonicalPayment {
  private CanonicalHeader header;
  private CanonicalGroup group;
  private CanonicalBatch batch;
  private CanonicalTransaction transaction;

  @Data
  public static class CanonicalHeader {
    private String msgType;
    private String correlationId;
  }

  @Data
  public static class CanonicalGroup {
    private String groupId;
    private String receiverBic;
    private String senderBic;
    private String messageId;
    private String createdDateTime;
    private Integer numberOfTransactions;
    private BigDecimal controlSum;
  }

  @Data
  public static class CanonicalBatch {
    private String bulkId;
    private String groupId;
    private String accountBatchId;
    private String paymentInfoId;
    private Integer numberOfTransactions;
    private BigDecimal controlSum;
  }

  @Data
  public static class CanonicalTransaction {
    private String paymentSchema;
    private String schemaType;
    private String apiType;
    private String country;
    private String currency;
    private String recipientType;
    private String cartridgeCode;
    private Long initiatingPartyId;

    private String paymentId;
    private String instructionId;
    private String endToEndId;
    private String purposeCode;
    private Long fxQuoteId;
    private BigDecimal transactionAmount;
    private String transactionCurrencyCode;
    private BigDecimal settlementAmount;
    private String settlementCurrencyCode;

    private String creditorName;
    private String creditorAccountName;
    private String creditorAccountIban;
    private String creditorAccountOtherId;
    private String creditorAccountCurrency;
    private String creditorCountryOfResidence;
    private String creditorPostalCountry;
    private String creditorPostalCity;
    private String creditorPostalStreet;
    private String creditorPostalPostalCode;
    private String creditorContactEmail;
    private String creditorContactPhone;
    private String creditorContactMobile;
    private String creditorContactFax;
    private String creditorAgentBic;
    private String creditorAgentName;
    private String creditorAgentClearingId;
    private String creditorAccountNumberType;
    private String creditorBankCodeType;

    private String debtorName;
    private String debtorAccountIban;
    private String debtorPostalCountry;
    private String debtorPostalCity;
    private String debtorPostalStreet;
    private String debtorPostalPostalCode;
    private String debtorContactEmail;
    private String debtorContactPhone;
    private String debtorContactMobile;
    private String debtorContactFax;

    private BigDecimal duePayableAmount;
    private String duePayableAmountCurrency;
    private List<String> remittance;
  }
}
