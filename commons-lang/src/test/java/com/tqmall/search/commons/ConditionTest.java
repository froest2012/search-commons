package com.tqmall.search.commons;

import com.tqmall.search.commons.param.condition.*;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xing on 16/1/24.
 * condition test
 */
public class ConditionTest {

    @Test
    public void conditionContainerTest() {
        EqualCondition<Integer> equalCondition = EqualCondition.build("id", 4);
        Assert.assertTrue(equalCondition.validation(4));
        Assert.assertFalse(equalCondition.validation(5));
        Assert.assertFalse(equalCondition.validation(4.0));

        RangeCondition<BigDecimal> rangeCondition = RangeCondition.build("price", BigDecimal.ONE, BigDecimal.TEN);
        Assert.assertTrue(rangeCondition.validation(BigDecimal.ONE));
        Assert.assertTrue(rangeCondition.validation(BigDecimal.TEN));
        Assert.assertTrue(rangeCondition.validation(BigDecimal.valueOf(2.0)));
        Assert.assertTrue(rangeCondition.validation(2.0));
        Assert.assertFalse(rangeCondition.validation(BigDecimal.valueOf(-1.0)));
        Assert.assertFalse(rangeCondition.validation(-1.0));

        InCondition<String> inCondition = InCondition.build("name", "xing", "wang", "yan", "lin");
        Assert.assertNotNull(inCondition);
        Assert.assertTrue(inCondition.validation("xing"));
        Assert.assertTrue(inCondition.validation("yan"));
        Assert.assertFalse(inCondition.validation("Xing"));

        ModifiableConditionContainer conditionContainer = new ModifiableConditionContainer()
                .addMust(equalCondition)
                .addMust(rangeCondition)
                .add(ConditionContainer.Type.SHOULD, inCondition);

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("id", 3);
        dataMap.put("price", BigDecimal.valueOf(3.0));
        dataMap.put("value", 4);
        dataMap.put("name", "yan");
        Assert.assertFalse(conditionContainer.validation(dataMap));

        dataMap.put("id", 4);
        Assert.assertTrue(conditionContainer.validation(dataMap));
        dataMap.put("value", 2);
        conditionContainer.minimumShouldMatch(2);
        Assert.assertFalse(conditionContainer.validation(dataMap));

        conditionContainer.minimumShouldMatch(1);
        Assert.assertTrue(conditionContainer.validation(dataMap));

        conditionContainer.add(ConditionContainer.Type.MUST_NOT, equalCondition);
        Assert.assertFalse(conditionContainer.validation(dataMap));
    }
}
