package com.voteSmart.backend.repository;

import com.voteSmart.backend.model.PoliticianCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PoliticianCacheRepository extends JpaRepository<PoliticianCache, Long> {
    Optional<PoliticianCache> findByCacheKey(String cacheKey);

}
