package com.example.cocktailbar.pricing;

import com.example.cocktailbar.model.Drink;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.List;

import static com.example.cocktailbar.TestFixtures.at;
import static com.example.cocktailbar.TestFixtures.cola;
import static com.example.cocktailbar.TestFixtures.lager;
import static com.example.cocktailbar.TestFixtures.mojito;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PricingStrategyTest {

    private final PricingStrategy standard = new StandardPricing();
    private final PricingStrategy happyHour = new HappyHourPricing(standard);
    private final PricingStrategy lateNight = new LateNightPricing(standard);

    // ---- standard ----

    @Test
    void standardPricingReturnsTheMenuPriceAtAnyTime() {
        assertEquals(new BigDecimal("8.50"), standard.priceFor(mojito(), at(3, 0)));
        assertEquals(new BigDecimal("8.50"), standard.priceFor(mojito(), at(18, 0)));
    }

    // ---- happy hour: 17:00 included, 19:00 excluded ----

    @ParameterizedTest
    @CsvSource({"16,59,false", "17,0,true", "18,59,true", "19,0,false", "12,0,false"})
    void happyHourDiscountsCocktailsOnlyBetweenFiveAndSeven(int hour, int minute, boolean discounted) {
        BigDecimal expected = discounted ? new BigDecimal("5.95") : new BigDecimal("8.50"); // 8.50 x 0.70

        assertEquals(expected, happyHour.priceFor(mojito(), at(hour, minute)));
    }

    @Test
    void happyHourDoesNotDiscountBeersOrSoftDrinks() {
        assertEquals(new BigDecimal("4.50"), happyHour.priceFor(lager(), at(17, 30)));
        assertEquals(new BigDecimal("3.00"), happyHour.priceFor(cola(), at(17, 30)));
    }

    // ---- late night: 23:00 included, 04:00 excluded, crosses midnight ----

    @ParameterizedTest
    @CsvSource({"22,59,false", "23,0,true", "0,0,true", "3,59,true", "4,0,false", "12,0,false"})
    void lateNightAddsASurchargeBetweenElevenPmAndFourAm(int hour, int minute, boolean surcharged) {
        BigDecimal expected = surcharged ? new BigDecimal("3.45") : new BigDecimal("3.00"); // 3.00 x 1.15

        assertEquals(expected, lateNight.priceFor(cola(), at(hour, minute)));
    }

    @Test
    void surchargeIsRoundedHalfUpToCents() {
        // 8.50 x 1.15 = 9.775 -> 9.78 (and not 9.77)
        assertEquals(new BigDecimal("9.78"), lateNight.priceFor(mojito(), at(23, 30)));
    }

    // ---- composition ----

    @Test
    void stackedRulesApplyOnlyTheOnesActiveAtThatTime() {
        PricingStrategy stacked = new HappyHourPricing(new LateNightPricing(new StandardPricing()));

        assertEquals(new BigDecimal("5.95"), stacked.priceFor(mojito(), at(17, 30))); // happy hour only
        assertEquals(new BigDecimal("9.78"), stacked.priceFor(mojito(), at(23, 30))); // late night only
        assertEquals(new BigDecimal("8.50"), stacked.priceFor(mojito(), at(12, 0)));  // neither
    }

    @Test
    void decoratorsRequireAStrategyToWrap() {
        assertThrows(NullPointerException.class, () -> new HappyHourPricing(null));
        assertThrows(NullPointerException.class, () -> new LateNightPricing(null));
    }

    // ---- Liskov: any strategy can replace another without breaking callers ----

    @Test
    void everyStrategyReturnsAPositivePriceInCentsAtEveryHour() {
        List<PricingStrategy> strategies = List.of(
                standard,
                happyHour,
                lateNight,
                new HappyHourPricing(new LateNightPricing(new StandardPricing())));
        List<Drink> drinks = List.of(mojito(), lager(), cola());

        for (PricingStrategy strategy : strategies) {
            for (Drink drink : drinks) {
                for (int hour = 0; hour < 24; hour++) {
                    BigDecimal price = strategy.priceFor(drink, at(hour, 0));

                    assertTrue(price.signum() > 0, strategy.getClass().getSimpleName() + " at " + hour + "h");
                    assertEquals(2, price.scale());
                }
            }
        }
    }
}
