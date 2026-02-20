package com.example.transformation.mapper;

import com.example.transformation.dto.canonical.CanonicalPayment;
import com.example.transformation.dto.visa.AccountPayoutRequest2;
import com.example.transformation.dto.visa.AccountTransactionDetail2;
import com.example.transformation.dto.visa.Bank1;
import com.example.transformation.dto.visa.PayoutToAccountRequest1RecipientDetail;
import com.example.transformation.dto.visa.PayoutToAccountRequestSenderDetail;
import com.example.transformation.dto.visa.RecipientAccountPayoutAddress1;
import com.example.transformation.dto.visa.SenderAccountPayoutAddress1;
import com.example.transformation.dto.visa.TransactionalStructuredRemittanceInner;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-19T22:50:14+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.8 (Oracle Corporation)"
)
@Component
public class CanonicalAccountPayoutMapperImpl implements CanonicalAccountPayoutMapper {

    @Override
    public AccountPayoutRequest2 toAccount(CanonicalPayment payment) {
        if ( payment == null ) {
            return null;
        }

        AccountPayoutRequest2 accountPayoutRequest2 = new AccountPayoutRequest2();

        if ( payment.getTransaction() != null ) {
            if ( accountPayoutRequest2.getRecipientDetail() == null ) {
                accountPayoutRequest2.setRecipientDetail( new PayoutToAccountRequest1RecipientDetail() );
            }
            canonicalTransactionToPayoutToAccountRequest1RecipientDetail( payment.getTransaction(), accountPayoutRequest2.getRecipientDetail() );
        }
        if ( accountPayoutRequest2.getRecipientDetail() == null ) {
            accountPayoutRequest2.setRecipientDetail( new PayoutToAccountRequest1RecipientDetail() );
        }
        canonicalPaymentToPayoutToAccountRequest1RecipientDetail( payment, accountPayoutRequest2.getRecipientDetail() );
        if ( payment.getTransaction() != null ) {
            if ( accountPayoutRequest2.getSenderDetail() == null ) {
                accountPayoutRequest2.setSenderDetail( new PayoutToAccountRequestSenderDetail() );
            }
            canonicalTransactionToPayoutToAccountRequestSenderDetail( payment.getTransaction(), accountPayoutRequest2.getSenderDetail() );
        }
        if ( accountPayoutRequest2.getSenderDetail() == null ) {
            accountPayoutRequest2.setSenderDetail( new PayoutToAccountRequestSenderDetail() );
        }
        canonicalPaymentToPayoutToAccountRequestSenderDetail( payment, accountPayoutRequest2.getSenderDetail() );
        if ( payment.getTransaction() != null ) {
            if ( accountPayoutRequest2.getTransactionDetail() == null ) {
                accountPayoutRequest2.setTransactionDetail( new AccountTransactionDetail2() );
            }
            canonicalTransactionToAccountTransactionDetail2( payment.getTransaction(), accountPayoutRequest2.getTransactionDetail() );
        }
        if ( accountPayoutRequest2.getTransactionDetail() == null ) {
            accountPayoutRequest2.setTransactionDetail( new AccountTransactionDetail2() );
        }
        canonicalPaymentToAccountTransactionDetail2( payment, accountPayoutRequest2.getTransactionDetail() );

        accountPayoutRequest2.setPayoutMethod( com.example.transformation.mapper.VisaPayoutMappingSupport.mapPayoutMethod(null, "B") );

        return accountPayoutRequest2;
    }

    protected void canonicalTransactionToBank1(CanonicalPayment.CanonicalTransaction canonicalTransaction, Bank1 mappingTarget) {
        if ( canonicalTransaction == null ) {
            return;
        }

        mappingTarget.setBankCode( canonicalTransaction.getCreditorAgentClearingId() );
        mappingTarget.setBankName( canonicalTransaction.getCreditorAgentName() );
        mappingTarget.setAccountName( canonicalTransaction.getCreditorAccountName() );
        mappingTarget.setAccountNumberType( VisaPayoutMappingSupport.mapAccountNumberType( canonicalTransaction.getCreditorAccountNumberType() ) );
    }

