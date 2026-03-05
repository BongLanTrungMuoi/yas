// package com.yas.media;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertInstanceOf;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import static org.junit.jupiter.api.Assertions.assertNull;
// import static org.junit.jupiter.api.Assertions.assertTrue;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.doNothing;
// import static org.mockito.Mockito.mock;
// import static org.mockito.Mockito.times;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// import com.yas.media.controller.MediaController;
// import com.yas.media.model.Media;
// import com.yas.media.model.dto.MediaDto;
// import com.yas.media.service.MediaService;
// import com.yas.media.viewmodel.MediaPostVm;
// import com.yas.media.viewmodel.MediaVm;
// import com.yas.media.viewmodel.NoFileMediaVm;

// import java.io.ByteArrayInputStream;
// import java.util.Collections;
// import java.util.List;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;

// import org.springframework.http.HttpHeaders;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;

// class MediaControllerTest {

//     @Mock
//     private MediaService mediaService;

//     @InjectMocks
//     private MediaController mediaController;

//     private Media media;
//     private MediaVm mediaVm;
//     private MediaPostVm mediaPostVm;

//     @BeforeEach
//     void setUp() {

//         MockitoAnnotations.openMocks(this);

//         media = new Media();
//         media.setId(1L);
//         media.setCaption("Test Caption");
//         media.setFileName("test-file.png");
//         media.setMediaType("image/png");

//         mediaVm = new MediaVm(
//                 1L,
//                 "Test Caption",
//                 "test-file.png",
//                 "image/png",
//                 "http://localhost/medias/1/file/test-file.png"
//         );

//         // FIX: MediaPostVm không có constructor rỗng
//         mediaPostVm = mock(MediaPostVm.class);
//     }

//     // ==================== POST /medias ====================

//     @Test
//     void create_whenValidRequest_thenReturnOkWithNoFileMediaVm() {

//         when(mediaService.saveMedia(any(MediaPostVm.class))).thenReturn(media);

//         ResponseEntity<Object> response = mediaController.create(mediaPostVm);

//         assertEquals(HttpStatus.OK, response.getStatusCode());
//         assertNotNull(response.getBody());
//         assertInstanceOf(NoFileMediaVm.class, response.getBody());

//         NoFileMediaVm body = (NoFileMediaVm) response.getBody();

//         assertEquals(1L, body.id());
//         assertEquals("Test Caption", body.caption());
//         assertEquals("test-file.png", body.fileName());
//         assertEquals("image/png", body.mediaType());

//         verify(mediaService, times(1)).saveMedia(any(MediaPostVm.class));
//     }

//     @Test
//     void create_whenCaptionIsNull_thenReturnOkWithNullCaption() {

//         media.setCaption(null);

//         when(mediaService.saveMedia(any(MediaPostVm.class))).thenReturn(media);

//         ResponseEntity<Object> response = mediaController.create(mediaPostVm);

//         assertEquals(HttpStatus.OK, response.getStatusCode());

//         NoFileMediaVm body = (NoFileMediaVm) response.getBody();

//         assertNotNull(body);
//         assertNull(body.caption());
//     }

//     // ==================== DELETE ====================

//     @Test
//     void delete_whenValidId_thenReturnNoContent() {

//         doNothing().when(mediaService).removeMedia(1L);

//         ResponseEntity<Void> response = mediaController.delete(1L);

//         assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
//         assertNull(response.getBody());

//         verify(mediaService, times(1)).removeMedia(1L);
//     }

//     @Test
//     void delete_whenAnyId_thenDelegateToService() {

//         Long id = 99L;

//         doNothing().when(mediaService).removeMedia(id);

//         mediaController.delete(id);

//         verify(mediaService, times(1)).removeMedia(id);
//     }

//     // ==================== GET /medias/{id} ====================

//     @Test
//     void get_whenValidId_thenReturnOkWithMediaVm() {

//         when(mediaService.getMediaById(1L)).thenReturn(mediaVm);

//         ResponseEntity<MediaVm> response = mediaController.get(1L);

//         assertEquals(HttpStatus.OK, response.getStatusCode());
//         assertEquals(mediaVm, response.getBody());

//         verify(mediaService, times(1)).getMediaById(1L);
//     }

//     // @Test
//     // void get_whenMediaNotFound_thenReturnOkWithNullBody() {

//     //     when(mediaService.getMediaById(999L)).thenReturn(null);

//     //     ResponseEntity<MediaVm> response = mediaController.get(999L);

//     //     assertEquals(HttpStatus.OK, response.getStatusCode());
//     //     assertNull(response.getBody());
//     // }

//     // ==================== GET /medias?ids ====================

//     @Test
//     void getByIds_whenValidIds_thenReturnOkWithList() {

//         List<Long> ids = List.of(1L, 2L);
//         List<MediaVm> mediaVms = List.of(mediaVm);

//         when(mediaService.getMediaByIds(ids)).thenReturn(mediaVms);

//         ResponseEntity<List<MediaVm>> response = mediaController.getByIds(ids);

//         assertEquals(HttpStatus.OK, response.getStatusCode());
//         assertNotNull(response.getBody());
//         assertEquals(1, response.getBody().size());
//         assertEquals(mediaVm, response.getBody().get(0));
//     }

//     // @Test
//     // void getByIds_whenNoMatchingIds_thenReturnEmptyList() {

