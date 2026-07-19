import { useEffect, useRef } from 'react'
import { CloseOutlined } from '@mui/icons-material'
export default function Modal({ isOpen, onClose, title, children, maxWidth = 'max-w-lg' }) {
  const overlayRef = useRef(null)
  useEffect(() => { document.body.style.overflow = isOpen ? 'hidden' : ''; return () => { document.body.style.overflow = '' } }, [isOpen])
  if (!isOpen) return null
  return <div ref={overlayRef} className="fixed inset-0 z-50 flex items-end bg-slate-950/50 p-0 backdrop-blur-[1px] sm:items-center sm:justify-center sm:p-4" onClick={(e) => e.target === overlayRef.current && onClose()}><div className={`flex max-h-[92vh] w-full flex-col rounded-t-2xl bg-white shadow-2xl sm:max-h-[90vh] sm:rounded-2xl ${maxWidth}`}><div className="flex items-center justify-between border-b border-slate-100 px-5 py-4 sm:px-6"><h2 className="text-lg font-semibold text-slate-900">{title}</h2><button onClick={onClose} className="icon-button" aria-label="Close dialog"><CloseOutlined fontSize="small" /></button></div><div className="flex-1 overflow-y-auto px-5 py-5 sm:px-6">{children}</div></div></div>
}