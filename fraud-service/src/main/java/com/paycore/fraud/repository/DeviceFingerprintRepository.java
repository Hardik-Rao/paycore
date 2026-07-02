package com.paycore.fraud.repository;

import com.paycore.fraud.domain.DeviceFingerprint;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface DeviceFingerprintRepository extends JpaRepository<DeviceFingerprint, UUID> {
    Optional<DeviceFingerprint> findByAccountIdAndDeviceHash(String accountId, String deviceHash);
}
