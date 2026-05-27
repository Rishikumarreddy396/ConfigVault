package com.configvault.repository;

import com.configvault.model.ConfigVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfigVersionRepository extends JpaRepository<ConfigVersion, Long> {
    List<ConfigVersion> findByConfigIdOrderByVersionNumberDesc(Long configId);
}
