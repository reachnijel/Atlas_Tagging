package com.manulife.edl.atlas.tags;

import com.google.common.base.Strings;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Paths;

public class DdDownloader {
    private static final Logger LOGGER = LogManager.getLogger();

    final private HttpClientContext context = HttpClientContext.create();
    private CloseableHttpClient httpClient;
    private RequestConfig autoRedirectReqConfig;

    /**
     * Default constructor, it should be used in line with Bearer Token authorization.
     *
     * @param
     * @return
     * @author Tao
     * @date June 23rd, 2020
     */
    public DdDownloader() {
        httpClient = HttpClientBuilder.create().build();
        context.setCookieStore(new BasicCookieStore());
        autoRedirectReqConfig = RequestConfig.copy(RequestConfig.DEFAULT).setRedirectsEnabled(true).
                setRelativeRedirectsAllowed(true).build();
        context.setCookieStore(new BasicCookieStore());
    }

    /**
     * A parameterized constructor to build a Bitbucket Downloader using BasicAuth.
     *
     * @param username BitBucket Username
     * @param pwd      BitBucket Password
     * @return
     * @author Tao
     * @date July 7th, 2020
     */
    public DdDownloader(String username, String pwd) {
        if (!username.contains("@")) {
            username = username + "@MFCGD.COM";
        }
        httpClient = HttpClientBuilder.create().build();
        autoRedirectReqConfig = RequestConfig.copy(RequestConfig.DEFAULT).setRedirectsEnabled(true).
                setRelativeRedirectsAllowed(true).build();

        HttpHost host = new HttpHost("git.ap.manulife.com", 443, "https");
        AuthCache authCache = new BasicAuthCache();
        authCache.put(host, new BasicScheme());

        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials =
                new UsernamePasswordCredentials(username, pwd);
        provider.setCredentials(AuthScope.ANY, credentials);
        context.setCredentialsProvider(provider);
        context.setAuthCache(authCache);
        context.setCookieStore(new BasicCookieStore());
    }

    /**
     * This is used to download the DD file from Bitbucket.
     *
     * @param url The DD URL to download from
     * @return Null if failed or the file name if succeeded.
     * @author Tao
     * @date
     */
    public String download(String url) throws URISyntaxException {
        if (Strings.isNullOrEmpty(url)) {
            LOGGER.error("The DD URL is null or empty.");
            return null;
        }
        if (!url.endsWith("&raw"))
            url = url.concat("&raw");

        LOGGER.debug("The DD URL is:" + url);

        URIBuilder uri = new URIBuilder(url);
        HttpGet httpGet = new HttpGet(uri.build());
        httpGet.setConfig(autoRedirectReqConfig);

        // execute http get request and serialize the DD to working directory
        try (CloseableHttpResponse response = httpClient.execute(httpGet, context)) {
            // return if error occur when the http request fails
            if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode()) {
                LOGGER.error("Download DD from Git failed, the response status is " + response.getStatusLine().getReasonPhrase() +
                        " " + response.getStatusLine().getStatusCode());
                return null;
            }

            String disposition = response.getFirstHeader("Content-Disposition").getValue();
            LOGGER.debug("Header Content-Disposition: " + disposition);
            // get the original file name
            String filename = disposition.replaceFirst("(?i)^.*filename=\"([^\"]+)\".*$", "$1");
            // generate the absolute path from the working directory
            String filePath = Paths.get(".").toAbsolutePath().normalize() + File.separator + filename;
            LOGGER.debug("Absolute path: " + filePath);

            HttpEntity entity = response.getEntity();
            if (null != entity) {
                BufferedInputStream bufferedInputStream = new BufferedInputStream(entity.getContent());
                File file = new File(filePath);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
                int inByte;
                while ((inByte = bufferedInputStream.read()) != -1)
                    bufferedOutputStream.write(inByte);
                bufferedInputStream.close();
                bufferedOutputStream.close();
            }

            return filename;
        } catch (ClientProtocolException e) {
            LOGGER.error("Something wrong with the HTTP request, please have a look: ", e);
            return null;
        } catch (FileNotFoundException e) {
            LOGGER.error("Unable to create new file in working directory, please check it out: ", e);
            return null;
        } catch (IOException e) {
            LOGGER.error("Http request to download DD failed: ", e);
            return null;
        }
    }

    /**
     * This is used to download the DD file from Bitbucket use Bearer Token Authentication.
     *
     * @param url The DD URL to download from
     * @return Null if failed or the file name if succeeded.
     * @author Tao
     * @date
     */
    public String bearerDownload(String url) throws URISyntaxException {
        if (Strings.isNullOrEmpty(url)) {
            LOGGER.error("The DD URL is null or empty.");
            return null;
        }
        if (!url.endsWith("&raw"))
            url = url.concat("&raw");

        LOGGER.debug("The DD URL is:" + url);

        URIBuilder uri = new URIBuilder(url);
        HttpGet httpGet = new HttpGet(uri.build());
        httpGet.setConfig(autoRedirectReqConfig);

        // for authorization, we could use the bearer token instead of basic authorization type
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, "Bearer NDc3OTYxNTIyNTM2OsACtTZ2C4lAaisefzZ2Qr+7LeZv");

        // execute http get request and serialize the DD to working directory
        try (CloseableHttpResponse response = httpClient.execute(httpGet, context)) {
            // return if error occur when the http request fails
            if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode()) {
                LOGGER.error("Download DD from Git failed, the response status is " + response.getStatusLine().getReasonPhrase() +
                        " " + response.getStatusLine().getStatusCode());
                return null;
            }

            String disposition = response.getFirstHeader("Content-Disposition").getValue();
            LOGGER.debug("Header Content-Disposition: " + disposition);
            // get the original file name
            String filename = disposition.replaceFirst("(?i)^.*filename=\"([^\"]+)\".*$", "$1");
            // generate the absolute path from the working directory
            String filePath = Paths.get(".").toAbsolutePath().normalize() + File.separator + filename;
            LOGGER.debug("Absolute path: " + filePath);

            HttpEntity entity = response.getEntity();
            if (null != entity) {
                BufferedInputStream bufferedInputStream = new BufferedInputStream(entity.getContent());
                File file = new File(filePath);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
                int inByte;
                while ((inByte = bufferedInputStream.read()) != -1)
                    bufferedOutputStream.write(inByte);
                bufferedInputStream.close();
                bufferedOutputStream.close();
            }

            return filename;
        } catch (ClientProtocolException e) {
            LOGGER.error("Something wrong with the HTTP request, please have a look: ", e);
            return null;
        } catch (FileNotFoundException e) {
            LOGGER.error("Unable to create new file in working directory, please check it out: ", e);
            return null;
        } catch (IOException e) {
            LOGGER.error("Http request to download DD failed: ", e);
            return null;
        }
    }
//    public static void main(String[] args) {
////        String url = "https://git.ap.manulife.com/projects/BDP/repos/atlas_script/browse/tagsAutomation/test_atlas_tags_automation.xlsx?at=refs%2Fheads%2FDEVO-108&raw";
//        String url = "https://git.ap.manulife.com/projects/BDP/repos/vn/raw/dd/CAS/EDL_DataDict_VN_CAS.xlsx?at=refs%2Fheads%2Fmaster&raw";
//        DdDownloader dwld = new DdDownloader();
//        try {
//            String filename = dwld.BearerDownload(url);
//            LOGGER.info(filename);
//        } catch (URISyntaxException e) {
//            LOGGER.error("The URI provide is wrong: ", e);
//        }
//    }
}	
