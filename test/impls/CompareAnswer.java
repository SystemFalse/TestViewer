package test.impls;

import java.util.Arrays;

public record CompareAnswer(String[] aGroup, String[] bGroup, int[][] pares) {

    public CompareAnswer {
        if (aGroup.length == 0) {
            throw new RuntimeException("нет вариантов ответа в столбце 1");
        }
        if (aGroup.length > 10) {
            throw new RuntimeException("слишком много вариантов ответа в столбце 1");
        }
        if (bGroup.length == 0) {
            throw new RuntimeException("нет вариантов ответа в столбце 2");
        }
        if (bGroup.length > 10) {
            throw new RuntimeException("слишком много вариантов ответа в столбце 2");
        }
        if (pares.length == 0) {
            throw new RuntimeException("не указан ни один вариант ответа");
        }
        if (pares[0].length != 2) {
            throw new RuntimeException("неверный массив ответов");
        }
        if (pares.length > Math.min(aGroup.length, bGroup.length)) {
            throw new RuntimeException("ответов больше, чем вариантов");
        }
        for (int[] pare : pares) {
            if (pare[0] > aGroup.length) {
                throw new RuntimeException("индекс ответа в первом столбце больше максимального");
            }
            if (pare[1] > bGroup.length) {
                throw new RuntimeException("индекс ответа во втором столбце больше максимального");
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompareAnswer answer)) return false;

        if (!Arrays.equals(aGroup, answer.aGroup)) return false;
        if (!Arrays.equals(bGroup, answer.bGroup)) return false;
        return Arrays.deepEquals(pares, answer.pares);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(aGroup);
        result = 31 * result + Arrays.hashCode(bGroup);
        result = 31 * result + Arrays.deepHashCode(pares);
        return result;
    }
}