    protected void canonicalTransactionToRecipientAccountPayoutAddress1(CanonicalPayment.CanonicalTransaction canonicalTransaction, RecipientAccountPayoutAddress1 mappingTarget) {
        if ( canonicalTransaction == null ) {
            return;
        }

        mappingTarget.setAddressLine1( canonicalTransaction.getCreditorPostalStreet() );
        mappingTarget.setCity( canonicalTransaction.getCreditorPostalCity() );
        mappingTarget.setCountry( canonicalTransaction.getCreditorPostalCountry() );
        mappingTarget.setPostalCode( canonicalTransaction.getCreditorPostalPostalCode() );
    }

    protected void canonicalTransactionToPayoutToAccountRequest1RecipientDetail(CanonicalPayment.CanonicalTransaction canonicalTransaction, PayoutToAccountRequest1RecipientDetail mappingTarget) {
        if ( canonicalTransaction == null ) {
            return;
        }

        if ( mappingTarget.getBank() == null ) {
            mappingTarget.setBank( new Bank1() );
        }
        canonicalTransactionToBank1( canonicalTransaction, mappingTarget.getBank() );
        if ( mappingTarget.getAddress() == null ) {
            mappingTarget.setAddress( new RecipientAccountPayoutAddress1() );
        }
        canonicalTransactionToRecipientAccountPayoutAddress1( canonicalTransaction, mappingTarget.getAddress() );
        mappingTarget.setType( VisaPayoutMappingSupport.mapRecipientTypeEnum( canonicalTransaction.getRecipientType() ) );
        mappingTarget.setName( canonicalTransaction.getCreditorName() );
        mappingTarget.setContactEmail( canonicalTransaction.getCreditorContactEmail() );
    }

    protected void canonicalPaymentToBank1(CanonicalPayment canonicalPayment, Bank1 mappingTarget) {
        if ( canonicalPayment == null ) {
            return;
        }

        mappingTarget.setAccountNumber( VisaPayoutMappingSupport.bankAccountNumberFromCanonical( canonicalPayment.getTransaction() ) );
        mappingTarget.setCurrencyCode( VisaPayoutMappingSupport.bankCurrencyCodeFromCanonical( canonicalPayment.getTransaction() ) );
        mappingTarget.setCountryCode( VisaPayoutMappingSupport.bankCountryCodeFromCanonical( canonicalPayment.getTransaction() ) );
    }

    protected void canonicalPaymentToPayoutToAccountRequest1RecipientDetail(CanonicalPayment canonicalPayment, PayoutToAccountRequest1RecipientDetail mappingTarget) {
        if ( canonicalPayment == null ) {
            return;
        }

        if ( mappingTarget.getBank() == null ) {
            mappingTarget.setBank( new Bank1() );
        }
        canonicalPaymentToBank1( canonicalPayment, mappingTarget.getBank() );
        mappingTarget.setContactNumber( VisaPayoutMappingSupport.recipientContactNumberFromCanonical( canonicalPayment.getTransaction() ) );
        mappingTarget.setContactNumberType( VisaPayoutMappingSupport.mapRecipientContactNumberTypeFromCanonical( canonicalPayment.getTransaction() ) );
    }

    protected void canonicalTransactionToSenderAccountPayoutAddress1(CanonicalPayment.CanonicalTransaction canonicalTransaction, SenderAccountPayoutAddress1 mappingTarget) {
        if ( canonicalTransaction == null ) {
            return;
        }

        mappingTarget.setAddressLine1( canonicalTransaction.getDebtorPostalStreet() );
        mappingTarget.setCity( canonicalTransaction.getDebtorPostalCity() );
        mappingTarget.setCountry( canonicalTransaction.getDebtorPostalCountry() );
        mappingTarget.setPostalCode( canonicalTransaction.getDebtorPostalPostalCode() );
    }

    protected void canonicalTransactionToPayoutToAccountRequestSenderDetail(CanonicalPayment.CanonicalTransaction canonicalTransaction, PayoutToAccountRequestSenderDetail mappingTarget) {
        if ( canonicalTransaction == null ) {
            return;
        }

        if ( mappingTarget.getAddress() == null ) {
            mappingTarget.setAddress( new SenderAccountPayoutAddress1() );
        }
        canonicalTransactionToSenderAccountPayoutAddress1( canonicalTransaction, mappingTarget.getAddress() );
        mappingTarget.setName( canonicalTransaction.getDebtorName() );
        mappingTarget.setSenderAccountNumber( canonicalTransaction.getDebtorAccountIban() );
        mappingTarget.setContactEmail( canonicalTransaction.getDebtorContactEmail() );

        mappingTarget.setType( com.example.transformation.mapper.VisaPayoutMappingSupport.mapRecipientTypeEnum("C") );
    }

