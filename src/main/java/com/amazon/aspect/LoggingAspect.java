package com.amazon.aspect;

import com.amazon.entity.Company;
import com.amazon.entity.User;
import com.amazon.repository.CompanyRepository;
import com.amazon.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    private static final Logger logger = LogManager.getLogger(LoggingAspect.class);
    private final CompanyRepository companyRepository;

    private UserRepository userRepository;

    public LoggingAspect(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Pointcut("execution(* com.amazon..*(..)) throws RuntimeException")
    public void runtimeExceptionPointcut() {
    }

    @Around("@annotation(com.amazon.annotation.ExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        logger.info("* {} method execution starts", methodName);

        long startTime = System.currentTimeMillis();

        Object proceed = joinPoint.proceed();

        long executionTime = System.currentTimeMillis() - startTime;

        logger.info("*** " + methodName + " executed in " + executionTime +" ms");

        return proceed;
    }

    @AfterThrowing(pointcut = "runtimeExceptionPointcut()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, RuntimeException ex) {
        String methodName = joinPoint.getSignature().getName();
        String exceptionName = ex.getClass().getSimpleName();
        String exceptionMessage = ex.getMessage();
        String detailedMessage = String.format("Exception in method: %s | Exception: %s | Message: %s",
                methodName, exceptionName, exceptionMessage);

        logger.error(detailedMessage);
    }


    @Pointcut("@annotation(com.amazon.annotation.LoggingAnnotation)")
    public void logCompanyActionPointcut() {
    }


    @After("logCompanyActionPointcut()")
    public void logCompanyAction(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof Long) {
            Long companyId = (Long) args[0];
            Company company = companyRepository.findById(companyId).orElse(null);
            if (company != null) {
                Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                String username = "Unknown";
                String firstName = "Unknown";
                String lastName = "Unknown";

                if (principal instanceof UserDetails) {
                    username = ((UserDetails) principal).getUsername();
                    User user = userRepository.findByUsername(username);
                    firstName = user.getFirstname();
                    lastName = user.getLastname();
                }

                String methodName = joinPoint.getSignature().getName();
                String action = methodName.contains("activate") ? "activate" : "deactivate";
                logger.info("Method: {}, Company: {}, User: {} {} ({})",
                        action, company.getTitle(), firstName, lastName, username);
            }
        }
    }

}
