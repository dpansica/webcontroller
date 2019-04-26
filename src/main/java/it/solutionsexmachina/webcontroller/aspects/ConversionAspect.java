package it.solutionsexmachina.webcontroller.aspects;

import it.solutionsexmachina.webcontroller.annotation.ServiceMethod;
import it.solutionsexmachina.webcontroller.aspects.annotations.ConversionMethod;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

import java.lang.reflect.Method;
import java.util.function.Function;

@Aspect
public class ConversionAspect {

    @Pointcut("@annotation(serviceMethod) && execution(* *(..))")
    public void conversion(ServiceMethod serviceMethod) {
    }

    @Around("conversion(serviceMethod)")
    public Object resultConversion(ProceedingJoinPoint pjp, ServiceMethod serviceMethod) throws Throwable {
        Object result = pjp.proceed();
        return convert(pjp, serviceMethod, result);
    }

    private Object convert(ProceedingJoinPoint pjp, ServiceMethod serviceMethod, Object result) throws Exception{
        Method[] methods = pjp.getThis().getClass().getMethods();
        for (Method method : methods) {

            ConversionMethod conversionMethod = method.getAnnotation(ConversionMethod.class);
            if (conversionMethod!=null){

                String serviceName = conversionMethod.serviceName();

                if (serviceMethod.value().equals(serviceName)){

                    Function conversionFunction = (Function) method.invoke(pjp.getThis(), pjp.getArgs());
                    Object converted = conversionFunction.apply(result);

                    return converted;
                }
            }
        }
        return result;
    }

}
