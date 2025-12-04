package org.example.roommanager.service;

import org.example.roommanager.dto.ApplicationRequest;
import org.example.roommanager.dto.ApplicationResponse;

import java.util.List;

public interface ApplicationService {

    ApplicationResponse createApplication(Long userId, ApplicationRequest request);

    List<ApplicationResponse> listMyApplications(Long userId);

    List<ApplicationResponse> listPendingApplications();

    void approve(Long appId, Long adminId);

    void reject(Long appId, Long adminId);
}