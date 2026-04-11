/**
 * 格式化日期时间为：YYYY-MM-DD HH:mm:ss
 * 支持多种输入格式：Date对象、时间戳、ISO字符串、中国格式字符串
 */
export function formatDateTime(dateInput: Date | string | number | null | undefined): string {
  // 更精确的空值检查
  if (dateInput === null || dateInput === undefined || dateInput === '') {
    return ''
  }

  let date: Date

  // 处理不同类型的输入
  if (dateInput instanceof Date) {
    date = dateInput
  } else if (typeof dateInput === 'number') {
    // 时间戳（毫秒）
    date = new Date(dateInput)
  } else if (typeof dateInput === 'string') {
    // 字符串格式处理
    const trimmedStr = dateInput.trim()

    // 处理中国格式的时间字符串：2026-01-23 16:31:26
    if (/^\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2}:\d{2}$/.test(trimmedStr)) {
      // 直接格式化，因为已经符合目标格式
      return trimmedStr
    }

    // 处理 ISO 格式或其他格式
    date = new Date(trimmedStr)

    // 如果解析失败，尝试手动解析中国格式
    if (isNaN(date.getTime())) {
      // 尝试解析 2026-01-23T16:31:26 格式
      const parts = trimmedStr.replace('T', ' ').split(/[- :]/)
      if (parts.length >= 6) {
        const [year, month, day, hour, minute, second] = parts.map(Number)
        date = new Date(year, month - 1, day, hour, minute, second)
      }
    }
  } else {
    return ''
  }

  // 检查日期是否有效
  if (isNaN(date.getTime())) {
    return ''
  }

  // 格式化日期
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')

  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

/**
 * 格式化日期为：YYYY-MM-DD
 */
export function formatDate(dateInput: Date | string | number | null | undefined): string {
  const formatted = formatDateTime(dateInput)
  return formatted ? formatted.split(' ')[0] : ''
}

/**
 * 格式化时间为：HH:mm:ss
 */
export function formatTime(dateInput: Date | string | number | null | undefined): string {
  const formatted = formatDateTime(dateInput)
  return formatted ? formatted.split(' ')[1] || '' : ''
}
