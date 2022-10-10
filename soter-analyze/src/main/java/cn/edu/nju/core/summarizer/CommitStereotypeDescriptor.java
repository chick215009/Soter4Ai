package cn.edu.nju.core.summarizer;

import cn.edu.nju.core.stereotype.stereotyped.StereotypedCommit;
import cn.edu.nju.core.stereotype.taxonomy.CommitStereotype;

import java.util.HashMap;
import java.util.Map;

public class CommitStereotypeDescriptor {
    static Map<CommitStereotype,String> formalDescriptionMap = new HashMap<>();
    static Map<CommitStereotype,String> markDescriptionMap = new HashMap<>();

    public static void init(){
        formalDescriptionMap.put(CommitStereotype.STRUCTURE_MODIFIER,"This is a structure modifier commit: this change set is composed only of setter and getter methods, and these methods perform simple access and modifications to the data. ");
        formalDescriptionMap.put(CommitStereotype.STATE_ACCESS_MODIFIER,"This is a state access modifier commit: this change set is composed only of accessor methods, and these methods provide a client with information, but the data members are not modified. ");
        formalDescriptionMap.put(CommitStereotype.STATE_UPDATE_MODIFIER,"This is a state update modifier commit: this change set is composed only of mutator methods, and these methods provide changes related to updates of an object's state. ");
        formalDescriptionMap.put(CommitStereotype.BEHAVIOR_MODIFIER,"This is a behavior modifier commit: this change set is composed of command and non-void-command methods, and these methods execute complex internal behavioral changes within an object. ");
        formalDescriptionMap.put(CommitStereotype.OBJECT_CREATION_MODIFIER,"This is an object creation modifier commit: this change set is composed of factory, constructor, copy constructor and destructor methods, and these methods allow the creation of objects. ");
        formalDescriptionMap.put(CommitStereotype.RELATIONSHIP_MODIFIER,"This is a relationship modifier commit: this change set is composed mainly of collaborators and low number of controller methods, and these methods implement generalization, dependency and association performing calls on parameters or local variable objects. ");
        formalDescriptionMap.put(CommitStereotype.CONTROL_MODIFIER,"This is a control modifier commit: this change set is composed mainly of controller, factory, constructor, copy-constructor and destructor methods, and these methods modify the external behavior of the participating classes. ");
        formalDescriptionMap.put(CommitStereotype.LARGE_MODIFIER,"This is a large modifier commit: this is a commit with many methods and combines multiple roles. ");
        formalDescriptionMap.put(CommitStereotype.LAZY_MODIFIER,"This is a lazy modifier commit: this change set is composed of getter and setter methods mainly, and a low percentage of other methods. These methods denote new or planned feature that is not yet completed. ");
        formalDescriptionMap.put(CommitStereotype.DEGENERATE_MODIFIER,"This is a degenerate modifier commit: this change set is composed of empty, incidental, and abstract methods. These methods indicate that a new feature is planned. ");
        formalDescriptionMap.put(CommitStereotype.SMALL_MODIFIER,"This is a small modifier commit that does not change the system significantly.");
        formalDescriptionMap.put(CommitStereotype.UNKNOWN_MODIFIER,"This is a unknown modifier commit that does not change the system significantly.");

        markDescriptionMap.put(CommitStereotype.STRUCTURE_MODIFIER,"Ty0 ");
        markDescriptionMap.put(CommitStereotype.STATE_ACCESS_MODIFIER,"Ty1 ");
        markDescriptionMap.put(CommitStereotype.STATE_UPDATE_MODIFIER,"Ty2 ");
        markDescriptionMap.put(CommitStereotype.BEHAVIOR_MODIFIER,"Ty3 ");
        markDescriptionMap.put(CommitStereotype.OBJECT_CREATION_MODIFIER,"Ty4 ");
        markDescriptionMap.put(CommitStereotype.RELATIONSHIP_MODIFIER,"Ty5 ");
        markDescriptionMap.put(CommitStereotype.CONTROL_MODIFIER,"Ty6 ");
        markDescriptionMap.put(CommitStereotype.LARGE_MODIFIER,"Ty7 ");
        markDescriptionMap.put(CommitStereotype.LAZY_MODIFIER,"Ty8 ");
        markDescriptionMap.put(CommitStereotype.DEGENERATE_MODIFIER,"Ty9 ");
        markDescriptionMap.put(CommitStereotype.SMALL_MODIFIER,"Ty10 ");
        markDescriptionMap.put(CommitStereotype.UNKNOWN_MODIFIER,"Ty11 ");

    }
    public static String describe( StereotypedCommit stereotypedCommit) {
        if (formalDescriptionMap.isEmpty() || markDescriptionMap.isEmpty()){
            init();
        }

        return markDescriptionMap.get(stereotypedCommit.getStereotypes().get(0));
    }
}
