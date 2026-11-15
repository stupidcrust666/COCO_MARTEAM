public interface Discountable {
    double getDiscountRate();
    double calculateDiscount(double amount);   // returns the amount to be deducted
    boolean isDiscountEligible();
}
