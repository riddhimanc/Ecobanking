
package com.ecobank.core.services;

import com.ecobank.core.domain.CustomerOpportunityRepository;
import com.ecobank.core.domain.CustomerRepository;
import com.ecobank.core.domain.ProductRepository;
import com.ecobank.core.domain.AiOpportunitySuggestionDTO;

import com.ecobank.core.models.Customer;
import com.ecobank.core.models.CustomerOpportunity;
import com.ecobank.core.models.Account;
import com.ecobank.core.models.Transactions;   
import com.ecobank.core.models.products;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CustomerOpportunityServices
 * - Catalog-aware, rule-based "AI" suggestions over balances + transactions
 * - Maps suggestions to PRODUCTS and inserts rows into CUSTOMER_OPPORTUNITIES
 * - Existing CRUD/filter methods preserved
 */
@Service
public class CustomerOpportunityServices {

    private final CustomerOpportunityRepository opps;
    private final CustomerRepository customers;
    private final AccountServices accountServices;
    private final TransactionServices transactionServices;
    private final ProductRepository productRepository;

    public CustomerOpportunityServices(CustomerOpportunityRepository opps,
                                       CustomerRepository customers,
                                       AccountServices accountServices,
                                       TransactionServices transactionServices,
                                       ProductRepository productRepository) {
        this.opps = opps;
        this.customers = customers;
        this.accountServices = accountServices;
        this.transactionServices = transactionServices;
        this.productRepository = productRepository;
    }

    /* -------- SELECT -------- */

    @Transactional(readOnly = true)
    public List<CustomerOpportunity> getOpportunitiesByCustomer(Long customerId) {
        return opps.findByCustomer_Id(customerId);
    }

    @Transactional(readOnly = true)
    public CustomerOpportunity getOpportunityById(Long id) {
        return opps.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Opportunity not found: " + id));
    }

    /* -------- INSERT -------- */

