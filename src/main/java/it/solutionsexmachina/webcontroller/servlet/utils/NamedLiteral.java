package it.solutionsexmachina.webcontroller.servlet.utils;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Named;

public class NamedLiteral extends AnnotationLiteral<Named> implements Named {

    private String value;

    NamedLiteral(String value) {
        this.value = value;
    }

    @Override
    public String value() {
        return value;
    }
}
