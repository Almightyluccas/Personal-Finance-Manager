package storage;

import java.time.LocalDate;

/**
 * Represents a single financial transaction record, consisting of a date,
 * a category, and a monetary amount.
 *
 * @author Mohammed
 */
public record Transaction(LocalDate date, String category, double amount) {

    /**
     * Returns the date on which the transaction occurred.
     *
     * @return the transaction date
     * @author Mohammed
     */
    public LocalDate date() {
        return date;
    }

    /**
     * Returns the category assigned to this transaction (e.g. "Groceries").
     *
     * @return the transaction category
     * @author Mohammed
     */
    public String category() {
        return category;
    }

    /**
     * Returns the monetary amount of the transaction.
     *
     * @return the transaction amount
     * @author Mohammed
     */
    public double amount() {
        return amount;
    }
}
