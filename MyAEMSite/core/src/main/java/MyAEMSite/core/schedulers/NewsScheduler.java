package MyAEMSite.core.schedulers;

import MyAEMSite.core.services.NewsApiService;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.commons.json.JSONException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.io.IOException;

@Designate(ocd=NewsScheduler.Config.class)
@Component(service=Runnable.class)
public class NewsScheduler implements Runnable {

    @Reference
    NewsApiService newsApiService;

    @ObjectClassDefinition(name="A scheduled task",
            description = "Updates the news from news API in the site")
    public static @interface Config {

        @AttributeDefinition(name = "Cron-job expression")
        String scheduler_expression() default "*/30 * * * * ?";

        @AttributeDefinition(name = "Concurrent task",
                description = "Whether or not to schedule this task concurrently")
        boolean scheduler_concurrent() default false;

        @AttributeDefinition(name = "News API",
                description = "Can be configured in /system/console/configMgr")
        String newsApi() default "https://newsapi.org/v2/top-headlines";

        @AttributeDefinition(name = "Country Locale",
                description = "Can be configured in /system/console/configMgr")
        String country() default "us";

        @AttributeDefinition(name = "API Key",
                description = "Can be configured in /system/console/configMgr")
        String apiKey() default "2633f92ce16a480c84cf075b79bdffcf";
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String apiKey;
    private String newsApi;
    private String country;

    @Override
    public void run() {
        logger.debug("NewsScheduler is now running, myParameter='{}'", newsApi+"?"+"country="+country+"apiKey="+apiKey);
        String newsString = newsApi+"?"+"country="+country+"apiKey="+apiKey;
        try {
            newsApiService.getAndSaveNewsHeadlines(newsString);
        } catch (IOException | JSONException | LoginException | RepositoryException e) {
            e.printStackTrace();
        }
    }

    @Activate
    protected void activate(final NewsScheduler.Config config) {
        apiKey = config.apiKey();
        country = config.country();
        newsApi = config.newsApi();
    }
}
