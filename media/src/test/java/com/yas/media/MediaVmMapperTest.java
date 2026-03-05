package com.yas.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.yas.media.mapper.MediaVmMapper;
import com.yas.media.model.Media;
import com.yas.media.viewmodel.MediaVm;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class MediaVmMapperTest {

    private final MediaVmMapper mapper = Mappers.getMapper(MediaVmMapper.class);

    @Test
    void toVm_whenValidMedia_thenReturnMediaVm() {

        Media media = new Media();
        media.setId(1L);
        media.setCaption("Test Caption");
        media.setFileName("test.png");
        media.setMediaType("image/png");

        MediaVm result = mapper.toVm(media);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Caption", result.getCaption());
        assertEquals("test.png", result.getFileName());
        assertEquals("image/png", result.getMediaType());
    }

    @Test
    void toVm_whenCaptionNull_thenReturnVmWithNullCaption() {

        Media media = new Media();
        media.setId(2L);
        media.setCaption(null);
        media.setFileName("file.png");
        media.setMediaType("image/png");

        MediaVm result = mapper.toVm(media);

        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertNull(result.getCaption());
    }

    @Test
    void toVm_whenMediaNull_thenReturnNull() {

        MediaVm result = mapper.toVm(null);

        assertNull(result);
    }
}