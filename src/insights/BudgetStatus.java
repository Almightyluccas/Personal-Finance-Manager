package insights;

/**
 * Represents the overall yearly budget result.
 *
 * <p>A budget can end with extra money, lose money, or break even.</p>
 *
 * @author Waliur Sun, Adrian Singh, Felix Santos
 */
public enum BudgetStatus {

    /** The user earned more than they spent. */
    SURPLUS,

    /** The user spent more than they earned. */
    DEFICIT,

    /** The user's income and expenses were equal. */
    BALANCED
}