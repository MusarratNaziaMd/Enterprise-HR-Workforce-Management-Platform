import { SearchOutlined, ClearOutlined } from '@mui/icons-material'

export default function SearchInput({ value, onChange, placeholder = 'Search...' }) {
  return (
    <div className="relative">
      <SearchOutlined className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" fontSize="small" />
      <input
        type="text"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
        className="input-field pl-10 pr-9"
      />
      {value && (
        <button
          onClick={() => onChange('')}
          className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
        >
          <ClearOutlined fontSize="small" />
        </button>
      )}
    </div>
  )
}
