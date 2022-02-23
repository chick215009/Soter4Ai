package cn.edu.nju.core.entity;

public class MyModule {
    private String name;

    public MyModule(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return name.equals(((MyModule)obj).getName());
    }

    public String getName() {
        return name;
    }
}
