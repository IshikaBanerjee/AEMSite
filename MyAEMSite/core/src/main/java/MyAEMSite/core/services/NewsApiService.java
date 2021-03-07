package MyAEMSite.core.services;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.commons.json.JSONException;

import javax.jcr.RepositoryException;
import java.io.IOException;

public interface NewsApiService {
    public void getAndSaveNewsHeadlines(String newsString) throws IOException, JSONException, LoginException, RepositoryException;
}
