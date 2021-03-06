package com.tqmall.search.commons.param.condition;

import java.util.Objects;

/**
 * Created by xing on 16/1/24.
 * 等值比较条件
 */
public class EqualCondition<T> extends Condition {

    private static final long serialVersionUID = 4180473296298181745L;

    /**
     * value可以为null
     */
    private final T value;

    public EqualCondition(String field, T value) {
        super(field);
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    @Override
    public boolean validation(Object value) {
        return Objects.equals(this.value, value);
    }

    @Override
    public String toString() {
        return "EqualCondition{" + super.toString() + ", value = " + value;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof EqualCondition && super.equals(o);
    }

    private final static int HASH_CODE_FACTOR = EqualCondition.class.getSimpleName().hashCode();

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + HASH_CODE_FACTOR;
    }

    /**
     * 如果value无效, 返回null
     */
    public static <T> EqualCondition<T> build(String field, T value) {
        if (value == null) return null;
        else return new EqualCondition<>(field, value);
    }
}
