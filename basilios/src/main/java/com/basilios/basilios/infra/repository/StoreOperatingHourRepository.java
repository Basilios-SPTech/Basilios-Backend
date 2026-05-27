package com.basilios.basilios.infra.repository;

import com.basilios.basilios.core.model.StoreOperatingHour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StoreOperatingHourRepository extends JpaRepository<StoreOperatingHour, Long> {

    List<StoreOperatingHour> findByStoreId(Long storeId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from StoreOperatingHour soh where soh.store.id = :storeId")
    int deleteAllByStoreIdInBulk(@Param("storeId") Long storeId);
}