    @Transactional
    public CustomerOpportunity createOpportunity(Long customerId, Long productId, String assignedStaff, String status) {
        Customer customer = customers.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));
        CustomerOpportunity co = new CustomerOpportunity();
        co.setCustomer(customer);
        co.setProductId(productId); // FK for Product
        co.setAssignedStaff(assignedStaff);
        co.setStatus(status);
        return opps.save(co);
    }

    /* -------- UPDATE -------- */

    @Transactional
    public CustomerOpportunity updateOpportunity(Long id, String assignedStaff, String status) {
        CustomerOpportunity co = opps.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Opportunity not found: " + id));
        if (assignedStaff != null && !assignedStaff.isBlank()) co.setAssignedStaff(assignedStaff);
        if (status != null && !status.isBlank()) co.setStatus(status);
        return opps.save(co);
    }

    /* -------- DELETE -------- */

    @Transactional
    public void deleteOpportunity(Long id) {
        opps.deleteById(id);
    }

    /* -------- Filters -------- */

    @Transactional(readOnly = true)
    public List<CustomerOpportunity> getPending(Long customerId) {
        return opps.findByCustomer_IdAndStatusIn(customerId, List.of("NEW", "CONTACTED", "PENDING"));
    }

    @Transactional(readOnly = true)
    public List<CustomerOpportunity> getFulfilled(Long customerId) {
        return opps.findByCustomer_IdAndStatusIn(customerId, List.of("WON", "FULFILLED", "CLOSED"));
    }

    /* ============================================================
     *                       AI COMPONENT
     * ============================================================ */

    /**
     * Analyze all accounts + recent transactions and produce suggestions mapped to your catalog.
     *
     * @param customerId   customer id
     * @param lookbackDays analysis window, e.g., 90
     * @return suggestions (immutable record DTO)
     */
    @Transactional(readOnly = true)
    public List<AiOpportunitySuggestionDTO> analyzeCustomerForSuggestions(Long customerId, int lookbackDays) {
        Customer customer = customers.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));

        List<Account> accounts = accountServices.getAccountsByCustomerId(customerId);
        if (accounts == null || accounts.isEmpty()) return List.of();

        LocalDateTime since = LocalDateTime.now().minusDays(lookbackDays);

        BigDecimal totalBalance   = BigDecimal.ZERO;
        BigDecimal recentDebits   = BigDecimal.ZERO;
        BigDecimal recentCredits  = BigDecimal.ZERO;
        int salaryCredits         = 0;
        int fuelPayments          = 0; // vehicle indicator
        int rentOrHousing         = 0; // home/housing indicator

        List<AiOpportunitySuggestionDTO> suggestions = new ArrayList<>();

        for (Account acc : accounts) {
            totalBalance = totalBalance.add(toBigDecimal(acc.getBalance()));

            // Transactions by accountNo (adapt if you prefer accountId)
            List<Transactions> txs = transactionServices.getTransactionsByAccountNo(acc.getAccountNo());
            if (txs == null || txs.isEmpty()) continue;

            List<Transactions> recent = txs.stream()
                    .filter(t -> t.getCreatedAt() != null && !t.getCreatedAt().isBefore(since))
                    .collect(Collectors.toList());

            for (Transactions t : recent) {
                BigDecimal amt = toBigDecimal(t.getAmount()).abs();
                if (isCredit(t)) {
                    recentCredits = recentCredits.add(amt);
                    if (contains(t.getDescription(), "salary") || contains(t.getDescription(), "payroll")) {
                        salaryCredits++;
                    }
                } else {
                    recentDebits = recentDebits.add(amt);
                    if (contains(t.getDescription(), "fuel") || contains(t.getDescription(), "petrol") || contains(t.getDescription(), "diesel")) {
                        fuelPayments++;
                    }
                    if (contains(t.getDescription(), "rent") || contains(t.getDescription(), "lease")
                            || contains(t.getDescription(), "housing") || contains(t.getDescription(), "home emi")) {
                        rentOrHousing++;
                    }
                }
            }

            // Per-account: Flexible Term Deposit when idle balance is high and spend is low
            BigDecimal debitSum = sumAmounts(recent, false);
            if (gte(toBigDecimal(acc.getBalance()), bd(150_000)) && lt(debitSum, bd(50_000))) {
                suggestions.add(new AiOpportunitySuggestionDTO(
                        null,
                        "FLEXI-Deposit", // exact PRODUCT_CODE from your catalog
                        "Optimize Savings: Flexible Term Deposit",
                        "High balance with limited spend—move idle funds to FLEXI deposit.",
                        "Call customer to propose FD laddering.",
                        "Wealth / Deposit",
                        conf(toBigDecimal(acc.getBalance()), bd(150_000), bd(700_000)).doubleValue(),
                        "Confirm liquidity needs and breakage penalties."
                ));
            }
        }

        // Cross-account: Gold Credit Card upgrade
        if (salaryCredits >= 2 && gte(recentDebits, bd(100_000))) {
            suggestions.add(new AiOpportunitySuggestionDTO(
                    null,
                    "CR-CARD",
                    "Credit Card Upgrade Opportunity",
                    "Regular salary credits with high spend suggest card upgrade.",
                    "Send targeted upgrade offer (Gold Credit Card).",
                    "Cards",
                    conf(recentDebits, bd(100_000), bd(500_000)).doubleValue(),
                    "Verify limit and repayment discipline."
            ));
        }

        // Cross-account: Home Loan
        if (rentOrHousing >= 2 && gte(recentCredits, bd(100_000))) {
            suggestions.add(new AiOpportunitySuggestionDTO(
                    null,
                    "PR-LOAN",
                    "Home Loan Pre-Approval",
                    "Consistent housing-related payments with significant inflows.",
                    "Invite for home loan pre-qualification.",
                    "Loans",
                    conf(bd(rentOrHousing), bd(2), bd(6)).doubleValue(),
                    "Run mortgage affordability & bureau."
            ));
        }

        // Cross-account: Car Loan
        if (fuelPayments >= 8 && lt(totalBalance, bd(150_000))) {
            suggestions.add(new AiOpportunitySuggestionDTO(
                    null,
                    "V-LOAN",
                    "Vehicle Loan Offer",
                    "Frequent fuel payments indicate active vehicle usage; consider upgrade financing.",
                    "Offer competitive car loan.",
                    "Loans",
                    conf(bd(fuelPayments), bd(8), bd(20)).doubleValue(),
                    "Check vehicle ownership/status; evaluate debt-to-income."
            ));
        }

        // Cross-account: Digi Wallet
        if (gte(recentDebits, bd(25_000)) && lt(totalBalance, bd(75_000))) {
            suggestions.add(new AiOpportunitySuggestionDTO(
                    null,
                    "D-Wallet",
                    "Digi Wallet Cashback Campaign",
                    "Frequent small payments—wallet cashback could improve engagement.",
                    "Push D-Wallet onboarding with limited-time cashback.",
                    "Payments / Wallet",
                    0.65,
                    "KYC & e-mandates as applicable."
            ));
        }

        // Cross-account: Virtual Cards (online usage)
        long onlineTx = accounts.stream()
                .flatMap(a -> {
                    List<Transactions> list = transactionServices.getTransactionsByAccountNo(a.getAccountNo());
                    return (list == null ? List.<Transactions>of() : list).stream();
                })
                .filter(t -> t.getCreatedAt() != null && !t.getCreatedAt().isBefore(since))
                .filter(t -> contains(t.getDescription(), "online") || contains(t.getDescription(), "ecom") || contains(t.getDescription(), "upi"))
                .count();
        if (onlineTx >= 10) {
            suggestions.add(new AiOpportunitySuggestionDTO(
                    null,
                    "Virtual Cards",
                    "Virtual Card Safety",
                    "Heavy online usage—offer virtual cards for safer online payments.",
                    "Enable virtual CR/DR card issuance.",
                    "Cards / Digital",
                    conf(bd(onlineTx), bd(10), bd(40)).doubleValue(),
                    "Ensure device binding & 2FA."
            ));
        }

        return dedupeByTitle(suggestions);
    }

    /**
     * Generate suggestions AND insert them into CUSTOMER_OPPORTUNITIES.
     * Resolves productId using the PRODUCTS catalog before insert.
     */
    @Transactional
    public List<CustomerOpportunity> generateAndInsertOpportunitiesForCustomer(Long customerId,
                                                                               int lookbackDays,
                                                                               String defaultAssignedStaff) {
        List<AiOpportunitySuggestionDTO> suggestions = analyzeCustomerForSuggestions(customerId, lookbackDays);
        if (suggestions.isEmpty()) return List.of();

        List<CustomerOpportunity> created = new ArrayList<>();
        for (AiOpportunitySuggestionDTO s : suggestions) {
            Long productId = s.productId();
            if (productId == null) {
                productId = resolveProductIdByCodeOrTitle(s.productCode(), s.title());
            }
            if (productId == null) continue; // skip if we couldn't map to catalog

            // Optional: repository-level dedupe
            // if (opps.existsByCustomer_IdAndProductIdAndStatus(customerId, productId, "NEW")) continue;

            CustomerOpportunity co = createOpportunity(customerId, productId, defaultAssignedStaff, "NEW");
            created.add(co);
        }
        return created;
    }

    /* ============================================================
     *                      Catalog Resolution
     * ============================================================ */

    /**
     * Resolve Product ID using (1) code, (2) exact name, (3) synonyms, (4) partial name.
     */
    private Long resolveProductIdByCodeOrTitle(String productCode, String title) {
        // 1) Try productCode
        if (productCode != null && !productCode.isBlank()) {
            Optional<products> byCode = productRepository.findByProductCodeIgnoreCase(productCode);
            if (byCode.isPresent()) return byCode.get().getId();
        }

        // 2) Try exact name from title
        if (title != null && !title.isBlank()) {
            Optional<products> byExactName = productRepository.findByNameIgnoreCase(title.trim());
            if (byExactName.isPresent()) return byExactName.get().getId();
        }

        // 3) Synonyms → PRODUCT_CODE
        String codeFromSynonyms = mapTitleToCatalogCode(title);
        if (codeFromSynonyms != null) {
            Optional<products> bySynCode = productRepository.findByProductCodeIgnoreCase(codeFromSynonyms);
            if (bySynCode.isPresent()) return bySynCode.get().getId();
        }

        // 4) Fallback: partial name match
        if (title != null && !title.isBlank()) {
            List<products> byContains = productRepository.findByNameContainingIgnoreCase(title);
            if (!byContains.isEmpty()) return byContains.get(0).getId();
        }

        return null; // unresolved
    }

    /** Map common phrases to real PRODUCT_CODEs in your catalog */
    private String mapTitleToCatalogCode(String title) {
        if (title == null) return null;
        String t = title.toLowerCase(Locale.ROOT);

        // Deposits / Savings
        if (t.contains("fixed deposit") || t.contains("flexible term deposit") || t.contains("fd"))
            return "FLEXI-Deposit";
        if (t.contains("premium savings") || t.contains("savings") || t.contains("interest"))
            return "PR-SAV";

        // Cards
        if (t.contains("credit card") || t.contains("card upgrade") || t.contains("gold card"))
            return "CR-CARD";
        if (t.contains("virtual card"))
            return "Virtual Cards";

        // Wallet
        if (t.contains("wallet") || t.contains("digi wallet"))
            return "D-Wallet";

        // Loans
        if (t.contains("home loan") || t.contains("mortgage") || t.contains("housing"))
            return "PR-LOAN";
        if (t.contains("car loan") || t.contains("vehicle loan") || t.contains("auto loan"))
            return "V-LOAN";

        // Generic fallback
        if (t.contains("loan")) return "PR-LOAN";

        return null;
    }

    /* ============================================================
     *                      Helpers / Heuristics
     * ============================================================ */

    private BigDecimal sumAmounts(List<Transactions> txs, boolean credits) {
        if (txs == null || txs.isEmpty()) return BigDecimal.ZERO;
        return txs.stream()
                .filter(t -> credits == isCredit(t))
                .map(t -> toBigDecimal(t.getAmount()).abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal bd(double v) { return BigDecimal.valueOf(v); }
    private static BigDecimal bd(int v)    { return BigDecimal.valueOf(v); }
    private static BigDecimal bd(long v)   { return BigDecimal.valueOf(v); }

    /** Accepts BigDecimal; returns ZERO for null */
    private static BigDecimal safe(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    /** Overload for Double (and auto-boxed double) */
    private static BigDecimal safe(Double v) {
        return v == null ? BigDecimal.ZERO : BigDecimal.valueOf(v);
    }

    /** Generic conversion for any Number (BigDecimal, Double, Integer, etc.) */
    private static BigDecimal toBigDecimal(Number n) {
        if (n == null) return BigDecimal.ZERO;
        if (n instanceof BigDecimal bd) return bd;
        if (n instanceof Long l)        return BigDecimal.valueOf(l);
        if (n instanceof Integer i)     return BigDecimal.valueOf(i.longValue());
        if (n instanceof Double d)      return BigDecimal.valueOf(d);
        if (n instanceof Float f)       return BigDecimal.valueOf(f.doubleValue());
        return new BigDecimal(n.toString());
    }

    private static boolean gt(BigDecimal a, BigDecimal b)  { return a.compareTo(b) > 0; }
    private static boolean gte(BigDecimal a, BigDecimal b) { return a.compareTo(b) >= 0; }
    private static boolean lt(BigDecimal a, BigDecimal b)  { return a.compareTo(b) < 0; }

    private static BigDecimal conf(BigDecimal value, BigDecimal low, BigDecimal high) {
        if (value == null) return bd(0.35);
        if (value.compareTo(low) <= 0) return bd(0.45);
        if (value.compareTo(high) >= 0) return bd(0.9);
        BigDecimal span = high.subtract(low);
        BigDecimal pos  = value.subtract(low);
        BigDecimal ratio = span.signum() == 0 ? bd(0.7) : pos.divide(span, 4, RoundingMode.HALF_UP);
        return bd(0.45).add(ratio.multiply(bd(0.45))); // 0.45 .. 0.9
    }

    private static boolean contains(String text, String needle) {
        if (text == null || needle == null) return false;
        return text.toLowerCase(Locale.ROOT).contains(needle.toLowerCase(Locale.ROOT));
    }

    /**
     * Guess transaction polarity if you don’t have an explicit type field.
     * If your Transactions has a type (CREDIT/DEBIT), use that instead.
     */
    private static boolean isCredit(Transactions t) {
        Number amtNum = t.getAmount();
        if (amtNum != null) {
            BigDecimal amt = toBigDecimal(amtNum);
            return amt.signum() >= 0;
        }
        return contains(t.getDescription(), "salary")
                || contains(t.getDescription(), "interest")
                || contains(t.getDescription(), "refund");
    }

    private List<AiOpportunitySuggestionDTO> dedupeByTitle(List<AiOpportunitySuggestionDTO> in) {
        Map<String, AiOpportunitySuggestionDTO> map = new LinkedHashMap<>();
        for (AiOpportunitySuggestionDTO s : in) {
            String key = (s.title() == null ? "" : s.title().trim().toLowerCase());
            map.putIfAbsent(key, s);
        }
        return new ArrayList<>(map.values());
    }
}
