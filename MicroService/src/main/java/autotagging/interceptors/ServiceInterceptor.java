package de.funkedigital.autotagging.interceptors;

import de.funkedigital.autotagging.entities.ExecutedArticle;
import de.funkedigital.autotagging.entities.FailedArticle;
import de.funkedigital.autotagging.entities.PendingArticle;
import de.funkedigital.autotagging.repositories.ExecutedArticleRepository;
import de.funkedigital.autotagging.repositories.FailedArticleRepository;
import de.funkedigital.autotagging.repositories.PendingArticleRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
@Aspect
public class ServiceInterceptor {

    @Autowired
    private ExecutedArticleRepository executedArticleRepository;

    @Autowired
    private PendingArticleRepository pendingArticleRepository;

    @Autowired
    private FailedArticleRepository failedArticleRepository;

    //@Around("execution(* de.funkedigital.autotagging.services.AutoTaggingServiceInterface.run(..))")
    public void aroundScheduleService(ProceedingJoinPoint joinPoint) {
        PendingArticle pendingArticle = pendingArticleRepository.findTopArticle((String)joinPoint.getArgs()[0]);
        Long articleId = pendingArticle.getArticleId();
        String publication = pendingArticle.getPublication();

        try {
            joinPoint.proceed(new Object[]{articleId});
            executedArticleRepository.save(new ExecutedArticle(articleId, publication));
        } catch (Throwable th) {
            failedArticleRepository.save(new FailedArticle(articleId, publication));
        } finally {
            pendingArticleRepository.delete(articleId);
        }
    }

    //@Around("execution(* de.funkedigital.autotagging.services.AutoTaggingWebService.returnKeywords(..))")
    public void aroundWebService(ProceedingJoinPoint joinPoint) {
        Long articleId = (Long) joinPoint.getArgs()[0];
        String publication = (String) joinPoint.getArgs()[1];

        try {
            joinPoint.proceed(joinPoint.getArgs());
            executedArticleRepository.save(new ExecutedArticle(articleId, publication));
        } catch (Throwable th) {
            failedArticleRepository.save(new FailedArticle(articleId, publication));
        } finally {
            pendingArticleRepository.delete(articleId);
        }
    }
}
