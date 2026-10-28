package com.ntd.csdl.controller;
import com.ntd.csdl.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.math.BigDecimal;
@RestController @RequiredArgsConstructor @RequestMapping("/api/queries")
public class QueryController {
 private final RoomService rooms;private final TenantService tenants;private final EmployeeService employees;
 private final ContractService contracts;private final InvoiceService invoices;private final IncidentService incidents;
 private final IncidentResolutionService resolutions;private final CleaningSupportService cleaning;
 private final UtilityReadingService readings;private final ViolationService violations;
 private String value(String value){if(value==null||value.isBlank())throw new IllegalArgumentException("Vui lòng nhập điều kiện tra cứu");return value.trim();}
 @GetMapping("/{module}/{action}")
 public Object query(@PathVariable String module,@PathVariable String action,@RequestParam(required=false) String value){
   return switch(module+"/"+action){
    case "tenants/name" -> tenants.searchByName(value(value));
    case "employees/name" -> employees.searchByName(value(value));
    case "rooms/status" -> rooms.getByStatus(value(value));
    case "rooms/type" -> rooms.getByType(value(value));
    case "rooms/max-price" -> {BigDecimal price;try{price=new BigDecimal(value(value));}catch(NumberFormatException e){throw new IllegalArgumentException("Giá phải là số");}if(price.signum()<0)throw new IllegalArgumentException("Giá không được âm");yield rooms.findByMaxPrice(price);}
    case "contracts/expired" -> contracts.getExpiredContracts();
    case "contracts/tenant" -> contracts.getByTenant(value(value));
    case "contracts/room" -> contracts.getByRoom(value(value));
    case "invoices/contract" -> invoices.getByContract(value(value));
    case "invoices/total" -> java.util.Map.of("label","Tổng giá trị hóa đơn (không phải tiền đã thu)","amount",invoices.getTotalRevenue());
    case "incidents/room" -> incidents.getByRoom(value(value));
    case "incidents/status" -> incidents.getByStatus(value(value));
    case "incident-resolutions/incident" -> resolutions.getByIncident(value(value));
    case "incident-resolutions/employee" -> resolutions.getByEmployee(value(value));
    case "incident-resolutions/uncompleted" -> resolutions.getUncompleted();
    case "cleaning-supports/room" -> cleaning.getByRoom(value(value));
    case "cleaning-supports/employee" -> cleaning.getByEmployee(value(value));
    case "cleaning-supports/uncompleted" -> cleaning.getUncompleted();
    case "utility-readings/room" -> readings.getByRoom(value(value));
    case "violations/room" -> violations.getByRoom(value(value));
    default -> throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Không có chức năng tra cứu này");
   };
 }
}
