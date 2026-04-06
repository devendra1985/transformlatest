package com.example.transformation.mapper;

import com.example.transformation.dto.canonical.CanonicalPayment;
import com.example.transformation.dto.visa.AccountPayoutRequest2;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "default",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = VisaPayoutMappingSupport.class
)
public interface CanonicalAccountPayoutMapper {

  @Mapping(
      target = "payoutMethod",
      expression = "java(com.example.transformation.mapper.VisaPayoutMappingSupport.mapPayoutMethod(null, \"B\"))"
  )
  @Mapping(
      target = "recipientDetail.type",
      source = "transaction.recipientType",
      qualifiedByName = "recipientTypeEnum"
  )
  @Mapping(target = "recipientDetail.name", source = "transaction.creditorName")
  @Mapping(target = "recipientDetail.bank.bankCode", source = "transaction.creditorAgentClearingId")
  @Mapping(target = "recipientDetail.bank.bankName", source = "transaction.creditorAgentName")
  @Mapping(target = "recipientDetail.bank.accountName", source = "transaction.creditorAccountName")
  @Mapping(target = "recipientDetail.bank.accountNumber", source = "transaction",
      qualifiedByName = "bankAccountNumberFromCanonical")
  @Mapping(target = "recipientDetail.bank.currencyCode", source = "transaction",
      qualifiedByName = "bankCurrencyCodeFromCanonical")
  @Mapping(target = "recipientDetail.bank.countryCode", source = "transaction",
      qualifiedByName = "bankCountryCodeFromCanonical")
  @Mapping(
      target = "recipientDetail.bank.accountNumberType",
      source = "transaction.creditorAccountNumberType",
      qualifiedByName = "bankAccountNumberType"
  )
  @Mapping(target = "recipientDetail.address.addressLine1", source = "transaction.creditorPostalStreet")
  @Mapping(target = "recipientDetail.address.city", source = "transaction.creditorPostalCity")
  @Mapping(target = "recipientDetail.address.country", source = "transaction.creditorPostalCountry")
  @Mapping(target = "recipientDetail.address.postalCode", source = "transaction.creditorPostalPostalCode")
  @Mapping(target = "recipientDetail.contactEmail", source = "transaction.creditorContactEmail")
  @Mapping(target = "recipientDetail.contactNumber", source = "transaction",
      qualifiedByName = "recipientContactNumberFromCanonical")
  @Mapping(
      target = "recipientDetail.contactNumberType",
      source = "transaction",
      qualifiedByName = "recipientContactNumberTypeFromCanonical"
  )
  @Mapping(target = "senderDetail.name", source = "transaction.debtorName")
  @Mapping(
      target = "senderDetail.type",
      expression = "java(com.example.transformation.mapper.VisaPayoutMappingSupport.mapRecipientTypeEnum(\"C\"))"
  )
  @Mapping(target = "senderDetail.senderAccountNumber", source = "transaction.debtorAccountIban")
  @Mapping(target = "senderDetail.address.addressLine1", source = "transaction.debtorPostalStreet")
  @Mapping(target = "senderDetail.address.city", source = "transaction.debtorPostalCity")
  @Mapping(target = "senderDetail.address.country", source = "transaction.debtorPostalCountry")
  @Mapping(target = "senderDetail.address.postalCode", source = "transaction.debtorPostalPostalCode")
  @Mapping(target = "senderDetail.contactEmail", source = "transaction.debtorContactEmail")
  @Mapping(target = "senderDetail.contactNumber", source = "transaction",
      qualifiedByName = "senderContactNumberFromCanonical")
  @Mapping(
      target = "senderDetail.contactNumberType",
      source = "transaction",
      qualifiedByName = "senderContactNumberTypeFromCanonical"
  )
  @Mapping(target = "transactionDetail.businessApplicationId", constant = "PP")
  @Mapping(target = "transactionDetail.clientReferenceId", source = "transaction.instructionId")
  @Mapping(target = "transactionDetail.initiatingPartyId", source = "transaction.initiatingPartyId")
  @Mapping(
      target = "transactionDetail.transactionAmount",
      source = "transaction",
      qualifiedByName = "transactionAmountFromCanonical"
  )
  @Mapping(
      target = "transactionDetail.transactionCurrencyCode",
      source = "transaction",
      qualifiedByName = "transactionCurrencyCodeFromCanonical"
  )
  @Mapping(
      target = "transactionDetail.settlementCurrencyCode",
      source = "transaction",
      qualifiedByName = "settlementCurrencyCodeFromCanonical"
  )
  @Mapping(target = "transactionDetail.endToEndId", source = "transaction.endToEndId")
  @Mapping(target = "transactionDetail.purposeOfPayment", source = "transaction.purposeCode")
  @Mapping(target = "transactionDetail.quoteId", source = "transaction.fxQuoteId")
  @Mapping(
      target = "transactionDetail.statementNarrative",
      source = "transaction.remittance",
      qualifiedByName = "firstRemittance"
  )
  @Mapping(
      target = "transactionDetail.structuredRemittance",
      source = "transaction",
      qualifiedByName = "structuredRemittanceFromCanonical"
  )
  @Mapping(
      target = "transactionDetail.fundingModel",
      expression = "java(com.example.transformation.mapper.VisaPayoutMappingSupport.mapFundingModel())"
  )
  @Mapping(
      target = "transactionDetail.paymentRail",
      expression = "java(com.example.transformation.mapper.VisaPayoutMappingSupport.mapPaymentRail())"
  )
  @Mapping(
      target = "transactionDetail.payoutSpeed",
      expression = "java(com.example.transformation.mapper.VisaPayoutMappingSupport.mapPayoutSpeed())"
  )
  AccountPayoutRequest2 toAccount(CanonicalPayment payment);
}
