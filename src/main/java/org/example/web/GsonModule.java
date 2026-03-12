package org.example.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.jooby.Body;
import io.jooby.Context;
import io.jooby.Extension;
import io.jooby.Jooby;
import io.jooby.MediaType;
import io.jooby.MessageDecoder;
import io.jooby.MessageEncoder;
import io.jooby.ServiceRegistry;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nonnull;

public class GsonModule implements Extension, MessageDecoder, MessageEncoder {
    private Gson gson;

    public GsonModule(@Nonnull Gson gson) {
        this.gson = gson;
    }

    public GsonModule() {
        this((new GsonBuilder()).create());
    }

    public void install(@Nonnull Jooby application) throws Exception {
        application.decoder(MediaType.json, this);
        application.encoder(MediaType.json, this);
        ServiceRegistry services = application.getServices();
        services.put(Gson.class, this.gson);
    }

    @Nonnull
    public Object decode(@Nonnull Context ctx, @Nonnull Type type) throws Exception {
        Body body = ctx.body();
        if (body.isInMemory()) {
            return this.gson.fromJson(new InputStreamReader(new ByteArrayInputStream(body.bytes()), StandardCharsets.UTF_8), type);
        } else {
            InputStream stream = body.stream();
            Throwable var5 = null;

            Object var6;
            try {
                var6 = this.gson.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), type);
            } catch (Throwable var15) {
                var5 = var15;
                throw var15;
            } finally {
                if (stream != null) {
                    if (var5 != null) {
                        try {
                            stream.close();
                        } catch (Throwable var14) {
                            var5.addSuppressed(var14);
                        }
                    } else {
                        stream.close();
                    }
                }

            }

            return var6;
        }
    }

    @Nonnull
    public byte[] encode(@Nonnull Context ctx, @Nonnull Object value) {
        ctx.setDefaultResponseType(MediaType.json);
        return this.gson.toJson(value).getBytes(StandardCharsets.UTF_8);
    }
}

