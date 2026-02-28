/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.genai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.Arrays;
import java.util.Base64;
import java.util.function.BiFunction;

public class ReplaySanitizer {

  private static final Logger log = Logger.getLogger(ReplaySanitizer.class.getName());

  public static void sanitizeMapByPath(
      Object data, String path, Sanitizer sanitizer, boolean debug) {
    if (!(data instanceof Map)) {
      if (debug) {
        log.warning("data is not map type: " + data + ", path: " + path);
      }
      return;
    }

    @SuppressWarnings("unchecked") // Safe cast since we checked the type above
    Map<String, Object> m = (Map<String, Object>) data;

    String[] keys = path.split("\\.");
    String key = keys[0];

    // Handle path not exists.
    if (key.startsWith("[]")) {
      if (!m.containsKey(key.substring(2))) {
        if (debug) {
          log.warning("path doesn't exist: " + data + ", path: " + path);
        }
        return;
      }
    } else if (!m.containsKey(key)) {
      if (debug) {
        log.warning("path doesn't exist: " + data + ", path: " + path);
      }
      return;
    }

    // We are at the last component of the path.
    if (key.startsWith("[]") && keys.length == 1) {
      List<Object> items = new ArrayList<>();
      Object v = m.get(key.substring(2));
      if (!(v instanceof List)) {
        if (debug) {
          log.warning("data is not list type as the path denoted: " + data + ", path: " + path);
        }
        return;
      }

      @SuppressWarnings("unchecked") // Safe cast since we checked the type above
      List<Object> listV = (List<Object>) v;
      for (Object item : listV) {
        items.add(sanitizer.sanitize(item, key));
      }
      m.put(key.substring(2), items);
      return;
    } else if (keys.length == 1) {
      m.put(key, sanitizer.sanitize(m.get(key), path));
      return;
    }

    if (key.startsWith("[]")) {
      Object v = m.get(key.substring(2));
      if (!(v instanceof List)) {
        if (debug) {
          log.warning("data is not list type as the path denoted: " + data + ", path: " + path);
        }
        return;
      }
      @SuppressWarnings("unchecked") // Safe cast since we checked the type above
      List<Object> listV = (List<Object>) v;

      for (int i = 0; i < listV.size(); i++) {
        Object element = listV.get(i);
        sanitizeMapByPath(
            element, String.join(".", Arrays.copyOfRange(keys, 1, keys.length)), sanitizer, debug);
      }
    } else {
      sanitizeMapByPath(
          m.get(key), String.join(".", Arrays.copyOfRange(keys, 1, keys.length)), sanitizer, debug);
    }
  }

  public interface Sanitizer {
    Object sanitize(Object data, String path);
  }
}
