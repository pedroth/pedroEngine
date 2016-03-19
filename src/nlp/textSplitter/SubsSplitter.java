package nlp.textSplitter;

import inputOutput.MyText;
import other.TeseProj.TimeInterval;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SubsSplitter implements TextSplitter {
    private static final HashSet<Character> betaSet = new HashSet<>(2);
    private static final HashSet<Character> betaStarSet = new HashSet<>(2);

    static {
        betaSet.add('<');
        betaSet.add('[');
        betaStarSet.add('>');
        betaStarSet.add(']');
    }

    private List<Subtitle> subtitles = new ArrayList<>(1);
    private List<String> subtitleWords = new ArrayList<>(1);
    private List<Integer> indexWords2Subtitle = new ArrayList<>(1);
    private StringBuilder stringBuilder;

    public static void main(String[] args) {
        MyText text = new MyText();
        text.read("C:/pedro/escolas/ist/Tese/Series/OverTheGardenWall/OverTheGardenWall1.srt");
//        text.read("C:/pedro/escolas/ist/Tese/Series/Breaking Bad/Season 1/Breaking Bad S1E01-Pilot.srt");
        SubsSplitter subsSplitter = new SubsSplitter();
        String[] split = subsSplitter.split(text.getText());
        StringBuilder stringBuilder = new StringBuilder(split.length);
        for (String s : split) {
            stringBuilder.append(s + "\n");
        }
        text.write("C:/Users/Pedroth/Desktop/Subs.txt", stringBuilder.toString());
    }

    private static boolean isAlphaBetChar(Character c) {
        int diff = c - 'a';
        return diff >= 0 && diff <= 'z' - 'a';
    }

    @Override
    public String[] split(String in) {
        String[] lines = in.split("\\r?\\n");
        for (int i = 1; i < lines.length; i++) {
            Subtitle subtitle = new Subtitle();
            //First line doesn't matter
            //Second time has the time interval
            String[] split = lines[i].replace(",", ".").split(" --> ");
            TimeInterval interval = new TimeInterval(LocalTime.parse(split[0]), LocalTime.parse(split[1]));
            subtitle.setInterval(interval);
            int j = i + 1;
            StringBuilder stringBuilder = new StringBuilder();
            //Rest of the subtitle in certain interval
            while (j < lines.length - 1 && !"".equals(lines[j])) {
                processLine(subtitle, lines[j].toLowerCase());
                stringBuilder.append(lines[j]);
                stringBuilder.append(String.format("\n"));
                j++;
            }
            subtitle.setOriginalText(stringBuilder.toString());
            addWord(subtitle);
            i = j + 1;
        }
        return subtitleWords.toArray(new String[subtitleWords.size()]);
    }

    private void addWord(Subtitle subtitle) {
        int size = subtitles.size();
        for (String s : subtitle.getProcessText()) {
            subtitleWords.add(s);
            indexWords2Subtitle.add(size);
        }
        this.subtitles.add(subtitle);
    }

    private void appendChar(Character c) {
        if (this.stringBuilder == null) {
            this.stringBuilder = new StringBuilder();
        }
        this.stringBuilder.append(c);
    }

    private void appendStringToSubtitle(Subtitle subtitle) {
        String string = this.stringBuilder.toString();
        if (!string.isEmpty()) {
            subtitle.getProcessText().add(string);
            this.stringBuilder = new StringBuilder();
        }
    }

    private void processLine(Subtitle subtitle, String line) {
        StateMachine stateMachine = new StateOne();
        for (int i = 0; i < line.length(); i++) {
            stateMachine = stateMachine.run(line.charAt(i), subtitle);
        }
        appendStringToSubtitle(subtitle);
    }

    interface StateMachine {

        StateMachine run(Character c, Subtitle subtitle);
    }

    public class Subtitle {

        private TimeInterval interval;

        private String originalText;

        private List<String> processText;

        public Subtitle() {
            this.processText = new ArrayList<>(3);
        }

        public Subtitle(TimeInterval interval, String originalText) {
            this.interval = interval;
            this.originalText = originalText;
        }

        public TimeInterval getInterval() {
            return interval;
        }

        public void setInterval(TimeInterval interval) {
            this.interval = interval;
        }

        public String getOriginalText() {
            return originalText;
        }

        public void setOriginalText(String originalText) {
            this.originalText = originalText;
        }

        public List<String> getProcessText() {
            return processText;
        }

        public void setProcessText(List<String> processText) {
            this.processText = processText;
        }

    }

    class StateOne implements StateMachine {

        @Override
        public StateMachine run(Character c, Subtitle subtitle) {
            boolean isAlphaBetChar = isAlphaBetChar(c);
            boolean isBeta = betaSet.contains(c);
            if (isAlphaBetChar) {
                appendChar(c);
                return this;
            }
            if (isBeta) {
                return new StateThree();
            }
            appendStringToSubtitle(subtitle);
            return new StateTwo();
        }

    }

    class StateTwo implements StateMachine {

        @Override
        public StateMachine run(Character c, Subtitle subtitle) {
            boolean isAlphaBetChar = isAlphaBetChar(c);
            boolean isBeta = betaSet.contains(c);
            if (isAlphaBetChar) {
                appendChar(c);
                return new StateOne();
            }
            if (isBeta) {
                return new StateThree();
            }
            return this;
        }
    }

    class StateThree implements StateMachine {

        @Override
        public StateMachine run(Character c, Subtitle subtitle) {
            boolean isBetaStar = betaStarSet.contains(c);
            if (isBetaStar)
                return new StateOne();

            return this;
        }
    }

}
