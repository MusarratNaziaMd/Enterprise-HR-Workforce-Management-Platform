import CircularProgress from '@mui/material/CircularProgress'

export default function LoadingSpinner({ size = 40, fullPage = false }) {
  if (fullPage) {
    return (
      <div className="space-y-6 animate-pulse" aria-label="Loading content" role="status">
        <div className="space-y-2">
          <div className="skeleton h-7 w-44" />
          <div className="skeleton h-4 w-72 max-w-full" />
        </div>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
          {[0, 1, 2, 3].map((item) => <div key={item} className="card space-y-4 p-5"><div className="skeleton h-4 w-24" /><div className="skeleton h-8 w-16" /></div>)}
        </div>
        <div className="card space-y-4 p-5">
          {[0, 1, 2, 3, 4].map((item) => <div key={item} className="skeleton h-12 w-full" />)}
        </div>
      </div>
    )
  }
  return <div className="flex items-center justify-center py-8" role="status" aria-label="Loading"><CircularProgress size={size} /></div>
}