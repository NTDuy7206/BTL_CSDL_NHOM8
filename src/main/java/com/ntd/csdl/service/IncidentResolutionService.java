package com.ntd.csdl.service;

import com.ntd.csdl.dto.IncidentResolutionDTO;
import com.ntd.csdl.entity.Employee;
import com.ntd.csdl.entity.Incident;
import com.ntd.csdl.entity.IncidentResolution;
import com.ntd.csdl.repo.EmployeeRepository;
import com.ntd.csdl.repo.IncidentRepository;
import com.ntd.csdl.repo.IncidentResolutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncidentResolutionService {

    private final IncidentResolutionRepository resolutionRepository;
    private final IncidentRepository incidentRepository;
    private final EmployeeRepository employeeRepository;

    // CREATE
    public IncidentResolution create(
            IncidentResolutionDTO dto
    ) {

        if (resolutionRepository
                .existsById(dto.getResolutionId())) {

            throw new RuntimeException(
                    "Bản ghi xử lý sự cố đã tồn tại"
            );
        }

        Incident incident = incidentRepository
                .findById(dto.getIncidentId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy sự cố"
                        )
                );

        Employee employee = employeeRepository
                .findById(dto.getEmployeeId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy nhân viên"
                        )
                );

        IncidentResolution resolution =
                new IncidentResolution();

        resolution.setResolutionId(
                dto.getResolutionId()
        );

        resolution.setResolutionDate(
                dto.getResolutionDate()
        );

        resolution.setResult(dto.getResult());

        resolution.setIsCompleted(
                dto.getIsCompleted()
        );

        resolution.setIncident(incident);
        resolution.setEmployee(employee);

        return resolutionRepository.save(
                resolution
        );
    }


    public IncidentResolution update(
            String id,
            IncidentResolutionDTO dto
    ) {

        IncidentResolution existing =
                resolutionRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Không tìm thấy bản ghi xử lý"
                                )
                        );

        Incident incident =
                incidentRepository.findById(
                        dto.getIncidentId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy sự cố"
                        )
                );

        Employee employee =
                employeeRepository.findById(
                        dto.getEmployeeId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy nhân viên"
                        )
                );

        existing.setResolutionDate(
                dto.getResolutionDate()
        );

        existing.setResult(dto.getResult());

        existing.setIsCompleted(
                dto.getIsCompleted()
        );

        existing.setIncident(incident);
        existing.setEmployee(employee);

        return resolutionRepository.save(
                existing
        );
    }

    // READ ALL
    public List<IncidentResolution> getAll() {

        return resolutionRepository.findAll();
    }

    // READ BY ID
    public IncidentResolution getById(String id) {

        return resolutionRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy thông tin xử lý sự cố"
                        ));
    }

    // Lấy danh sách xử lý của một sự cố
    public List<IncidentResolution> getByIncident(
            String incidentId
    ) {

        return resolutionRepository
                .findByIncidentIncidentId(incidentId);
    }

    // Lấy danh sách công việc của một nhân viên
    public List<IncidentResolution> getByEmployee(
            String employeeId
    ) {

        return resolutionRepository
                .findByEmployeeEmployeeId(employeeId);
    }

    // Lấy các xử lý chưa hoàn thành
    public List<IncidentResolution> getUncompleted() {

        return resolutionRepository
                .findUncompletedResolutions();
    }

    // UPDATE
    public IncidentResolution update(
            String id,
            IncidentResolution resolution
    ) {

        IncidentResolution existing = getById(id);

        existing.setResolutionDate(
                resolution.getResolutionDate()
        );

        existing.setResult(
                resolution.getResult()
        );

        existing.setIsCompleted(
                resolution.getIsCompleted()
        );

        return resolutionRepository.save(existing);
    }

    // Đánh dấu đã hoàn thành
    public IncidentResolution complete(String id) {

        IncidentResolution resolution = getById(id);

        resolution.setIsCompleted(true);

        if (resolution.getResolutionDate() == null) {
            resolution.setResolutionDate(LocalDate.now());
        }

        return resolutionRepository.save(resolution);
    }

    // DELETE
    public void delete(String id) {

        if (!resolutionRepository.existsById(id)) {
            throw new RuntimeException(
                    "Thông tin xử lý sự cố không tồn tại"
            );
        }

        resolutionRepository.deleteById(id);
    }
}