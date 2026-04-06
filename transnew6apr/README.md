# Transformation Layer

A **CJSON → API** transformation service built with **Java 21**, **Spring Boot**, and **Apache Camel**.

This repo is now scoped to a single flow: **VISABA_GBR_GBP_B2B_TEMPLATE**.  
It accepts a **single CJSON transaction**, maps it to the Visa OpenAPI DTO **`AccountPayoutRequest2`**, validates via Drools, and returns the transformed payload.

## Routing & Flow Decision (C1/C2/C3/C4)

Routing starts from the **cartridge code** in the routing key.  
Cartridge codes map to a **flowId** in `config/cartridge-master.yaml`, then the flowId
maps to **from/to** in `config/transformation-flow-master.yaml`.

Example routing key from input data:
`C1_VISABA_GBR_GBP_B2C`

Required CJSON fields for cartridge selection:
- `cartridgeCode` (C1/C2/C3/C4)
- `apiType` (PayoutAPI / FXAPI / ValidationAPI)
- `schemaType` (VISABA / VISAWA / VISACARDS)

Flow mapping in config:
- `C1` → `CJSON_TO_API`
- `C2` → `CJSON_TO_XML`
- `C3` → `API_TO_CJSON`
- `C4` → `XML_TO_API`

## Outbound Flow (CJSON → OpenAPI)

1) **Template selection + flow decision** (`RoutingProcessor`)
   - Reads `pymtSchemeTp`, `cdtrCtryOfRes`, `pymtAmtCcy`, `recipientType`
   - Builds routing key: `C1_VISABA_GBR_GBP_B2C` (example)
   - Resolves `flowId` from cartridge code (`C1` → `CJSON_TO_API`)
   - Loads bundle from `cartridge.json`

2) **Context resolution** (`CartridgeResolver`)
   - Resolves `templates/VISABA_GBR_GBP_B2B_TEMPLATE`
   - Camel endpoint: `direct:VISABA-VISABA_GBR_GBP_B2B_TEMPLATE`

3) **Camel pipeline** (`CartridgeRouteBuilder`)
   - `persistRaw` → `schemaValidate` → `routeResolve` → `validate`
   - `enrich` → `dtoTransform` → `externalApiCall` → `persistTransformed`

4) **Mapping + validation**
   - `CanonicalAccountPayoutMapper` → Visa OpenAPI DTO
   - `DroolsValidationService` → template rules only

## Class-by-class execution (example key: `C1_VISABA_GBR_GBP_B2C`)

1) **HTTP entry** → `TransformationController`  
   Receives CJSON, resolves `flowId` from cartridge code (C1 → `CJSON_TO_API`), sends to Camel route.

2) **Raw persistence** → `RawPersistProcessor`  
   Stores raw request if persistence is enabled.

3) **Schema validation** → `SchemaValidateProcessor`  
   Validates against `visa-sendpayout-input.schema.json`.

4) **Normalize to canonical** → `RoutingProcessor` + `CanonicalNormalizer`  
   CJSON → `CjsonPayoutRequest` → `CanonicalPayment`, builds routing key.

5) **Select template** → `CartridgeBundleLoader`  
   Uses routing key to load template paths from `cartridge.json`.

6) **Resolve context** → `CartridgeResolver`  
   Loads template paths + flow definition (`from/to`) from config.

7) **Validate + enrich** → `ValidateProcessor` + `EnrichProcessor`  
   Resolves context for rules + loads `enrich.yaml`.

8) **Map to OpenAPI** → `DtoTransformProcessor` + `CanonicalAccountPayoutMapper`  
   Canonical → `AccountPayoutRequest2`.

9) **External API call** → `ExternalApiProcessor`  
   Calls Visa API or returns DTO if invoke disabled.

10) **Persist result** → `TransformedPersistProcessor`  
    Stores transformed payload if persistence is enabled.

### Outbound Flow Diagram

```mermaid
flowchart TD
  A[HTTP CJSON Request] --> B[Schema Validate<br/>visa-sendpayout-input.schema.json]
  B --> C[Jackson → CjsonPayoutRequest]
  C --> D[CanonicalNormalizer → CanonicalPayment]
  D --> E[RoutingKeyResolver + cartridge-master.yaml]
  E --> F[cartridge.json lookup]
  F --> G[CartridgeResolver → template context]
  G --> H[EnrichProcessor (enrich.yaml)]
  H --> I[CanonicalAccountPayoutMapper → AccountPayoutRequest2]
  I --> J[DroolsValidationService (template rules)]
  J --> K[ExternalApiProcessor]
  K --> L[Response]
```

## OpenAPI Mapping (AccountPayoutRequest2)

Mapping is implemented in:
- `CanonicalAccountPayoutMapper`
- `VisaPayoutMappingSupport`
- `CanonicalNormalizer` (CJSON → Canonical)

### payoutMethod
- `payoutMethod` ← `payoutMethod` (default `B`)

### recipientDetail
- `recipientDetail.type` ← `recipientType` (`B` → `C`, else `I`)
- `recipientDetail.name` ← `cdtrNm`
- `recipientDetail.firstName` ← `recipientFirstName`
- `recipientDetail.lastName` ← `recipientLastName`
- `recipientDetail.address.addressLine1` ← `cdtrPstlAdrStrtNm`
- `recipientDetail.address.city` ← `cdtrPstlAdrTwnNm`
- `recipientDetail.address.country` ← `cdtrPstlAdrCtry`
- `recipientDetail.address.postalCode` ← `cdtrPstlAdrPstCd`
- `recipientDetail.contactEmail` ← `cdtrCtctEmailAdr`
- `recipientDetail.contactNumber` ← first of `cdtrCtctPhneNb` / `cdtrCtctMobNb` / `cdtrCtctFaxNb`
- `recipientDetail.contactNumberType` ← HOME / MOBILE / WORK based on which contact field is present

