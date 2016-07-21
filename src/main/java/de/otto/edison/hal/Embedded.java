package de.otto.edison.hal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;

/**
 * Created by guido on 05.07.16.
 */
@JsonSerialize(using = Embedded.EmbeddedSerializer.class)
@JsonDeserialize(using = Embedded.EmbeddedDeserializer.class)
public class Embedded {

    private final Map<String,List<HalRepresentation>> items;

    Embedded() {
        items =null;}

    private Embedded(final Map<String, List<HalRepresentation>> items) {
        this.items = items;
    }

    public static Embedded emptyEmbedded() {
        return new Embedded(null);
    }

    public static Embedded embedded(final String rel,
                                    final List<HalRepresentation> embeddedRepresentations) {
        return new Embedded(singletonMap(rel, embeddedRepresentations));
    }

    public static EmbeddedItemsBuilder embeddedBuilder() {
        return new EmbeddedItemsBuilder();
    }

    @JsonIgnore
    public List<HalRepresentation> getItemsBy(final String rel) {
        if (items != null) {
            return items.containsKey(rel) ? items.get(rel) : emptyList();
        } else {
            return emptyList();
        }
    }

    @JsonIgnore
    public <E extends HalRepresentation> List<E> getItemsBy(final String rel, final Class<E> asType) {
        if (items != null) {
            List<E> representations = new ArrayList<>();
            items.get(rel).forEach(i -> representations.add(asType.cast(i)));
            return items.containsKey(rel) ? representations : emptyList();
        } else {
            return emptyList();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Embedded embedded = (Embedded) o;

        return this.items != null ? this.items.equals(embedded.items) : embedded.items == null;

    }

    @Override
    public int hashCode() {
        return items != null ? items.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Embedded{" +
                "items=" + items +
                '}';
    }

    public final static class EmbeddedItemsBuilder {
        private final Map<String,List<HalRepresentation>> _embedded = new LinkedHashMap<>();

        public EmbeddedItemsBuilder withEmbedded(final String rel, final List<HalRepresentation> embeddedRepresentations) {
            _embedded.put(rel, embeddedRepresentations);
            return this;
        }

        public Embedded build() {
            return _embedded.isEmpty() ? emptyEmbedded() : new Embedded(_embedded);
        }

        public static EmbeddedItemsBuilder copyOf(final Embedded embedded) {
            final EmbeddedItemsBuilder builder = new EmbeddedItemsBuilder();
            if (embedded != null && embedded.items != null) {
                builder._embedded.putAll(embedded.items);
            }
            return builder;
        }
    }

    static class EmbeddedSerializer extends JsonSerializer<Embedded> {

        @Override
        public void serialize(Embedded value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
            gen.writeObject(value.items);
        }
    }

    static class EmbeddedDeserializer extends JsonDeserializer<Embedded> {

        private static final TypeReference<Map<String, List<HalRepresentation>>> TYPE_REF_LIST_OF_HAL_REPRESENTATIONS = new TypeReference<Map<String, List<HalRepresentation>>>() {};

        @Override
        public Embedded deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            final Map<String,List<HalRepresentation>> items = p.readValueAs(TYPE_REF_LIST_OF_HAL_REPRESENTATIONS);
            return new Embedded(items);
        }
    }
}