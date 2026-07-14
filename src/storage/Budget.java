package storage;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user's budget for a given year, containing a collection of
 * {@link Transaction} records and operations to query and modify them.
 *
 * @author Mohammed, Ayub, Fuad
 */
public class Budget {
    /**
     * The year this budget represents.
     */
    private final int year;

    /**
     * The list of transactions stored in this budget.
     */
    private final List<Transaction> transactions;

    /**
     * Constructs a new, empty {@code Budget} for the specified year.
     *
     * @param year the year this budget represents
     * @author Mohammed, Ayub, Fuad
     */
    public Budget(int year) {
        this.year = year;
        this.transactions = new ArrayList<>();
    }

    /**
     * Returns the year associated with this budget.
     *
     * @return the budget year
     * @author Mohammed, Ayub, Fuad
     */
    public int getYear() {
        return year;
    }

    /**
     * Returns all transactions currently stored in this budget.
     *
     * @return a list of all transactions
     * @author Mohammed, Ayub, Fuad
     */
    public List<Transaction> getTransactions() {
        return transactions;
    }

    /**
     * Adds a new transaction to this budget.
     *
     * @param transaction the transaction to add
     * @author Mohammed, Ayub, Fuad
     */
    public void addTransaction(Transaction transaction) {
        if (transaction != null) {
            transactions.add(transaction);
        }
    }

    /**
     * Returns all transactions that occurred in the specified month.
     *
     * @param month the month to filter by (1-12)
     * @return a list of transactions occurring in the given month
     * @author Mohammed, Ayub, Fuad
     */
    public List<Transaction> getTransactionsByMonth(int month) {
        List<Transaction> result = new ArrayList<>();
        if (month < 1 || month > 12) {
            return result;
        }
        for(Transaction transaction : transactions) {
            if(transaction.date().getMonthValue() == month) {
                result.add(transaction);
            }
        }
        return result;
    }

    /**
     * Returns all transactions that belong to the specified category.
     *
     * @param category the category to filter by
     * @return a list of transactions belonging to the given category
     * @author Mohammed, Ayub, Fuad
     */
    public List<Transaction> getTransactionsByCategory(String category) {
        List<Transaction> result = new ArrayList<>();
        if(category == null || category.isBlank()) {
            return result;
        }
        for(Transaction transaction : transactions) {
            if(transaction.category().equalsIgnoreCase(category)) {
                result.add(transaction);
            }
        }
        return result;
    }
}
