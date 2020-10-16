package ru.chellenge.task1.pojo;

import lombok.Builder;
import lombok.Data;
import lombok.With;

@Data
public class Man {

    private int id;
    private String name;
    private String surname;
    private int age;
    private String birthdate;

    @Override
    public String toString() {
        return "('"+name+"','"+surname+"','"+age+"','"+birthdate+"')";
    }
}
