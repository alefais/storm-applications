package FraudDetectionOriginal;

import com.google.common.collect.ImmutableList;
import Util.stream.StreamValues;

import java.util.List;

/**
 *
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class TransactionParser extends Parser {

    @Override
    public List<StreamValues> parse(String input) {
        String[] items = input.split(",", 2);
        return ImmutableList.of(new StreamValues(items[0], items[1]));
    }
    
}
