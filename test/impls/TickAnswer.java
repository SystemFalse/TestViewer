package test.impls;

import java.util.Arrays;

public record TickAnswer(String[] variants, int[] rights) {

    public TickAnswer {
        if (variants.length < 2) {
            throw new RuntimeException("слишком мало вариантов ответа");
        }
        if (variants.length > 10) {
            throw new RuntimeException("слишком много вариантов ответа");
        }
        if (rights.length > 10) {
            throw new RuntimeException("слишком много правильных вариантов ответа");
        }
        for (int right : rights) {
            if (right > variants.length) {
                throw new RuntimeException("неправильный номер правильного ответа: " + right);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TickAnswer answer)) return false;

        if (!Arrays.equals(variants, answer.variants)) return false;
        return Arrays.equals(rights, answer.rights);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(variants);
        result = 31 * result + Arrays.hashCode(rights);
        return result;
    }
}
