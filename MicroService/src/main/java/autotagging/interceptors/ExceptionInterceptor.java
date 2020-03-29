package de.funkedigital.autotagging.interceptors;

import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class ExceptionInterceptor {

    @AfterThrowing(pointcut = "execution(* de.funkedigital.autotagging..*(..)))", throwing = "ex")
    public void afterThrowing(Exception ex) {
        //TODO: add article to failed
    }
}
