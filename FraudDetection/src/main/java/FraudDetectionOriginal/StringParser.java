package FraudDetectionOriginal;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import Util.stream.StreamValues;

import java.util.List;

/**
 *
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class StringParser extends Parser {
    private static final Logger LOG = LoggerFactory.getLogger(StringParser.class);

    @Override
    public List<StreamValues> parse(String str) {
        if (StringUtils.isBlank(str))
            return null;
        
        return ImmutableList.of(new StreamValues(str));
    }
}