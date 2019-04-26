package it.solutionsexmachina.webcontroller.converter;

import org.apache.commons.beanutils.converters.NumberConverter;

import java.math.BigDecimal;
import java.math.RoundingMode;


public class BigDecimalCustomConverter extends NumberConverter
{
    public BigDecimalCustomConverter(boolean allowDecimals)
    {
        super(allowDecimals);
    }

    public BigDecimalCustomConverter(boolean allowDecimals, Object defaultValue)
    {
        super(allowDecimals, defaultValue);
    }

    @Override
    protected Class<?> getDefaultType()
    {
        return BigDecimal.class;
    }

    @Override
    protected <T> T convertToType(Class<T> targetType, Object value) throws Throwable
    {
        T bigDecimal = super.convertToType(targetType, value);

        bigDecimal = (T) ((BigDecimal) bigDecimal).setScale(2, RoundingMode.DOWN);

        return bigDecimal;
    }

}
