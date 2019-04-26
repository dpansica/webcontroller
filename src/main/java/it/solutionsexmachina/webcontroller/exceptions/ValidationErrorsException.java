package it.solutionsexmachina.webcontroller.exceptions;

import java.util.ArrayList;
import java.util.List;

public class ValidationErrorsException extends Exception {

    private List<String> errors = new ArrayList<>();

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public ValidationErrorsException addError(String error){
        errors.add(error);
        return this;
    }

    @Override
    public String getMessage() {
        String errorsString = "";
        for (String error : errors) {
            errorsString += error +"|";
        }

        return errorsString;
    }

    public ValidationErrorsException addErrors(List<String> errors) {
        for (String error : errors) {
            addError(error);
        }

        return this;
    }
}