    protected void canonicalPaymentToPayoutToAccountRequestSenderDetail(CanonicalPayment canonicalPayment, PayoutToAccountRequestSenderDetail mappingTarget) {
        if ( canonicalPayment == null ) {
            return;
        }

        mappingTarget.setContactNumber( VisaPayoutMappingSupport.senderContactNumberFromCanonical( canonicalPayment.getTransaction() ) );
        mappingTarget.setContactNumberType( VisaPayoutMappingSupport.mapSenderContactNumberTypeFromCanonical( canonicalPayment.getTransaction() ) );
    }

    protected void canonicalTransactionToAccountTransactionDetail2(CanonicalPayment.CanonicalTransaction canonicalTransaction, AccountTransactionDetail2 mappingTarget) {
        if ( canonicalTransaction == null ) {
            return;
        }

        mappingTarget.setClientReferenceId( canonicalTransaction.getInstructionId() );
        mappingTarget.setInitiatingPartyId( canonicalTransaction.getInitiatingPartyId() );
        mappingTarget.setEndToEndId( canonicalTransaction.getEndToEndId() );
        mappingTarget.setPurposeOfPayment( canonicalTransaction.getPurposeCode() );
        mappingTarget.setQuoteId( canonicalTransaction.getFxQuoteId() );
        mappingTarget.setStatementNarrative( VisaPayoutMappingSupport.firstRemittance( canonicalTransaction.getRemittance() ) );
        if ( canonicalTransaction.getTransactionAmount() != null ) {
            mappingTarget.setTransactionAmount( canonicalTransaction.getTransactionAmount().doubleValue() );
        }
        else {
            mappingTarget.setTransactionAmount( null );
        }
        mappingTarget.setSettlementCurrencyCode( canonicalTransaction.getSettlementCurrencyCode() );
        mappingTarget.setTransactionCurrencyCode( canonicalTransaction.getTransactionCurrencyCode() );

        mappingTarget.setBusinessApplicationId( "PP" );
        mappingTarget.setFundingModel( com.example.transformation.mapper.VisaPayoutMappingSupport.mapFundingModel() );
        mappingTarget.setPaymentRail( com.example.transformation.mapper.VisaPayoutMappingSupport.mapPaymentRail() );
        mappingTarget.setPayoutSpeed( com.example.transformation.mapper.VisaPayoutMappingSupport.mapPayoutSpeed() );
    }

    protected void canonicalPaymentToAccountTransactionDetail2(CanonicalPayment canonicalPayment, AccountTransactionDetail2 mappingTarget) {
        if ( canonicalPayment == null ) {
            return;
        }

        mappingTarget.setTransactionAmount( VisaPayoutMappingSupport.transactionAmountFromCanonical( canonicalPayment.getTransaction() ) );
        mappingTarget.setTransactionCurrencyCode( VisaPayoutMappingSupport.transactionCurrencyCodeFromCanonical( canonicalPayment.getTransaction() ) );
        mappingTarget.setSettlementCurrencyCode( VisaPayoutMappingSupport.settlementCurrencyCodeFromCanonical( canonicalPayment.getTransaction() ) );
        if ( mappingTarget.getStructuredRemittance() != null ) {
            List<TransactionalStructuredRemittanceInner> list = VisaPayoutMappingSupport.structuredRemittanceFromCanonical( canonicalPayment.getTransaction() );
            if ( list != null ) {
                mappingTarget.getStructuredRemittance().clear();
                mappingTarget.getStructuredRemittance().addAll( list );
            }
            else {
                mappingTarget.setStructuredRemittance( null );
            }
        }
        else {
            List<TransactionalStructuredRemittanceInner> list = VisaPayoutMappingSupport.structuredRemittanceFromCanonical( canonicalPayment.getTransaction() );
            if ( list != null ) {
                mappingTarget.setStructuredRemittance( list );
            }
        }
    }
}
