/**
 * 统一请求封装
 */
const BASE = '/api/v1'

export interface Result<T> {
  code: number
  message: string
  data: T
}

export async function request<T>(
  url: string,
  options: RequestInit = {},
): Promise<Result<T>> {
  const token = localStorage.getItem('starvocab_token')
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string>),
  }
  if (token) headers.Authorization = `Bearer ${token}`

  const res = await fetch(`${BASE}${url}`, { ...options, headers })
  const json = (await res.json()) as Result<T>
  if (json.code !== 0) {
    throw new Error(json.message || '星云服务异常')
  }
  return json
}

export function get<T>(url: string) {
  return request<T>(url, { method: 'GET' })
}
export function post<T>(url: string, body: unknown) {
  return request<T>(url, { method: 'POST', body: JSON.stringify(body) })
}
export function put<T>(url: string, body: unknown) {
  return request<T>(url, { method: 'PUT', body: JSON.stringify(body) })
}
