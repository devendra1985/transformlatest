package com.example.transformation.normalize;

import com.example.transformation.dto.canonical.CanonicalPayment;
import com.example.transformation.dto.canonical.CanonicalPayment.CanonicalBatch;
import com.example.transformation.dto.canonical.CanonicalPayment.CanonicalGroup;
import com.example.transformation.dto.canonical.CanonicalPayment.CanonicalHeader;
import com.example.transformation.dto.canonical.CanonicalPayment.CanonicalTransaction;
import com.example.transformation.dto.cjson.CjsonPayoutRequest;
import java.math.BigDecimal;
import java.util.List;

public class CanonicalNormalizer {

  public CanonicalPayment normalize(CjsonPayoutRequest request) {
    CanonicalPayment payment = new CanonicalPayment();
    payment.setHeader(normalizeHeader(request));
    payment.setGroup(normalizeGroup(request));
    payment.setBatch(normalizeBatch(request));
    payment.setTransaction(normalizeTransaction(request));
    return payment;
  }

  private CanonicalHeader normalizeHeader(CjsonPayoutRequest request) {
    CanonicalHeader header = new CanonicalHeader();
    if (request.getHeader() != null) {
      header.setMsgType(request.getHeader().getMsgTp());
      header.setCorrelationId(request.getHeader().getCorrelationId());
    }
    return header;
  }

  private CanonicalGroup normalizeGroup(CjsonPayoutRequest request) {
    CanonicalGroup group = new CanonicalGroup();
    CjsonPayoutRequest.GroupHeader grp = first(request.getPaymentData() != null
        ? request.getPaymentData().getGrpHdr() : null);
    if (grp != null) {
      group.setGroupId(grp.getGrpId());
      group.setReceiverBic(grp.getRcvrBic());
      group.setSenderBic(grp.getSndrBic());
      group.setMessageId(grp.getMsgId());
      group.setCreatedDateTime(grp.getCreDtTm());
      group.setNumberOfTransactions(grp.getNbOfTxs());
      group.setControlSum(grp.getCtrlSum());
    }
    return group;
  }

  private CanonicalBatch normalizeBatch(CjsonPayoutRequest request) {
    CanonicalBatch batch = new CanonicalBatch();
    CjsonPayoutRequest.BulkInfo bulk = first(request.getPaymentData() != null
        ? request.getPaymentData().getBulk() : null);
    if (bulk != null) {
      batch.setBulkId(bulk.getBulkId());
      batch.setGroupId(bulk.getGrpId());
      batch.setAccountBatchId(bulk.getAcctBtchId());
      batch.setPaymentInfoId(bulk.getPymtInfId());
      batch.setNumberOfTransactions(bulk.getNbOfTxs());
      batch.setControlSum(bulk.getCtrlSum());
    }
    return batch;
  }

