package cn.edu.nju.core.summarizer;

import java.util.Objects;

public class CmDescription {
    public CmDescription(String description, String stype, int rangeLEnd, int rangeREnd) {
        this.description = description;
        this.stype = stype;
        this.rangeLEnd = rangeLEnd;
        this.rangeREnd = rangeREnd;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getRangeLEnd() {
        return rangeLEnd;
    }

    public void setRangeLEnd(int rangeLEnd) {
        this.rangeLEnd = rangeLEnd;
    }

    public int getRangeREnd() {
        return rangeREnd;
    }

    public void setRangeREnd(int rangeREnd) {
        this.rangeREnd = rangeREnd;
    }

    String description;
    String stype;
    int rangeLEnd;
    int rangeREnd;

    public String getStype() {
        return stype;
    }

    public void setStype(String stype) {
        this.stype = stype;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CmDescription that = (CmDescription) o;
        return rangeLEnd == that.rangeLEnd && rangeREnd == that.rangeREnd && Objects.equals(description, that.description) && Objects.equals(stype, that.stype);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, stype, rangeLEnd, rangeREnd);
    }
}
