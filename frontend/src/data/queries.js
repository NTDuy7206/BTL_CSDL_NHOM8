export const queryActions={
 tenants:[['name','Tìm khách theo tên','Tên khách','An']],
 employees:[['name','Tìm nhân viên theo tên','Tên nhân viên','An']],
 rooms:[['status','Theo tình trạng','Tình trạng','AVAILABLE',[['AVAILABLE','Phòng trống'],['OCCUPIED','Đã thuê']]],['type','Theo loại phòng','Loại phòng','Studio'],['max-price','Giá thuê tối đa','Giá tối đa (₫)','3000000']],
 contracts:[['expired','Hợp đồng hết hạn'],['tenant','Hợp đồng theo khách','Mã khách thuê','DEMO_KT01'],['room','Hợp đồng theo phòng','Mã phòng','DEMO_P01']],
 invoices:[['contract','Hóa đơn theo hợp đồng','Mã hợp đồng','DEMO_HD01'],['total','Tổng giá trị hóa đơn']],
 incidents:[['room','Sự cố theo phòng','Mã phòng','DEMO_P01'],['status','Theo trạng thái','Trạng thái','PENDING',[['PENDING','Chờ xử lý'],['COMPLETED','Đã xử lý']]]],
 'incident-resolutions':[['incident','Theo sự cố','Mã sự cố','DEMO_SC01'],['employee','Theo nhân viên','Mã nhân viên','DEMO_NV01'],['uncompleted','Chưa hoàn thành'],['complete','Đánh dấu hoàn thành','Mã xử lý','DEMO_XL06']],
 'cleaning-supports':[['room','Theo phòng','Mã phòng','DEMO_P01'],['employee','Theo nhân viên','Mã nhân viên','DEMO_NV01'],['uncompleted','Chưa hoàn thành'],['complete','Đánh dấu hoàn thành','Mã hỗ trợ','DEMO_DD02']],
 'utility-readings':[['room','Chỉ số theo phòng','Mã phòng','DEMO_P01']],
 violations:[['room','Vi phạm theo phòng','Mã phòng','DEMO_P01']]
}
