package test.impls;

import java.util.Arrays;

public sealed interface CheckRule permits AAR, ACC, RIW {

    int check(int[] selected, int[] right, int maxPoints);

    byte byteNumber();

    static CheckRule ifAllAreRight() {
        return new AAR();
    }

    static CheckRule accordingly() {
        return new ACC();
    }

    static CheckRule reduceIfWrong() {
        return new RIW();
    }

    static CheckRule fromByteNumber(byte number) {
        return switch (number) {
            case 0 -> new AAR();
            case 1 -> new ACC();
            case 2 -> new RIW();
            default -> throw new RuntimeException("неизвестный тип проверки - " + number);
        };
    }
}

final class AAR implements CheckRule {

    @Override
    public int check(int[] selected, int[] right, int maxPoints) {
        if (selected.length == 0) {
            return 0;
        }
        return Arrays.equals(selected, right) ? maxPoints : 0;
    }

    @Override
    public byte byteNumber() {
        return 0;
    }
}

final class ACC implements CheckRule {

    @Override
    public int check(int[] selected, int[] right, int maxPoints) {
        if (selected.length == 0) {
            return 0;
        }
        int correct = 0;
        for (int i = 0 ; i < right.length; i++) {
            if (i >= selected.length) {
                break;
            }
            if (selected[i] == right[i]) {
                correct++;
            } else {
                correct--;
            }
        }
        return (int) ((float) correct / right.length * maxPoints);
    }

    @Override
    public byte byteNumber() {
        return 1;
    }
}

final class RIW implements CheckRule {

    @Override
    public int check(int[] selected, int[] right, int maxPoints) {
        if (selected.length == 0) {
            return 0;
        }
        for (int i = 0 ; i < right.length; i++) {
            if (i >= selected.length) {
                maxPoints = maxPoints - right.length - i;
                break;
            }
            int correct = right[i];
            if (Arrays.binarySearch(selected, correct) < 0) {
                maxPoints = maxPoints - 1;
            }
        }
        return Math.max(maxPoints, 0);
    }

    @Override
    public byte byteNumber() {
        return 2;
    }
}