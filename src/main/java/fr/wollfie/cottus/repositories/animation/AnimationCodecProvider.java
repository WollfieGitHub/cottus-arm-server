package fr.wollfie.cottus.repositories.animation;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AnimationCodecProvider implements CodecProvider {
    
    @Inject AnimationCodec animationCodec;
    
    @Override
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
        if (clazz.equals(AnimationRepositoryEntryImpl.class)) {
            return (Codec<T>) animationCodec;
        }
        return null;
    }

}