package Web_Drink_Store.webstore.enums;

import java.math.BigDecimal;

public enum DrinkSize {
    M(BigDecimal.ZERO),
    L(BigDecimal.valueOf(5000)),
    XL(BigDecimal.valueOf(10000));

    private final BigDecimal surcharge;

    DrinkSize(BigDecimal surcharge) {
        this.surcharge = surcharge;
    }

    public BigDecimal getSurcharge() {
        return surcharge;
    }
}
