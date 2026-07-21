package reports;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import storage.Budget;
import storage.Transaction;

/**
 * Shared calculation helper for report totals and deterministic report rows.
 *
 * @author Alyssa Johnson
 * @author Tahsin Abid
 */
final class ReportAnalytics {

    private ReportAnalytics() {
    }

    static boolean isValidMonth(int month) {
        return month >= 1 && month <= 12;
    }

    static ReportTotals forBudget(Budget budget) {
        return buildTotals(budget, sanitizeTransactions(budget == null ? null : budget.getTransactions()), null);
    }

    static ReportTotals forMonth(Budget budget, int month) {
        List<Transaction> transactions = budget == null || !isValidMonth(month)
                ? List.of()
                : sanitizeTransactions(budget.getTransactionsByMonth(month));
        return buildTotals(budget, transactions, month);
    }

    private static ReportTotals buildTotals(Budget budget, List<Transaction> transactions, Integer month) {
        int year = budget == null ? 0 : budget.getYear();
        double income = 0.0d;
        double expenses = 0.0d;
        Map<String, CategoryAccumulator> categoryMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        List<ReportRow> rows = new ArrayList<>();

        for (Transaction transaction : transactions) {
            double amount = transaction.amount();
            String category = normalizeCategory(transaction.category());
            CategoryAccumulator accumulator = categoryMap.computeIfAbsent(category, key -> new CategoryAccumulator());

            if (amount >= 0) {
                income += amount;
                accumulator.income += amount;
            } else {
                double expense = Math.abs(amount);
                expenses += expense;
                accumulator.expenses += expense;
            }

            rows.add(new ReportRow(transaction.date(), category, amount));
        }

        List<CategoryTotals> categories = new ArrayList<>();
        for (Map.Entry<String, CategoryAccumulator> entry : categoryMap.entrySet()) {
            categories.add(new CategoryTotals(
                    entry.getKey(),
                    entry.getValue().income,
                    entry.getValue().expenses));
        }

        rows.sort(Comparator
                .comparing(ReportRow::date, Comparator.nullsLast(LocalDate::compareTo))
                .thenComparing(ReportRow::category, String.CASE_INSENSITIVE_ORDER)
                .thenComparingDouble(ReportRow::amount));

        return new ReportTotals(year, month, income, expenses, rows, categories);
    }

    private static List<Transaction> sanitizeTransactions(List<Transaction> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }

        List<Transaction> cleaned = new ArrayList<>();
        for (Transaction transaction : source) {
            if (transaction != null && transaction.date() != null) {
                cleaned.add(transaction);
            }
        }
        return cleaned;
    }

    private static String normalizeCategory(String category) {
        return (category == null || category.isBlank()) ? "Uncategorized" : category.trim();
    }

    static final class ReportTotals {
        private final int year;
        private final Integer month;
        private final double income;
        private final double expenses;
        private final double net;
        private final List<ReportRow> transactions;
        private final List<CategoryTotals> categories;

        ReportTotals(
                int year,
                Integer month,
                double income,
                double expenses,
                List<ReportRow> transactions,
                List<CategoryTotals> categories) {
            this.year = year;
            this.month = month;
            this.income = income;
            this.expenses = expenses;
            this.net = income - expenses;
            this.transactions = List.copyOf(transactions);
            this.categories = List.copyOf(categories);
        }

        int year() {
            return year;
        }

        int month() {
            return month == null ? 0 : month;
        }

        double income() {
            return income;
        }

        double expenses() {
            return expenses;
        }

        double net() {
            return net;
        }

        int transactionCount() {
            return transactions.size();
        }

        List<ReportRow> transactions() {
            return transactions;
        }

        List<CategoryTotals> categories() {
            return categories;
        }
    }

    static final class ReportRow {
        private final LocalDate date;
        private final String category;
        private final double amount;

        ReportRow(LocalDate date, String category, double amount) {
            this.date = date;
            this.category = category;
            this.amount = amount;
        }

        LocalDate date() {
            return date;
        }

        String category() {
            return category;
        }

        double amount() {
            return amount;
        }
    }

    static final class CategoryTotals {
        private final String category;
        private final double income;
        private final double expenses;
        private final double net;

        CategoryTotals(String category, double income, double expenses) {
            this.category = category;
            this.income = income;
            this.expenses = expenses;
            this.net = income - expenses;
        }

        String category() {
            return category;
        }

        double income() {
            return income;
        }

        double expenses() {
            return expenses;
        }

        double net() {
            return net;
        }
    }

    private static final class CategoryAccumulator {
        private double income;
        private double expenses;
    }
}
