package MyAEMSite.core.services.impl;

import MyAEMSite.core.services.NewsApiService;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.jackrabbit.oak.commons.json.JsonObject;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

@Component(name="NewsApiService", service=NewsApiServiceImpl.class, immediate=true)
public class NewsApiServiceImpl implements NewsApiService {
    @Reference
    ResourceResolverFactory resourceResolverFactory;

    @Override
    public void getAndSaveNewsHeadlines(String newsString) throws IOException, JSONException, LoginException, RepositoryException {
        HashMap<String, Object> param = new HashMap<String, Object>();
        param.put(ResourceResolverFactory.SUBSERVICE, "writeservice");
        ResourceResolver resourceResolver = resourceResolverFactory.getResourceResolver(param);
        Session session = resourceResolver.adaptTo(Session.class);
        CloseableHttpClient closeableHttpClient = HttpClientBuilder.create().build();
        HttpGet httpGetRequest = new HttpGet(newsString);
        httpGetRequest.addHeader("accept","application/json");
        HttpResponse httpResponse = closeableHttpClient.execute(httpGetRequest);
        if(httpResponse.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("Failed to fetch--> HTTP Status Code:"+ httpResponse.getStatusLine().getStatusCode());
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
        String output;
        String jsonString = "";
        while ((output = reader.readLine()) != null) {
            jsonString = jsonString + output;
        }
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray articles = jsonObject.getJSONArray("articles");
        Node rootNode = session.getRootNode();
        Node newsNode = rootNode.addNode("newsNode");
        for (int i=0; i< articles.length(); i++) {
            JSONObject jsonObj = articles.getJSONObject(i);
            String title = jsonObj.get("title").toString();
            String desc = jsonObj.get("description").toString();
            String nodeName = "news" + i;
            Node node = newsNode.addNode(nodeName);
            node.setProperty("title", title);
            node.setProperty("description", desc);
        }
        session.save();
    }
}
