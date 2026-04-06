package com.example.transformation.mapper;

import com.example.transformation.dto.canonical.CanonicalPayment.CanonicalTransaction;
import com.example.transformation.dto.cjson.CjsonPayoutRequest;
import com.example.transformation.dto.visa.AccountTransactionDetail2;
import com.example.transformation.dto.visa.Bank1;
import com.example.transformation.dto.visa.FundingModel1;
import com.example.transformation.dto.visa.PayoutMethod;
import com.example.transformation.dto.visa.PayoutSpeed;
import com.example.transformation.dto.visa.PayoutToAccountRequest1RecipientDetail;
import com.example.transformation.dto.visa.PayoutToAccountRequestSenderDetail;
import com.example.transformation.dto.visa.TransactionalStructuredRemittanceInner;
import com.example.transformation.dto.visa.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.mapstruct.Named;

public final class VisaPayoutMappingSupport {
  private VisaPayoutMappingSupport() {}

  @Named("firstRemittance")
  public static String firstRemittance(List<String> values) {
    if (values == null || values.isEmpty()) {
      return null;
    }
    return values.get(0);
  }

  private static String mapRecipientType(String recipientType) {
    if (recipientType == null || recipientType.isBlank()) {
      return "I";
    }
    if ("B".equalsIgnoreCase(recipientType)) {
      return "C";
    }
    return recipientType;
  }

  @Named("recipientTypeEnum")
  public static Type mapRecipientTypeEnum(String recipientType) {
    return Type.fromValue(mapRecipientType(recipientType));
  }

