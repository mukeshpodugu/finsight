package com.finsight.service;

import com.finsight.entity.AIInsight;
import com.finsight.entity.Budget;
import com.finsight.entity.Transaction;
import com.finsight.entity.User;
import com.finsight.repository.AIInsightRepository;
import com.finsight.repository.BudgetRepository;
import com.finsight.repository.CategoryRepository;
import com.finsight.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AIService {

    private final AIInsightRepository aiInsightRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${finsight.gemini.api-key}")
    private String apiKey;

    @Value("${finsight.gemini.api-url}")
    private String apiUrl;

    @Value("${finsight.gemini.model}")
    private String modelName;

    public AIService(AIInsightRepository aiInsightRepository,
                     TransactionRepository transactionRepository,
                     BudgetRepository budgetRepository,
                     CategoryRepository categoryRepository) {
        this.aiInsightRepository = aiInsightRepository;
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
    }

    public String getSpendingInsights(User user, int month, int year) {
        Optional<AIInsight> cached = aiInsightRepository
                .findByUserAndInsightTypeAndTargetMonthAndTargetYear(user, "SPENDING_INSIGHT", month, year);
        if (cached.isPresent()) {
            return cached.get().getInsightText();
        }

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        List<Transaction> transactions = transactionRepository.findAllByUserAndTransactionDateBetween(user, start, end);

        BigDecimal incomeSum = BigDecimal.ZERO;
        BigDecimal expenseSum = BigDecimal.ZERO;
        Map<String, BigDecimal> categoryExpenses = new HashMap<>();

        for (Transaction t : transactions) {
            if ("INCOME".equals(t.getType())) {
                incomeSum = incomeSum.add(t.getAmount());
            } else {
                expenseSum = expenseSum.add(t.getAmount());
                categoryExpenses.merge(t.getCategory().getName(), t.getAmount(), BigDecimal::add);
            }
        }

        String categoriesData = categoryExpenses.entrySet().stream()
                .map(e -> e.getKey() + ": ₹" + e.getValue())
                .collect(Collectors.joining(", "));

        String prompt = String.format(
                "Analyze the personal finance spending data for user %s %s for %d/%d.\n" +
                "Income: ₹%s\n" +
                "Total Expenses: ₹%s\n" +
                "Expenses per Category: [%s]\n" +
                "Provide professional, portfolio-grade financial analysis. " +
                "Summarize: 1. Largest spending category. 2. Unnecessary spending trends. 3. Savings opportunities. " +
                "Limit the response to 3 short paragraphs. Be motivating and direct.",
                user.getFirstName(), user.getLastName(), month, year, incomeSum, expenseSum, categoriesData
        );

        String resultText;
        if ("mock-key".equals(apiKey) || apiKey.trim().isEmpty() || "mock-key".equalsIgnoreCase(apiKey)) {
            resultText = generateMockInsights(user, incomeSum, expenseSum, categoryExpenses);
        } else {
            resultText = callGeminiAPI(prompt);
            if (resultText == null) {
                resultText = generateMockInsights(user, incomeSum, expenseSum, categoryExpenses);
            }
        }

        AIInsight insight = AIInsight.builder()
                .user(user)
                .insightText(resultText)
                .insightType("SPENDING_INSIGHT")
                .targetMonth(month)
                .targetYear(year)
                .build();
        aiInsightRepository.save(insight);

        return resultText;
    }

    public Map<String, Object> getFinancialHealthScore(User user, int month, int year) {
        Optional<AIInsight> cached = aiInsightRepository
                .findByUserAndInsightTypeAndTargetMonthAndTargetYear(user, "HEALTH_SCORE", month, year);

        if (cached.isPresent()) {
            AIInsight insight = cached.get();
            return Map.of("score", insight.getHealthScore(), "explanation", insight.getInsightText());
        }

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        List<Transaction> transactions = transactionRepository.findAllByUserAndTransactionDateBetween(user, start, end);
        List<Budget> budgets = budgetRepository.findAllByUserAndMonthAndYear(user, month, year);

        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expenses = BigDecimal.ZERO;
        for (Transaction t : transactions) {
            if ("INCOME".equals(t.getType())) {
                income = income.add(t.getAmount());
            } else {
                expenses = expenses.add(t.getAmount());
            }
        }

        double savingsRatio = 0;
        if (income.compareTo(BigDecimal.ZERO) > 0) {
            savingsRatio = income.subtract(expenses).multiply(new BigDecimal("100"))
                    .divide(income, 2, RoundingMode.HALF_UP).doubleValue();
        }

        int score = 50;
        if (savingsRatio > 30) score += 25;
        else if (savingsRatio > 10) score += 15;
        else if (savingsRatio < 0) score -= 15;

        long overspentCount = 0;
        for (Budget b : budgets) {
            BigDecimal spentCat = transactionRepository.sumExpenseByCategoryAndDateBetween(user, b.getCategory(), start, end);
            if (spentCat != null && spentCat.compareTo(b.getAmount()) > 0) {
                overspentCount++;
            }
        }
        score -= (int) (overspentCount * 10);
        score = Math.max(0, Math.min(100, score));

        String prompt = String.format(
                "Evaluate financial health score (calculated base algorithmically as %d out of 100) for user %s %s.\n" +
                "Month details: Income ₹%s, Expenses ₹%s, Savings Ratio: %.1f%%, Number of Overspent budgets: %d.\n" +
                "Provide a detailed, expert personal financial counselor explanation for the score (around 100-150 words). " +
                "List 2 actionable tips to improve this score next month.",
                score, user.getFirstName(), user.getLastName(), income, expenses, savingsRatio, overspentCount
        );

        String explanation;
        if ("mock-key".equals(apiKey) || apiKey.trim().isEmpty() || "mock-key".equalsIgnoreCase(apiKey)) {
            explanation = generateMockHealthExplanation(score, savingsRatio, overspentCount);
        } else {
            explanation = callGeminiAPI(prompt);
            if (explanation == null) {
                explanation = generateMockHealthExplanation(score, savingsRatio, overspentCount);
            }
        }

        AIInsight insight = AIInsight.builder()
                .user(user)
                .insightText(explanation)
                .insightType("HEALTH_SCORE")
                .healthScore(score)
                .targetMonth(month)
                .targetYear(year)
                .build();
        aiInsightRepository.save(insight);

        return Map.of("score", score, "explanation", explanation);
    }

    public String getBudgetRecommendations(User user, int month, int year) {
        Optional<AIInsight> cached = aiInsightRepository
                .findByUserAndInsightTypeAndTargetMonthAndTargetYear(user, "BUDGET_RECOMMENDATION", month, year);
        if (cached.isPresent()) {
            return cached.get().getInsightText();
        }

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        List<Transaction> transactions = transactionRepository.findAllByUserAndTransactionDateBetween(user, start, end);

        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expenses = BigDecimal.ZERO;
        for (Transaction t : transactions) {
            if ("INCOME".equals(t.getType())) {
                income = income.add(t.getAmount());
            } else {
                expenses = expenses.add(t.getAmount());
            }
        }

        String prompt = String.format(
                "Suggest ideal monthly budget allocations for user %s %s using 50/30/20 rule.\n" +
                "Current monthly Income is: ₹%s. Current monthly Expenses: ₹%s.\n" +
                "Provide clear recommended limits for: 1. Needs (Food, Rent, Utilities). " +
                "2. Wants (Shopping, Entertainment, Travel). 3. Savings (Investments). " +
                "Keep formatting clean and professional. Limit to 120 words.",
                user.getFirstName(), user.getLastName(), income, expenses
        );

        String recommendations;
        if ("mock-key".equals(apiKey) || apiKey.trim().isEmpty() || "mock-key".equalsIgnoreCase(apiKey)) {
            recommendations = generateMockRecommendations(income);
        } else {
            recommendations = callGeminiAPI(prompt);
            if (recommendations == null) {
                recommendations = generateMockRecommendations(income);
            }
        }

        AIInsight insight = AIInsight.builder()
                .user(user)
                .insightText(recommendations)
                .insightType("BUDGET_RECOMMENDATION")
                .targetMonth(month)
                .targetYear(year)
                .build();
        aiInsightRepository.save(insight);

        return recommendations;
    }

    @SuppressWarnings("rawtypes")
    private String callGeminiAPI(String promptText) {
        try {
            String url = String.format("%s/%s:generateContent?key=%s", apiUrl, modelName, apiKey);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> textPart = Map.of("text", promptText);
            Map<String, Object> partObj = Map.of("parts", List.of(textPart));
            Map<String, Object> contentObj = Map.of("contents", List.of(partObj));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(contentObj, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List candidates = (List) response.getBody().get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map firstCandidate = (Map) candidates.getFirst();
                    Map content = (Map) firstCandidate.get("content");
                    List parts = (List) content.get("parts");
                    Map firstPart = (Map) parts.getFirst();
                    return (String) firstPart.get("text");
                }
            }
        } catch (Exception e) {
            System.err.println("Gemini API call failed, falling back to mock: " + e.getMessage());
        }
        return null;
    }

    private String generateMockInsights(User user, BigDecimal income, BigDecimal expense, Map<String, BigDecimal> categoryExpenses) {
        String topCategory = categoryExpenses.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None");

        return String.format(
                "Hello %s. Based on your spending details this month, your largest expenditure category was **%s**.\n\n" +
                "You saved ₹%s out of your ₹%s total earnings. We noticed significant savings opportunities in entertainment " +
                "and dining options. Consider allocating some of these funds to your savings goals.\n\n" +
                "Tip: Limit your shopping bills to improve your cash reserves by 15%% next month.",
                user.getFirstName(), topCategory, income.subtract(expense), income
        );
    }

    private String generateMockHealthExplanation(int score, double savingsRatio, long overspentCount) {
        String status = score >= 80 ? "Excellent" : (score >= 60 ? "Good" : "Needs Attention");
        return String.format(
                "Your financial health score is **%d/100** (%s). Your savings ratio is %.1f%%, and you have overspent " +
                "on %d category budgets this month.\n\n" +
                "**Actionable Tips to Improve:**\n" +
                "1. Limit Wants categories to keep within your budget targets.\n" +
                "2. Automate a ₹5,000 monthly contribution directly to your active savings goal at the start of the month.",
                score, status, savingsRatio, overspentCount
        );
    }

    private String generateMockRecommendations(BigDecimal income) {
        if (income.compareTo(BigDecimal.ZERO) == 0) {
            income = new BigDecimal("50000");
        }
        BigDecimal needs = income.multiply(new BigDecimal("0.50")).setScale(0, RoundingMode.HALF_UP);
        BigDecimal wants = income.multiply(new BigDecimal("0.30")).setScale(0, RoundingMode.HALF_UP);
        BigDecimal savings = income.multiply(new BigDecimal("0.20")).setScale(0, RoundingMode.HALF_UP);

        return String.format(
                "Based on the **50/30/20 Rule** for your monthly income of ₹%s, here are your recommendations:\n\n" +
                "*   **Needs (50%%): ₹%s** — Limit food, rent, and utility expenses to this threshold.\n" +
                "*   **Wants (30%%): ₹%s** — Direct this toward shopping, cinema, and dining out.\n" +
                "*   **Savings & Investments (20%%): ₹%s** — Move this directly to your laptop/vacation goals.",
                income, needs, wants, savings
        );
    }
}