//     //     List<Long> ids = List.of(999L);

//     //     when(mediaService.getMediaByIds(ids)).thenReturn(Collections.emptyList());

//     //     ResponseEntity<List<MediaVm>> response = mediaController.getByIds(ids);

//     //     assertEquals(HttpStatus.OK, response.getStatusCode());
//     //     assertNotNull(response.getBody());
//     //     assertTrue(response.getBody().isEmpty());
//     // }
// }

package com.yas.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.media.controller.MediaController;
import com.yas.media.model.Media;
import com.yas.media.model.dto.MediaDto;
import com.yas.media.service.MediaService;
import com.yas.media.viewmodel.MediaPostVm;
import com.yas.media.viewmodel.MediaVm;
import com.yas.media.viewmodel.NoFileMediaVm;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

class MediaControllerTest {

    @Mock
    private MediaService mediaService;

    @InjectMocks
    private MediaController mediaController;

    private Media media;
    private MediaVm mediaVm;
    private MediaPostVm mediaPostVm;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        media = new Media();
        media.setId(1L);
        media.setCaption("Test Caption");
        media.setFileName("test-file.png");
        media.setMediaType("image/png");

        mediaVm = new MediaVm(
                1L,
                "Test Caption",
                "test-file.png",
                "image/png",
                "http://localhost/medias/1/file/test-file.png"
        );

        mediaPostVm = mock(MediaPostVm.class);
    }

    // ==================== CREATE ====================

    @Test
    void create_whenValidRequest_thenReturnOkWithNoFileMediaVm() {

        when(mediaService.saveMedia(any(MediaPostVm.class))).thenReturn(media);

        ResponseEntity<Object> response = mediaController.create(mediaPostVm);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(NoFileMediaVm.class, response.getBody());

        NoFileMediaVm body = (NoFileMediaVm) response.getBody();

        assertEquals(1L, body.id());
        assertEquals("Test Caption", body.caption());
        assertEquals("test-file.png", body.fileName());
        assertEquals("image/png", body.mediaType());

        verify(mediaService, times(1)).saveMedia(any(MediaPostVm.class));
    }

    @Test
    void create_whenCaptionIsNull_thenReturnOkWithNullCaption() {

        media.setCaption(null);

        when(mediaService.saveMedia(any(MediaPostVm.class))).thenReturn(media);

        ResponseEntity<Object> response = mediaController.create(mediaPostVm);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        NoFileMediaVm body = (NoFileMediaVm) response.getBody();

        assertNotNull(body);
        assertNull(body.caption());
    }

    // ==================== DELETE ====================

    @Test
    void delete_whenValidId_thenReturnNoContent() {

        doNothing().when(mediaService).removeMedia(1L);

        ResponseEntity<Void> response = mediaController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(mediaService, times(1)).removeMedia(1L);
    }

    @Test
    void delete_whenAnyId_thenDelegateToService() {

        Long id = 99L;

        doNothing().when(mediaService).removeMedia(id);

        mediaController.delete(id);

        verify(mediaService, times(1)).removeMedia(id);
    }

    // ==================== GET BY ID ====================

    @Test
    void get_whenValidId_thenReturnOkWithMediaVm() {

        when(mediaService.getMediaById(1L)).thenReturn(mediaVm);

        ResponseEntity<MediaVm> response = mediaController.get(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mediaVm, response.getBody());

        verify(mediaService, times(1)).getMediaById(1L);
    }

    @Test
    void get_whenNotFound_thenReturn404() {

        when(mediaService.getMediaById(999L)).thenReturn(null);

        ResponseEntity<MediaVm> response = mediaController.get(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ==================== GET BY IDS ====================

    @Test
    void getByIds_whenValidIds_thenReturnOkWithList() {

        List<Long> ids = List.of(1L, 2L);
        List<MediaVm> mediaVms = List.of(mediaVm);

        when(mediaService.getMediaByIds(ids)).thenReturn(mediaVms);

        ResponseEntity<List<MediaVm>> response = mediaController.getByIds(ids);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(mediaVm, response.getBody().get(0));
    }

    // @Test
    // void getByIds_whenEmpty_thenReturnEmptyList() {

    //     List<Long> ids = List.of(999L);

    //     when(mediaService.getMediaByIds(ids)).thenReturn(Collections.emptyList());

    //     ResponseEntity<List<MediaVm>> response = mediaController.getByIds(ids);

    //     assertEquals(HttpStatus.OK, response.getStatusCode());

    //     if (response.getBody() != null) {
    //         assertTrue(response.getBody().isEmpty());
    //     }
    // }

    // ==================== GET FILE ====================

    // @Test
    // void getFile_whenValidRequest_thenReturnFileResponse() {

    //     MediaDto mediaDto = mock(MediaDto.class);

    //     InputStream stream = new ByteArrayInputStream("test-file".getBytes());

    //     when(mediaService.getFile(1L, "test-file.png")).thenReturn(mediaDto);

    //     ResponseEntity<?> response = mediaController.getFile(1L, "test-file.png");

    //     assertEquals(HttpStatus.OK, response.getStatusCode());

    //     assertEquals(
    //             "attachment; filename=\"test-file.png\"",
    //             response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)
    //     );

    //     verify(mediaService, times(1)).getFile(1L, "test-file.png");
    // }

}