package org.twonote.rgwadmin4j.impl;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableSet;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

/**
 * AWS authentication for Amazon S3 for the wonderful java okhttp client
 *
 * <p>The auth class that implements okhttp's Interceptor. It adds the Date and the Authorization
 * header seamlessly according to your request. Usage:
 *
 * <pre>{@code
 * OkHttpClient client = new OkHttpClient.Builder()
 * .addInterceptor(new S3Auth())
 * .build();
 * Request request = new Request.Builder()
 * .url("http://www.publicobject.com/helloworld.txt")
 * .header("User-Agent", "OkHttp Example")
 * .build();
 * Response response = client.newCall(request).execute();
 * response.body().close();
 * }</pre>
 *
 * <p>Inspired by https://github.com/tax/python-requests-aws Created by hrchu on 2017/3/22.
 */
class S3Auth implements Interceptor {

  private final String accessKey;
  private final String secretKey;

  /*
  The subResources that must be included when constructing the CanonicalizedResource Element are acl, lifecycle,
  location, logging, notification, partNumber, policy, requestPayment, torrent, uploadId, uploads, versionId,
  versioning, versions, and website.
   */
  private final Set<String> subResources =
      ImmutableSet.of(
          "acl",
          "lifecycle",
          "location",
          "logging",
          "notification",
          "partNumber",
          "policy",
          "requestPayment",
          "torrent",
          "uploadId",
          "uploads",
          "versionId",
          "versioning",
          "versions",
          "website");

  /*
  If the request specifies query string parameters overriding the response header values (see Get Object), append the
  query string parameters and their values. When signing, you do not encode these values; however, when making the
  request, you must encode these parameter values. The query string parameters in a GET request include
  response-content-type, response-content-language, response-expires, response-cache-control,
  response-content-disposition, and response-content-encoding.
  */
  // TODO: implement this
  Set<String> queryStrings =
      ImmutableSet.of(
          "response-content-type",
          "response-content-language",
          "response-expires",
          "response-cache-control",
          "response-content-disposition",
          "response-content-encoding");

  public S3Auth(String accessKey, String secretKey) {
    this.accessKey = accessKey;
    this.secretKey = secretKey;
  }

  private static String encodeBase64(byte[] data) {
    String base64 = Base64.getEncoder().encodeToString(data);
    if (base64.endsWith("\r\n")) base64 = base64.substring(0, base64.length() - 2);
    if (base64.endsWith("\n")) base64 = base64.substring(0, base64.length() - 1);

    return base64;
  }

  @Override
  public Response intercept(Interceptor.Chain chain) throws IOException {
    Request request = chain.request();
    String httpVerb = request.method();
    String date = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneId.of("GMT")));
    String resource = request.url().encodedPath();

    try {
      String subresource = request.url().queryParameterName(0);
      if (subResources.contains(subresource)) {
        resource += "?" + subresource;
      }
    } catch (Exception e) {
      // not match, do nothing here.
    }

    String sign = sign(httpVerb, date, resource);

    request = request.newBuilder().header("Authorization", sign).header("Date", date).build();

    return chain.proceed(request);
  }

  private String sign(String httpVerb, String date, String resource) {
    return sign(httpVerb, "", "", date, resource, null);
  }

  private String sign(
      String httpVerb,
      String contentMD5,
      String contentType,
      String date,
      String resource,
      Map<String, String> metas) {

    StringBuilder stringToSign = new StringBuilder(
        httpVerb + "\n" + CharMatcher.whitespace().trimFrom(contentMD5) + "\n" + CharMatcher.whitespace()
            .trimFrom(contentType) + "\n" + date + "\n");
    if (metas != null) {
      for (Map.Entry<String, String> entity : metas.entrySet()) {
        stringToSign.append(CharMatcher.whitespace().trimFrom(entity.getKey())).append(":")
            .append(CharMatcher.whitespace().trimFrom(entity.getValue())).append("\n");
      }
    }
    stringToSign.append(resource);
    try {
      Mac mac = Mac.getInstance("HmacSHA1");
      byte[] keyBytes = secretKey.getBytes("UTF8");
      SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");
      mac.init(signingKey);
      byte[] signBytes = mac.doFinal(stringToSign.toString().getBytes("UTF8"));
      String signature = encodeBase64(signBytes);
      return "AWS" + " " + accessKey + ":" + signature;
    } catch (Exception e) {
      throw new RuntimeException("MAC CALC FAILED.");
    }
  }
}
