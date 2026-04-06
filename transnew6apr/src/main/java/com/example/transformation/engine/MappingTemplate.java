package com.example.transformation.engine;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Data;

/**
 * One mapping template per API type. Loaded from JSON on classpath.
 *
 * <p>Example:
 * <pre>
 * {
 *   "apiType": "CREATE_PAYMENT",
 *   "targetClass": "com.example.transformation.dto.visa.AccountPayoutRequest2",
 *   "rules": [
 *     { "source": "transaction.creditorName",  "target": "recipientDetail.name" },
 *     { "source": "transaction.instructionId",  "target": "transactionDetail.clientReferenceId" },
 *     { "target": "transactionDetail.businessApplicationId", "defaultValue": "PP" },
 *     { "source": "transaction.transactionAmount", "target": "transactionDetail.transactionAmount", "transform": "toMoney" }
 *   ]
 * }
 * </pre>
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MappingTemplate {

    private String apiType;
    private String targetClass;
    private List<MappingRule> rules;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MappingRule {
        /** Dot-path into the source CanonicalPayment (e.g. "transaction.creditorName"). Null if using defaultValue only. */
        private String source;
        /** Dot-path into the target API DTO (e.g. "recipientDetail.bank.accountNumber"). */
        private String target;
        /** Static value to use when source is null or source path is not specified. */
        private String defaultValue;
        /** Named transform function to apply (e.g. "toMoney", "uppercase", "recipientTypeEnum"). */
        private String transform;
    }
}
