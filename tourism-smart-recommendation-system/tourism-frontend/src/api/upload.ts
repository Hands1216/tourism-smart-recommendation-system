import service from './index'

/**
 * 上传单个图片
 */
export function uploadImage(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return service.post<string>('/upload/image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 批量上传图片
 */
export function uploadImages(files: File[]) {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })
  return service.post<string[]>('/upload/images', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