  @Named("initiatingPartyId")
  public static Long parseInitiatingPartyId(String initiatingParty) {
    if (initiatingParty == null || initiatingParty.isBlank()) {
      return null;
    }
    try {
      return Long.parseLong(initiatingParty.trim());
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  @Named("initiatingPartyIdFromRequest")
  public static Long initiatingPartyIdFromRequest(CjsonPayoutRequest request) {
    if (request == null) {
      return null;
    }
    Long fromName = parseInitiatingPartyId(request.getInitgPtyNm());
    if (fromName != null) {
      return fromName;
    }
    List<CjsonPayoutRequest.InitgPtyOrgIdOthr> ids = request.getInitgPtyOrgIdOthr();
    if (ids == null || ids.isEmpty()) {
      return null;
    }
    return parseInitiatingPartyId(ids.get(0).getId());
  }

  public static List<TransactionalStructuredRemittanceInner> structuredRemittance(
      Double amount, String currency) {
    if (amount == null && (currency == null || currency.isBlank())) {
      return null;
    }
    TransactionalStructuredRemittanceInner item = new TransactionalStructuredRemittanceInner();
    if (amount != null) {
      item.setAmount(amount);
    }
    if (currency != null) {
      item.setAmountCurrencyCode(currency);
    }
    List<TransactionalStructuredRemittanceInner> list = new ArrayList<>();
    list.add(item);
    return list;
  }

  @Named("bankAccountNumberType")
  public static Bank1.AccountNumberTypeEnum mapAccountNumberType(String type) {
    if (type == null || type.isBlank()) {
      return Bank1.AccountNumberTypeEnum.DEFAULT;
    }
    if ("T".equalsIgnoreCase(type)) {
      return Bank1.AccountNumberTypeEnum.DEFAULT;
    }
    try {
      return Bank1.AccountNumberTypeEnum.fromValue(type);
    } catch (IllegalArgumentException ex) {
      return Bank1.AccountNumberTypeEnum.DEFAULT;
    }
  }

  @Named("bankAccountNumber")
  public static String bankAccountNumber(CjsonPayoutRequest request) {
    if (request == null) {
      return null;
    }
    return firstNonBlank(request.getCrAcctIban(), request.getCdtrAcctOthrId());
  }

  @Named("bankAccountNumberFromCanonical")
  public static String bankAccountNumberFromCanonical(CanonicalTransaction tx) {
    if (tx == null) {
      return null;
    }
    return firstNonBlank(tx.getCreditorAccountIban(), tx.getCreditorAccountOtherId());
  }

  @Named("bankCurrencyCode")
  public static String bankCurrencyCode(CjsonPayoutRequest request) {
    if (request == null) {
      return null;
    }
    return firstNonBlank(request.getCdtrAcctCcy(), request.getCrAcctCcy());
  }

  @Named("bankCurrencyCodeFromCanonical")
  public static String bankCurrencyCodeFromCanonical(CanonicalTransaction tx) {
    if (tx == null) {
      return null;
    }
    return firstNonBlank(tx.getCreditorAccountCurrency(), tx.getSettlementCurrencyCode());
  }

  @Named("bankCountryCode")
  public static String bankCountryCode(CjsonPayoutRequest request) {
    if (request == null) {
      return null;
    }
    return firstNonBlank(request.getCdtrCtryOfRes(), request.getCdtrPstlAdrCtry());
  }

  @Named("bankCountryCodeFromCanonical")
  public static String bankCountryCodeFromCanonical(CanonicalTransaction tx) {
    if (tx == null) {
      return null;
    }
    return firstNonBlank(tx.getCreditorCountryOfResidence(), tx.getCreditorPostalCountry());
  }

  @Named("recipientContactNumber")
  public static String recipientContactNumber(CjsonPayoutRequest request) {
    if (request == null) {
      return null;
    }
    return firstNonBlank(request.getCdtrCtctPhneNb(), request.getCdtrCtctMobNb(),
        request.getCdtrCtctFaxNb());
  }

  @Named("recipientContactNumberFromCanonical")
  public static String recipientContactNumberFromCanonical(CanonicalTransaction tx) {
    if (tx == null) {
      return null;
    }
    return firstNonBlank(tx.getCreditorContactPhone(), tx.getCreditorContactMobile(),
        tx.getCreditorContactFax());
  }

  @Named("senderContactNumber")
  public static String senderContactNumber(CjsonPayoutRequest request) {
    if (request == null) {
      return null;
    }
    return firstNonBlank(request.getDbtrCtctPhneNb(), request.getDbtrCtctMobNb(),
        request.getDbtrCtctFaxNb());
  }

  @Named("senderContactNumberFromCanonical")
  public static String senderContactNumberFromCanonical(CanonicalTransaction tx) {
    if (tx == null) {
      return null;
    }
    return firstNonBlank(tx.getDebtorContactPhone(), tx.getDebtorContactMobile(),
        tx.getDebtorContactFax());
  }

  @Named("recipientContactNumberType")
  public static PayoutToAccountRequest1RecipientDetail.ContactNumberTypeEnum mapRecipientContactNumberType(
      CjsonPayoutRequest request) {
    if (request == null) {
      return null;
    }
    if (isNonBlank(request.getCdtrCtctPhneNb())) {
      return PayoutToAccountRequest1RecipientDetail.ContactNumberTypeEnum.HOME;
    }
    if (isNonBlank(request.getCdtrCtctMobNb())) {
      return PayoutToAccountRequest1RecipientDetail.ContactNumberTypeEnum.MOBILE;
    }
    if (isNonBlank(request.getCdtrCtctFaxNb())) {
      return PayoutToAccountRequest1RecipientDetail.ContactNumberTypeEnum.WORK;
    }
    return null;
  }

  @Named("recipientContactNumberTypeFromCanonical")
  public static PayoutToAccountRequest1RecipientDetail.ContactNumberTypeEnum mapRecipientContactNumberTypeFromCanonical(
      CanonicalTransaction tx) {
    if (tx == null) {
      return null;
    }
    if (isNonBlank(tx.getCreditorContactPhone())) {
      return PayoutToAccountRequest1RecipientDetail.ContactNumberTypeEnum.HOME;
    }
    if (isNonBlank(tx.getCreditorContactMobile())) {
      return PayoutToAccountRequest1RecipientDetail.ContactNumberTypeEnum.MOBILE;
    }
    if (isNonBlank(tx.getCreditorContactFax())) {
      return PayoutToAccountRequest1RecipientDetail.ContactNumberTypeEnum.WORK;
    }
    return null;
  }

  @Named("senderContactNumberType")
  public static PayoutToAccountRequestSenderDetail.ContactNumberTypeEnum mapSenderContactNumberType(
      CjsonPayoutRequest request) {
    if (request == null) {
      return null;
    }
    if (isNonBlank(request.getDbtrCtctPhneNb())) {
      return PayoutToAccountRequestSenderDetail.ContactNumberTypeEnum.HOME;
    }
    if (isNonBlank(request.getDbtrCtctMobNb())) {
      return PayoutToAccountRequestSenderDetail.ContactNumberTypeEnum.MOBILE;
    }
    if (isNonBlank(request.getDbtrCtctFaxNb())) {
      return PayoutToAccountRequestSenderDetail.ContactNumberTypeEnum.WORK;
    }
    return null;
  }

  @Named("senderContactNumberTypeFromCanonical")
  public static PayoutToAccountRequestSenderDetail.ContactNumberTypeEnum mapSenderContactNumberTypeFromCanonical(
      CanonicalTransaction tx) {
    if (tx == null) {
      return null;
    }
    if (isNonBlank(tx.getDebtorContactPhone())) {
      return PayoutToAccountRequestSenderDetail.ContactNumberTypeEnum.HOME;
    }
    if (isNonBlank(tx.getDebtorContactMobile())) {
      return PayoutToAccountRequestSenderDetail.ContactNumberTypeEnum.MOBILE;
    }
    if (isNonBlank(tx.getDebtorContactFax())) {
      return PayoutToAccountRequestSenderDetail.ContactNumberTypeEnum.WORK;
    }
    return null;
  }

  @Named("transactionAmountFromCanonical")
  public static Double transactionAmountFromCanonical(CanonicalTransaction tx) {
    if (tx == null) {
      return null;
    }
    return toMoney(tx.getTransactionAmount());
  }

  @Named("transactionCurrencyCodeFromCanonical")
  public static String transactionCurrencyCodeFromCanonical(CanonicalTransaction tx) {
    if (tx == null) {
      return null;
    }
    return firstNonBlank(tx.getTransactionCurrencyCode(), tx.getSettlementCurrencyCode());
  }

  @Named("settlementCurrencyCodeFromCanonical")
  public static String settlementCurrencyCodeFromCanonical(CanonicalTransaction tx) {
    if (tx == null) {
      return null;
    }
    return firstNonBlank(tx.getSettlementCurrencyCode(), tx.getTransactionCurrencyCode());
  }

  @Named("structuredRemittanceFromRequest")
  public static List<TransactionalStructuredRemittanceInner> structuredRemittanceFromRequest(
      CjsonPayoutRequest request) {
    if (request == null) {
      return null;
    }
    Double amount = request.getDuePyblAmt() == null ? null : request.getDuePyblAmt().doubleValue();
    return structuredRemittance(amount, request.getDuePyblAmtCcy());
  }

  @Named("structuredRemittanceFromCanonical")
  public static List<TransactionalStructuredRemittanceInner> structuredRemittanceFromCanonical(
      CanonicalTransaction tx) {
    if (tx == null) {
      return null;
    }
    return structuredRemittance(toMoney(tx.getDuePayableAmount()), tx.getDuePayableAmountCurrency());
  }

  private static Double toMoney(BigDecimal value) {
    if (value == null) {
      return null;
    }
    return value.setScale(2, RoundingMode.HALF_UP).doubleValue();
  }

  public static PayoutMethod mapPayoutMethod(String value, String fallback) {
    String chosen = (value == null || value.isBlank()) ? fallback : value;
    try {
      return PayoutMethod.fromValue(chosen);
    } catch (IllegalArgumentException ex) {
      return PayoutMethod.fromValue(fallback);
    }
  }

  public static FundingModel1 mapFundingModel() {
    return FundingModel1.PREFUNDED;
  }

  public static PayoutSpeed mapPayoutSpeed() {
    return PayoutSpeed.STANDARD;
  }

  public static AccountTransactionDetail2.PaymentRailEnum mapPaymentRail() {
    return AccountTransactionDetail2.PaymentRailEnum.SWIFT;
  }

  private static String firstNonBlank(String... values) {
    if (values == null) {
      return null;
    }
    for (String value : values) {
      if (isNonBlank(value)) {
        return value;
      }
    }
    return null;
  }

  private static boolean isNonBlank(String value) {
    return value != null && !value.isBlank();
  }
}