  private CanonicalTransaction normalizeTransaction(CjsonPayoutRequest request) {
    CanonicalTransaction tx = new CanonicalTransaction();
    CjsonPayoutRequest.TxInf source = first(request.getPaymentData() != null
        ? request.getPaymentData().getTxInf() : null);
    if (source == null) {
      return tx;
    }

    String schema = source.getExeData() != null ? source.getExeData().getPymtSchemeTp() : null;
    tx.setPaymentSchema(schema);
    tx.setSchemaType(firstNonBlank(source.getSchemaType(), schema));
    tx.setApiType(source.getApiType());
    tx.setCartridgeCode(source.getCartridgeCode());
    tx.setCountry(firstNonBlank(source.getCdtrCtryOfRes(), source.getCdtrPstlAdrCtry()));
    tx.setCurrency(firstNonBlank(source.getPymtAmtCcy(), source.getCrPymtAmtCcy(), source.getCrAcctCcy()));
    tx.setRecipientType(source.getRecipientType());

    tx.setInitiatingPartyId(parseInitiatingPartyId(
        source.getInitgPtyNm(), source.getInitgPtyOrgIdOthr()));
    tx.setPaymentId(source.getPaymentId());
    tx.setInstructionId(source.getInstrId());
    tx.setEndToEndId(source.getEndToEndId());
    tx.setPurposeCode(source.getPurpCd());
    tx.setFxQuoteId(source.getFxQuoteId());
    tx.setTransactionAmount(firstNonNull(source.getPymtAmt(), source.getCrPymtAmt()));
    tx.setTransactionCurrencyCode(firstNonBlank(source.getPymtAmtCcy(), source.getCrPymtAmtCcy()));
    tx.setSettlementAmount(firstNonNull(source.getCrPymtAmt(), source.getPymtAmt()));
    tx.setSettlementCurrencyCode(firstNonBlank(
        source.getCdtrAcctCcy(), source.getCrAcctCcy(), source.getCrPymtAmtCcy()));

    tx.setCreditorName(source.getCdtrNm());
    tx.setCreditorAccountName(source.getCdtrAcctNm());
    tx.setCreditorAccountIban(source.getCrAcctIban());
    tx.setCreditorAccountOtherId(source.getCdtrAcctOthrId());
    tx.setCreditorAccountCurrency(firstNonBlank(source.getCdtrAcctCcy(), source.getCrAcctCcy()));
    tx.setCreditorCountryOfResidence(source.getCdtrCtryOfRes());
    tx.setCreditorPostalCountry(source.getCdtrPstlAdrCtry());
    tx.setCreditorPostalCity(source.getCdtrPstlAdrTwnNm());
    tx.setCreditorPostalStreet(source.getCdtrPstlAdrStrtNm());
    tx.setCreditorPostalPostalCode(source.getCdtrPstlAdrPstCd());
    tx.setCreditorContactEmail(source.getCdtrCtctEmailAdr());
    tx.setCreditorContactPhone(source.getCdtrCtctPhneNb());
    tx.setCreditorContactMobile(source.getCdtrCtctMobNb());
    tx.setCreditorContactFax(source.getCdtrCtctFaxNb());
    tx.setCreditorAgentBic(source.getCdtrAgtBic());
    tx.setCreditorAgentName(source.getCdtrAgtNm());
    tx.setCreditorAgentClearingId(source.getCdtrAgtClrSysMmbId());
    tx.setCreditorAccountNumberType(source.getAccountNumberType());
    tx.setCreditorBankCodeType(source.getBankCodeType());

    tx.setDebtorName(source.getDbtrNm());
    tx.setDebtorAccountIban(source.getDbtrAcctIban());
    tx.setDebtorPostalCountry(source.getDbtrPstlAdrCtry());
    tx.setDebtorPostalCity(source.getDbtrPstlAdrTwnNm());
    tx.setDebtorPostalStreet(source.getDbtrPstlAdrStrtNm());
    tx.setDebtorPostalPostalCode(source.getDbtrPstlAdrPstCd());
    tx.setDebtorContactEmail(source.getDbtrCtctEmailAdr());
    tx.setDebtorContactPhone(source.getDbtrCtctPhneNb());
    tx.setDebtorContactMobile(source.getDbtrCtctMobNb());
    tx.setDebtorContactFax(source.getDbtrCtctFaxNb());

    tx.setDuePayableAmount(source.getDuePyblAmt());
    tx.setDuePayableAmountCurrency(source.getDuePyblAmtCcy());
    tx.setRemittance(source.getRmtInfUstrd());

    return tx;
  }

  private static Long parseInitiatingPartyId(
      String initgPtyNm, List<CjsonPayoutRequest.InitgPtyOrgIdOthr> ids) {
    Long fromName = parseLong(initgPtyNm);
    if (fromName != null) {
      return fromName;
    }
    if (ids == null || ids.isEmpty()) {
      return null;
    }
    return parseLong(ids.get(0).getId());
  }

  private static Long parseLong(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return Long.parseLong(value.trim());
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  private static <T> T first(List<T> list) {
    return (list == null || list.isEmpty()) ? null : list.get(0);
  }

  private static BigDecimal firstNonNull(BigDecimal first, BigDecimal second) {
    return first != null ? first : second;
  }

  private static String firstNonBlank(String... values) {
    if (values == null) {
      return null;
    }
    for (String value : values) {
      if (value != null && !value.isBlank()) {
        return value;
      }
    }
    return null;
  }
}
