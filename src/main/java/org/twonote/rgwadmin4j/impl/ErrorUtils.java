package org.twonote.rgwadmin4j.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.Response;

import java.lang.reflect.Type;
import java.util.Map;

/** Created by petertc on 3/24/17. */
class ErrorUtils {
  private static final Type mapType = new TypeToken<Map<String, String>>() {}.getType();
  private static final Gson gson = new Gson();

  static RgwAdminException parseError(Response response) {
    try {
      //noinspection unchecked
      return new RgwAdminException(
          response.code(),
          ((Map<String, String>) gson.fromJson(response.body().string(), mapType)).get("Code"));
    } catch (Exception e) {
      return new RgwAdminException(response.code());
    }
  }
}
