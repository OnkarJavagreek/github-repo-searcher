package com.freightfox.githubsearcher.repository;

import com.freightfox.githubsearcher.entity.StoredRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoredRepositoryRepository extends JpaRepository<StoredRepository, Long>, JpaSpecificationExecutor<StoredRepository> {

    Optional<StoredRepository> findByNameAndOwner(String name, String owner);
}
