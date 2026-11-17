package com.college.courier.web.service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Map;
public final class TariffCalculator {
 private static final Map<String,BigDecimal> SURCHARGES=Map.of(
  "LOCAL",BigDecimal.ZERO,"REGIONAL",new BigDecimal("15"),"NATIONAL",new BigDecimal("70"));
 private TariffCalculator(){}
 public static BigDecimal calculate(BigDecimal weightKg,String zone){
  if(weightKg==null||weightKg.signum()<=0) throw new IllegalArgumentException("Weight must be greater than zero");
  BigDecimal surcharge=SURCHARGES.get(zone==null?"":zone.toUpperCase(Locale.ROOT));
  if(surcharge==null) throw new IllegalArgumentException("Destination zone must be LOCAL, REGIONAL or NATIONAL");
  return new BigDecimal("50").add(weightKg.multiply(new BigDecimal("10"))).add(surcharge).setScale(2,RoundingMode.HALF_UP);
 }
}
