package test.impls;

import org.data_transfer.FunctionalTransfer;
import org.data_transfer.io.Reader;
import org.data_transfer.io.Writer;
import org.data_transfer.util.Array;
import org.data_transfer.util.IntTransfer;
import org.data_transfer.util.StringTransfer;
import org.data_transfer.util.Transferor;
import test.Question;

import java.util.Arrays;

public class CQTransfer implements Transferor<Question<?>> {

    @Override
    public String type() {
        return "compare-question";
    }

    @Override
    public short transferId() {
        return 0b101010001001010;
    }

    @Override
    public void transfer(Question<?> question, Writer writer) {
        CompareQuestion cq = (CompareQuestion) question;
        FunctionalTransfer.transferString(cq.getTitle(), writer);
        FunctionalTransfer.transferInt(cq.getMaxPoints(), writer);
        Array<String> group = new Array<>(StringTransfer.INSTANCE);
        group.addAll(Arrays.asList(cq.getRightAnswer().aGroup()));
        FunctionalTransfer.transferArray(group, writer);
        group = new Array<>(StringTransfer.INSTANCE);
        group.addAll(Arrays.asList(cq.getRightAnswer().bGroup()));
        FunctionalTransfer.transferArray(group, writer);
        Array<Integer> pares = new Array<>(IntTransfer.INSTANCE);
        int[][] ps = cq.getRightAnswer().pares();
        for (int[] p : ps) {
            pares.add(p[0]);
            pares.add(p[1]);
        }
        FunctionalTransfer.transferArray(pares, writer);
        FunctionalTransfer.transferByte(cq.getCheckRule().byteNumber(), writer);
    }

    @Override
    public Question<?> transfer(Reader reader) {
        String title = FunctionalTransfer.transferString(reader);
        int maxPoints = FunctionalTransfer.transferInt(reader);
        Array<String> aGroup = FunctionalTransfer.transferArray(reader);
        Array<String> bGroup = FunctionalTransfer.transferArray(reader);
        Array<Integer> ps = FunctionalTransfer.transferArray(reader);
        int[][] pares = new int[ps.size() / 2][2];
        for (int i = 0, j = 0; j < pares.length; i += 2, j++) {
            pares[j][0] = ps.get(i);
            pares[j][1] = ps.get(i + 1);
        }
        CheckRule checkRule = CheckRule.fromByteNumber(
                FunctionalTransfer.transferByte(reader));
        return new CompareQuestion(title, maxPoints, checkRule, new CompareAnswer(
                aGroup.toArray(new String[0]), bGroup.toArray(new String[0]), pares));
    }
}
