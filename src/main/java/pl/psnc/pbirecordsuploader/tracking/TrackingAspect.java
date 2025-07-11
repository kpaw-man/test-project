package pl.psnc.pbirecordsuploader.tracking;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Aspect
@Configuration
public class TrackingAspect {
    @Pointcut("@annotation(pl.psnc.pbirecordsuploader.tracking.annotations.Track)")
    public void trackHarvestingAnnotation() {
    }

    /**
     * Advice that adds tracking identifier - trackId - to {@link TrackingContext}. It gets trackId from method parameters
     * so it is very important to use {@link pl.psnc.pbirecordsuploader.tracking.annotations} only on methods with at least
     * one parameter implementing {@link Traceable}.
     * <br> After method finishes it clears {@link TrackingContext}.
     *
     * @param proceedingJoinPoint adviced method
     * @return parameter implementing {@link Traceable}
     * @throws IllegalStateException if no argument implements {@link Traceable}
     */
    @Around("trackHarvestingAnnotation()")
    public Object aroundAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Traceable traceableMsg = getTraceableArgument(proceedingJoinPoint);
        TrackingContext.setTrackId(traceableMsg.getTrackId());
        try {
            return proceedingJoinPoint.proceed();
        } finally {
            TrackingContext.clear();
        }
    }

    private Traceable getTraceableArgument(ProceedingJoinPoint proceedingJoinPoint) {
        return (Traceable) Arrays.stream(proceedingJoinPoint.getArgs())
                .filter(Traceable.class::isInstance)
                .findFirst().orElseThrow(() -> new IllegalStateException(
                        "Couldn't find argument of type " + Traceable.class.getCanonicalName()));
    }
}

