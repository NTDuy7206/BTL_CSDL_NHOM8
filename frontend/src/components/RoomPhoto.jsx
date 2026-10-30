import {useEffect,useRef,useState} from 'react'
import Icon from './Icon.jsx'
export default function RoomPhoto({photo,request,large=false,alt='Ảnh phòng'}){
 const[url,setUrl]=useState(''),[failed,setFailed]=useState(false),[visible,setVisible]=useState(false),element=useRef(null)
 useEffect(()=>{if(!photo)return;if(!window.IntersectionObserver){setVisible(true);return}const observer=new IntersectionObserver(entries=>{if(entries.some(e=>e.isIntersecting)){setVisible(true);observer.disconnect()}},{rootMargin:'300px'});if(element.current)observer.observe(element.current);return()=>observer.disconnect()},[photo?.id])
 useEffect(()=>{if(!photo||!visible)return;let active=true,objectUrl;const abort=new AbortController();setUrl('');setFailed(false);request(large?photo.url:photo.thumbnailUrl,{responseType:'blob',signal:abort.signal}).then(blob=>{if(active){objectUrl=URL.createObjectURL(blob);setUrl(objectUrl)}}).catch(()=>{if(active)setFailed(true)});return()=>{active=false;abort.abort();if(objectUrl)URL.revokeObjectURL(objectUrl)}},[request,photo?.id,visible,large])
 return <div ref={element} className={`room-photo ${large?'room-photo-large':''}`}>{url?<img src={url} alt={alt}/>:<div className="room-photo-placeholder"><Icon name="building" size={large?64:40}/><span>{photo?(failed?'Không tải được ảnh':'Đang tải ảnh…'):'Chưa có ảnh thực tế'}</span></div>}</div>
}
