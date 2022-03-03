package cn.edu.nju.analyze.domain.vo;

import cn.edu.nju.core.stereotype.taxonomy.MethodStereotype;
import lombok.Data;

import java.util.*;

@Data
public class CommitGeneratedStatisticsVO {
    private List<List<String>> res;
    private Map<String, Integer> map;
    public CommitGeneratedStatisticsVO() {
        int i = 0;
        res = new ArrayList<>();
        map = new HashMap<>();

        res.add(new ArrayList<>(Arrays.asList("product")));
        map.put("product", i++);

        res.add(new ArrayList<>(Arrays.asList(MethodStereotype.GET.name())));
        map.put(MethodStereotype.GET.name(), i++);

        res.add(new ArrayList<>(Arrays.asList(MethodStereotype.PREDICATE.name())));
        map.put(MethodStereotype.PREDICATE.name(), i++);

        res.add(new ArrayList<>(Arrays.asList(MethodStereotype.PROPERTY.name())));
        map.put(MethodStereotype.PROPERTY.name(), i++);

        res.add(new ArrayList<>(Arrays.asList(MethodStereotype.VOID_ACCESSOR.name())));
        map.put(MethodStereotype.VOID_ACCESSOR.name(), i++);

        res.add(new ArrayList<>(Arrays.asList(MethodStereotype.SET.name())));
        map.put(MethodStereotype.SET.name(), i++);

        res.add(new ArrayList<>(Arrays.asList(MethodStereotype.COMMAND.name())));
        map.put(MethodStereotype.COMMAND.name(), i++);

        res.add(new ArrayList<>(Arrays.asList(MethodStereotype.NON_VOID_COMMAND.name())));
        map.put(MethodStereotype.NON_VOID_COMMAND.name(), i++);

        res.add(new ArrayList<>(Arrays.asList(MethodStereotype.CONSTRUCTOR.name())));
        map.put(MethodStereotype.CONSTRUCTOR.name(), i++);

        res.add(new ArrayList<>(Arrays.asList(MethodStereotype.DESTRUCTOR.name())));
        map.put(MethodStereotype.DESTRUCTOR.name(), i++);

        res.add(new ArrayList<>(Arrays.asList(MethodStereotype.COPY_CONSTRUCTOR.name())));
        map.put(MethodStereotype.COPY_CONSTRUCTOR.name(), i++);

        res.add(new ArrayList<>(Arrays.asList(MethodStereotype.FACTORY.name())));
        map.put(MethodStereotype.FACTORY.name(), i++);

        res.add(new ArrayList<>(Arrays.asList(MethodStereotype.COLLABORATOR.name())));
        map.put(MethodStereotype.COLLABORATOR.name(), i++);

        res.add(new ArrayList<>(Arrays.asList(MethodStereotype.CONTROLLER.name())));
        map.put(MethodStereotype.CONTROLLER.name(), i++);

        res.add(new ArrayList<>(Arrays.asList(MethodStereotype.LOCAL_CONTROLLER.name())));
        map.put(MethodStereotype.LOCAL_CONTROLLER.name(), i++);

        res.add(new ArrayList<>(Arrays.asList(MethodStereotype.ABSTRACT.name())));
        map.put(MethodStereotype.ABSTRACT.name(), i++);

        res.add(new ArrayList<>(Arrays.asList(MethodStereotype.EMPTY.name())));
        map.put(MethodStereotype.EMPTY.name(), i++);

        res.add(new ArrayList<>(Arrays.asList(MethodStereotype.INCIDENTAL.name())));
        map.put(MethodStereotype.INCIDENTAL.name(), i++);

    }
}
