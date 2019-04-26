package it.solutionsexmachina.webcontroller.aspects;

import it.solutionsexmachina.webcontroller.annotation.ServiceMethod;
import it.solutionsexmachina.webcontroller.aspects.annotations.ValidationMethod;
import it.solutionsexmachina.webcontroller.exceptions.ValidationErrorsException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;

@Aspect
public class ValidationAspect {

    @Pointcut("@annotation(serviceMethod) && execution(* *(..))")
    public void validation(ServiceMethod serviceMethod) {
    }

    @Before("validation(serviceMethod)")
    public void executeAuditAnnotationBefore(JoinPoint pjp, ServiceMethod serviceMethod) throws Throwable {
        validate(pjp, serviceMethod);
    }

    private void validate(JoinPoint pjp, ServiceMethod serviceMethod) throws Exception{
        Method[] methods = pjp.getThis().getClass().getMethods();
        for (Method method : methods) {

            ValidationMethod validationMethod = method.getAnnotation(ValidationMethod.class);
            if (validationMethod!=null){

                String serviceName = validationMethod.serviceName();

                if (serviceMethod.value().equals(serviceName)){
                    Function validationFunction = (Function) method.invoke(pjp.getThis(), pjp.getArgs());
                    List<String> errors = (List<String>) validationFunction.apply(pjp.getArgs()[0]);

                    if (errors.size()>0) {
                        throw new ValidationErrorsException().addErrors(errors);
                    }
                }
            }
        }
    }

}
