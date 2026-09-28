package com.ntd.csdl.service;

import com.ntd.csdl.entity.Employee;
import com.ntd.csdl.repo.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public Employee create(Employee employee) {

        if (employee.getEmployeeId() == null ||
                employee.getEmployeeId().isBlank()) {

            throw new RuntimeException(
                    "Employee ID không được để trống"
            );
        }

        if (employeeRepository.existsById(
                employee.getEmployeeId())) {

            throw new RuntimeException(
                    "Nhân viên đã tồn tại"
            );
        }

        return employeeRepository.save(employee);
    }

    public List<Employee> getAll() {
        return employeeRepository.findAll();
    }

    public Employee getById(String id) {

        return employeeRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy nhân viên"
                        ));
    }

    public List<Employee> searchByName(String name) {

        return employeeRepository
                .findByFullNameContainingIgnoreCase(name);
    }

    public void delete(String id) {

        if (!employeeRepository.existsById(id)) {
            throw new RuntimeException(
                    "Nhân viên không tồn tại"
            );
        }

        employeeRepository.deleteById(id);
    }
}