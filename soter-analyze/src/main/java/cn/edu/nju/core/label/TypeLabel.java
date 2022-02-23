package cn.edu.nju.core.label;

public enum TypeLabel {
    /*
    接口、抽象类、枚举、注解、嵌套类（包含内部类）
     */
    INTERFACE("interface", 0),
    ABSTRACT("abstract", 1),
    ENUMERATION("enumeration", 2),
    ANNOTATION("annotation", 3),
    NESTED_CLASSES("nested class", 4),
    NO_PUBLIC("no public", 5),
    USUAL_CLASS("usual class", 6);


    private String label;
    private int id;

    TypeLabel(String label, int id) {
        this.label = label;
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public int getId() {
        return id;
    }
}
