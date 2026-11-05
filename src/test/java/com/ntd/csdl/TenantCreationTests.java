package com.ntd.csdl;
import com.ntd.csdl.service.TenantService;
import com.ntd.csdl.repo.TenantRepository;
import com.ntd.csdl.dto.TenantDTO;
import com.ntd.csdl.entity.Tenant;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
class TenantCreationTests {
 @Test void blankCccdBecomesNullAndTenantIsFlushedBeforeOwnershipLink() {
  var repo=mock(TenantRepository.class);when(repo.saveAndFlush(any())).thenAnswer(i->i.getArgument(0));
  var service=new TenantService(repo);var dto=new TenantDTO();dto.setCccd("  ");dto.setFullName("Khách mới");
  Tenant created=service.create(dto);
  assertNull(created.getCccd());assertTrue(created.getTenantId().startsWith("KT"));assertTrue(created.getTenantId().length()<=20);
  verify(repo).saveAndFlush(created);verify(repo,never()).save(any());
 }
 @Test void duplicateCccdGivesSpecificConflict() {
  var repo=mock(TenantRepository.class);when(repo.findByCccd("012345678901")).thenReturn(new Tenant());
  var dto=new TenantDTO();dto.setCccd(" 012345678901 ");
  var e=assertThrows(org.springframework.web.server.ResponseStatusException.class,()->new TenantService(repo).create(dto));
  assertEquals(409,e.getStatusCode().value());assertTrue(e.getReason().contains("CCCD"));verify(repo,never()).saveAndFlush(any());
 }
}
