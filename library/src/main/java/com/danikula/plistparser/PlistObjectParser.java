package com.danikula.plistparser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import com.google.api.client.util.ObjectParser;

import static com.google.api.client.util.Preconditions.checkNotNull;

public class PlistObjectParser implements ObjectParser {

    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    private PlistXmlToObjectMapper plistConverter;

    public PlistObjectParser() {
        plistConverter = new PlistXmlToObjectMapper();
    }

    public <T> T parseAndClose(Reader reader, Class<T> dataClass) throws IOException {
        return (T) parseAndClose(reader, (Type) dataClass);
    }

    @Override
    public <T> T parseAndClose(InputStream inputStream, Charset charset, Class<T> clazz) throws IOException {
        return (T) parseAndClose(inputStream, charset, (Type) clazz);
    }

    @Override
    public Object parseAndClose(Reader reader, Type type) throws IOException {
        InputStream readerInputStream = new ReaderInputStream(reader);
        return parseAndClose(readerInputStream, DEFAULT_CHARSET, type);
    }

    @Override
    public Object parseAndClose(InputStream inputStream, Charset charset, Type type) throws IOException {
        checkNotNull(charset);
        checkNotNull(inputStream);
        checkNotNull(type);

        try {
            return plistConverter.convertPlistStreamToObject(inputStream, type);
        } catch (PlistParseException e) {
            e.printStackTrace();
            throw new IOException("Error parsing plist resource");
        } finally {
            inputStream.close();
        }
    }
}
