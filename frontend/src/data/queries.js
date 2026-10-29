// Free-text lookups use the table keyword input. Only structured filters remain here.
export const queryActions={
 rooms:[['status','Theo tình trạng','Tình trạng','AVAILABLE',[['AVAILABLE','Phòng trống'],['OCCUPIED','Đã thuê']]],['max-price','Giá thuê tối đa','Giá tối đa (₫)','3000000']],
 contracts:[['expired','Hợp đồng hết hạn']],
 invoices:[['total','Tổng giá trị hóa đơn']],
 incidents:[['status','Theo trạng thái','Trạng thái','PENDING',[['PENDING','Chờ xử lý'],['COMPLETED','Đã xử lý']]]],
 'incident-resolutions':[['uncompleted','Chưa hoàn thành']],
 'cleaning-supports':[['uncompleted','Chưa hoàn thành']]
}
