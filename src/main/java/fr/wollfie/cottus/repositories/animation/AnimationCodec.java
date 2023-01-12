package fr.wollfie.cottus.repositories.animation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClientSettings;
import fr.wollfie.cottus.dto.animation.AnimationRepositoryEntry;
import fr.wollfie.cottus.models.animation.pathing.AnimationPrimitive;
import io.quarkus.logging.Log;
import org.bson.*;
import org.bson.codecs.Codec;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class AnimationCodec implements CollectibleCodec<AnimationRepositoryEntryImpl> {

    @Inject ObjectMapper defaultObjectMapper;

    private Codec<Document> documentCodec;

    @PostConstruct
    void init() {
        this.documentCodec = MongoClientSettings.getDefaultCodecRegistry().get(Document.class);
    }

    @Override
    public void encode(BsonWriter writer, AnimationRepositoryEntryImpl animation, EncoderContext encoderContext) {
        Document doc = new Document();
        doc.put("name", animation.getName());
        try {
            String value = defaultObjectMapper.writeValueAsString(animation.getAnimation());
            doc.put("animation", value);
        } catch (JsonProcessingException e) { throw new RuntimeException(e); }
        documentCodec.encode(writer, doc, encoderContext);
    }

    @Override
    public Class<AnimationRepositoryEntryImpl> getEncoderClass() {
        return AnimationRepositoryEntryImpl.class;
    }

    @Override
    public AnimationRepositoryEntryImpl generateIdIfAbsentFromDocument(AnimationRepositoryEntryImpl document) {
        if (!documentHasId(document)) {
            document.setId(UUID.randomUUID().toString());
        }
        return document;
    }

    @Override
    public boolean documentHasId(AnimationRepositoryEntryImpl document) {
        return document.getId() != null;
    }

    @Override
    public BsonValue getDocumentId(AnimationRepositoryEntryImpl document) {
        return new BsonString(document.getId());
    }

    @Override
    public AnimationRepositoryEntryImpl decode(BsonReader reader, DecoderContext decoderContext) {
        Document document = documentCodec.decode(reader, decoderContext);

        AnimationRepositoryEntryImpl animation;
        
        try {
            AnimationPrimitive animationPrimitive = this.defaultObjectMapper.readValue(document.getString("animation"), AnimationPrimitive.class);
            
            animation = new AnimationRepositoryEntryImpl(
                    document.getString("name"),
                    animationPrimitive
            );
            if (document.getString("id") != null) {
                animation.setId(document.getString("id"));
            }
        } catch (JsonProcessingException e) { throw new RuntimeException(e); }

        return animation;
    }
}
