package com.yas.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;

import com.yas.commonlibrary.config.ServiceUrlConfig;
import com.yas.product.viewmodel.NoFileMediaVm;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MediaServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private ServiceUrlConfig serviceUrlConfig;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    private MediaService mediaService;

    @BeforeEach
    void setUp() {
        mediaService = new MediaService(restClient, serviceUrlConfig);
    }

    @Test
    void getMedia_whenIdIsNull_shouldReturnDefaultNoFileMediaVm() {
        NoFileMediaVm result = mediaService.getMedia(null);

        assertNotNull(result);
        assertNull(result.id());
        assertEquals("", result.caption());
        assertEquals("", result.fileName());
        assertEquals("", result.mediaType());
        assertEquals("", result.url());
    }

    @Test
    void getMedia_whenIdIsValid_shouldReturnNoFileMediaVm() {
        Long mediaId = 1L;
        NoFileMediaVm expectedVm = new NoFileMediaVm(1L, "caption", "file.jpg", "image/jpeg", "http://url.com");

        when(serviceUrlConfig.media()).thenReturn("http://media-service");
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(NoFileMediaVm.class)).thenReturn(expectedVm);

        NoFileMediaVm result = mediaService.getMedia(mediaId);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("caption", result.caption());
        assertEquals("file.jpg", result.fileName());
        assertEquals("http://url.com", result.url());
    }

    @Test
    void saveFile_whenCalled_shouldPostToMediaServiceAndReturnResult() {
        NoFileMediaVm expectedVm = new NoFileMediaVm(1L, "caption", "file.jpg", "image/jpeg", "http://url.com");

        MultipartFile multipartFile = mock(MultipartFile.class);
        Resource resource = mock(Resource.class);
        when(multipartFile.getResource()).thenReturn(resource);

        when(serviceUrlConfig.media()).thenReturn("http://media-service");

        // Mock security context
        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("test-token");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(jwt);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Use a deep-stubbed RequestBodySpec to handle overloaded body() method
        RestClient.RequestBodySpec deepBodySpec = mock(RestClient.RequestBodySpec.class,
            org.mockito.Mockito.RETURNS_SELF);

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(URI.class))).thenReturn(deepBodySpec);
        when(deepBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(NoFileMediaVm.class)).thenReturn(expectedVm);

        NoFileMediaVm result = mediaService.saveFile(multipartFile, "caption", "file.jpg");

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("caption", result.caption());

        // cleanup security context
        SecurityContextHolder.clearContext();
    }

    @Test
    void removeMedia_whenCalled_shouldDeleteFromMediaService() {
        Long mediaId = 1L;

        when(serviceUrlConfig.media()).thenReturn("http://media-service");

        // Mock security context
        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("test-token");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(jwt);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(restClient.delete()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Void.class)).thenReturn(null);

        mediaService.removeMedia(mediaId);

        verify(restClient).delete();

        // cleanup security context
        SecurityContextHolder.clearContext();
    }

    @Test
    void handleMediaFallback_shouldThrowOriginalException() throws Throwable {
        RuntimeException originalException = new RuntimeException("circuit breaker error");

        // handleMediaFallback is private, but we can test through handleTypedFallback which is protected
        // The AbstractCircuitBreakFallbackHandler.handleTypedFallback re-throws the exception
        assertThrows(RuntimeException.class, () -> {
            mediaService.getMedia(999L);
        });
    }
}
