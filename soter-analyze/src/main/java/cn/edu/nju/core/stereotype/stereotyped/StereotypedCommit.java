package cn.edu.nju.core.stereotype.stereotyped;

import cn.edu.nju.core.stereotype.rules.CommitStereotypesRules;
import cn.edu.nju.core.stereotype.taxonomy.CommitStereotype;
import cn.edu.nju.core.stereotype.taxonomy.MethodStereotype;
import com.alibaba.fastjson.JSON;

import java.util.*;

public class StereotypedCommit {

    private List<StereotypedMethod> methods;
    private TreeMap<MethodStereotype, Integer> signatureMap = new TreeMap<MethodStereotype, Integer>();
    private CommitStereotype primaryStereotype;
    private CommitStereotype secondaryStereotype;

    public StereotypedCommit(List<StereotypedMethod> methods) {
        super();
        this.methods = methods;
    }

    public String buildSignature() {
//        signatureMap = new TreeMap<MethodStereotype, Integer>();
        if (methods.size() == 0) {
            return "";
        }
        for(Object object: methods) {
            if(object instanceof StereotypedMethod) {
                StereotypedMethod method = (StereotypedMethod) object;
                Integer value = null;
                if(!getSignatureMap().containsKey(method.getStereotypes().get(0))) {
                    getSignatureMap().put((MethodStereotype) method.getStereotypes().get(0), 1);
                } else {
                    value = getSignatureMap().get(method.getStereotypes().get(0));
                    getSignatureMap().put((MethodStereotype) method.getStereotypes().get(0), value + 1);
                }
            }
        }
//        System.out.println("signatures: " + getSignatureMap().toString());
        if (getSignatureMap().size() == 0) {
            return "";
        }else {
            return JSON.toJSONString(getSignatureMap());
        }
    }

    public String buildSignature(String username, String repoName, Map<String, Set<String>> stereotypeMap) {
        if (methods.size() == 0) {
            return "";
        }
        for(Object object: methods) {
            if(object instanceof StereotypedMethod) {
                StereotypedMethod method = (StereotypedMethod) object;
                Integer value = null;
                if(!getSignatureMap().containsKey(method.getStereotypes().get(0))) {
                    MethodStereotype methodStereotype = (MethodStereotype) method.getStereotypes().get(0);
                    System.out.println(method.getMethod().getName() + "-----" + methodStereotype.name());
                    if (!stereotypeMap.containsKey(methodStereotype.name())) {
                        stereotypeMap.put(methodStereotype.name(), new HashSet<>());
                    }
                    stereotypeMap.get(methodStereotype.name()).add(((StereotypedMethod) object).getTypeAbsolutePath());
                    getSignatureMap().put((MethodStereotype) method.getStereotypes().get(0), 1);
                } else {
                    MethodStereotype methodStereotype = (MethodStereotype) method.getStereotypes().get(0);
                    System.out.println(method.getMethod().getName() + "-----" + methodStereotype.name());
                    if (!stereotypeMap.containsKey(methodStereotype.name())) {
                        stereotypeMap.put(methodStereotype.name(), new HashSet<>());
                    }
                    stereotypeMap.get(methodStereotype.name()).add(((StereotypedMethod) object).getTypeAbsolutePath());
                    value = getSignatureMap().get(method.getStereotypes().get(0));
                    getSignatureMap().put((MethodStereotype) method.getStereotypes().get(0), value + 1);
                }

            }
        }
        System.out.println("===================");
//        System.out.println("signatures: " + getSignatureMap().toString());
        if (getSignatureMap().size() == 0) {
            return "";
        }else {
            return JSON.toJSONString(getSignatureMap());
        }
    }

    public CommitStereotype findStereotypes() {
        CommitStereotypesRules rules = new CommitStereotypesRules();
        primaryStereotype = null;

        //small modifier
        primaryStereotype = rules.checkSmallModifier(methods, signatureMap);
        if(primaryStereotype != null) {
            return primaryStereotype;
        }

        //large modifier
        primaryStereotype = rules.checkLargeModifier(methods, signatureMap);
        if(primaryStereotype != null) {
            return primaryStereotype;
        }

        //degenarate modifier
        primaryStereotype = rules.checkDegenerateModifier(methods, signatureMap);
        if(primaryStereotype != null) {
            return primaryStereotype;
        }

        //lazy modifier
        primaryStereotype = rules.checkLazyModifier(methods, signatureMap);
        if(primaryStereotype != null) {
            return primaryStereotype;
        }

        //control modifier
        primaryStereotype = rules.checkControlModifier(methods, signatureMap);
        if(primaryStereotype != null) {
            return primaryStereotype;
        }

        //relationship modifier
        primaryStereotype = rules.checkRelationshipModifier(methods, signatureMap);
        if(primaryStereotype != null) {
            secondaryStereotype = rules.checkBehaviorModifier(methods, signatureMap);
            if(secondaryStereotype != null) {
                CommitStereotype tmp = primaryStereotype;
                primaryStereotype = secondaryStereotype;
                secondaryStereotype = tmp;
            } else {
                secondaryStereotype = rules.checkStateAccessModifier(methods, signatureMap);
                if(secondaryStereotype != null) {
                    CommitStereotype tmp = primaryStereotype;
                    primaryStereotype = secondaryStereotype;
                    secondaryStereotype = tmp;
                }
            }
            return primaryStereotype;
        }

        //state update modifier
        primaryStereotype = rules.checkStateUpdateModifier(methods, signatureMap);
        if(primaryStereotype != null) {
            secondaryStereotype = rules.checkBehaviorModifier(methods, signatureMap);
            if(secondaryStereotype != null) {
                CommitStereotype tmp = primaryStereotype;
                primaryStereotype = secondaryStereotype;
                secondaryStereotype = tmp;
            }
            return primaryStereotype;
        }

        //state access modifier
        primaryStereotype = rules.checkStateAccessModifier(methods, signatureMap);
        if(primaryStereotype != null) {
            return primaryStereotype;
        }

        //structure modifier
        primaryStereotype = rules.checkStructureModifier(methods, signatureMap);
        if(primaryStereotype != null) {
            return primaryStereotype;
        }

        //object creation modifier
        primaryStereotype = rules.checkObjectCreationModifier(methods, signatureMap);
        if(primaryStereotype != null) {
            return primaryStereotype;
        }

        primaryStereotype = CommitStereotype.UNKNOWN_MODIFIER;
        return primaryStereotype;
    }

    public List<CommitStereotype> getStereotypes() {
        List<CommitStereotype> stereotypes = new ArrayList<CommitStereotype>();

        if(primaryStereotype !=  null) {
            stereotypes.add(primaryStereotype);
        }

        if(secondaryStereotype !=  null) {
            stereotypes.add(secondaryStereotype);
        }

        return stereotypes;
    }

    public TreeMap<MethodStereotype, Integer> getSignatureMap() {
        return signatureMap;
    }

    public void setSignatureMap(TreeMap<MethodStereotype, Integer> signatureMap) {
        this.signatureMap = signatureMap;
    }

    public List<StereotypedMethod> getMethods() {
        return methods;
    }

    public void setMethods(List<StereotypedMethod> methods) {
        this.methods = methods;
    }
}

