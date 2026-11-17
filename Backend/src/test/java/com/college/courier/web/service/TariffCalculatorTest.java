package com.college.courier.web.service;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
class TariffCalculatorTest {
 @Test void calculatesLocalTariff(){assertEquals(new BigDecimal("65.00"),TariffCalculator.calculate(new BigDecimal("1.5"),"LOCAL"));}
 @Test void calculatesRegionalTariff(){assertEquals(new BigDecimal("72.50"),TariffCalculator.calculate(new BigDecimal("0.75"),"REGIONAL"));}
 @Test void calculatesNationalTariff(){assertEquals(new BigDecimal("140.00"),TariffCalculator.calculate(new BigDecimal("2"),"NATIONAL"));}
 @Test void rejectsUnknownZone(){assertThrows(IllegalArgumentException.class,()->TariffCalculator.calculate(BigDecimal.ONE,"REMOTE"));}
 @Test void rejectsNonPositiveWeight(){assertThrows(IllegalArgumentException.class,()->TariffCalculator.calculate(BigDecimal.ZERO,"LOCAL"));}
}
