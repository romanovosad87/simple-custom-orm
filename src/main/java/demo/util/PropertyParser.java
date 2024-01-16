package demo.util;

import demo.exception.FailedToMatchException;
import lombok.experimental.UtilityClass;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class PropertyParser {
    private static final String PATTERN = "\\$\\{(.*?)}";
    private static final String PREFIX = "$";

    public static String processProperty(String propertyValue) {
        if (propertyValue.startsWith(PREFIX)) {
            return getEnvValue(propertyValue);
        }
        return propertyValue;
    }

    private static String getEnvValue(String propertyValue) {
        Pattern pattern = Pattern.compile(PATTERN);
        Matcher matcher = pattern.matcher(propertyValue);

        if (matcher.find()) {
            String key = matcher.group(1);
            return System.getenv(key);
        } else {
            throw new FailedToMatchException("Can't match a pattern to read a property value "
                    + "for extracting env variable");
        }
    }
}
