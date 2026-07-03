package com.apimonitor.repository;

import com.apimonitor.model.ApiLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ApiLogRepository extends JpaRepository<ApiLog, Long>, JpaSpecificationExecutor<ApiLog> {

    /**
     * Fetch today's logs: DATE(request_timestamp) = CURRENT_DATE
     */
    @Query(value = "SELECT * FROM api_logs WHERE DATE(request_timestamp) = CURDATE() " +
            "ORDER BY request_timestamp DESC", nativeQuery = true)
    List<ApiLog> findTodayLogs();

    /**
     * Fetch logs for a specific date: DATE(request_timestamp) = :date
     */
    @Query(value = "SELECT * FROM api_logs WHERE DATE(request_timestamp) = :date " +
            "ORDER BY request_timestamp DESC", nativeQuery = true)
    List<ApiLog> findByRequestDate(@Param("date") LocalDate date);

}
