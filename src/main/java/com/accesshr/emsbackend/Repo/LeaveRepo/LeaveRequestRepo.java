package com.accesshr.emsbackend.Repo.LeaveRepo;

import com.accesshr.emsbackend.Entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveRequestRepo extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByLeaveStatus(LeaveRequest.LeaveStatus status);

    List<LeaveRequest> findByManagerId(String managerId);

    List<LeaveRequest> findByEmployeeId(String employeeId);

    List<LeaveRequest> findByManagerIdAndLeaveStatus(String managerId, LeaveRequest.LeaveStatus leaveStatus);

    List<LeaveRequest> findByEmployeeIdAndLeaveStatus(String employeeId, LeaveRequest.LeaveStatus leaveStatus);

    Optional<LeaveRequest> findByEmployeeIdAndLeaveStartDateAndLeaveEndDate(String employeeId, LocalDate leaveStartDate, LocalDate leaveEndDate);

    long countByEmployeeIdAndLeaveType(String employeeId, LeaveRequest.LeaveType leaveType);

    Optional<LeaveRequest> findByEmployeeIdAndLeaveType(String employeeId, LeaveRequest.LeaveType leaveType);

//    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employeeId = :employeeId " +
//            "AND lr.leaveStartDate <= :leaveEndDate AND lr.leaveEndDate >= :leaveStartDate")
//    List<LeaveRequest> findOverlappingLeaves(@Param("employeeId") String employeeId,
//                                             @Param("leaveStartDate") LocalDate leaveStartDate,
//                                             @Param("leaveEndDate") LocalDate leaveEndDate);

    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.employeeId = :employeeId " +
            "AND EXISTS (SELECT 1 FROM LeaveRequest l WHERE l.employeeId = :employeeId " +
            "AND (l.leaveStartDate BETWEEN :leaveStartDate AND :leaveEndDate " +
            "OR l.leaveEndDate BETWEEN :leaveStartDate AND :leaveEndDate " +
            "OR :leaveStartDate BETWEEN l.leaveStartDate AND l.leaveEndDate " +
            "OR :leaveEndDate BETWEEN l.leaveStartDate AND l.leaveEndDate))")
    long countOverlappingLeaves(
            @Param("employeeId") String employeeId,
            @Param("leaveStartDate") LocalDate leaveStartDate,
            @Param("leaveEndDate") LocalDate leaveEndDate);


    @Query("SELECT SUM(lr.duration) " +
            "FROM LeaveRequest lr " +
            "WHERE lr.employeeId = :employeeId AND lr.leaveType = :leaveType " +
            "AND lr.leaveStatus != 'REJECTED'")
    Optional<Integer> getTotalLeaveDaysByEmployeeIdAndLeaveType(@Param("employeeId") String employeeId,
                                                                @Param("leaveType") LeaveRequest.LeaveType leaveType);

    @Modifying
    @Transactional
    @Query("DELETE FROM LeaveRequest lr WHERE YEAR(lr.leaveStartDate) < :year")
    void resetLeaveBalancesForAllEmployees(@Param("year") int year);
}
