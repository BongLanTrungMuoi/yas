package com.yas.customer.service;

import com.yas.commonlibrary.exception.AccessDeniedException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.customer.model.UserAddress;
import com.yas.customer.repository.UserAddressRepository;
import com.yas.customer.viewmodel.address.AddressDetailVm;
import com.yas.customer.viewmodel.address.AddressPostVm;
import com.yas.customer.viewmodel.address.AddressVm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAddressServiceTest {

    @Mock
    private UserAddressRepository userAddressRepository;

    @Mock
    private LocationService locationService;

    @InjectMocks
    private UserAddressService userAddressService;

    @BeforeEach
    void setup() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user1", null)
        );
    }

    @Test
    void getAddressDefault_shouldThrowNotFound_whenNoDefaultAddress() {

        when(userAddressRepository.findByUserIdAndIsActiveTrue(anyString()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            userAddressService.getAddressDefault();
        });
    }

    @Test
    void deleteAddress_shouldThrowNotFound_whenAddressNotExist() {

        when(userAddressRepository.findOneByUserIdAndAddressId(anyString(), eq(1L)))
                .thenReturn(null);

        assertThrows(NotFoundException.class, () -> {
            userAddressService.deleteAddress(1L);
        });
    }

    @Test
    void deleteAddress_shouldDeleteSuccessfully() {

        UserAddress address = new UserAddress();
        address.setAddressId(1L);

        when(userAddressRepository.findOneByUserIdAndAddressId(anyString(), eq(1L)))
                .thenReturn(address);

        userAddressService.deleteAddress(1L);

        verify(userAddressRepository).delete(address);
    }

    @Test
    void chooseDefaultAddress_shouldUpdateActiveAddress() {

        UserAddress a1 = new UserAddress();
        a1.setAddressId(1L);

        UserAddress a2 = new UserAddress();
        a2.setAddressId(2L);

        List<UserAddress> list = List.of(a1, a2);

        when(userAddressRepository.findAllByUserId(anyString()))
                .thenReturn(list);

        userAddressService.chooseDefaultAddress(2L);

        assertFalse(a1.getIsActive());
        assertTrue(a2.getIsActive());

        verify(userAddressRepository).saveAll(list);
    }

    @Test
    void createAddress_shouldCreateFirstAddressAsActive() {

        AddressPostVm postVm = mock(AddressPostVm.class);

        when(userAddressRepository.findAllByUserId(anyString()))
                .thenReturn(List.of());

        AddressVm addressVm = mock(AddressVm.class);
        when(addressVm.id()).thenReturn(10L);

        when(locationService.createAddress(postVm)).thenReturn(addressVm);

        UserAddress saved = UserAddress.builder()
                .userId("user1")
                .addressId(10L)
                .isActive(true)
                .build();

        when(userAddressRepository.save(any())).thenReturn(saved);

        assertNotNull(userAddressService.createAddress(postVm));

        verify(userAddressRepository).save(any());
    }
}