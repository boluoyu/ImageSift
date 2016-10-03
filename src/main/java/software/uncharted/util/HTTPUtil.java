/*
 * Copyright 2016 Uncharted Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package software.uncharted.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Base64;

/**
 * Created by cdickson on 01/10/16.
 */
public class HTTPUtil {
    public static String get(String url) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

    public static JsonNode postJSON(String url, JsonNode body) throws IOException {
        String response = post(url,body);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(response);
    }

    public static String post(String url, JsonNode body) {
        return post(url,body.toString());
    }

    public static String post(String urlToRead, String postData) {
        URL url;
        HttpURLConnection conn;
        try {
            url = new URL(urlToRead);
            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type","application/json");


            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(postData);
            wr.flush();
            wr.close();

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];
            InputStream is = conn.getInputStream();
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            return buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to read URL: " + urlToRead + " <" + e.getMessage() + ">");
        }
        return null;
    }

    public static JsonNode getJSON(String url) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String jsonStr = get(url);
        return mapper.readTree(jsonStr);
    }
}
