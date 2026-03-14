package com.yas.tax.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.yas.tax.config.ServiceUrlConfig;
import com.yas.tax.viewmodel.location.StateOrProvinceAndCountryGetNameVm;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.RestClient;

public class LocationServiceTest {

    private RestClient restClient;
    private ServiceUrlConfig serviceUrlConfig;
    private LocationService locationService;

    @BeforeEach
    void setUp() {
        restClient = mock(RestClient.class);
        serviceUrlConfig = mock(ServiceUrlConfig.class);
        locationService = new LocationService(restClient, serviceUrlConfig);

        // Mock Security Context để lấy JWT
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        Jwt jwt = mock(Jwt.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getTokenValue()).thenReturn("mock-token");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getStateOrProvinceAndCountryNames_shouldReturnList() {
        when(serviceUrlConfig.location()).thenReturn("http://location-service");

        // Mock chuỗi RestClient
        RestClient.RequestHeadersUriSpec uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec headersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(any(java.net.URI.class))).thenReturn(headersSpec);
        when(headersSpec.headers(any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);

        List<StateOrProvinceAndCountryGetNameVm> mockResponse = List.of(mock(StateOrProvinceAndCountryGetNameVm.class));
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(mockResponse);

        List<StateOrProvinceAndCountryGetNameVm> result = locationService
                .getStateOrProvinceAndCountryNames(List.of(1L));

        assertThat(result).hasSize(1);
    }

    @Test
    void handleLocationNameListFallback_shouldThrowException() throws Throwable {
        Throwable mockThrowable = new RuntimeException("Service Down");
        assertThatThrownBy(() -> locationService.handleLocationNameListFallback(mockThrowable))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Service Down");
    }
}