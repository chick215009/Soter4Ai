package ch.uzh.ifi.seal.changedistiller.model.entities;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.HeadType;

public class HeadChange {
    public HeadType changeType;

    public String content;

    public HeadChange(HeadType type,String content){
        this.changeType = type;
        this.content = content;
    }

    public String getDescribe(){
        StringBuilder describe = new StringBuilder();
        if (changeType == HeadType.ADD_IMPORT){
            describe.append(" The class add the import part of ");
        } else if (changeType == HeadType.DEL_IMPORT){
            describe.append(" The class delete the import part of ");
        } else if (changeType == HeadType.JAVADOC){
            describe.append(" The class change the JavaDoc : ");
        }

        describe.append(content);

        return describe.toString();
    }

    public HeadType getChangeType() {
        return changeType;
    }

    public void setChangeType(HeadType changeType) {
        this.changeType = changeType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
