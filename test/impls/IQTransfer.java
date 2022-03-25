package test.impls;

import org.data_transfer.FunctionalTransfer;
import org.data_transfer.io.Reader;
import org.data_transfer.io.Writer;
import org.data_transfer.util.Array;
import org.data_transfer.util.StringTransfer;
import org.data_transfer.util.Transferor;
import test.Question;

import java.util.List;

public class IQTransfer implements Transferor<Question<?>> {

    @Override
    public String type() {
        return "input-question";
    }

    @Override
    public short transferId() {
        return 0b10101010011010;
    }

    @Override
    public void transfer(Question<?> question, Writer writer) {
        InputQuestion iq = (InputQuestion) question;
        FunctionalTransfer.transferString(iq.getTitle(), writer);
        FunctionalTransfer.transferInt(iq.getMaxPoints(), writer);
        FunctionalTransfer.transferByte(iq.getInputType().byteNumber(), writer);
        Array<String> array = new Array<>(StringTransfer.INSTANCE);
        array.addAll(List.of(iq.getRightAnswer()));
        FunctionalTransfer.transferArray(array, writer);
    }

    @Override
    public Question<?> transfer(Reader in) {
        String title = FunctionalTransfer.transferString(in);
        int maxPoints = FunctionalTransfer.transferInt(in);
        SmartTextField.InputType it = SmartTextField.InputType.forByteNumber(FunctionalTransfer.transferByte(in));
        Array<String> array = FunctionalTransfer.transferArray(in);
        String[] rightAnswer = array.toArray(new String[0]);
        return new InputQuestion(title, maxPoints, it, rightAnswer);
    }
}
