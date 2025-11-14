package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a statement for a given invoice of performances.
 */
public class StatementPrinter {

    private final Invoice invoice;
    private final Map<String, Play> plays;

    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.invoice = invoice;
        this.plays = plays;
    }

    /**
     * Compute the amount for a single performance.
     *
     * @param performance    the performance
     * @param play the play information
     * @return the amount owed for that performance
     * @throws RuntimeException if one of the play types is not known
     */
    private int getAmount(Performance performance, Play play) {
        int thisAmount = 0;

        switch (play.getType()) {
            case "tragedy":
                thisAmount = Constants.TRAGEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.TRAGEDY_AUDIENCE_THRESHOLD) {
                    thisAmount += Constants.TRAGEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience() - Constants.TRAGEDY_AUDIENCE_THRESHOLD);
                }
                break;

            case "comedy":
                thisAmount = Constants.COMEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.COMEDY_AUDIENCE_THRESHOLD) {
                    thisAmount += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + (Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience() - Constants.COMEDY_AUDIENCE_THRESHOLD));
                }
                thisAmount += Constants.COMEDY_AMOUNT_PER_AUDIENCE * performance.getAudience();
                break;

            default:
                throw new RuntimeException(
                        String.format("unknown type: %s", play.getType()));
        }

        return thisAmount;
    }

    /**
     * Compute the volume credits for a single performance.
     *
     * @param performance the performance
     * @param play the play information
     * @return the volume credits earned for this performance
     */
    private int getVolumeCredits(Performance performance, Play play) {
        int volumeCredits = Math.max(
                performance.getAudience() - Constants.BASE_VOLUME_CREDIT_THRESHOLD, 0);

        if ("comedy".equals(play.getType())) {
            volumeCredits += performance.getAudience()
                    / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
        }

        return volumeCredits;
    }

    /**
     * Format the given amount (in cents) as a US dollar currency string.
     *
     * @param amount the amount in cents
     * @return a formatted currency string in US locale
     */
    private String usd(int amount) {
        final NumberFormat frmt = NumberFormat.getCurrencyInstance(Locale.US);
        return frmt.format(amount / Constants.CENTS_PER_DOLLAR);
    }

    /**
     * Compute the total amount owed for all performances on this invoice.
     *
     * @return the total amount in cents
     */
    private int getTotalAmount() {
        int totalAmount = 0;
        for (Performance performance : invoice.getPerformances()) {
            final Play play = plays.get(performance.getPlayID());
            totalAmount += getAmount(performance, play);
        }
        return totalAmount;
    }

    /**
     * Compute the total volume credits for all performances on this invoice.
     *
     * @return the total volume credits
     */
    private int getTotalVolumeCredits() {
        int volumeCredits = 0;
        for (Performance performance : invoice.getPerformances()) {
            final Play play = plays.get(performance.getPlayID());
            volumeCredits += getVolumeCredits(performance, play);
        }
        return volumeCredits;
    }

    /**
     * Returns a formatted statement of the invoice associated with this printer.
     *
     * @return the formatted statement
     * @throws RuntimeException if one of the play types is not known
     */
    public String statement() {
        final int totalAmount = getTotalAmount();
        final int volumeCredits = getTotalVolumeCredits();
        final StringBuilder result = new StringBuilder(
                "Statement for " + invoice.getCustomer() + System.lineSeparator()
        );

        for (Performance p : invoice.getPerformances()) {
            final Play play = plays.get(p.getPlayID());
            final int thisAmount = getAmount(p, play);

            result.append(String.format(
                    "  %s: %s (%s seats)%n",
                    play.getName(),
                    usd(thisAmount),
                    p.getAudience()
            ));
        }

        result.append(String.format(
                "Amount owed is %s%n",
                usd(totalAmount)
        ));
        result.append(String.format(
                "You earned %s credits%n",
                volumeCredits
        ));

        return result.toString();
    }
}

