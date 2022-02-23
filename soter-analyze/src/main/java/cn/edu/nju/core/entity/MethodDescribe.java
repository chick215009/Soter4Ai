package cn.edu.nju.core.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MethodDescribe {
    String methodName;
    String stereoType;
    String describe;
}
