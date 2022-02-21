package cn.edu.nju.core.textgenerator.phrase;

import cn.edu.nju.core.textgenerator.pos.TaggedTerm;

import java.io.IOException;
import java.util.LinkedList;

public abstract class Phrase {
    protected LinkedList<TaggedTerm> taggedPhrase;

    protected Phrase() {
        super();
    }

    protected Phrase(final LinkedList<TaggedTerm> taggedPhrase) {
        super();
        this.taggedPhrase = taggedPhrase;
    }

    public abstract void generate() throws IOException, ClassNotFoundException;

    public abstract String toString();
}