### recipientDetail.bank
- `recipientDetail.bank.bankCode` ← `cdtrAgtClrSysMmbId`
- `recipientDetail.bank.bankName` ← `cdtrAgtNm`
- `recipientDetail.bank.accountName` ← `cdtrAcctNm`
- `recipientDetail.bank.accountNumber` ← `crAcctIban` or `cdtrAcctOthrId`
- `recipientDetail.bank.accountNumberType` ← `accountNumberType` (`T` → `DEFAULT`)
- `recipientDetail.bank.currencyCode` ← `cdtrAcctCcy` or `crAcctCcy`
- `recipientDetail.bank.countryCode` ← `cdtrCtryOfRes` or `cdtrPstlAdrCtry`

### senderDetail
- `senderDetail.name` ← `dbtrNm`
- `senderDetail.type` ← constant `C`
- `senderDetail.senderAccountNumber` ← `dbtrAcctIban`
- `senderDetail.address.addressLine1` ← `dbtrPstlAdrStrtNm`
- `senderDetail.address.city` ← `dbtrPstlAdrTwnNm`
- `senderDetail.address.country` ← `dbtrPstlAdrCtry`
- `senderDetail.address.postalCode` ← `dbtrPstlAdrPstCd`
- `senderDetail.contactEmail` ← `dbtrCtctEmailAdr`
- `senderDetail.contactNumber` ← first of `dbtrCtctPhneNb` / `dbtrCtctMobNb` / `dbtrCtctFaxNb`
- `senderDetail.contactNumberType` ← HOME / MOBILE / WORK based on which contact field is present

### transactionDetail
- `transactionDetail.businessApplicationId` ← constant `PP`
- `transactionDetail.clientReferenceId` ← `instrId`
- `transactionDetail.initiatingPartyId` ← `initgPtyNm` (if numeric) else `initgPtyOrgIdOthr[0].id`
- `transactionDetail.transactionAmount` ← `pymtAmt` or `crPymtAmt`
- `transactionDetail.transactionCurrencyCode` ← `pymtAmtCcy` or `crPymtAmtCcy`
- `transactionDetail.settlementCurrencyCode` ← `cdtrAcctCcy` or `crAcctCcy`
- `transactionDetail.endToEndId` ← `endToEndId`
- `transactionDetail.purposeOfPayment` ← `purpCd`
- `transactionDetail.quoteId` ← `fxQuoteId`
- `transactionDetail.statementNarrative` ← first element of `rmtInfUstrd`
- `transactionDetail.structuredRemittance` ← `duePyblAmt` + `duePyblAmtCcy`
- `transactionDetail.paymentRail` ← constant `SWIFT`
- `transactionDetail.payoutSpeed` ← constant `STANDARD`
- `transactionDetail.fundingModel` ← constant `PREFUNDED`

## Template Layout

```
src/main/resources/cartridges/VISA/VISABA/templates/
  VISABA_GBR_GBP_B2B_TEMPLATE/
    route.yaml
    enrich.yaml
    rules/
```

## Run

```bash
mvn clean compile
mvn spring-boot:run
```

Disable external Visa call and return transformed DTO:

```bash
export APP_VISA_INVOKE_ENABLED=false
mvn spring-boot:run
```

## Test (Outbound)

```bash
curl -X POST "http://localhost:8080/api/transform/VISABA" \
  -H "Content-Type: application/json" \
  -d '{
    "paymentData": {
      "txInf": [
        {
          "pymtSchemeTp": "VISABA",
          "cdtrCtryOfRes": "GBR",
          "pymtAmtCcy": "GBP",
          "recipientType": "B"
        }
      ]
    }
  }'
```

Expected log:
`Resolved context: endpoint=direct:VISABA-VISABA_GBR_GBP_B2B_TEMPLATE`

## Add Wallet Payout (If Needed)

Wallet flow is currently removed. To add it back:
- Re‑add `WalletPayoutMapper` and wallet DTO mappings.
- Restore wallet rules (e.g., `rules/wallet.drl`).
- Add wallet templates under `cartridges/VISA/VISABA/templates/<TEMPLATE>/`.
- Update `cartridge.json` with a wallet entry (e.g., `"mapper": "WALLET"`).
- Update routing to select wallet (for example using `payoutMethod = W`).

## Add New Country/Currency Template

1) Create template folder:
```
src/main/resources/cartridges/VISA/VISABA/templates/VISABA_<COUNTRY>_<CURRENCY>_B2B_TEMPLATE/
```
2) Copy files from the current GBR template:
   - `route.yaml`
   - `enrich.yaml`
   - `rules/account.drl` (update package if needed)
3) Add routing entry in `cartridge.json`:
```
"C2_VISABA_<COUNTRY>_<CURRENCY>_B2B": {
  "cartridgeId": "VISABA",
  "templateId": "VISABA_<COUNTRY>_<CURRENCY>_B2B_TEMPLATE",
  "enrichPath": "cartridges/VISA/VISABA/templates/VISABA_<COUNTRY>_<CURRENCY>_B2B_TEMPLATE/enrich.yaml",
  "rulesPath": "cartridges/VISA/VISABA/templates/VISABA_<COUNTRY>_<CURRENCY>_B2B_TEMPLATE/rules",
  "routePath": "cartridges/VISA/VISABA/templates/VISABA_<COUNTRY>_<CURRENCY>_B2B_TEMPLATE/route.yaml"
}
```
4) Send CJSON with:
   - `pymtSchemeTp = VISABA`
   - `cdtrCtryOfRes = <COUNTRY>`
   - `pymtAmtCcy = <CURRENCY>`
   - `recipientType = B`
