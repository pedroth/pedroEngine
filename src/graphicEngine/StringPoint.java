package graphicEngine;


public class StringPoint extends Point {
    private String string;

    public StringPoint(String string) {
        this.string = string;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }
}
