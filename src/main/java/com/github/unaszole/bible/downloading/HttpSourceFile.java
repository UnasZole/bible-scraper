package com.github.unaszole.bible.downloading;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * A remote source file fetched via HTTP.
 */
public class HttpSourceFile implements SourceFile {
    public static class Builder implements SourceFile.Builder {
        public static URL toUrl(String str) {
            try {
                // Try to fix the URL almost as a browser would do, so that users can input URLs like visible
                // in their browser, even if not properly encoded.
                // Taken from https://stackoverflow.com/a/30640843
                URL rawUrl = new URL(str);
                return new URL(
                        new URI(rawUrl.getProtocol(), rawUrl.getUserInfo(), rawUrl.getHost(), rawUrl.getPort(),
                                rawUrl.getPath(), rawUrl.getQuery(), rawUrl.getRef()
                        ).toString().replace("%25", "%")
                );
            } catch (URISyntaxException | MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Optional<SourceFile> buildFrom(Function<String, Optional<String>> propertySource) {
            Optional<String> url = propertySource.apply("Url");
            if(!url.isPresent()) {
                return Optional.empty();
            }

            String method = propertySource.apply("Method").orElse("GET");
            String body = propertySource.apply("Body").orElse(null);

            return Optional.of(new HttpSourceFile(toUrl(url.get()), method, body));
        }
    }

    private static MessageDigest getMessageDigest() {
        try {
            return MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static final MessageDigest HASHER = getMessageDigest();

    private final URL url;
    private final Map<String, String> headers;
    private final String method;
    private final String body;

    public HttpSourceFile(URL url, Map<String, String> headers, String method, String body) {
        assert Objects.equals(url.getProtocol(), "http") || Objects.equals(url.getProtocol(), "https");
        this.url = url;
        this.headers = headers;
        this.method = method;
        this.body = body;
    }

    public HttpSourceFile(URL url, String method, String body) {
        this(url, null, method, body);
    }

    public HttpSourceFile(URL url) {
        this(url, "GET", null);
    }

    @Override
    public String getHash() {
        String hashSource = url.toString();
        if (!Objects.equals(method, "GET")) {
            hashSource += "%%M" + method;
        }
        if (headers != null) {
            hashSource += "%%H" + headers;
        }
        if (body != null) {
            hashSource += "%%B" + body;
        }

        Formatter formatter = new Formatter();
        for (byte b : HASHER.digest(hashSource.getBytes())) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    @Override
    public URI getBaseUri() {
        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStream openStream() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setDoInput(true);
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }
        }
        if (body != null) {
            connection.setDoOutput(true);
            OutputStream out = connection.getOutputStream();
            out.write(body.getBytes(StandardCharsets.UTF_8));
            out.close();
        }

        return connection.getInputStream();
    }

    @Override
    public String toString() {
        return method + " " + url
                + (headers != null ? " [" + headers + "]" : "")
                + (body != null ? " (" + body + ")" : "");
    }
}
