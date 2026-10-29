import { useState } from 'react'
import Icon from './Icon.jsx'

export default function AuthPage({ onLogin, onRegister }) {
  const [mode, setMode] = useState('login')
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [role, setRole] = useState('USER')
  const [roomCount,setRoomCount]=useState('1')
  const [rooms,setRooms]=useState([{roomId:'101',floor:1,area:20,roomType:'Phòng đơn',basePrice:2000000}])
  function resizeRooms(value){setRoomCount(value);const n=Number(value);if(Number.isInteger(n)&&n>=1&&n<=200)setRooms(old=>Array.from({length:n},(_,i)=>old[i]||{roomId:String(101+i),floor:1,area:20,roomType:'Phòng đơn',basePrice:2000000}))}
  function changeRoom(index,key,value){setRooms(old=>old.map((room,i)=>i===index?{...room,[key]:['floor','area','basePrice'].includes(key)?Number(value):value}:room))}
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [showPassword, setShowPassword] = useState(false)
  const isRegister = mode === 'register'

  async function submit(event) {
    event.preventDefault(); setError(''); setBusy(true)
    try { await (isRegister ? onRegister(username.trim(), password, role,role==='ADMIN'?Number(roomCount):undefined,role==='ADMIN'?rooms:undefined) : onLogin(username.trim(), password)) }
    catch (err) { setError(err.message || 'Đã xảy ra lỗi. Vui lòng thử lại.') }
    finally { setBusy(false) }
  }

  return <main className="auth-page">
    <section className="auth-art">
      <div className="auth-art-orb orb-a"/><div className="auth-art-orb orb-b"/>
      <div className="auth-art-content">
        <div className="auth-brand"><div className="auth-brand-icon"><Icon name="building" size={23}/></div><span>HomeBase</span><small>BUILDING MANAGEMENT</small></div>
        <div className="auth-copy"><span className="eyebrow"><i/> NỀN TẢNG QUẢN LÝ TÒA NHÀ</span><h1>Vận hành<br/>nhẹ nhàng.<br/><em>Quản lý dễ dàng.</em></h1><p>Mọi thông tin về phòng, khách thuê và hoạt động của tòa nhà — ở cùng một nơi.</p>
          <div className="auth-visual-card"><div className="visual-card-top"><span><i/> TỔNG QUAN TÒA NHÀ</span><b>Hôm nay <Icon name="chevron" size={13}/></b></div><div className="visual-card-body"><div className="visual-number"><small>Phòng đang được quản lý</small><strong>11 <span>phân hệ</span></strong></div><div className="mini-bars"><i/><i/><i/><i/><i/><i/><i/><i/><i/><i/><i/><i/><i/><i/><i/><i/><i/><i/><i/><i/></div></div><div className="visual-card-foot"><span><i className="mini-dot green-dot"/> Dữ liệu tập trung</span><span><i className="mini-dot blue-dot"/> API bảo mật</span></div></div>
        </div>
        <div className="art-footer"><span>HOMEBASE</span><span>·</span><span>NHÓM 8 · BÀI TẬP LỚN CƠ SỞ DỮ LIỆU</span></div>
      </div>
    </section>
    <section className="auth-side"><div className="auth-topline"><span className="auth-mobile-brand"><Icon name="building" size={18}/> HomeBase</span><span>HỖ TRỢ <b>NHÓM 8</b></span></div><div className="auth-card">
      <div className="auth-card-mark"><Icon name="home" size={19}/></div>
      <div className="auth-mode-switch" role="group" aria-label="Chọn đăng nhập hoặc đăng ký">
        <button type="button" aria-pressed={!isRegister} className={!isRegister?'active':''} onClick={()=>{setMode('login');setError('')}}>Đăng nhập</button>
        <button type="button" aria-pressed={isRegister} className={isRegister?'active':''} onClick={()=>{setMode('register');setError('')}}>Tạo tài khoản</button>
      </div>
      <span className="eyebrow muted-eyebrow">{isRegister?'BẮT ĐẦU SỬ DỤNG':'CHÀO MỪNG TRỞ LẠI'}</span><h2>{isRegister?'Tạo tài khoản':'Đăng nhập'}</h2><p className="auth-description">{isRegister?'Tạo tài khoản để truy cập hệ thống quản lý tòa nhà.':'Đăng nhập bằng tài khoản của bạn để tiếp tục.'}</p>
      {error&&<div className="alert alert-error"><Icon name="incident" size={16}/><span>{error}</span></div>}
      <form onSubmit={submit} className="auth-form"><label className="form-field"><span>Tên đăng nhập</span><div className="input-icon"><Icon name="users" size={17}/><input autoComplete="username" required value={username} onChange={e=>setUsername(e.target.value)} placeholder="Nhập tên đăng nhập" /></div></label><label className="form-field"><span>Mật khẩu</span><div className="input-icon"><Icon name="rules" size={17}/><input type={showPassword?'text':'password'} autoComplete={isRegister?'new-password':'current-password'} required value={password} onChange={e=>setPassword(e.target.value)} placeholder="Nhập mật khẩu" /><button type="button" className="password-toggle" onClick={()=>setShowPassword(v=>!v)} aria-label={showPassword?'Ẩn mật khẩu':'Hiện mật khẩu'}>{showPassword?<Icon name="eyeOff" size={17}/>:<Icon name="eye" size={17}/>}</button></div></label>{isRegister&&<label className="form-field"><span>Loại tài khoản</span><div className="input-icon"><Icon name="building" size={17}/><select value={role} onChange={event=>setRole(event.target.value)}><option value="USER">Khách thuê (USER)</option><option value="ADMIN">Chủ nhà (ADMIN)</option></select></div></label>}{isRegister&&role==='ADMIN'&&<label className="form-field"><span>Số phòng cho thuê cố định</span><input required type="number" min="1" max="200" value={roomCount} onChange={e=>resizeRooms(e.target.value)}/><small>Khai báo từng phòng bên dưới. Mã được thêm tiền tố chủ nhà để hai chủ nhà có thể cùng dùng số 101.</small></label>}{isRegister&&role==='ADMIN'&&<div className="registration-rooms"><h3>Danh sách phòng của bạn</h3>{rooms.map((room,index)=><fieldset key={index}><legend>Phòng {index+1}</legend>{[['roomId','Số / mã phòng','text'],['floor','Tầng','number'],['area','Diện tích (m²)','number'],['roomType','Loại phòng','text'],['basePrice','Giá thuê (₫/tháng)','number']].map(([key,label,type])=><label className="form-field" key={key}><span>{label}</span><input required type={type} value={room[key]} min={key==='floor'?0:key==='area'?0.01:1} step={key==='area'||key==='basePrice'?'0.01':1} maxLength={key==='roomId'?12:key==='roomType'?50:undefined} pattern={key==='roomId'?'[A-Za-z0-9_-]{1,12}':undefined} onChange={e=>changeRoom(index,key,e.target.value)}/></label>)}</fieldset>)}</div>}<button className="button button-primary button-wide" disabled={busy}>{busy?<span className="spinner small-spinner"/>:null}{busy?'Đang xử lý…':isRegister?'Tạo tài khoản':'Đăng nhập'}{!busy&&<Icon name="chevron" size={17}/>}</button></form>
      <div className="auth-note"><Icon name="check" size={15}/><span>{isRegister?(role==='ADMIN'?<>Chủ nhà khai báo số phòng, cấu hình phòng của mình và duyệt yêu cầu thuê gửi đến mình.</>:<>Khách thuê được xem thông tin, đăng ký hợp đồng, báo sự cố và thanh toán hóa đơn của mình.</>):<>Đăng nhập để sử dụng các chức năng theo vai trò khách thuê hoặc chủ nhà.</>}</span></div>
    </div><footer className="auth-copyright">© 2026 HomeBase <i/> Quản lý tòa nhà thông minh</footer></section>
  </main>
}
