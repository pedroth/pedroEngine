package nlp.textSplitter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Pedroth on 2/28/2016.
 */
public class RegexSplitter implements TextSplitter {
    private String regex;

    public RegexSplitter(String regex) {
        this.regex = regex;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }


    public List<String> getAllMatches(String text) {
        List<String> matches = new ArrayList<>();
        Matcher m = Pattern.compile("(?=(" + regex + "))").matcher(text);
        while (m.find()) {
            matches.add(m.group(1));
        }
        return matches;
    }

    @Override
    public String[] split(String in) {
        List<String> allMatches = getAllMatches(in);
        return allMatches.toArray(new String[0]);
    }
}
