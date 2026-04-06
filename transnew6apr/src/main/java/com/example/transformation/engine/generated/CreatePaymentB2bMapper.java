package com.example.transformation.engine.generated;

import com.example.transformation.dto.canonical.CanonicalPayment;
import com.example.transformation.dto.canonical.CanonicalPayment.CanonicalTransaction;
import com.example.transformation.dto.visa.AccountPayoutRequest2;
import com.example.transformation.dto.visa.AccountTransactionDetail2;
import com.example.transformation.dto.visa.Bank1;
import com.example.transformation.dto.visa.PayoutMethod;
import com.example.transformation.dto.visa.PayoutToAccountRequest1RecipientDetail;
import com.example.transformation.dto.visa.PayoutToAccountRequestSenderDetail;
import com.example.transformation.dto.visa.RecipientAccountPayoutAddress1;
import com.example.transformation.dto.visa.SenderAccountPayoutAddress1;
import com.example.transformation.dto.visa.Type;
import com.example.transformation.engine.CartridgeMapper;
import com.example.transformation.engine.TransformFunctions;
import org.springframework.stereotype.Component;

/**
 * Generated cartridge mapper for template: CREATE_PAYMENT_B2B
 *
 * <p>GENERATED CODE — DO NOT EDIT.
 * Modify the JSON template and re-run the build.
 */
@Component
public final class CreatePaymentB2bMapper implements CartridgeMapper<AccountPayoutRequest2> {

    @Override
    public String templateKey() { return "CREATE_PAYMENT_B2B"; }

    @Override
    public AccountPayoutRequest2 map(CanonicalPayment source) {
        AccountPayoutRequest2 target = new AccountPayoutRequest2();
        CanonicalTransaction txn = source.getTransaction();
        if (txn == null) return target;

        // default: payoutMethod = "B"
        target.setPayoutMethod(PayoutMethod.fromValue("B"));

        // ── transactionDetail ────────────────────────────────────────────────
        AccountTransactionDetail2 transactionDetail = new AccountTransactionDetail2();
        transactionDetail.setBusinessApplicationId("PP");
        if (txn.getInstructionId() != null) transactionDetail.setClientReferenceId(txn.getInstructionId());
        if (txn.getInitiatingPartyId() != null) transactionDetail.setInitiatingPartyId(txn.getInitiatingPartyId());
        if (txn.getTransactionAmount() != null) transactionDetail.setTransactionAmount(((Number) TransformFunctions.toMoney((Object) txn.getTransactionAmount())).doubleValue());
        if (txn.getTransactionCurrencyCode() != null) transactionDetail.setTransactionCurrencyCode(txn.getTransactionCurrencyCode());
        if (txn.getSettlementCurrencyCode() != null) transactionDetail.setSettlementCurrencyCode(txn.getSettlementCurrencyCode());
        if (txn.getEndToEndId() != null) transactionDetail.setEndToEndId(txn.getEndToEndId());
        if (txn.getPurposeCode() != null) transactionDetail.setPurposeOfPayment(txn.getPurposeCode());
        if (txn.getFxQuoteId() != null) transactionDetail.setQuoteId(txn.getFxQuoteId());
        if (txn.getRemittance() != null) transactionDetail.setStatementNarrative((String) TransformFunctions.firstElement((Object) txn.getRemittance()));

        // ── recipientDetail ──────────────────────────────────────────────────
        PayoutToAccountRequest1RecipientDetail recipientDetail = new PayoutToAccountRequest1RecipientDetail();
        if (txn.getCreditorName() != null) recipientDetail.setName(txn.getCreditorName());
        if (txn.getRecipientType() != null) recipientDetail.setType(Type.fromValue(String.valueOf(TransformFunctions.recipientType((Object) txn.getRecipientType()))));
        if (txn.getCreditorContactEmail() != null) recipientDetail.setContactEmail(txn.getCreditorContactEmail());
        if (txn.getCreditorContactPhone() != null) recipientDetail.setContactNumber(txn.getCreditorContactPhone());

        Bank1 recipientDetailBank = new Bank1();
        if (txn.getCreditorAgentClearingId() != null) recipientDetailBank.setBankCode(txn.getCreditorAgentClearingId());
        if (txn.getCreditorAgentName() != null) recipientDetailBank.setBankName(txn.getCreditorAgentName());
        if (txn.getCreditorAccountName() != null) recipientDetailBank.setAccountName(txn.getCreditorAccountName());
        if (txn.getCreditorAccountIban() != null) recipientDetailBank.setAccountNumber(txn.getCreditorAccountIban());
        if (txn.getCreditorAccountCurrency() != null) recipientDetailBank.setCurrencyCode(txn.getCreditorAccountCurrency());
        if (txn.getCreditorPostalCountry() != null) recipientDetailBank.setCountryCode(txn.getCreditorPostalCountry());
        if (txn.getCreditorAccountNumberType() != null) recipientDetailBank.setAccountNumberType(Bank1.AccountNumberTypeEnum.fromValue(String.valueOf(txn.getCreditorAccountNumberType())));
        recipientDetail.setBank(recipientDetailBank);

        RecipientAccountPayoutAddress1 recipientDetailAddress = new RecipientAccountPayoutAddress1();
        if (txn.getCreditorPostalStreet() != null) recipientDetailAddress.setAddressLine1(txn.getCreditorPostalStreet());
        if (txn.getCreditorPostalCity() != null) recipientDetailAddress.setCity(txn.getCreditorPostalCity());
        if (txn.getCreditorPostalCountry() != null) recipientDetailAddress.setCountry(txn.getCreditorPostalCountry());
        if (txn.getCreditorPostalPostalCode() != null) recipientDetailAddress.setPostalCode(txn.getCreditorPostalPostalCode());
        recipientDetail.setAddress(recipientDetailAddress);

        // ── senderDetail ─────────────────────────────────────────────────────
        PayoutToAccountRequestSenderDetail senderDetail = new PayoutToAccountRequestSenderDetail();
        if (txn.getDebtorName() != null) senderDetail.setName(txn.getDebtorName());
        if (txn.getDebtorAccountIban() != null) senderDetail.setSenderAccountNumber(txn.getDebtorAccountIban());
        if (txn.getDebtorContactEmail() != null) senderDetail.setContactEmail(txn.getDebtorContactEmail());
        if (txn.getDebtorContactPhone() != null) senderDetail.setContactNumber(txn.getDebtorContactPhone());

        SenderAccountPayoutAddress1 senderDetailAddress = new SenderAccountPayoutAddress1();
        if (txn.getDebtorPostalStreet() != null) senderDetailAddress.setAddressLine1(txn.getDebtorPostalStreet());
        if (txn.getDebtorPostalCity() != null) senderDetailAddress.setCity(txn.getDebtorPostalCity());
        if (txn.getDebtorPostalCountry() != null) senderDetailAddress.setCountry(txn.getDebtorPostalCountry());
        if (txn.getDebtorPostalPostalCode() != null) senderDetailAddress.setPostalCode(txn.getDebtorPostalPostalCode());
        senderDetail.setAddress(senderDetailAddress);

        // ── wire back into target ────────────────────────────────────────────
        target.setTransactionDetail(transactionDetail);
        target.setRecipientDetail(recipientDetail);
        target.setSenderDetail(senderDetail);
        return target;
    }
}
