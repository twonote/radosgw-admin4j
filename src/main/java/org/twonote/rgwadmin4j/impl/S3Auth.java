package org.twonote.rgwadmin4j.impl;

import com.google.common.base.CharMatcher;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;

/**
 * AWS authentication for Amazon S3  for the wonderful java okhttp client
 * <p>
 * The auth class that implements okhttp's Interceptor. It adds the Date and the Authorization header seamlessly according to your request.
 * </p>
 * Usage:
 * <pre>
 *    {@code
 * OkHttpClient client = new OkHttpClient.Builder()
 * .addInterceptor(new S3Auth())
 * .build();
 *
 * Request request = new Request.Builder()
 * .url("http://www.publicobject.com/helloworld.txt")
 * .header("User-Agent", "OkHttp Example")
 * .build();
 *
 * Response response = client.newCall(request).execute();
 * response.body().close();
 * }
 *  </pre>
 *  <p>
 *    Inspired by https://github.com/tax/python-requests-aws
 *  </p>
 * Created by hrchu on 2017/3/22.
 */
class S3Auth implements Interceptor {
  private static final Logger LOG = LoggerFactory.getLogger(S3Auth.class);

  private final String accessKey;
  private final String secretKey;

  public S3Auth(String accessKey, String secretKey) {
    this.accessKey = accessKey;
    this.secretKey = secretKey;
  }

  @Override
  public Response intercept(Interceptor.Chain chain) throws IOException {
    Request request = chain.request();
    String httpVerb = request.method();
    String date = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneId.of("GMT")));
    String resource = request.url().encodedPath();
    String sign = sign(httpVerb, date, resource);
    request = request.newBuilder()
        .header("Authorization", sign)
        .header("Date", date)
        .build();
    return chain.proceed(request);
  }

  private String sign(String httpVerb, String date, String resource) {
    return sign(httpVerb, "", "", date, resource, null);
  }

  private String sign(String httpVerb, String contentMD5,
      String contentType, String date, String resource,
      Map<String, String> metas) {

    String stringToSign = httpVerb + "\n"
        + CharMatcher.whitespace().trimFrom(contentMD5) + "\n"
        + CharMatcher.whitespace().trimFrom(contentType) + "\n" + date + "\n";
    if (metas != null) {
      for (Map.Entry<String, String> entity : metas.entrySet()) {
        stringToSign += CharMatcher.whitespace().trimFrom(entity.getKey()) + ":"
            + CharMatcher.whitespace().trimFrom(entity.getValue()) + "\n";
      }
    }
    stringToSign += resource;
    try {
      Mac mac = Mac.getInstance("HmacSHA1");
      byte[] keyBytes = secretKey.getBytes("UTF8");
      SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");
      mac.init(signingKey);
      byte[] signBytes = mac.doFinal(stringToSign.getBytes("UTF8"));
      String signature = encodeBase64(signBytes);
      return "AWS" + " " + accessKey + ":" + signature;
    } catch (Exception e) {
      throw new RuntimeException("MAC CALC FAILED.");
    }

  }

  private static String encodeBase64(byte[] data) {
    String base64 = new String(Base64.getEncoder().encodeToString(data));
    if (base64.endsWith("\r\n"))
      base64 = base64.substring(0, base64.length() - 2);
    if (base64.endsWith("\n"))
      base64 = base64.substring(0, base64.length() - 1);

    return base64;
  }
}