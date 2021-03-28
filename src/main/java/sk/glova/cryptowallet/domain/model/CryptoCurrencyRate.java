package sk.glova.cryptowallet.domain.model;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class CryptoCurrencyRate {

    String name;
    Map<String, BigDecimal> currencyRates;

    // TODO consider split Comparator into separate class
    @AllArgsConstructor
    public static class Comparator implements java.util.Comparator<CryptoCurrencyRate> {

        final String sortBy;
        final String sortOrder;

        @Override
        public int compare(CryptoCurrencyRate o1, CryptoCurrencyRate o2) {

            try {
                Field field1 = o1.getClass().getDeclaredField(sortBy);
                Field field2 = o2.getClass().getDeclaredField(sortBy);

                field1.setAccessible(true);
                field2.setAccessible(true);

                if (o1.getClass().getDeclaredField(sortBy).getType() == Map.class) {
                    return 0;
                } else {
                    String d1 = (String) field1.get(o1);
                    String d2 = (String) field2.get(o2);
                    return (sortOrder.equalsIgnoreCase("asc")) ? d1.compareTo(d2) : d2.compareTo(d1);
                }
            } catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("Missing variable sortBy");
            } catch (ClassCastException e) {
                throw new RuntimeException("sortBy is not found in class list");
            }
        }

    }

}