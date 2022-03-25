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

public class TQTransfer implements Transferor<Question<?>> {

    @Override
    public String type() {
        return "tick-question";
    }

    @Override
    public short transferId() {
        return 0b100011110010101;
    }

    @Override
    public void transfer(Question<?> question, Writer writer) {
        TickQuestion tq = (TickQuestion) question;
        FunctionalTransfer.transferString(tq.getTitle(), writer);
        FunctionalTransfer.transferInt(tq.getMaxPoints(), writer);
        Array<String> variants = new Array<>(StringTransfer.INSTANCE);
        variants.addAll(Arrays.asList(tq.getRightAnswer().variants()));
        FunctionalTransfer.transferArray(variants, writer);
        Array<Integer> rights = new Array<>(IntTransfer.INSTANCE);
        int[] v = tq.getRightAnswer().rights();
        for (int i : v) {
            rights.add(i);
        }
        FunctionalTransfer.transferArray(rights, writer);
        FunctionalTransfer.transferByte(tq.getCheckRule().byteNumber(), writer);
    }

    @Override
    public Question<?> transfer(Reader reader) {
        String title = FunctionalTransfer.transferString(reader);
        int maxPoints = FunctionalTransfer.transferInt(reader);
        Array<String> variants = FunctionalTransfer.transferArray(reader);
        Array<Integer> rights = FunctionalTransfer.transferArray(reader);
        int[] v = new int[rights.size()];
        for (int i = 0; i < rights.size(); i++) {
            v[i] = rights.get(i);
        }
        CheckRule checkRule = CheckRule.fromByteNumber(
                FunctionalTransfer.transferByte(reader));
        return new TickQuestion(title, maxPoints, checkRule, new TickAnswer(
                variants.toArray(new String[0]), v));
    }
}
