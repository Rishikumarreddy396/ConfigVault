package com.configvault.repository;

import com.configvault.model.ConfigVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfigVersionRepository extends JpaRepository<ConfigVersion, Long> {
    List<ConfigVersion> findByConfigIdOrderByVersionNumberDesc(Long configId);
    Optional<ConfigVersion> findByConfigIdAndVersionNumber(Long configId, Integer versionNumber);
}
