package storage;

import java.time.LocalDate;

/**
 * Represents a single financial transaction record, consisting of a date,
 * a category, and a monetary amount.
 * <p>
 * As a {@code record}, the canonical accessors {@code date()},
 * {@code category()}, and {@code amount()} are generated automatically by
 * the compiler and do not need to be declared explicitly here.
 *
 * @author Mohammed
 */
public record Transaction(LocalDate date, String category, double amount) {
}
