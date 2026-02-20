package com.example.transformation.dto.cjson;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CjsonPayoutRequest {
  private Header header;
  private PaymentData paymentData;

  private final Map<String, Object> additionalProperties = new HashMap<>();

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {
    additionalProperties.put(name, value);
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return additionalProperties;
  }

  // Convenience getters for mappers (first txInf entry)
  @JsonIgnore
  public String getPayoutMethod() {
    return txInfValue(TxInf::getPayoutMethod);
  }

  @JsonIgnore
  public String getRecipientType() {
    return txInfValue(TxInf::getRecipientType);
  }

  @JsonIgnore
  public String getRecipientFirstName() {
    return txInfValue(TxInf::getRecipientFirstName);
  }

  @JsonIgnore
  public String getRecipientLastName() {
    return txInfValue(TxInf::getRecipientLastName);
  }

  @JsonIgnore
  public String getCdtrAgtClrSysMmbId() {
    return txInfValue(TxInf::getCdtrAgtClrSysMmbId);
  }

  @JsonIgnore
  public String getCdtrAgtNm() {
    return txInfValue(TxInf::getCdtrAgtNm);
  }

  @JsonIgnore
  public String getCdtrAcctNm() {
    return txInfValue(TxInf::getCdtrAcctNm);
  }

  @JsonIgnore
  public String getCdtrNm() {
    return txInfValue(TxInf::getCdtrNm);
  }

  @JsonIgnore
  public String getAccountNumberType() {
    return txInfValue(TxInf::getAccountNumberType);
  }

  @JsonIgnore
  public String getBankCodeType() {
    return txInfValue(TxInf::getBankCodeType);
  }

  @JsonIgnore
  public String getCdtrPstlAdrStrtNm() {
    return txInfValue(TxInf::getCdtrPstlAdrStrtNm);
  }

  @JsonIgnore
  public String getCdtrPstlAdrTwnNm() {
    return txInfValue(TxInf::getCdtrPstlAdrTwnNm);
  }

  @JsonIgnore
  public String getCdtrPstlAdrCtry() {
    return txInfValue(TxInf::getCdtrPstlAdrCtry);
  }

  @JsonIgnore
  public String getCdtrPstlAdrPstCd() {
    return txInfValue(TxInf::getCdtrPstlAdrPstCd);
  }

  @JsonIgnore
  public String getCdtrCtctEmailAdr() {
    return txInfValue(TxInf::getCdtrCtctEmailAdr);
  }

  @JsonIgnore
  public String getCdtrCtctPhneNb() {
    return txInfValue(TxInf::getCdtrCtctPhneNb);
  }

  @JsonIgnore
  public String getCdtrCtctMobNb() {
    return txInfValue(TxInf::getCdtrCtctMobNb);
  }

  @JsonIgnore
  public String getCdtrCtctFaxNb() {
    return txInfValue(TxInf::getCdtrCtctFaxNb);
  }

  @JsonIgnore
  public String getDbtrNm() {
    return txInfValue(TxInf::getDbtrNm);
  }

  @JsonIgnore
  public String getSenderType() {
    return txInfValue(TxInf::getSenderType);
  }

  @JsonIgnore
  public String getDbtrAcctIban() {
    return txInfValue(TxInf::getDbtrAcctIban);
  }

  @JsonIgnore
  public String getDbtrPstlAdrStrtNm() {
    return txInfValue(TxInf::getDbtrPstlAdrStrtNm);
  }

  @JsonIgnore
  public String getDbtrPstlAdrTwnNm() {
    return txInfValue(TxInf::getDbtrPstlAdrTwnNm);
  }

  @JsonIgnore
  public String getDbtrPstlAdrCtry() {
    return txInfValue(TxInf::getDbtrPstlAdrCtry);
  }

  @JsonIgnore
  public String getDbtrPstlAdrPstCd() {
    return txInfValue(TxInf::getDbtrPstlAdrPstCd);
  }

  @JsonIgnore
  public String getDbtrCtctEmailAdr() {
    return txInfValue(TxInf::getDbtrCtctEmailAdr);
  }

  @JsonIgnore
  public String getDbtrCtctPhneNb() {
    return txInfValue(TxInf::getDbtrCtctPhneNb);
  }

  @JsonIgnore
  public String getDbtrCtctMobNb() {
    return txInfValue(TxInf::getDbtrCtctMobNb);
  }

  @JsonIgnore
  public String getDbtrCtctFaxNb() {
    return txInfValue(TxInf::getDbtrCtctFaxNb);
  }

  @JsonIgnore
  public String getInstrId() {
    return txInfValue(TxInf::getInstrId);
  }

  @JsonIgnore
  public String getInitgPtyNm() {
    return txInfValue(TxInf::getInitgPtyNm);
  }

  @JsonIgnore
  public String getEndToEndId() {
    return txInfValue(TxInf::getEndToEndId);
  }

  @JsonIgnore
  public String getPurpCd() {
    return txInfValue(TxInf::getPurpCd);
  }

  @JsonIgnore
  public Long getFxQuoteId() {
    return txInfValue(TxInf::getFxQuoteId);
  }

  @JsonIgnore
  public List<String> getRmtInfUstrd() {
    return txInfValue(TxInf::getRmtInfUstrd);
  }

  @JsonIgnore
  public String getCrAcctIban() {
    return txInfValue(TxInf::getCrAcctIban);
  }

  @JsonIgnore
  public String getCdtrAcctOthrId() {
    return txInfValue(TxInf::getCdtrAcctOthrId);
  }

  @JsonIgnore
  public String getCdtrAcctCcy() {
    return txInfValue(TxInf::getCdtrAcctCcy);
  }

  @JsonIgnore
  public String getCrAcctCcy() {
    return txInfValue(TxInf::getCrAcctCcy);
  }

  @JsonIgnore
  public String getCdtrCtryOfRes() {
    return txInfValue(TxInf::getCdtrCtryOfRes);
  }

  @JsonIgnore
  public Double getPymtAmt() {
    BigDecimal value = txInfValue(TxInf::getPymtAmt);
    return value == null ? null : value.doubleValue();
  }

  @JsonIgnore
  public BigDecimal getCrPymtAmt() {
    return txInfValue(TxInf::getCrPymtAmt);
  }

  @JsonIgnore
  public String getPymtAmtCcy() {
    return txInfValue(TxInf::getPymtAmtCcy);
  }

  @JsonIgnore
  public String getCrPymtAmtCcy() {
    return txInfValue(TxInf::getCrPymtAmtCcy);
  }

  @JsonIgnore
  public BigDecimal getDuePyblAmt() {
    return txInfValue(TxInf::getDuePyblAmt);
  }

  @JsonIgnore
  public String getDuePyblAmtCcy() {
    return txInfValue(TxInf::getDuePyblAmtCcy);
  }

  @JsonIgnore
  public List<InitgPtyOrgIdOthr> getInitgPtyOrgIdOthr() {
    return txInfValue(TxInf::getInitgPtyOrgIdOthr);
  }

  private <T> T txInfValue(java.util.function.Function<TxInf, T> getter) {
    TxInf txInf = firstTxInf();
    return txInf == null ? null : getter.apply(txInf);
  }

  private TxInf firstTxInf() {
    if (paymentData == null || paymentData.getTxInf() == null || paymentData.getTxInf().isEmpty()) {
      return null;
    }
    return paymentData.getTxInf().get(0);
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Header {
    private String msgTp;
    private String correlationId;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class PaymentData {
    private List<GroupHeader> grpHdr;
    private List<BulkInfo> bulk;
    private List<TxInf> txInf;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class GroupHeader {
    private String grpId;
    @Pattern(regexp = "^[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?$")
    private String rcvrBic;
    @Pattern(regexp = "^[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?$")
    private String sndrBic;
    private String msgId;
    private String creDtTm;
    private Integer nbOfTxs;
    private BigDecimal ctrlSum;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class BulkInfo {
    private String bulkId;
    private String grpId;
    private String acctBtchId;
    private String pymtInfId;
    private Integer nbOfTxs;
    private BigDecimal ctrlSum;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class TxInf {
    private ExeData exeData;
    private String cartridgeCode;
    private String apiType;
    private String schemaType;
    private String payoutMethod;
    private String paymentId;
    private String grpId;
    private String orgnlGrpId;
    private String bulkId;
    private String sysCreDtTm;
    private String sourceSysId;
    private String pymtRef;
    private String parentPymtRef;
    private String brnchCd;
    private String orgnlWorkflowId;
    private String workflowId;
    private Integer evSeqId;
    private String eventSource;
    private String parentId;
    private String routingSlip;
    private String currentStep;
    private String src;
    private String chanl;
    @Pattern(regexp = "^[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?$")
    private String rcvrBic;
    @Pattern(regexp = "^[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?$")
    private String sndrBic;
    private String acctBtchId;
    private String releaseDt;
    private String releaseTm;
    private String careTp;
    private Integer fundChkCOECnt;
    private String prcsngDt;
    private String drValDt;
    private String crValDt;
    @Pattern(regexp = "^[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?$")
    private String orgnlDbtrAgtBic;
    @Pattern(regexp = "^[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?$")
    private String drAgtBic;
    private String drAcctRefId;
    private String orgnlDbtrAcctIban;
    private String drAcctIban;
    @Pattern(regexp = "^[A-Z]{3}$")
    private String drAcctCcy;
    private BigDecimal drPymtAmt;
    @Pattern(regexp = "^[A-Z]{3}$")
    private String drPymtAmtCcy;
    private String drRtrn;
    private String drItmTpCd;
    @Pattern(regexp = "^[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?$")
    private String orgnlCdtrAgtBic;
    @Pattern(regexp = "^[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?$")
    private String crAgtBic;
    private String crAcctRefId;
    private String orgnlCdtrAcctIban;
    private String crAcctIban;
    private String purpCd;
    @Pattern(regexp = "^[A-Z]{3}$")
    private String crAcctCcy;
    private String crRtrn;
    private String crItmTpCd;
    private BigDecimal basePymtAmt;
    @Pattern(regexp = "^[A-Z]{3}$")
    private String basePymtAmtCcy;
    private BigDecimal crPymtAmt;
    @Pattern(regexp = "^[A-Z]{3}$")
    private String crPymtAmtCcy;
    private String eventDtTm;
    private String eventDescription;
    private String instrId;
    private String endToEndId;
    private Long fxQuoteId;
    private String uetr;
    private String pymtInfId;
    private String msgId;
    private String orgnlMsgId;
    private String orgnlPymtInfId;
    private String orgnlCreDtTm;
    private String orgnlInstrId;
    private String orgnlEndToEndId;
    private String msgTp;
    private String initgPtyNm;
    private String initgPtyPstlAdrCtry;
    @Pattern(regexp = "^[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?$")
    private String initgPtyOrgIdBic;
    private List<InitgPtyOrgIdOthr> initgPtyOrgIdOthr;
    private String initgPtyCtryOfRes;
    private String pmtMtd;
    private String dbtrNm;
    private String senderType;
    private String dbtrPstlAdrStrtNm;
    private String dbtrPstlAdrBldgNb;
    private String dbtrPstlAdrPstCd;
    private String dbtrPstlAdrTwnNm;
    private String dbtrPstlAdrCtry;
    private String ultmtDbtrNm;
    private String ultmtDbtrPstlAdrCtry;
    private String ultmtDbtrPstlAdrLine1;
    private List<SvcLvl> svcLvl;
    private String ctgyPurpCd;
    private BigDecimal pymtAmt;
    @Pattern(regexp = "^[A-Z]{3}$")
    private String pymtAmtCcy;
    private BigDecimal instdAmt;
    @Pattern(regexp = "^[A-Z]{3}$")
    private String instdAmtCcy;
    private BigDecimal orgnlPymtAmt;
    @Pattern(regexp = "^[A-Z]{3}$")
    private String orgnlPymtAmtCcy;
    private String cdtrAcctNm;
    private String valDt;
    private String orgnlValDt;
    private String ultmtDbtrNm2;
    private String ultmtDbtrPstlAdrCtry2;
    private String cdtrAcctNm2;
    private String cdtrNm;
    private String cdtrPstlAdrStrtNm;
    private String cdtrPstlAdrBldgNb;
    private String cdtrPstlAdrPstCd;
    private String cdtrPstlAdrTwnNm;
    private String cdtrPstlAdrCtry;
    @Email
    private String cdtrCtctEmailAdr;
    private String cdtrCtctPhneNb;
    private String cdtrCtctMobNb;
    private String cdtrCtctFaxNb;
    private String recipientType;
    private String recipientFirstName;
    private String recipientLastName;
    private String cdtrAgtClrSysMmbId;
    private String ultmtCdtrPstlAdrLine1;
    private String dbtrAcctIban;
    private String dbtrCtctEmailAdr;
    private String dbtrCtctPhneNb;
    private String dbtrCtctMobNb;
    private String dbtrCtctFaxNb;
    @Pattern(regexp = "^[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?$")
    private String cdtrAgtBic;
    private String cdtrAgtNm;
    private String cdtrAgtPstlAdrCtry;
    @Pattern(regexp = "^[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?$")
    private String dbtrAgtBic;
    private String dbtrAgtNm;
    private String dbtrAgtPstlAdrCtry;
    private String cdtrAcctOthrId;
    private String accountNumberType;
    private String bankCodeType;
    private String cdtrCtryOfRes;
    @Pattern(regexp = "^[A-Z]{3}$")
    private String cdtrAcctCcy;
    private List<String> rmtInfUstrd;
    private String sourceCorrelationId;
    private BigDecimal duePyblAmt;
    @Pattern(regexp = "^[A-Z]{3}$")
    private String duePyblAmtCcy;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ExeData {
    private String pymtSts;
    private String dupChkValdtnSts;
    private String pymtSchemeTp;
    private String orIgPymtSchemeTp;
    private String pymtSchemeCutOffTm;
    private String valdnSts;
    private String sancSts;
    private String drSttlmTp;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class InitgPtyOrgIdOthr {
    private String id;
    private String schmeNmCd;
    private String issr;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class SvcLvl {
    private String cd;
  }
}
