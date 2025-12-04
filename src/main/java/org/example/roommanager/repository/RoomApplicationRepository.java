package org.example.roommanager.repository;

import org.example.roommanager.entity.RoomApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomApplicationRepository extends JpaRepository<RoomApplication, Long> {

    List<RoomApplication> findByUserIdOrderByCreateTimeDesc(Long userId);

    List<RoomApplication> findByStatusOrderByCreateTimeAsc(String status);
}