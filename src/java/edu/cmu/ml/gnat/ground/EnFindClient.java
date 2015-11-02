package edu.cmu.ml.gnat.ground;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class EnFindClient {
//	private static final String ORIGIN = "http://curtis.ml.cmu.edu";
	private static final String ORIGIN = "api";
	private static final String TYPE = "json";
	private static final String MAXMENTIONS = "-1";
	private static final String MINSCORE = "0.3";
	private static final String KEY = "KNVRJ5";
	private static final String RESPONSEFORMAT = "json";
	private static final String URL="http://service.enfind.com/process";
	private static final int MAX_ARTICLE_LENGTH = 20000;
	//?key=KNVRJ5&origin=http://curtis.ml.cmu.edu&minScore=0.3&maxMentions=-1&type=json";
	public CloseableHttpClient httpclient;
	public Gson gson;
	public boolean first=true;
	public PrintStream o;
	public EnFindClient(PrintStream out) {
		 httpclient = HttpClients.createDefault();
	     gson = new Gson();
	     this.o = out;
	}
	
	public List<NameValuePair> formDataTemplate() {
		List<NameValuePair> formData = new ArrayList<NameValuePair>();
		formData.add(new BasicNameValuePair("key", KEY));
		formData.add(new BasicNameValuePair("origin", ORIGIN));
		formData.add(new BasicNameValuePair("minScore",MINSCORE));
		formData.add(new BasicNameValuePair("maxMentions",MAXMENTIONS));
		formData.add(new BasicNameValuePair("type",TYPE));
		formData.add(new BasicNameValuePair("responseFormat",RESPONSEFORMAT));
		return formData;
	}
	
	public int hashArticle(String text) {
		int d = 0;
		for (int i=0; i<text.length(); i++) {
        	char k = text.charAt(i);
        	d = (d << 5) - d + k;
        	d = d & d;
		}
        return Math.abs(d);
	}
	
	private void err(int articleId, String path, String url, String reason) {
    	o.println("#"+articleId+"\t"+path+"\t"+url+"\t"+reason);
	}
	
	public int fetch(String path) throws IOException {
		
		StringBuilder sb = new StringBuilder();
		BufferedReader r = new BufferedReader(new FileReader(path));
		for (String line; (line=r.readLine()) != null;) sb.append(line);
		r.close();
		CachedArticle a = gson.fromJson(sb.toString(), new TypeToken<CachedArticle>(){}.getType());
		a.setContents(a.getContents().trim().replaceAll("\\s\\s*", " "));
		int articleId = hashArticle(a.getContents());
		
		if (a.getContents().equals("")) {
			System.err.println("<empty>");
        	err(articleId,path,a.getUrl(),"document empty");
			return -1;
		}
		if (a.getContents().length() > MAX_ARTICLE_LENGTH) {
			System.err.println("<too long!>");
        	err(articleId,path,a.getUrl(),"too long");
			return -1;
		}
		if (first) { first = false; }
		else {
		    try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		HttpPost httppost = new HttpPost(URL);
		List<NameValuePair> formData = formDataTemplate();
		formData.add(new BasicNameValuePair("articleId",String.valueOf(articleId)));
		formData.add(new BasicNameValuePair("url","http://curtis.ml.cmu.edu/gnat/?id="+articleId));
		formData.add(new BasicNameValuePair("title",""));
		formData.add(new BasicNameValuePair("article",a.getContents()));
		httppost.setEntity(new UrlEncodedFormEntity(formData));
		
		CloseableHttpResponse response = null;
		Wrapper w = null;
		List<EntityLink> extractions = null;
		try {
			response = httpclient.execute(httppost);
			System.err.println(response.getStatusLine());
		    HttpEntity entity = response.getEntity();
		    if (entity != null) {
		    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
		        entity.writeTo(baos);
		        String result = baos.toString();
		        if (result.indexOf('{') < 0 || result.lastIndexOf('}') < 0) {
		        	System.err.println("Trouble with "+path+":\n"+result);
		        	err(articleId,path,a.getUrl(),"response empty");
		        	return -1;
		        }
		        result = result.substring(result.indexOf('{'), result.lastIndexOf('}')+1).replaceAll("\\\\\"", "\"");
//		        System.out.println(result.replaceAll(",", ",\n"));
		        
		        w = gson.fromJson(
		        		result, 
		        		new TypeToken<Wrapper>(){}.getType());
		        extractions = w.getMetaInfo();
		        if (extractions.size() > 0) o.println(articleId+"\t"+path+"\t"+a.getUrl());
		        for (EntityLink pr : extractions) {
		        	o.println(articleId+"\t"+pr.toString());
		        }
		        
		    }
		    EntityUtils.consume(entity);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} finally {
		    if (response != null)
				response.close();
		}
		if (extractions != null) return extractions.size();
		return -1;
	}
	
	public static void main(String[] args) throws IOException {
		if (args.length == 0) {
			System.err.println("Usage:\n\tresults.txt doc1.json doc2.json...");
			System.exit(0);
		}
		String results = args[0];
		PrintStream o = null;
		if ("-".equals(results)) o = System.out;
		else o = new PrintStream(new BufferedOutputStream(new FileOutputStream(results)));
			// otherwise open an output file stream
		EnFindClient client= new EnFindClient(o);
		for (int i=1; i<args.length; i++) {
			int n = client.fetch(args[i]);
			System.err.println(n+"\t"+args[i]);
		}
	}
	
	private class Wrapper {
		private List<EntityLink> metaInfo;
		private String cacheId;
		private int siteId;
		public List<EntityLink> getMetaInfo() {
			return metaInfo;
		}
		public void setMetaInfo(List<EntityLink> metaInfo) {
			this.metaInfo = metaInfo;
		}
		public String getCacheId() {
			return cacheId;
		}
		public void setCacheId(String cacheId) {
			this.cacheId = cacheId;
		}
		public int getSiteId() {
			return siteId;
		}
		public void setSiteId(int siteId) {
			this.siteId = siteId;
		}
	}
}
